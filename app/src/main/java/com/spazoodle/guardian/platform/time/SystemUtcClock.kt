package com.spazoodle.guardian.platform.time

import com.spazoodle.guardian.domain.clock.Clock

object SystemUtcClock : Clock {
    override fun nowUtcMillis(): Long = System.currentTimeMillis()
}
