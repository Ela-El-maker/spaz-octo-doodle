package com.spazoodle.guardian.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver

class AlarmRingingService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            buildForegroundNotification(
                alarmId = intent?.getLongExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, -1L) ?: -1L,
                triggerKind = intent?.getStringExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_KIND)
            )
        )
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildForegroundNotification(alarmId: Long, triggerKind: String?): Notification {
        return NotificationCompat.Builder(this, GuardianApp.CHANNEL_ALARM)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Guardian alarm")
            .setContentText("alarmId=$alarmId trigger=${triggerKind.orEmpty()}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
