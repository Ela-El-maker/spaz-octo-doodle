package com.spazoodle.guardian.platform.reliability

object StopGuardEvaluator {
    fun shouldConfirmStop(
        pressedDurationMs: Long,
        config: HoldToStopConfig,
        featureEnabled: Boolean
    ): Boolean {
        if (!featureEnabled || !config.enabled || config.mode == StopGuardMode.ONE_TAP) {
            return true
        }
        return pressedDurationMs >= config.holdDurationMs
    }
}
