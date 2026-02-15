package com.spazoodle.guardian.platform.reliability

import com.spazoodle.guardian.domain.model.LateRecoveryPolicy

object GuardianPolicyConfig {
    val lateRecoveryPolicy = LateRecoveryPolicy(
        recoverGraceMinutes = 10,
        missedNotifyEnabled = true
    )

    const val dedupeWindowMs = 8_000L
    const val triggerExecutionRetentionMs = 1000L * 60L * 60L * 24L * 30L
    val holdToStopConfig = HoldToStopConfig(
        enabled = true,
        holdDurationMs = 1_800L
    )
    val retentionConfig = RetentionConfig(
        historyRetentionDays = 30,
        cleanupIntervalHours = 24
    )
    val actionLaunchPolicy = ActionLaunchPolicy.REQUIRE_UNLOCK
}
