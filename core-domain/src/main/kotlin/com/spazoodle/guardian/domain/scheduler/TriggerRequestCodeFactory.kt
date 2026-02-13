package com.spazoodle.guardian.domain.scheduler

import com.spazoodle.guardian.domain.model.Trigger

object TriggerRequestCodeFactory {
    fun create(trigger: Trigger): Int {
        val seed = buildString {
            append(trigger.alarmId)
            append(':')
            append(trigger.kind.name)
            append(':')
            append(trigger.index)
            append(':')
            append(trigger.key.orEmpty())
        }
        return seed.hashCode() and Int.MAX_VALUE
    }
}
