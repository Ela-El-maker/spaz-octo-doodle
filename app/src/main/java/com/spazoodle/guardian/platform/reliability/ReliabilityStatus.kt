package com.spazoodle.guardian.platform.reliability

data class ReliabilityStatus(
    val notificationsEnabled: Boolean,
    val exactAlarmAllowed: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val fullScreenReady: Boolean,
    val dndAlarmsLikelyAllowed: Boolean,
    val healthScore: Int
)
