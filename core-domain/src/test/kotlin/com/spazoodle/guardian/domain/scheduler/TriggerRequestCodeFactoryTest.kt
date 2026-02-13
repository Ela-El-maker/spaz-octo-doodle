package com.spazoodle.guardian.domain.scheduler

import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.model.TriggerKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TriggerRequestCodeFactoryTest {

    @Test
    fun requestCodeIsStableForSameTrigger() {
        val trigger = Trigger(
            alarmId = 42L,
            kind = TriggerKind.MAIN,
            scheduledAtUtcMillis = 1_000L,
            index = 0,
            key = "MAIN"
        )

        val first = TriggerRequestCodeFactory.create(trigger)
        val second = TriggerRequestCodeFactory.create(trigger)

        assertEquals(first, second)
    }

    @Test
    fun requestCodeChangesAcrossKinds() {
        val main = Trigger(
            alarmId = 42L,
            kind = TriggerKind.MAIN,
            scheduledAtUtcMillis = 1_000L,
            index = 0,
            key = "MAIN"
        )
        val pre = Trigger(
            alarmId = 42L,
            kind = TriggerKind.PRE_ALERT,
            scheduledAtUtcMillis = 500L,
            index = 0,
            key = "PT10M"
        )

        assertNotEquals(
            TriggerRequestCodeFactory.create(main),
            TriggerRequestCodeFactory.create(pre)
        )
    }
}
