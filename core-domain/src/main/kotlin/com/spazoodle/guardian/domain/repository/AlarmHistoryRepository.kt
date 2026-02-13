package com.spazoodle.guardian.domain.repository

import com.spazoodle.guardian.domain.model.AlarmEvent

interface AlarmHistoryRepository {
    suspend fun append(event: AlarmEvent)
}
