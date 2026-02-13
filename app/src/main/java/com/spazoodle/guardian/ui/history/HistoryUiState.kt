package com.spazoodle.guardian.ui.history

import com.spazoodle.guardian.domain.model.AlarmEvent

data class HistoryUiState(
    val events: List<AlarmEvent> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)
