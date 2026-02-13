package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.repository.AlarmRepository

class RescheduleAllActiveAlarmsUseCase(
    private val alarmRepository: AlarmRepository,
    private val computeSchedulePlanUseCase: ComputeSchedulePlanUseCase
) {
    suspend operator fun invoke(): List<SchedulePlan> {
        return alarmRepository
            .getEnabledAlarms()
            .map { computeSchedulePlanUseCase(it) }
    }
}
