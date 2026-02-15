package com.spazoodle.guardian.platform.reliability

data class ReliabilityStatus(
    val notificationsEnabled: Boolean,
    val exactAlarmAllowed: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val fullScreenReady: Boolean,
    val dndAlarmsLikelyAllowed: Boolean,
    val healthScore: Int,
    val strictModeBlocked: Boolean,
    val riskLevel: RiskLevel,
    val riskReasons: List<RiskReason>,
    val restoredAfterStopLikely: Boolean,
    val restoredMissingTriggerCount: Int,
    val restoredTotalPlanCount: Int,
    val restoredAtUtcMillis: Long?,
    val manufacturer: String,
    val oemSteps: List<OemStep>
)
