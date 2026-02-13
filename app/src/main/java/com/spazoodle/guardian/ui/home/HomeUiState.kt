package com.spazoodle.guardian.ui.home

import com.spazoodle.guardian.domain.model.Alarm

data class HomeUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
