package com.spazoodle.guardian.platform.reliability

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import com.spazoodle.guardian.GuardianApp
import com.spazoodle.guardian.runtime.GuardianRuntime

class ReliabilityScanner(
    private val context: Context
) {
    fun scan(): ReliabilityStatus {
        val startupRecovery = StartupRecoveryStore(context).snapshot()
        val notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val exactAlarmAllowed = GuardianRuntime.alarmScheduler(context).canScheduleExactAlarms()

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val batteryOptimizationIgnored = powerManager.isIgnoringBatteryOptimizations(context.packageName)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val fullScreenReady = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(GuardianApp.CHANNEL_ALARM)
            channel != null && channel.importance >= NotificationManager.IMPORTANCE_HIGH
        } else {
            true
        }

        val dndAlarmsLikelyAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            true
        }

        val riskReasons = mutableListOf<RiskReason>().apply {
            if (!notificationsEnabled) add(RiskReason.NOTIFICATION_DISABLED)
            if (!exactAlarmAllowed) add(RiskReason.EXACT_ALARM_BLOCKED)
            if (!batteryOptimizationIgnored) add(RiskReason.BATTERY_OPTIMIZED)
            if (!fullScreenReady) add(RiskReason.FULL_SCREEN_NOT_READY)
            if (!dndAlarmsLikelyAllowed) add(RiskReason.DND_MAY_SUPPRESS)
        }

        val manufacturer = Build.MANUFACTURER.orEmpty()
        val oemPlaybook = OemReliabilityPlaybooks.resolve(manufacturer)
        if (oemPlaybook != null) {
            riskReasons.add(RiskReason.OEM_BACKGROUND_RESTRICTIONS)
        }

        val healthScore = listOf(
            notificationsEnabled,
            exactAlarmAllowed,
            batteryOptimizationIgnored,
            fullScreenReady,
            dndAlarmsLikelyAllowed
        ).count { it } * 20

        val riskLevel = when {
            !exactAlarmAllowed && GuardianFeatureFlags.strictExactGate -> RiskLevel.BLOCKED
            riskReasons.size >= 3 -> RiskLevel.HIGH
            riskReasons.isNotEmpty() -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        return ReliabilityStatus(
            notificationsEnabled = notificationsEnabled,
            exactAlarmAllowed = exactAlarmAllowed,
            batteryOptimizationIgnored = batteryOptimizationIgnored,
            fullScreenReady = fullScreenReady,
            dndAlarmsLikelyAllowed = dndAlarmsLikelyAllowed,
            healthScore = healthScore,
            strictModeBlocked = !exactAlarmAllowed && GuardianFeatureFlags.strictExactGate,
            riskLevel = riskLevel,
            riskReasons = riskReasons,
            restoredAfterStopLikely = startupRecovery.recoveredAfterStopLikely,
            restoredMissingTriggerCount = startupRecovery.missingTriggerCount,
            restoredTotalPlanCount = startupRecovery.totalPlanCount,
            restoredAtUtcMillis = startupRecovery.recoveredAtUtcMillis,
            manufacturer = manufacturer,
            oemSteps = oemPlaybook?.steps().orEmpty()
        )
    }
}
