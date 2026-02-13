package com.spazoodle.guardian.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spazoodle.guardian.service.AlarmRingingService

class AlarmTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId <= 0L) return

        val serviceIntent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_TRIGGER_KIND, intent.getStringExtra(EXTRA_TRIGGER_KIND))
            putExtra(EXTRA_TRIGGER_INDEX, intent.getIntExtra(EXTRA_TRIGGER_INDEX, -1))
            putExtra(EXTRA_TRIGGER_KEY, intent.getStringExtra(EXTRA_TRIGGER_KEY))
            putExtra(
                EXTRA_SCHEDULED_AT_UTC_MILLIS,
                intent.getLongExtra(EXTRA_SCHEDULED_AT_UTC_MILLIS, -1L)
            )
        }
        context.startForegroundService(serviceIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_TRIGGER_KIND = "extra_trigger_kind"
        const val EXTRA_TRIGGER_INDEX = "extra_trigger_index"
        const val EXTRA_TRIGGER_KEY = "extra_trigger_key"
        const val EXTRA_SCHEDULED_AT_UTC_MILLIS = "extra_scheduled_at_utc_millis"
    }
}
