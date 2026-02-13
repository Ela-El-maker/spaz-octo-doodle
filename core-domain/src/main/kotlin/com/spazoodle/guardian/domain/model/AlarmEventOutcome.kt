package com.spazoodle.guardian.domain.model

enum class AlarmEventOutcome {
    SCHEDULED,
    FIRED,
    DISMISSED,
    SNOOZED,
    JOINED,
    MISSED,
    CANCELED,
    RESCHEDULED
}
