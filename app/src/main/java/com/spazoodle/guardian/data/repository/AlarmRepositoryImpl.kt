package com.spazoodle.guardian.data.repository

import com.spazoodle.guardian.data.local.dao.AlarmDao
import com.spazoodle.guardian.data.local.entity.AlarmEntity
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.EscalationSpec
import com.spazoodle.guardian.domain.model.NagSpec
import com.spazoodle.guardian.domain.model.PreAlertOffset
import com.spazoodle.guardian.domain.model.PrimaryAction
import com.spazoodle.guardian.domain.model.PrimaryActionType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.repository.AlarmRepository

class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao
) : AlarmRepository {

    override suspend fun upsert(alarm: Alarm) {
        alarmDao.upsert(alarm.toEntity())
    }

    override suspend fun getAllAlarms(): List<Alarm> {
        return alarmDao.getAll().map { it.toDomain() }
    }

    override suspend fun getEnabledAlarms(): List<Alarm> {
        return alarmDao.getEnabled().map { it.toDomain() }
    }

    override suspend fun getUpcomingAlarms(fromUtcMillis: Long, limit: Int): List<Alarm> {
        return alarmDao.getUpcoming(fromUtcMillis = fromUtcMillis, limit = limit).map { it.toDomain() }
    }

    override suspend fun getById(alarmId: Long): Alarm? {
        return alarmDao.getById(alarmId)?.toDomain()
    }

    override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
        alarmDao.setEnabled(
            alarmId = alarmId,
            enabled = enabled,
            updatedAtUtcMillis = System.currentTimeMillis()
        )
    }
}

private fun Alarm.toEntity(): AlarmEntity {
    val preAlertKeys = policy.preAlerts.joinToString(",") { it.key }
    val preAlertOffsets = policy.preAlerts.joinToString(",") { it.offsetMillis.toString() }
    val nag = policy.nagSpec
    val escalation = policy.escalationSpec

    return AlarmEntity(
        id = id,
        title = title,
        type = type.name,
        triggerAtUtcMillis = triggerAtUtcMillis,
        timezoneIdAtCreation = timezoneIdAtCreation,
        enabled = enabled,
        meetingUrl = primaryAction
            ?.takeIf { it.type == PrimaryActionType.OPEN_URL }
            ?.value,
        primaryActionType = primaryAction?.type?.name,
        primaryActionValue = primaryAction?.value,
        primaryActionLabel = primaryAction?.label,
        preAlertKeysCsv = preAlertKeys,
        preAlertOffsetsCsv = preAlertOffsets,
        nagEnabled = nag?.enabled ?: false,
        nagRepeatMinutesCsv = nag?.repeatMinutes?.joinToString(",") ?: "",
        nagMaxCount = nag?.maxNagCount ?: 0,
        nagMaxWindowMinutes = nag?.maxNagWindowMinutes ?: 0,
        escalationEnabled = escalation?.enabled ?: false,
        escalationStepUpAfterNagCount = escalation?.stepUpAfterNagCount ?: 0,
        escalationToneSequenceCsv = escalation?.toneSequence?.joinToString(",") ?: "",
        snoozeDurationsCsv = snoozeSpec.durationsMinutes.joinToString(","),
        snoozeDefaultMinutes = snoozeSpec.defaultMinutes,
        createdAtUtcMillis = createdAtUtcMillis,
        updatedAtUtcMillis = updatedAtUtcMillis
    )
}

private fun AlarmEntity.toDomain(): Alarm {
    val preAlertKeys = preAlertKeysCsv.splitCsv()
    val preAlertOffsets = preAlertOffsetsCsv
        .splitCsv()
        .mapNotNull { it.toLongOrNull() }
    val preAlerts = preAlertKeys.zip(preAlertOffsets).map { (key, offsetMillis) ->
        PreAlertOffset(key = key, offsetMillis = offsetMillis)
    }
    val nagSpec = if (nagEnabled) {
        NagSpec(
            enabled = true,
            repeatMinutes = nagRepeatMinutesCsv.splitCsv().mapNotNull { it.toIntOrNull() },
            maxNagCount = nagMaxCount,
            maxNagWindowMinutes = nagMaxWindowMinutes
        )
    } else {
        null
    }
    val escalationSpec = if (escalationEnabled) {
        EscalationSpec(
            enabled = true,
            stepUpAfterNagCount = escalationStepUpAfterNagCount,
            toneSequence = escalationToneSequenceCsv.splitCsv()
        )
    } else {
        null
    }

    val resolvedPrimaryAction = if (!primaryActionType.isNullOrBlank() && !primaryActionValue.isNullOrBlank()) {
        PrimaryAction(
            type = runCatching { PrimaryActionType.valueOf(primaryActionType) }
                .getOrDefault(PrimaryActionType.OPEN_URL),
            value = primaryActionValue,
            label = primaryActionLabel
        )
    } else if (!meetingUrl.isNullOrBlank()) {
        PrimaryAction(
            type = PrimaryActionType.OPEN_URL,
            value = meetingUrl,
            label = "Open"
        )
    } else {
        null
    }

    return Alarm(
        id = id,
        title = title,
        type = runCatching { AlarmType.valueOf(type) }.getOrDefault(AlarmType.ALARM),
        triggerAtUtcMillis = triggerAtUtcMillis,
        timezoneIdAtCreation = timezoneIdAtCreation,
        enabled = enabled,
        primaryAction = resolvedPrimaryAction,
        policy = AlarmPolicy(
            preAlerts = preAlerts,
            nagSpec = nagSpec,
            escalationSpec = escalationSpec
        ),
        snoozeSpec = SnoozeSpec(
            durationsMinutes = snoozeDurationsCsv.splitCsv().mapNotNull { it.toIntOrNull() },
            defaultMinutes = snoozeDefaultMinutes
        ),
        createdAtUtcMillis = createdAtUtcMillis,
        updatedAtUtcMillis = updatedAtUtcMillis
    )
}

private fun String.splitCsv(): List<String> {
    if (isBlank()) return emptyList()
    return split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
