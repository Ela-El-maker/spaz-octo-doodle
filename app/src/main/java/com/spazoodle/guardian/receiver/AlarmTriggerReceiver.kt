package com.spazoodle.guardian.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.MainActivity
import com.spazoodle.guardian.domain.model.TriggerKind
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarmRepository = GuardianRuntime.alarmRepository(appContext)
                val alarm = alarmRepository.getById(alarmId)
                if (alarm == null || !alarm.enabled) return@launch

                GuardianRuntime.recordFireEventUseCase(appContext).invoke(
                    alarmId = alarmId,
                    triggerKind = triggerKind
                )

                if (triggerKind == TriggerKind.PRE_ALERT) {
                    postPreAlertNotification(
                        context = appContext,
                        alarmId = alarmId,
                        title = alarm.title
                    )
                    return@launch
                }

                val serviceIntent = Intent(appContext, AlarmRingingService::class.java).apply {
                    putExtra(EXTRA_ALARM_ID, alarmId)
                    putExtra(EXTRA_TRIGGER_KIND, triggerKind.name)
                    putExtra(EXTRA_TRIGGER_INDEX, intent.getIntExtra(EXTRA_TRIGGER_INDEX, -1))
                    putExtra(EXTRA_TRIGGER_KEY, intent.getStringExtra(EXTRA_TRIGGER_KEY))
                    putExtra(
                        EXTRA_SCHEDULED_AT_UTC_MILLIS,
                        intent.getLongExtra(EXTRA_SCHEDULED_AT_UTC_MILLIS, -1L)
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

    private fun postPreAlertNotification(context: Context, alarmId: Long, title: String) {
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

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TRIGGER_KIND = "extra_trigger_kind"
        const val EXTRA_TRIGGER_INDEX = "extra_trigger_index"
        const val EXTRA_TRIGGER_KEY = "extra_trigger_key"
        const val EXTRA_SCHEDULED_AT_UTC_MILLIS = "extra_scheduled_at_utc_millis"
    }
}
