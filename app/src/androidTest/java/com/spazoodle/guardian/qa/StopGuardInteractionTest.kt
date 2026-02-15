package com.spazoodle.guardian.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.platform.reliability.HoldToStopConfig
import com.spazoodle.guardian.platform.reliability.StopGuardEvaluator
import com.spazoodle.guardian.platform.reliability.StopGuardMode
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StopGuardInteractionTest {

    @Test
    fun shortPress_doesNotConfirmStop_whenGuardEnabled() {
        val config = HoldToStopConfig(
            enabled = true,
            holdDurationMs = 1_800L,
            mode = StopGuardMode.HOLD_TO_CONFIRM
        )
        assertFalse(
            StopGuardEvaluator.shouldConfirmStop(
                pressedDurationMs = 300L,
                config = config,
                featureEnabled = true
            )
        )
    }

    @Test
    fun longPress_confirmsStop_whenGuardEnabled() {
        val config = HoldToStopConfig(
            enabled = true,
            holdDurationMs = 1_800L,
            mode = StopGuardMode.HOLD_TO_CONFIRM
        )
        assertTrue(
            StopGuardEvaluator.shouldConfirmStop(
                pressedDurationMs = 2_000L,
                config = config,
                featureEnabled = true
            )
        )
    }
}
