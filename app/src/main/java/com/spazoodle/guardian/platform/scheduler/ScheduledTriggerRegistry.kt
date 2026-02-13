package com.spazoodle.guardian.platform.scheduler

import android.content.Context

class ScheduledTriggerRegistry(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun readCodes(alarmId: Long): Set<Int> {
        val values = prefs.getStringSet(key(alarmId), emptySet()) ?: emptySet()
        return values.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun writeCodes(alarmId: Long, codes: Set<Int>) {
        prefs.edit()
            .putStringSet(key(alarmId), codes.map { it.toString() }.toSet())
            .apply()
    }

    fun clear(alarmId: Long) {
        prefs.edit().remove(key(alarmId)).apply()
    }

    private fun key(alarmId: Long): String = "alarm_codes_$alarmId"

    companion object {
        private const val PREFS_NAME = "guardian_scheduler_registry"
    }
}
