package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.repository.AlarmRepository

class DeleteAlarmUseCase(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        alarmRepository.delete(alarmId)
    }
}

