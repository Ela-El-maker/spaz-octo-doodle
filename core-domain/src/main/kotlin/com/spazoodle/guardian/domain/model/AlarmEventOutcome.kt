package com.spazoodle.guardian.domain.model

enum class AlarmEventOutcome {
    SCHEDULED,
    FIRED,
    DISMISSED,
    SNOOZED,
    ACTION_LAUNCHED,
    MISSED,
    CANCELED,
    RESCHEDULED
}
