package com.spazoodle.guardian.platform.reliability

enum class StopGuardMode {
    ONE_TAP,
    HOLD_TO_CONFIRM
}

data class HoldToStopConfig(
    val enabled: Boolean,
    val holdDurationMs: Long,
    val mode: StopGuardMode = if (enabled) StopGuardMode.HOLD_TO_CONFIRM else StopGuardMode.ONE_TAP
)

data class RetentionConfig(
    val historyRetentionDays: Int,
    val cleanupIntervalHours: Int
)

enum class ActionLaunchPolicy {
    REQUIRE_UNLOCK,
    DIRECT_WHEN_ALLOWED
}

object GuardianDiagnosticTags {
    const val ACTION_BLOCKED_DEVICE_LOCKED = "ACTION_BLOCKED_DEVICE_LOCKED"
    const val STOP_GUARD_STARTED = "STOP_GUARD_STARTED"
    const val STOP_GUARD_CONFIRMED = "STOP_GUARD_CONFIRMED"
    const val RETENTION_PRUNE_RUN = "RETENTION_PRUNE_RUN"
    const val AUDIO_ROUTE_EXTERNAL_WARNING = "AUDIO_ROUTE_EXTERNAL_WARNING"
}
