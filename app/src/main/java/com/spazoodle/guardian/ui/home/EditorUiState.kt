package com.spazoodle.guardian.ui.home

data class EditorUiState(
    val draft: AlarmDraft = AlarmDraft(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
