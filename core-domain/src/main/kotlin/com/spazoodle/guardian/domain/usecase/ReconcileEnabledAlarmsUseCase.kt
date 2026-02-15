package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.repository.AlarmRepository

data class ReconcileEnabledAlarmsResult(
    val reschedulablePlans: List<SchedulePlan>,
    val missedOneTimeAlarmIds: List<Long>
)

class ReconcileEnabledAlarmsUseCase(
    private val alarmRepository: AlarmRepository,
    private val computeSchedulePlanUseCase: ComputeSchedulePlanUseCase,
    private val clock: Clock
) {
    suspend operator fun invoke(): ReconcileEnabledAlarmsResult {
        val now = clock.nowUtcMillis()
        val plans = mutableListOf<SchedulePlan>()
        val missedAlarmIds = mutableListOf<Long>()

        alarmRepository.getEnabledAlarms().forEach { alarm ->
            if (alarm.triggerAtUtcMillis <= now) {
                missedAlarmIds += alarm.id
            } else {
                runCatching { computeSchedulePlanUseCase(alarm) }
                    .onSuccess { plans += it }
                    .onFailure { missedAlarmIds += alarm.id }
            }
        }

        return ReconcileEnabledAlarmsResult(
            reschedulablePlans = plans,
            missedOneTimeAlarmIds = missedAlarmIds
        )
    }
}

