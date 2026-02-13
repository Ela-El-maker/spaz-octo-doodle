package com.spazoodle.guardian.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alarm_history",
    indices = [
        Index(value = ["alarmId"]),
        Index(value = ["eventAtUtcMillis"])
    ]
)
data class AlarmHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alarmId: Long,
    val triggerKind: String,
    val outcome: String,
    val eventAtUtcMillis: Long,
    val detail: String?
)
