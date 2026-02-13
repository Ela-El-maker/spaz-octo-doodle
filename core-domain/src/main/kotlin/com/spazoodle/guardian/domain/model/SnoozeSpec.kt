package com.spazoodle.guardian.domain.model

data class SnoozeSpec(
    val durationsMinutes: List<Int>,
    val defaultMinutes: Int
)
