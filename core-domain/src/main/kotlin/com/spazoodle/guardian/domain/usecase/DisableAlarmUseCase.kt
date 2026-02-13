package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.repository.AlarmRepository

class DisableAlarmUseCase(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmRepository.setEnabled(alarmId, false)
    }
}
