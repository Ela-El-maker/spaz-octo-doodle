package com.spazoodle.guardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val triggerAtUtcMillis: Long,
    val timezoneIdAtCreation: String,
    val enabled: Boolean,
    val meetingUrl: String?
)
