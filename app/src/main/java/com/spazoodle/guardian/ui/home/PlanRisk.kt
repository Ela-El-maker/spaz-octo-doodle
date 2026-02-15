package com.spazoodle.guardian.ui.home

import com.spazoodle.guardian.platform.reliability.RiskReason

data class PlanRisk(
    val label: String,
    val reasons: List<RiskReason>
)
