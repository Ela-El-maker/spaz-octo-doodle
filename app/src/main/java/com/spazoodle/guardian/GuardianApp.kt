package com.spazoodle.guardian

import android.app.PendingIntent
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.platform.reliability.SchemaPolicyGuard
import com.spazoodle.guardian.platform.reliability.StartupRecoveryStore
import com.spazoodle.guardian.platform.scheduler.ScheduledTriggerRegistry
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver
import com.spazoodle.guardian.runtime.GuardianRuntime
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.domain.usecase.ReconcileEnabledAlarmsResult
import com.spazoodle.guardian.worker.GuardianRetentionWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GuardianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SchemaPolicyGuard(this).check()
        createNotificationChannels()
        scheduleRetentionWorker()
        rescheduleActiveAlarmsOnStartup()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val alarmAudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val channels = listOf(
            NotificationChannel(
                CHANNEL_ALARM,
                "Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(alarmSound, alarmAudioAttributes)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                description = "High-priority alarm notifications that can appear full-screen."
            },
            NotificationChannel(
                CHANNEL_PRE_ALERT,
                "Pre-alert",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                CHANNEL_DIAGNOSTICS,
                "Diagnostics",
                NotificationManager.IMPORTANCE_LOW
            )
        )
        manager.createNotificationChannels(channels)
    }

    private fun rescheduleActiveAlarmsOnStartup() {
        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            runCatching {
                GuardianRuntime.pruneRetention(
                    context = this@GuardianApp,
                    nowUtcMillis = now,
                    retentionMs = GuardianPolicyConfig.triggerExecutionRetentionMs
                )
            }

            val plans = runCatching {
                GuardianRuntime.reconcileEnabledAlarmsUseCase(this@GuardianApp).invoke()
            }.getOrElse {
                // fallback behavior if reconcile path fails
                val fallbackPlans = runCatching {
                    GuardianRuntime.rescheduleAllActiveAlarmsUseCase(this@GuardianApp).invoke()
                }.getOrElse { emptyList() }
                ReconcileEnabledAlarmsResult(
                    reschedulablePlans = fallbackPlans,
                    missedOneTimeAlarmIds = emptyList()
                )
            }

            plans.missedOneTimeAlarmIds.forEach { alarmId ->
                runCatching {
                    GuardianRuntime.recordFireEventUseCase(this@GuardianApp).invoke(
                        alarmId = alarmId,
                        triggerKind = TriggerKind.MAIN,
                        outcome = AlarmEventOutcome.MISSED,
                        deliveryState = DeliveryState.MISSED,
                        detail = "missed_startup_reconcile"
                    )
                    GuardianRuntime.finalizeOneTimeAlarmUseCase(this@GuardianApp).invoke(alarmId)
                    GuardianRuntime.alarmScheduler(this@GuardianApp).cancelAlarm(alarmId)
                }
            }

            val recoveryStore = StartupRecoveryStore(this@GuardianApp)
            val missingTriggerCount = countMissingScheduledTriggers(plans.reschedulablePlans)
            if (plans.reschedulablePlans.isNotEmpty() && missingTriggerCount > 0) {
                recoveryStore.markRecovered(
                    missingTriggerCount = missingTriggerCount,
                    totalPlanCount = plans.reschedulablePlans.size,
                    atUtcMillis = now
                )
            } else {
                recoveryStore.clear()
            }

            if (plans.reschedulablePlans.isNotEmpty()) {
                GuardianRuntime.alarmScheduler(this@GuardianApp).rescheduleAll(plans.reschedulablePlans)
            }
        }
    }

    private fun countMissingScheduledTriggers(plans: List<SchedulePlan>): Int {
        var missing = 0
        val registry = ScheduledTriggerRegistry(this)
        plans.forEach { plan ->
            plan.triggers.forEach { trigger ->
                val triggerIdentity =
                    "${trigger.alarmId}:${trigger.kind.name}:${trigger.index}:${trigger.key.orEmpty()}:${trigger.scheduledAtUtcMillis}"
                val mappedCode = registry.findCode(plan.alarmId, triggerIdentity)
                val resolvedCode = mappedCode ?: registry.readCodes(plan.alarmId).firstOrNull()
                if (resolvedCode == null) {
                    missing += 1
                } else {
                    val pendingIntent = PendingIntent.getBroadcast(
                        this,
                        resolvedCode,
                        Intent(this, AlarmTriggerReceiver::class.java),
                        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                    )
                    if (pendingIntent == null) missing += 1
                }
            }
        }
        return missing
    }

    private fun scheduleRetentionWorker() {
        if (!GuardianFeatureFlags.retentionWorkerEnabled) return
        val config = GuardianPolicyConfig.retentionConfig
        val request = PeriodicWorkRequestBuilder<GuardianRetentionWorker>(
            config.cleanupIntervalHours.toLong(),
            TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            GuardianRetentionWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        const val CHANNEL_ALARM = "guardian_alarm"
        const val CHANNEL_PRE_ALERT = "guardian_pre_alert"
        const val CHANNEL_DIAGNOSTICS = "guardian_diagnostics"
    }
}
