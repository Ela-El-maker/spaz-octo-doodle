package com.spazoodle.guardian.domain.usecase

import com.spazoodle.guardian.domain.clock.Clock
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DefaultPreAlertOffsets
import com.spazoodle.guardian.domain.model.PreAlertOffset
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.model.TriggerKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComputeSchedulePlanUseCaseTest {

    @Test
    fun computesMainAndPreAlertsInChronologicalOrder() {
        val clock = FakeClock(1_000_000L)
        val useCase = ComputeSchedulePlanUseCase(clock)
        val alarm = testAlarm(
            triggerAt = 10_000_000L,
            preAlerts = listOf(
                DefaultPreAlertOffsets.ONE_HOUR,
                DefaultPreAlertOffsets.TEN_MINUTES
            )
        )

        val plan = useCase(alarm)

        assertEquals(3, plan.triggers.size)
        assertEquals(TriggerKind.PRE_ALERT, plan.triggers[0].kind)
        assertEquals(TriggerKind.PRE_ALERT, plan.triggers[1].kind)
        assertEquals(TriggerKind.MAIN, plan.triggers[2].kind)
        assertTrue(plan.triggers[0].scheduledAtUtcMillis < plan.triggers[1].scheduledAtUtcMillis)
        assertTrue(plan.triggers[1].scheduledAtUtcMillis < plan.triggers[2].scheduledAtUtcMillis)
    }

    @Test
    fun skipsPreAlertsThatAreAlreadyInPast() {
        val clock = FakeClock(9_700_000L)
        val useCase = ComputeSchedulePlanUseCase(clock)
        val alarm = testAlarm(
            triggerAt = 10_000_000L,
            preAlerts = listOf(
                PreAlertOffset("OLD", 500_000L),
                PreAlertOffset("SOON", 100_000L)
            )
        )

        val plan = useCase(alarm)

        assertEquals(2, plan.triggers.size)
        assertEquals("SOON", plan.triggers.first().key)
        assertEquals(TriggerKind.MAIN, plan.triggers.last().kind)
    }

    private fun testAlarm(triggerAt: Long, preAlerts: List<PreAlertOffset>): Alarm {
        return Alarm(
            id = 1L,
            title = "Virtual Meeting",
            type = AlarmType.MEETING,
            triggerAtUtcMillis = triggerAt,
            timezoneIdAtCreation = "Africa/Nairobi",
            enabled = true,
            meetingUrl = "https://meet.google.com/abc",
            policy = AlarmPolicy(preAlerts = preAlerts),
            snoozeSpec = SnoozeSpec(durationsMinutes = listOf(5, 10, 15), defaultMinutes = 10),
            createdAtUtcMillis = 900_000L,
            updatedAtUtcMillis = 900_000L
        )
    }

    private class FakeClock(
        private val now: Long
    ) : Clock {
        override fun nowUtcMillis(): Long = now
    }
}
