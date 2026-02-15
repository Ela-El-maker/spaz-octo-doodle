package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.app.KeyguardManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.PrimaryActionType
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.platform.reliability.GuardianDiagnosticTags
import com.spazoodle.guardian.platform.reliability.ActionLaunchPolicyEvaluator
import com.spazoodle.guardian.runtime.GuardianRuntime
import com.spazoodle.guardian.service.AlarmRingingService
import com.spazoodle.guardian.ui.ringing.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId <= 0L) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val action = intent.action.orEmpty()
                if (GuardianFeatureFlags.dedupeGuard) {
                    val now = System.currentTimeMillis()
                    val actionKey = "action:$alarmId:$action:${intent.getIntExtra(EXTRA_SNOOZE_MINUTES, -1)}"
                    val shouldProcess = GuardianRuntime.triggerDeduper(appContext).shouldProcess(actionKey, now)
                    if (!shouldProcess) return@launch
                }

                when (action) {
                    ACTION_STOP_GUARD_STARTED -> handleStopGuardStarted(appContext, alarmId)
                    ACTION_STOP -> handleStop(appContext, alarmId)
                    ACTION_SNOOZE -> handleSnooze(
                        context = appContext,
                        alarmId = alarmId,
                        minutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)
                    )

                    ACTION_DO -> handleDo(
                        context = appContext,
                        alarmId = alarmId,
                        actionTypeRaw = intent.getStringExtra(EXTRA_PRIMARY_ACTION_TYPE),
                        actionValue = intent.getStringExtra(EXTRA_PRIMARY_ACTION_VALUE)
                    )
                }
            } catch (error: Throwable) {
                Log.e(TAG, "Action handling failed for alarmId=$alarmId", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleStop(context: Context, alarmId: Long) {
        GuardianRuntime.acknowledgeAlarmUseCase(context).invoke(
            alarmId = alarmId,
            outcome = AlarmEventOutcome.DISMISSED
        )
        GuardianRuntime.recordFireEventUseCase(context).invoke(
            alarmId = alarmId,
            triggerKind = TriggerKind.MAIN,
            outcome = AlarmEventOutcome.DISMISSED,
            deliveryState = DeliveryState.DISMISSED,
            detail = "completed_user_stop:${GuardianDiagnosticTags.STOP_GUARD_CONFIRMED}"
        )
        GuardianRuntime.finalizeOneTimeAlarmUseCase(context).invoke(alarmId)
        GuardianRuntime.alarmScheduler(context).cancelAlarm(alarmId)
        context.stopService(Intent(context, AlarmRingingService::class.java))
    }

    private suspend fun handleStopGuardStarted(context: Context, alarmId: Long) {
        GuardianRuntime.recordFireEventUseCase(context).invoke(
            alarmId = alarmId,
            triggerKind = TriggerKind.MAIN,
            outcome = AlarmEventOutcome.FIRED,
            deliveryState = DeliveryState.FIRED,
            detail = GuardianDiagnosticTags.STOP_GUARD_STARTED
        )
    }

    private suspend fun handleSnooze(context: Context, alarmId: Long, minutes: Int) {
        val safeMinutes = minutes.coerceAtLeast(1)
        GuardianRuntime.acknowledgeAlarmUseCase(context).invoke(
            alarmId = alarmId,
            outcome = AlarmEventOutcome.SNOOZED
        )

        val now = System.currentTimeMillis()
        val snoozeAt = now + safeMinutes * 60_000L
        val plan = SchedulePlan(
            alarmId = alarmId,
            triggers = listOf(
                Trigger(
                    alarmId = alarmId,
                    kind = TriggerKind.SNOOZE,
                    scheduledAtUtcMillis = snoozeAt,
                    index = 0,
                    key = "SNOOZE_${safeMinutes}M"
                )
            )
        )
        GuardianRuntime.alarmScheduler(context).schedule(plan)
        context.stopService(Intent(context, AlarmRingingService::class.java))
    }

    private suspend fun handleDo(
        context: Context,
        alarmId: Long,
        actionTypeRaw: String?,
        actionValue: String?
    ) {
        if (isLaunchBlockedByLock(context)) {
            GuardianRuntime.recordFireEventUseCase(context).invoke(
                alarmId = alarmId,
                triggerKind = TriggerKind.MAIN,
                outcome = AlarmEventOutcome.FIRED,
                deliveryState = DeliveryState.FIRED,
                detail = GuardianDiagnosticTags.ACTION_BLOCKED_DEVICE_LOCKED
            )
            postUnlockRequiredNotification(context, alarmId)
            return
        }

        val fallbackAction = GuardianRuntime.alarmRepository(context)
            .getById(alarmId)
            ?.primaryAction

        val actionType = runCatching { PrimaryActionType.valueOf(actionTypeRaw.orEmpty()) }
            .getOrNull() ?: fallbackAction?.type
        val value = actionValue ?: fallbackAction?.value

        GuardianRuntime.acknowledgeAlarmUseCase(context).invoke(
            alarmId = alarmId,
            outcome = AlarmEventOutcome.ACTION_LAUNCHED
        )
        GuardianRuntime.recordFireEventUseCase(context).invoke(
            alarmId = alarmId,
            triggerKind = TriggerKind.MAIN,
            outcome = AlarmEventOutcome.ACTION_LAUNCHED,
            deliveryState = DeliveryState.ACTION_LAUNCHED,
            detail = "completed_action_launch"
        )
        GuardianRuntime.finalizeOneTimeAlarmUseCase(context).invoke(alarmId)
        GuardianRuntime.alarmScheduler(context).cancelAlarm(alarmId)
        context.stopService(Intent(context, AlarmRingingService::class.java))
        launchPrimaryAction(context, actionType, value)
    }

    private fun isLaunchBlockedByLock(context: Context): Boolean {
        val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return ActionLaunchPolicyEvaluator.isBlockedByLock(
            isDeviceLocked = keyguard.isDeviceLocked,
            policy = GuardianPolicyConfig.actionLaunchPolicy,
            featureEnabled = GuardianFeatureFlags.lockscreenRequireUnlock
        )
    }

    private fun postUnlockRequiredNotification(context: Context, alarmId: Long) {
        val intent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            ("unlock_$alarmId").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, GuardianApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Unlock required")
            .setContentText("Unlock your device, then tap Do again.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()
        runCatching {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(context).notify(("unlock_required_$alarmId").hashCode(), notification)
            }
        }
    }

    private fun launchPrimaryAction(context: Context, type: PrimaryActionType?, value: String?) {
        if (type == null || value.isNullOrBlank()) return

        val intent = when (type) {
            PrimaryActionType.OPEN_URL,
            PrimaryActionType.OPEN_DEEPLINK,
            PrimaryActionType.OPEN_MAP_NAVIGATION,
            PrimaryActionType.OPEN_FILE -> Intent(Intent.ACTION_VIEW, Uri.parse(value))

            PrimaryActionType.CALL_NUMBER -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:$value"))

            PrimaryActionType.OPEN_NOTE,
            PrimaryActionType.OPEN_CHECKLIST -> Intent(Intent.ACTION_VIEW, Uri.parse(value))
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        runCatching { context.startActivity(intent) }
    }

    companion object {
        private const val TAG = "AlarmActionReceiver"

        const val ACTION_STOP = "com.spazoodle.guardian.action.STOP"
        const val ACTION_STOP_GUARD_STARTED = "com.spazoodle.guardian.action.STOP_GUARD_STARTED"
        const val ACTION_SNOOZE = "com.spazoodle.guardian.action.SNOOZE"
        const val ACTION_DO = "com.spazoodle.guardian.action.DO"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_PRIMARY_ACTION_TYPE = "extra_primary_action_type"
        const val EXTRA_PRIMARY_ACTION_VALUE = "extra_primary_action_value"
        const val EXTRA_PRIMARY_ACTION_LABEL = "extra_primary_action_label"
    }
}
