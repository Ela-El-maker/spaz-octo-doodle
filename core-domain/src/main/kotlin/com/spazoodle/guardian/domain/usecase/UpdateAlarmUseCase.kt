package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.repository.AlarmRepository

class UpdateAlarmUseCase(
    private val alarmRepository: AlarmRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(alarm: Alarm) {
        require(alarm.triggerAtUtcMillis > clock.nowUtcMillis()) {
            "Alarm trigger time must be in the future."
        }
        alarmRepository.upsert(alarm)
    }
}
