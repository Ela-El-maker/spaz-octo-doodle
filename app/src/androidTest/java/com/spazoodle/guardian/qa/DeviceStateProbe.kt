package com.spazoodle.guardian.qa

import android.app.NotificationManager
import android.content.Context
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat

data class DeviceStateSnapshot(
    val notificationsEnabled: Boolean,
    val interruptionFilter: Int,
    val batteryOptimized: Boolean
)

object DeviceStateProbe {
    fun snapshot(context: Context): DeviceStateSnapshot {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return DeviceStateSnapshot(
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            interruptionFilter = nm.currentInterruptionFilter,
            batteryOptimized = !pm.isIgnoringBatteryOptimizations(context.packageName)
        )
    }
}
