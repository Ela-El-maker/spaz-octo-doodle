package com.spazoodle.guardian.domain.model

data class AlarmEvent(
    val alarmId: Long,
    val triggerKind: TriggerKind,
    val outcome: AlarmEventOutcome,
    val eventAtUtcMillis: Long,
    val detail: String? = null
)
