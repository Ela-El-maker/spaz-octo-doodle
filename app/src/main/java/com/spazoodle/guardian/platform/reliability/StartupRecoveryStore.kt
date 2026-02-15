package com.spazoodle.guardian.platform.reliability

import android.content.Context

class StartupRecoveryStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun markRecovered(missingTriggerCount: Int, totalPlanCount: Int, atUtcMillis: Long) {
        prefs.edit()
            .putBoolean(KEY_RECOVERED, true)
            .putInt(KEY_MISSING_TRIGGER_COUNT, missingTriggerCount)
            .putInt(KEY_TOTAL_PLAN_COUNT, totalPlanCount)
            .putLong(KEY_RECOVERED_AT_UTC_MILLIS, atUtcMillis)
            .apply()
    }

    fun clear() {
        prefs.edit()
            .remove(KEY_RECOVERED)
            .remove(KEY_MISSING_TRIGGER_COUNT)
            .remove(KEY_TOTAL_PLAN_COUNT)
            .remove(KEY_RECOVERED_AT_UTC_MILLIS)
            .apply()
    }

    fun snapshot(): StartupRecoverySnapshot {
        return StartupRecoverySnapshot(
            recoveredAfterStopLikely = prefs.getBoolean(KEY_RECOVERED, false),
            missingTriggerCount = prefs.getInt(KEY_MISSING_TRIGGER_COUNT, 0),
            totalPlanCount = prefs.getInt(KEY_TOTAL_PLAN_COUNT, 0),
            recoveredAtUtcMillis = prefs.getLong(KEY_RECOVERED_AT_UTC_MILLIS, 0L)
                .takeIf { it > 0L }
        )
    }

    companion object {
        private const val PREFS_NAME = "guardian_startup_recovery"
        private const val KEY_RECOVERED = "recovered_after_stop_likely"
        private const val KEY_MISSING_TRIGGER_COUNT = "missing_trigger_count"
        private const val KEY_TOTAL_PLAN_COUNT = "total_plan_count"
        private const val KEY_RECOVERED_AT_UTC_MILLIS = "recovered_at_utc_millis"
    }
}

data class StartupRecoverySnapshot(
    val recoveredAfterStopLikely: Boolean,
    val missingTriggerCount: Int,
    val totalPlanCount: Int,
    val recoveredAtUtcMillis: Long?
)

