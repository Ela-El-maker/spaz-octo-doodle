package com.spazoodle.guardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_risk_cache")
data class AlarmRiskCacheEntity(
    @PrimaryKey val alarmId: Long,
    val riskLevel: String,
    val reasonsCsv: String,
    val updatedAtUtcMillis: Long
)
