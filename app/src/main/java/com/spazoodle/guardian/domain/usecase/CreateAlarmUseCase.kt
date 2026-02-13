package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.repository.AlarmRepository

class CreateAlarmUseCase(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarm: Alarm) {
        require(alarm.triggerAtUtcMillis > 0L) { "Trigger time must be positive." }
        alarmRepository.upsert(alarm)
    }
}
