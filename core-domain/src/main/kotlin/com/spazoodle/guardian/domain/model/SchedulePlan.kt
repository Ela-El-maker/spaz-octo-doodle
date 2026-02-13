package com.spazoodle.guardian.domain.model

data class SchedulePlan(
    val alarmId: Long,
    val triggers: List<Trigger>
)
