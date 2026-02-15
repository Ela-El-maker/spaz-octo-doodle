package com.spazoodle.guardian.ui.home

import com.spazoodle.guardian.domain.model.Alarm

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val planRisk: Map<Long, PlanRisk> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
