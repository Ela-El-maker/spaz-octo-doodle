package com.spazoodle.guardian.domain.model

data class Alarm(
    val id: Long,
    val title: String,
    val triggerAtUtcMillis: Long,
    val timezoneIdAtCreation: String,
    val enabled: Boolean,
    val meetingUrl: String?
)
