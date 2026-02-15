package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Test

class FinalizeOneTimeAlarmUseCaseTest {

    @Test
    fun finalizeDisablesAlarm() = runTest {
        val repository = FakeAlarmRepository(
            alarm = Alarm(
                id = 99L,
                title = "test",
                description = null,
                type = AlarmType.ALARM,
                triggerAtUtcMillis = 100_000L,
                timezoneIdAtCreation = "UTC",
                enabled = true,
                vibrateEnabled = true,
                ringtoneUri = null,
                primaryAction = null,
                policy = AlarmPolicy(preAlerts = emptyList(), nagSpec = null, escalationSpec = null),
                snoozeSpec = SnoozeSpec(durationsMinutes = listOf(5, 10), defaultMinutes = 10),
                createdAtUtcMillis = 1L,
                updatedAtUtcMillis = 1L
            )
        )

        val useCase = FinalizeOneTimeAlarmUseCase(repository)
        useCase.invoke(99L)

        assertFalse(repository.alarm.enabled)
    }

    private class FakeAlarmRepository(
        var alarm: Alarm
    ) : AlarmRepository {
        override suspend fun upsert(alarm: Alarm) {
            this.alarm = alarm
        }

        override suspend fun delete(alarmId: Long) {
            if (alarm.id == alarmId) {
                alarm = alarm.copy(enabled = false)
            }
        }

        override fun observeAllAlarms(): Flow<List<Alarm>> = flowOf(listOf(alarm))

        override suspend fun getAllAlarms(): List<Alarm> = listOf(alarm)

        override suspend fun getEnabledAlarms(): List<Alarm> = if (alarm.enabled) listOf(alarm) else emptyList()

        override suspend fun getUpcomingAlarms(fromUtcMillis: Long, limit: Int): List<Alarm> = getEnabledAlarms()

        override suspend fun getById(alarmId: Long): Alarm? = if (alarm.id == alarmId) alarm else null

        override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
            if (alarm.id == alarmId) {
                alarm = alarm.copy(enabled = enabled)
            }
        }
    }
}
