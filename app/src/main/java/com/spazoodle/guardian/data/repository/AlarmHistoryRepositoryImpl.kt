package com.spazoodle.guardian.data.repository

import com.spazoodle.guardian.data.local.dao.AlarmHistoryDao
import com.spazoodle.guardian.data.local.entity.AlarmHistoryEntity
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.TriggerKind
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
                detail = event.detail,
                scheduledAtUtcMillis = event.scheduledAtUtcMillis,
                firedAtUtcMillis = event.firedAtUtcMillis,
                delayMs = event.delayMs,
                wasDeduped = event.wasDeduped,
                deliveryState = event.deliveryState.name
            )
        )
    }

    override suspend fun getByAlarmId(alarmId: Long): List<AlarmEvent> {
        return alarmHistoryDao.getByAlarmId(alarmId).map { it.toDomain() }
    }

    override suspend fun getRecent(limit: Int): List<AlarmEvent> {
        return alarmHistoryDao.getRecent(limit).map { it.toDomain() }
    }
}

private fun AlarmHistoryEntity.toDomain(): AlarmEvent {
    return AlarmEvent(
        alarmId = alarmId,
        triggerKind = runCatching { TriggerKind.valueOf(triggerKind) }.getOrDefault(TriggerKind.MAIN),
        outcome = runCatching { AlarmEventOutcome.valueOf(outcome) }.getOrDefault(AlarmEventOutcome.FIRED),
        eventAtUtcMillis = eventAtUtcMillis,
        detail = detail,
        scheduledAtUtcMillis = scheduledAtUtcMillis,
        firedAtUtcMillis = firedAtUtcMillis,
        delayMs = delayMs,
        wasDeduped = wasDeduped,
        deliveryState = runCatching { DeliveryState.valueOf(deliveryState) }.getOrDefault(DeliveryState.FIRED)
    )
}
