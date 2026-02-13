package com.spazoodle.guardian.ui.reliability

import com.spazoodle.guardian.platform.reliability.ReliabilityStatus

data class ReliabilityUiState(
    val status: ReliabilityStatus? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)
