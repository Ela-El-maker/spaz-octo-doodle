package com.spazoodle.guardian.data.repository

import com.spazoodle.guardian.data.local.dao.AlarmDao
import com.spazoodle.guardian.data.local.entity.AlarmEntity
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.repository.AlarmRepository

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override suspend fun upsert(alarm: Alarm) {
        alarmDao.upsert(alarm.toEntity())
    }

    override suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabled().map { it.toDomain() }
    }
}

private fun Alarm.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        title = title,
        triggerAtUtcMillis = triggerAtUtcMillis,
        timezoneIdAtCreation = timezoneIdAtCreation,
        enabled = enabled,
        meetingUrl = meetingUrl
    )
}

private fun AlarmEntity.toDomain(): Alarm {
    return Alarm(
        id = id,
        title = title,
        triggerAtUtcMillis = triggerAtUtcMillis,
        timezoneIdAtCreation = timezoneIdAtCreation,
        enabled = enabled,
        meetingUrl = meetingUrl
    )
}
