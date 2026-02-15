package com.spazoodle.guardian.qa

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.platform.reliability.ActionLaunchPolicy
import com.spazoodle.guardian.platform.reliability.ActionLaunchPolicyEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionLaunchPolicyEvaluatorTest {

    @Test
    fun lockedDevice_blocksWhenPolicyRequiresUnlock() {
        assertTrue(
            ActionLaunchPolicyEvaluator.isBlockedByLock(
                isDeviceLocked = true,
                policy = ActionLaunchPolicy.REQUIRE_UNLOCK,
                featureEnabled = true
            )
        )
    }

    @Test
    fun unlockedDevice_orFeatureDisabled_doesNotBlock() {
        assertFalse(
            ActionLaunchPolicyEvaluator.isBlockedByLock(
                isDeviceLocked = false,
                policy = ActionLaunchPolicy.REQUIRE_UNLOCK,
                featureEnabled = true
            )
        )
        assertFalse(
            ActionLaunchPolicyEvaluator.isBlockedByLock(
                isDeviceLocked = true,
                policy = ActionLaunchPolicy.REQUIRE_UNLOCK,
                featureEnabled = false
            )
        )
    }
}
