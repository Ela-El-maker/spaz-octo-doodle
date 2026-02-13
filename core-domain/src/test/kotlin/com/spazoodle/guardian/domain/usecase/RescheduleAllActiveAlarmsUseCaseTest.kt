package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DefaultPreAlertOffsets
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.repository.AlarmRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RescheduleAllActiveAlarmsUseCaseTest {

    @Test
    fun buildsSchedulePlansForEnabledAlarms() = runTest {
        val repository = FakeAlarmRepository(
            alarms = listOf(
                testAlarm(id = 1L, triggerAt = 10_000_000L, enabled = true),
                testAlarm(id = 2L, triggerAt = 20_000_000L, enabled = false),
                testAlarm(id = 3L, triggerAt = 30_000_000L, enabled = true)
            )
        )
        val compute = ComputeSchedulePlanUseCase(FakeClock(1_000_000L))
        val useCase = RescheduleAllActiveAlarmsUseCase(repository, compute)

        val plans = useCase()

        assertEquals(2, plans.size)
        assertEquals(1L, plans[0].alarmId)
        assertEquals(3L, plans[1].alarmId)
    }

    private fun testAlarm(id: Long, triggerAt: Long, enabled: Boolean): Alarm {
        return Alarm(
            id = id,
            title = "Alarm $id",
            type = AlarmType.ALARM,
            triggerAtUtcMillis = triggerAt,
            timezoneIdAtCreation = "Africa/Nairobi",
            enabled = enabled,
            meetingUrl = null,
            policy = AlarmPolicy(preAlerts = listOf(DefaultPreAlertOffsets.TEN_MINUTES)),
            snoozeSpec = SnoozeSpec(durationsMinutes = listOf(5, 10, 15), defaultMinutes = 10),
            createdAtUtcMillis = 100L,
            updatedAtUtcMillis = 100L
        )
    }

    private class FakeClock(private val now: Long) : Clock {
        override fun nowUtcMillis(): Long = now
    }

    private class FakeAlarmRepository(
        private val alarms: List<Alarm>
    ) : AlarmRepository {
        override suspend fun upsert(alarm: Alarm) = Unit

        override suspend fun getEnabledAlarms(): List<Alarm> {
            return alarms.filter { it.enabled }.sortedBy { it.id }
        }

        override suspend fun getUpcomingAlarms(fromUtcMillis: Long, limit: Int): List<Alarm> {
            return alarms.filter { it.enabled && it.triggerAtUtcMillis >= fromUtcMillis }.take(limit)
        }

        override suspend fun getById(alarmId: Long): Alarm? {
            return alarms.firstOrNull { it.id == alarmId }
        }

        override suspend fun setEnabled(alarmId: Long, enabled: Boolean) = Unit
    }
}
