package com.spazoodle.guardian.domain.model

data class LateRecoveryPolicy(
    val recoverGraceMinutes: Int = 10,
    val missedNotifyEnabled: Boolean = true
)
