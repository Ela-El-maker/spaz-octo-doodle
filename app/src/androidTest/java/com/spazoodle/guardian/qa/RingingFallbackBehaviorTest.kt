package com.spazoodle.guardian.qa

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver
import com.spazoodle.guardian.service.AlarmRingingService
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RingingFallbackBehaviorTest {

    @Test
    fun fullScreenBlocked_stillRunsForegroundWithFallbackNotification() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmId = System.currentTimeMillis()

        val intent = Intent(context, AlarmRingingService::class.java).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE, "Fallback Path Alarm")
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_DESCRIPTION, "Validate heads-up fallback")
            putExtra(AlarmTriggerReceiver.EXTRA_TRIGGER_KIND, "MAIN")
            putExtra(AlarmTriggerReceiver.EXTRA_FULL_SCREEN_READY, false)
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_VIBRATE_ENABLED, false)
            putExtra(AlarmTriggerReceiver.EXTRA_ALARM_RINGTONE_URI, "")
        }

        context.startForegroundService(intent)
        Thread.sleep(1_500)

        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val alarmNotification = manager.activeNotifications.firstOrNull {
                it.id == AlarmRingingService.NOTIFICATION_ID
            }

            assertNotNull("Expected foreground alarm notification", alarmNotification)
            val contentText = alarmNotification?.notification?.extras?.getCharSequence("android.text")?.toString().orEmpty()
            assertTrue(
                "Expected fallback messaging when full-screen is blocked. text=$contentText",
                contentText.contains("Full-screen blocked", ignoreCase = true)
            )
            assertTrue(
                "Expected ongoing foreground notification",
                alarmNotification?.notification?.flags?.and(android.app.Notification.FLAG_ONGOING_EVENT) != 0
            )
        } finally {
            context.stopService(Intent(context, AlarmRingingService::class.java))
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(AlarmRingingService.NOTIFICATION_ID)
            Thread.sleep(300)
        }
    }
}
