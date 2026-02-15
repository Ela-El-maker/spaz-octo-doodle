package com.spazoodle.guardian.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.MainActivity
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.TriggerExecutionKey
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.platform.reliability.ReliabilityScanner
import com.spazoodle.guardian.runtime.GuardianRuntime
import com.spazoodle.guardian.service.AlarmRingingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId <= 0L) {
            pendingResult.finish()
            return
        }

        val triggerKind = parseTriggerKind(intent.getStringExtra(EXTRA_TRIGGER_KIND))
        val scheduledAtUtcMillis = intent.getLongExtra(EXTRA_SCHEDULED_AT_UTC_MILLIS, -1L)
            .takeIf { it > 0L }
        val triggerIndex = intent.getIntExtra(EXTRA_TRIGGER_INDEX, -1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val executionKey = TriggerExecutionKey(
                    alarmId = alarmId,
                    kind = triggerKind,
                    index = triggerIndex,
                    scheduledAtUtcMillis = scheduledAtUtcMillis ?: -1L
                ).asString()

                if (GuardianFeatureFlags.dedupeGuard) {
                    val deduper = GuardianRuntime.triggerDeduper(appContext)
                    val shouldProcess = deduper.shouldProcess(executionKey, now)
                    deduper.prune(
                        retentionMs = GuardianPolicyConfig.triggerExecutionRetentionMs,
                        nowUtcMillis = now
                    )
                    if (!shouldProcess) {
                        GuardianRuntime.recordFireEventUseCase(appContext).invoke(
                            alarmId = alarmId,
                            triggerKind = triggerKind,
                            scheduledAtUtcMillis = scheduledAtUtcMillis,
                            wasDeduped = true,
                            detail = "deduped_duplicate_drop"
                        )
                        return@launch
                    }
                }

                val alarmRepository = GuardianRuntime.alarmRepository(appContext)
                val alarm = alarmRepository.getById(alarmId)
                if (alarm == null || !alarm.enabled) return@launch

                if (triggerKind == TriggerKind.PRE_ALERT) {
                    GuardianRuntime.recordFireEventUseCase(appContext).invoke(
                        alarmId = alarmId,
                        triggerKind = triggerKind,
                        scheduledAtUtcMillis = scheduledAtUtcMillis
                    )
                    postPreAlertNotification(
                        context = appContext,
                        alarmId = alarmId,
                        title = alarm.title
                    )
                    return@launch
                }

                val delayMs = scheduledAtUtcMillis?.let { now - it } ?: 0L
                val graceMs = GuardianPolicyConfig.lateRecoveryPolicy.recoverGraceMinutes * 60_000L
                if (GuardianFeatureFlags.lateRecovery && delayMs > graceMs) {
                    GuardianRuntime.recordFireEventUseCase(appContext).invoke(
                        alarmId = alarmId,
                        triggerKind = triggerKind,
                        scheduledAtUtcMillis = scheduledAtUtcMillis,
                        outcome = AlarmEventOutcome.MISSED,
                        deliveryState = DeliveryState.MISSED,
                        detail = "missed_receiver_late:delay_ms=$delayMs"
                    )
                    if (GuardianPolicyConfig.lateRecoveryPolicy.missedNotifyEnabled) {
                        postMissedNotification(
                            context = appContext,
                            alarmId = alarmId,
                            title = alarm.title,
                            delayMs = delayMs
                        )
                    }
                    GuardianRuntime.finalizeOneTimeAlarmUseCase(appContext).invoke(alarmId)
                    GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarmId)
                    return@launch
                }

                val isRecoveredLate = delayMs > 0
                GuardianRuntime.recordFireEventUseCase(appContext).invoke(
                    alarmId = alarmId,
                    triggerKind = triggerKind,
                    scheduledAtUtcMillis = scheduledAtUtcMillis,
                    outcome = if (isRecoveredLate) AlarmEventOutcome.RECOVERED_LATE else AlarmEventOutcome.FIRED,
                    deliveryState = if (isRecoveredLate) DeliveryState.RECOVERED_LATE else DeliveryState.FIRED,
                    detail = if (isRecoveredLate) "recovered_late_delay_ms=$delayMs" else null
                )

                val serviceIntent = Intent(appContext, AlarmRingingService::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmId)
                    putExtra(EXTRA_TRIGGER_KIND, triggerKind.name)
                    putExtra(EXTRA_ALARM_TITLE, alarm.title)
                    putExtra(EXTRA_ALARM_DESCRIPTION, alarm.description)
                    putExtra(EXTRA_ALARM_VIBRATE_ENABLED, alarm.vibrateEnabled)
                    putExtra(EXTRA_ALARM_RINGTONE_URI, alarm.ringtoneUri)
                    putExtra(EXTRA_PRIMARY_ACTION_TYPE, alarm.primaryAction?.type?.name)
                    putExtra(EXTRA_PRIMARY_ACTION_VALUE, alarm.primaryAction?.value)
                    putExtra(EXTRA_PRIMARY_ACTION_LABEL, alarm.primaryAction?.label)
                    putExtra(EXTRA_TRIGGER_INDEX, intent.getIntExtra(EXTRA_TRIGGER_INDEX, -1))
                    putExtra(EXTRA_TRIGGER_KEY, intent.getStringExtra(EXTRA_TRIGGER_KEY))
                    putExtra(
                        EXTRA_SCHEDULED_AT_UTC_MILLIS,
                        intent.getLongExtra(EXTRA_SCHEDULED_AT_UTC_MILLIS, -1L)
                    )
                    putExtra(
                        EXTRA_FULL_SCREEN_READY,
                        ReliabilityScanner(appContext).scan().fullScreenReady
                    )
                }
                appContext.startForegroundService(serviceIntent)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun parseTriggerKind(raw: String?): TriggerKind {
        return runCatching {
            TriggerKind.valueOf(raw.orEmpty())
        }.getOrDefault(TriggerKind.MAIN)
    }

    @SuppressLint("MissingPermission")
    private fun postPreAlertNotification(context: Context, alarmId: Long, title: String) {
        if (!canPostNotifications(context)) return

        val contentIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, GuardianApp.CHANNEL_PRE_ALERT)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming alarm")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            ("pre_$alarmId").hashCode(),
            notification
        )
    }

    @SuppressLint("MissingPermission")
    private fun postMissedNotification(context: Context, alarmId: Long, title: String, delayMs: Long) {
        if (!canPostNotifications(context)) return

        val contentIntent = PendingIntent.getActivity(
            context,
            ("missed_open_$alarmId").hashCode(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val mins = delayMs / 60_000L
        val notification = NotificationCompat.Builder(context, GuardianApp.CHANNEL_DIAGNOSTICS)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Missed alarm")
            .setContentText("$title was delayed by ${mins}m and marked missed.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            ("missed_$alarmId").hashCode(),
            notification
        )
    }

    private fun canPostNotifications(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TRIGGER_KIND = "extra_trigger_kind"
        const val EXTRA_TRIGGER_INDEX = "extra_trigger_index"
        const val EXTRA_TRIGGER_KEY = "extra_trigger_key"
        const val EXTRA_SCHEDULED_AT_UTC_MILLIS = "extra_scheduled_at_utc_millis"
        const val EXTRA_ALARM_TITLE = "extra_alarm_title"
        const val EXTRA_ALARM_DESCRIPTION = "extra_alarm_description"
        const val EXTRA_ALARM_VIBRATE_ENABLED = "extra_alarm_vibrate_enabled"
        const val EXTRA_ALARM_RINGTONE_URI = "extra_alarm_ringtone_uri"
        const val EXTRA_PRIMARY_ACTION_TYPE = "extra_primary_action_type"
        const val EXTRA_PRIMARY_ACTION_VALUE = "extra_primary_action_value"
        const val EXTRA_PRIMARY_ACTION_LABEL = "extra_primary_action_label"
        const val EXTRA_AUDIO_ROUTE_WARNING = "extra_audio_route_warning"
        const val EXTRA_FULL_SCREEN_READY = "extra_full_screen_ready"
    }
}
