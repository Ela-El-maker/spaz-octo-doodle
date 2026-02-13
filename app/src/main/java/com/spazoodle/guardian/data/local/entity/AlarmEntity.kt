package com.spazoodle.guardian.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarms",
    indices = [
        Index(value = ["enabled", "triggerAtUtcMillis"])
    ]
)
data class AlarmEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val type: String,
    val triggerAtUtcMillis: Long,
    val timezoneIdAtCreation: String,
    val enabled: Boolean,
    val meetingUrl: String?,
    val primaryActionType: String?,
    val primaryActionValue: String?,
    val primaryActionLabel: String?,
    val preAlertKeysCsv: String,
    val preAlertOffsetsCsv: String,
    val nagEnabled: Boolean,
    val nagRepeatMinutesCsv: String,
    val nagMaxCount: Int,
    val nagMaxWindowMinutes: Int,
    val escalationEnabled: Boolean,
    val escalationStepUpAfterNagCount: Int,
    val escalationToneSequenceCsv: String,
    val snoozeDurationsCsv: String,
    val snoozeDefaultMinutes: Int,
    val createdAtUtcMillis: Long,
    val updatedAtUtcMillis: Long
)
