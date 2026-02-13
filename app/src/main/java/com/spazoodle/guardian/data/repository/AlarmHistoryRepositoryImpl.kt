package com.spazoodle.guardian.data.repository

import com.spazoodle.guardian.data.local.dao.AlarmHistoryDao
import com.spazoodle.guardian.data.local.entity.AlarmHistoryEntity
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.repository.AlarmHistoryRepository

class AlarmHistoryRepositoryImpl(
    private val alarmHistoryDao: AlarmHistoryDao
) : AlarmHistoryRepository {
    override suspend fun append(event: AlarmEvent) {
        alarmHistoryDao.insert(
            AlarmHistoryEntity(
                alarmId = event.alarmId,
                triggerKind = event.triggerKind.name,
                outcome = event.outcome.name,
                eventAtUtcMillis = event.eventAtUtcMillis,
                detail = event.detail
            )
        )
    }
}
