package com.spazoodle.guardian.domain.model

data class Trigger(
    val alarmId: Long,
    val kind: TriggerKind,
    val scheduledAtUtcMillis: Long,
    val index: Int,
    val key: String? = null
)
