package com.spazoodle.guardian.ui.home

data class AlarmDraft(
    val title: String = "",
    val description: String = "",
    val dateText: String = "",
    val timeText: String = "",
    val templateId: String = AlarmTemplatePreset.STANDARD.id,
    val vibrateEnabled: Boolean = true,
    val ringtoneUri: String = "",
    val ringtoneLabel: String = "Default alarm tone",
    val primaryActionType: String = "",
    val primaryActionValue: String = "",
    val primaryActionLabel: String = "",
    val enabled: Boolean = true,
    val preAlert1Day: Boolean = true,
    val preAlert1Hour: Boolean = true,
    val preAlert10Min: Boolean = true,
    val preAlert2Min: Boolean = true,
    val nagEnabled: Boolean = false,
    val nagRepeatMinutesCsv: String = "2,3,5,10",
    val nagMaxCount: String = "20",
    val nagMaxWindowMinutes: String = "120",
    val escalationEnabled: Boolean = false,
    val escalationStepAfterCount: String = "3"
)

enum class AlarmTemplatePreset(
    val id: String,
    val label: String
) {
    STANDARD("standard", "Standard"),
    CRITICAL("critical", "Critical"),
    TRAVEL("travel", "Travel"),
    QUIET("quiet", "Quiet")
}
