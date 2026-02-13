package com.spazoodle.guardian.domain.model

data class PrimaryAction(
    val type: PrimaryActionType,
    val value: String,
    val label: String? = null
)
