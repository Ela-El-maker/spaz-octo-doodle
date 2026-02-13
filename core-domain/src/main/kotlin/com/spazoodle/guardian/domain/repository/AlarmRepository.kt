package com.spazoodle.guardian.domain.repository

import com.spazoodle.guardian.domain.model.Alarm

interface AlarmRepository {
    suspend fun upsert(alarm: Alarm)
    suspend fun getEnabledAlarms(): List<Alarm>
    suspend fun getById(alarmId: Long): Alarm?
    suspend fun setEnabled(alarmId: Long, enabled: Boolean)
}
