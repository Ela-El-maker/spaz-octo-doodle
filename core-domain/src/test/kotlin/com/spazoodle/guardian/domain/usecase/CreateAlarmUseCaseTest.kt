package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.repository.AlarmRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateAlarmUseCaseTest {

    @Test
    fun storesAlarmWhenTriggerIsInFuture() = runTest {
        val repository = FakeAlarmRepository()
        val useCase = CreateAlarmUseCase(repository, FakeClock(1_000L))

        useCase(testAlarm(triggerAt = 2_000L))

        assertEquals(1, repository.saved.size)
    }

    @Test
    fun throwsWhenTriggerIsInPast() = runTest {
        val repository = FakeAlarmRepository()
        val useCase = CreateAlarmUseCase(repository, FakeClock(2_000L))

        var threw = false
        try {
            useCase(testAlarm(triggerAt = 1_000L))
        } catch (_: IllegalArgumentException) {
            threw = true
        }
        assertTrue(threw)
    }

    private fun testAlarm(triggerAt: Long): Alarm {
        return Alarm(
            id = 1L,
            title = "Future alarm",
            type = AlarmType.ALARM,
            triggerAtUtcMillis = triggerAt,
            timezoneIdAtCreation = "Africa/Nairobi",
            enabled = true,
            meetingUrl = null,
            policy = AlarmPolicy(),
            snoozeSpec = SnoozeSpec(durationsMinutes = listOf(5, 10, 15), defaultMinutes = 10),
            createdAtUtcMillis = 0L,
            updatedAtUtcMillis = 0L
        )
    }

    private class FakeClock(
        private val now: Long
    ) : Clock {
        override fun nowUtcMillis(): Long = now
    }

    private class FakeAlarmRepository : AlarmRepository {
        val saved = mutableListOf<Alarm>()

        override suspend fun upsert(alarm: Alarm) {
            saved += alarm
        }

        override suspend fun getEnabledAlarms(): List<Alarm> {
            return saved.filter { it.enabled }
        }

        override suspend fun getUpcomingAlarms(fromUtcMillis: Long, limit: Int): List<Alarm> {
            return saved
                .filter { it.enabled && it.triggerAtUtcMillis >= fromUtcMillis }
                .sortedBy { it.triggerAtUtcMillis }
                .take(limit)
        }

        override suspend fun getById(alarmId: Long): Alarm? {
            return saved.firstOrNull { it.id == alarmId }
        }

        override suspend fun setEnabled(alarmId: Long, enabled: Boolean) {
            val index = saved.indexOfFirst { it.id == alarmId }
            if (index >= 0) {
                val alarm = saved[index]
                saved[index] = alarm.copy(enabled = enabled)
            }
        }
    }
}
