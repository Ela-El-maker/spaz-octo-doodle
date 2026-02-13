package com.spazoodle.guardian.domain.clock

interface Clock {
    fun nowUtcMillis(): Long
}
