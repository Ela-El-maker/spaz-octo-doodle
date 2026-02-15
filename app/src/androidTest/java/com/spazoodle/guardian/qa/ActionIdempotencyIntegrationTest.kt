package com.spazoodle.guardian.qa

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.data.local.GuardianDatabaseFactory
import com.spazoodle.guardian.platform.reliability.TriggerDeduper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionIdempotencyIntegrationTest {

    @Test
    fun duplicateActionExecutionKey_isDroppedWithinDedupeWindow() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = GuardianDatabaseFactory.create(context)
        try {
            val deduper = TriggerDeduper(db.triggerExecutionDao(), dedupeWindowMs = 8_000L)
            val key = "action:123456:STOP:-1"
            val now = System.currentTimeMillis()

            val first = kotlinx.coroutines.runBlocking { deduper.shouldProcess(key, now) }
            val second = kotlinx.coroutines.runBlocking { deduper.shouldProcess(key, now + 300L) }
            val third = kotlinx.coroutines.runBlocking { deduper.shouldProcess(key, now + 8_500L) }

            assertTrue("First action should be processed", first)
            assertFalse("Immediate duplicate should be dropped", second)
            assertTrue("Action after dedupe window should process again", third)
        } finally {
            db.close()
        }
    }
}
