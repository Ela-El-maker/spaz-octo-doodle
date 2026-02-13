package com.spazoodle.guardian.platform.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver

class AndroidAlarmScheduler(
    private val context: Context
) : AlarmScheduler {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun scheduleMainTrigger(alarm: Alarm) {
        val pendingIntent = buildPendingIntent(alarm.id)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarm.triggerAtUtcMillis,
            pendingIntent
        )
    }

    override fun cancelMainTrigger(alarmId: Long) {
        alarmManager.cancel(buildPendingIntent(alarmId))
    }

    private fun buildPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
