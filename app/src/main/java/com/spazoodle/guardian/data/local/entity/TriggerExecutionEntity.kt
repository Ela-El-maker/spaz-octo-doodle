package com.spazoodle.guardian.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trigger_executions",
    indices = [
        Index(value = ["firstSeenAtUtcMillis"]),
        Index(value = ["handled"])
    ]
)
data class TriggerExecutionEntity(
    @PrimaryKey val executionKey: String,
    val firstSeenAtUtcMillis: Long,
    val lastSeenAtUtcMillis: Long,
    val handled: Boolean
)
