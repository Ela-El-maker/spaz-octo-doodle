package com.spazoodle.guardian.domain.model

data class AlarmPolicy(
    val preAlerts: List<PreAlertOffset> = emptyList(),
    val nagSpec: NagSpec? = null,
    val escalationSpec: EscalationSpec? = null
)
