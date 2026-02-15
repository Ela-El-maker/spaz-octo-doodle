package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.domain.repository.AlarmHistoryRepository

class RecordFireEventUseCase(
    private val alarmHistoryRepository: AlarmHistoryRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        alarmId: Long,
        triggerKind: TriggerKind,
        scheduledAtUtcMillis: Long? = null,
        outcome: AlarmEventOutcome = AlarmEventOutcome.FIRED,
        deliveryState: DeliveryState = DeliveryState.FIRED,
        wasDeduped: Boolean = false,
        detail: String? = null
    ) {
        val firedAt = clock.nowUtcMillis()
        val delayMs = scheduledAtUtcMillis?.let { firedAt - it }
        alarmHistoryRepository.append(
            AlarmEvent(
                alarmId = alarmId,
                triggerKind = triggerKind,
                outcome = outcome,
                eventAtUtcMillis = firedAt,
                detail = detail,
                scheduledAtUtcMillis = scheduledAtUtcMillis,
                firedAtUtcMillis = firedAt,
                delayMs = delayMs,
                wasDeduped = wasDeduped,
                deliveryState = deliveryState
            )
        )
    }
}
