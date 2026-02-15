package com.spazoodle.guardian.platform.scheduler

import android.content.Context

class ScheduledTriggerRegistry(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun resolveOrAllocateCode(alarmId: Long, triggerIdentity: String): Int {
        val existing = findCode(alarmId, triggerIdentity)
        if (existing != null) return existing

        val next = prefs.getInt(KEY_NEXT_REQUEST_CODE, INITIAL_REQUEST_CODE) + 1
        prefs.edit().putInt(KEY_NEXT_REQUEST_CODE, next).apply()
        putCode(alarmId, triggerIdentity, next)
        return next
    }

    fun findCode(alarmId: Long, triggerIdentity: String): Int? {
        return readMap(alarmId)[triggerIdentity]
    }

    fun putCode(alarmId: Long, triggerIdentity: String, requestCode: Int) {
        val updated = readMap(alarmId).toMutableMap()
        updated[triggerIdentity] = requestCode
        writeMap(alarmId, updated)
    }

    fun readCodes(alarmId: Long): Set<Int> {
        val legacy = prefs.getStringSet(legacyKey(alarmId), emptySet()) ?: emptySet()
        val legacyCodes = legacy.mapNotNull { it.toIntOrNull() }
        return readMap(alarmId).values.toSet() + legacyCodes
    }

    fun writeCodes(alarmId: Long, codes: Set<Int>) {
        // Keep legacy write path for backward compatibility with older scheduled entries.
        prefs.edit()
            .putStringSet(legacyKey(alarmId), codes.map { it.toString() }.toSet())
            .apply()
    }

    fun clear(alarmId: Long) {
        prefs.edit()
            .remove(legacyKey(alarmId))
            .remove(mapKey(alarmId))
            .apply()
    }

    private fun readMap(alarmId: Long): Map<String, Int> {
        val raw = prefs.getString(mapKey(alarmId), "").orEmpty()
        if (raw.isBlank()) return emptyMap()
        return raw.split("|")
            .mapNotNull { entry ->
                val idx = entry.indexOf('=')
                if (idx <= 0 || idx >= entry.lastIndex) return@mapNotNull null
                val key = entry.substring(0, idx)
                val value = entry.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
                key to value
            }
            .toMap()
    }

    private fun writeMap(alarmId: Long, values: Map<String, Int>) {
        val encoded = values.entries.joinToString("|") { "${it.key}=${it.value}" }
        prefs.edit().putString(mapKey(alarmId), encoded).apply()
    }

    private fun legacyKey(alarmId: Long): String = "alarm_codes_$alarmId"
    private fun mapKey(alarmId: Long): String = "alarm_trigger_map_$alarmId"

    companion object {
        private const val PREFS_NAME = "guardian_scheduler_registry"
        private const val KEY_NEXT_REQUEST_CODE = "next_request_code"
        private const val INITIAL_REQUEST_CODE = 10_000
    }
}
