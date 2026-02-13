package com.spazoodle.guardian.domain

import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.repository.AlarmRepository
import com.spazoodle.guardian.domain.usecase.CreateAlarmUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateAlarmUseCaseTest {

    @Test
    fun createAlarmStoresEntity() = runTest {
        val repository = FakeAlarmRepository()
        val useCase = CreateAlarmUseCase(repository)
        val alarm = Alarm(
            id = 1L,
            title = "Virtual Meeting",
            triggerAtUtcMillis = 1_700_000_000_000L,
            timezoneIdAtCreation = "Africa/Nairobi",
            enabled = true,
            meetingUrl = "https://meet.google.com/example"
        )

        useCase(alarm)

        assertEquals(1, repository.saved.size)
        assertEquals("Virtual Meeting", repository.saved.first().title)
    }

    private class FakeAlarmRepository : AlarmRepository {
        val saved = mutableListOf<Alarm>()

        override suspend fun upsert(alarm: Alarm) {
            saved += alarm
        }

        override suspend fun getEnabledAlarms(): List<Alarm> {
            return saved.filter { it.enabled }
        }
    }
}
