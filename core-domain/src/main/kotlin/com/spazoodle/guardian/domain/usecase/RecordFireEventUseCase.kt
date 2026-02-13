package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.domain.repository.AlarmHistoryRepository

class RecordFireEventUseCase(
    private val alarmHistoryRepository: AlarmHistoryRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(alarmId: Long, triggerKind: TriggerKind) {
        alarmHistoryRepository.append(
            AlarmEvent(
                alarmId = alarmId,
                triggerKind = triggerKind,
                outcome = AlarmEventOutcome.FIRED,
                eventAtUtcMillis = clock.nowUtcMillis()
            )
        )
    }
}
