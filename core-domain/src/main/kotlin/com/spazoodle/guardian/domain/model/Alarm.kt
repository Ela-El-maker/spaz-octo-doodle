package com.spazoodle.guardian.domain.model

data class Alarm(
    val id: Long,
    val title: String,
    val type: AlarmType,
    val triggerAtUtcMillis: Long,
    val timezoneIdAtCreation: String,
    val enabled: Boolean,
    val meetingUrl: String?,
    val policy: AlarmPolicy,
    val snoozeSpec: SnoozeSpec,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long
)
