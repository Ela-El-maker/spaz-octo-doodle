package com.spazoodle.guardian.platform.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.scheduler.TriggerRequestCodeFactory
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver

class AndroidAlarmScheduler(
    private val context: Context,
    private val registry: ScheduledTriggerRegistry = ScheduledTriggerRegistry(context)
) : AlarmScheduler {

    private val alarmManager: AlarmManager by lazy {
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    override fun schedule(plan: SchedulePlan) {
        cancelAlarm(plan.alarmId)

        val requestCodes = mutableSetOf<Int>()
        plan.triggers.forEach { trigger ->
            val requestCode = TriggerRequestCodeFactory.create(trigger)
            requestCodes += requestCode
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                trigger.scheduledAtUtcMillis,
                buildPendingIntent(trigger, requestCode)
            )
        }

        registry.writeCodes(plan.alarmId, requestCodes)
    }

    override fun cancelAlarm(alarmId: Long) {
        registry.readCodes(alarmId).forEach { requestCode ->
            alarmManager.cancel(buildPendingIntent(requestCode))
        }
        registry.clear(alarmId)
    }

    override fun rescheduleAll(plans: List<SchedulePlan>) {
        plans.forEach { schedule(it) }
    }

    private fun buildPendingIntent(trigger: Trigger, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmTriggerReceiver::class.java).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, trigger.alarmId)
            putExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_KIND, trigger.kind.name)
            putExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_INDEX, trigger.index)
            putExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_KEY, trigger.key)
            putExtra(AlarmTriggerReceiver.EXTRA_SCHEDULED_AT_UTC_MILLIS, trigger.scheduledAtUtcMillis)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildPendingIntent(requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmTriggerReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
