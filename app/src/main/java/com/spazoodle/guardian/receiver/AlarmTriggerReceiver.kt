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
        }
        context.startForegroundService(serviceIntent)
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}
