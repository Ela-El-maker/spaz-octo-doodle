package com.spazoodle.guardian.domain.model

enum class AlarmEventOutcome {
    SCHEDULED,
    FIRED,
    RECOVERED_LATE,
    DISMISSED,
    SNOOZED,
    ACTION_LAUNCHED,
    MISSED,
    CANCELED,
    RESCHEDULED
}
