package com.spazoodle.guardian

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class GuardianApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)
        val channels = listOf(
            NotificationChannel(
                CHANNEL_ALARM,
                "Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ),
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

    companion object {
        const val CHANNEL_ALARM = "guardian_alarm"
        const val CHANNEL_PRE_ALERT = "guardian_pre_alert"
        const val CHANNEL_DIAGNOSTICS = "guardian_diagnostics"
    }
}
