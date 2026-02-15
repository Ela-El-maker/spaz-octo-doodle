package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ReconcileEnabledAlarmsUseCaseTest {

    @Test
    fun pastEnabledAlarmIsClassifiedMissedAndFutureAlarmIsSchedulable() = runTest {
        val now = 10_000L
        val repository = FakeAlarmRepository(
            alarms = mutableListOf(
                alarm(id = 1L, triggerAt = now - 1_000L, enabled = true),
                alarm(id = 2L, triggerAt = now + 60_000L, enabled = true),
                alarm(id = 3L, triggerAt = now + 120_000L, enabled = false)
            )
        )
        val useCase = ReconcileEnabledAlarmsUseCase(
            alarmRepository = repository,
            computeSchedulePlanUseCase = ComputeSchedulePlanUseCase(FakeClock(now)),
            clock = FakeClock(now)
        )

        val result = useCase.invoke()

        assertEquals(listOf(1L), result.missedOneTimeAlarmIds)
        assertEquals(listOf(2L), result.reschedulablePlans.map { it.alarmId })
    }

    private class FakeClock(private val now: Long) : Clock {
        override fun nowUtcMillis(): Long = now
    }

    private class FakeAlarmRepository(
        private val alarms: MutableList<Alarm>
    ) : AlarmRepository {
        override suspend fun upsert(alarm: Alarm) {
            alarms.removeAll { it.id == alarm.id }
            alarms += alarm
        }

        override suspend fun delete(alarmId: Long) {
            alarms.removeAll { it.id == alarmId }
        }

        override fun observeAllAlarms(): Flow<List<Alarm>> = flowOf(alarms.toList())

        override suspend fun getAllAlarms(): List<Alarm> = alarms.toList()

        override suspend fun getEnabledAlarms(): List<Alarm> = alarms.filter { it.enabled }

        override suspend fun getUpcomingAlarms(fromUtcMillis: Long, limit: Int): List<Alarm> {
            return alarms.filter { it.enabled && it.triggerAtUtcMillis >= fromUtcMillis }
                .sortedBy { it.triggerAtUtcMillis }
                .take(limit)
        }

        override suspend fun getById(alarmId: Long): Alarm? = alarms.firstOrNull { it.id == alarmId }

        override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
            val idx = alarms.indexOfFirst { it.id == alarmId }
            if (idx >= 0) alarms[idx] = alarms[idx].copy(enabled = enabled)
        }
    }

    private fun alarm(id: Long, triggerAt: Long, enabled: Boolean): Alarm {
        return Alarm(
            id = id,
            title = "alarm-$id",
            description = null,
            type = AlarmType.ALARM,
            triggerAtUtcMillis = triggerAt,
            timezoneIdAtCreation = "UTC",
            enabled = enabled,
            vibrateEnabled = true,
            ringtoneUri = null,
            primaryAction = null,
            policy = AlarmPolicy(preAlerts = emptyList(), nagSpec = null, escalationSpec = null),
            snoozeSpec = SnoozeSpec(durationsMinutes = listOf(5, 10), defaultMinutes = 10),
            createdAtUtcMillis = 1L,
            updatedAtUtcMillis = 1L
        )
    }
}
