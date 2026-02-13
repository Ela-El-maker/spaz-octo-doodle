package com.spazoodle.guardian.domain.model

data class NagSpec(
    val enabled: Boolean,
    val repeatMinutes: List<Int>,
    val maxNagCount: Int,
    val maxNagWindowMinutes: Int
)
