package com.spazoodle.guardian.domain.model

data class AlarmEvent(
    val alarmId: Long,
    val triggerKind: TriggerKind,
    val outcome: AlarmEventOutcome,
    val eventAtUtcMillis: Long,
    val detail: String? = null,
    val scheduledAtUtcMillis: Long? = null,
    val firedAtUtcMillis: Long? = null,
    val delayMs: Long? = null,
    val wasDeduped: Boolean = false,
    val deliveryState: DeliveryState = DeliveryState.FIRED
)
