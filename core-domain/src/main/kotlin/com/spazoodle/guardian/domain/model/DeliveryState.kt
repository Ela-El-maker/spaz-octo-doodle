package com.spazoodle.guardian.domain.model

enum class DeliveryState {
    SCHEDULED,
    FIRED,
    RECOVERED_LATE,
    MISSED,
    DISMISSED,
    SNOOZED,
    ACTION_LAUNCHED
}
