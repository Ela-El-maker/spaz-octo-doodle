package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.model.TriggerKind

class ComputeSchedulePlanUseCase(
    private val clock: Clock
) {
    operator fun invoke(alarm: Alarm): SchedulePlan {
        val now = clock.nowUtcMillis()
        require(alarm.triggerAtUtcMillis > now) {
            "Cannot compute schedule plan for past alarm trigger time."
        }

        val triggers = mutableListOf<Trigger>()

        alarm.policy.preAlerts
            .distinctBy { it.key }
            .sortedByDescending { it.offsetMillis }
            .forEachIndexed { index, preAlert ->
                val scheduledAt = alarm.triggerAtUtcMillis - preAlert.offsetMillis
                if (scheduledAt > now) {
                    triggers += Trigger(
                        alarmId = alarm.id,
                        kind = TriggerKind.PRE_ALERT,
                        scheduledAtUtcMillis = scheduledAt,
                        index = index,
                        key = preAlert.key
                    )
                }
            }

        triggers += Trigger(
            alarmId = alarm.id,
            kind = TriggerKind.MAIN,
            scheduledAtUtcMillis = alarm.triggerAtUtcMillis,
            index = 0,
            key = "MAIN"
        )

        return SchedulePlan(
            alarmId = alarm.id,
            triggers = triggers.sortedBy { it.scheduledAtUtcMillis }
        )
    }
}
