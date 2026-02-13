package com.spazoodle.guardian.ui.home

data class AlarmDraft(
    val title: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val primaryActionType: String = "",
    val primaryActionValue: String = "",
    val primaryActionLabel: String = "",
    val enabled: Boolean = true,
    val preAlert1Day: Boolean = true,
    val preAlert1Hour: Boolean = true,
    val preAlert10Min: Boolean = true,
    val preAlert2Min: Boolean = true,
    val nagEnabled: Boolean = false
)
