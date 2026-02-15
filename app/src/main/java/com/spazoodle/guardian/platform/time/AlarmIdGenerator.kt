package com.spazoodle.guardian.platform.time

import android.content.Context
import java.util.concurrent.atomic.AtomicLong

class AlarmIdGenerator(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val counter = AtomicLong(
        prefs.getLong(KEY_COUNTER, System.currentTimeMillis().coerceAtLeast(1L))
    )

    fun nextId(): Long {
        val next = counter.incrementAndGet()
        prefs.edit().putLong(KEY_COUNTER, next).apply()
        return next
    }

    companion object {
        private const val PREFS_NAME = "guardian_alarm_id_generator"
        private const val KEY_COUNTER = "counter"
    }
}

