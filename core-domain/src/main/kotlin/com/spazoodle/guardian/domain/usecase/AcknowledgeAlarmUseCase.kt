package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.domain.repository.AlarmHistoryRepository

class AcknowledgeAlarmUseCase(
    private val alarmHistoryRepository: AlarmHistoryRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(alarmId: Long, outcome: AlarmEventOutcome) {
        require(
            outcome == AlarmEventOutcome.DISMISSED ||
                outcome == AlarmEventOutcome.SNOOZED ||
                outcome == AlarmEventOutcome.JOINED
        ) {
            "Outcome must be a user acknowledgement outcome."
        }

        alarmHistoryRepository.append(
            AlarmEvent(
                alarmId = alarmId,
                triggerKind = TriggerKind.MAIN,
                outcome = outcome,
                eventAtUtcMillis = clock.nowUtcMillis()
            )
        )
    }
}
