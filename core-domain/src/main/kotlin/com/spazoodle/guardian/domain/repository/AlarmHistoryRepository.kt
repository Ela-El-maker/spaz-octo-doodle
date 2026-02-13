package com.spazoodle.guardian.domain.repository

import com.spazoodle.guardian.domain.model.AlarmEvent

interface AlarmHistoryRepository {
    suspend fun append(event: AlarmEvent)
    suspend fun getByAlarmId(alarmId: Long): List<AlarmEvent>
    suspend fun getRecent(limit: Int): List<AlarmEvent>
}
