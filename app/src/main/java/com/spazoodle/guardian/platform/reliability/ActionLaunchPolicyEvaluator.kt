package com.spazoodle.guardian.platform.reliability

object ActionLaunchPolicyEvaluator {
    fun isBlockedByLock(
        isDeviceLocked: Boolean,
        policy: ActionLaunchPolicy,
        featureEnabled: Boolean
    ): Boolean {
        if (!featureEnabled) return false
        return policy == ActionLaunchPolicy.REQUIRE_UNLOCK && isDeviceLocked
    }
}
