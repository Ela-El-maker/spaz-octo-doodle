package com.spazoodle.guardian.domain.model

data class EscalationSpec(
    val enabled: Boolean,
    val stepUpAfterNagCount: Int,
    val toneSequence: List<String>
)
