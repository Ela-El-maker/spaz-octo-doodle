package com.spazoodle.guardian.ui.reliability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityScreen(
    onBack: () -> Unit,
    reliabilityViewModel: ReliabilityViewModel = viewModel()
) {
    val uiState by reliabilityViewModel.uiState.collectAsState()
    val status = uiState.status
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reliability Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Health Score: ${status?.healthScore ?: 0}%", style = MaterialTheme.typography.headlineSmall)
            if (status != null) {
                Text("Risk: ${status.riskLevel}", style = MaterialTheme.typography.titleMedium)
            }

            if (uiState.message != null) {
                Text(uiState.message ?: "", color = MaterialTheme.colorScheme.primary)
            }
            if (uiState.latestMissedSummary != null) {
                Text(
                    uiState.latestMissedSummary ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (status?.strictModeBlocked == true) {
                Text(
                    "Guardian strict mode blocked: exact alarms are required for reliable delivery.",
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (status?.restoredAfterStopLikely == true) {
                val restoredAt = status.restoredAtUtcMillis?.let {
                    Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEE, d MMM • h:mm a"))
                } ?: "recent startup"
                Text(
                    "Schedules were restored on $restoredAt " +
                        "(${status.restoredMissingTriggerCount} missing trigger(s) rehydrated across " +
                        "${status.restoredTotalPlanCount} plan(s)).",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                "App stop behavior",
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "• Swiping Guardian away from Recents should still allow alarms to ring.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "• If Guardian is Force Stopped in system settings, Android blocks alarms until you open Guardian again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                "• OEM background restrictions can delay/suppress alarms. Use OEM playbook steps below and run Test in 15s after changes.",
                style = MaterialTheme.typography.bodySmall
            )

            StatusRow(
                title = "Notifications",
                ok = status?.notificationsEnabled == true,
                onFix = { reliabilityViewModel.openNotificationSettings() }
            )
            StatusRow(
                title = "Exact alarms",
                ok = status?.exactAlarmAllowed == true,
                onFix = { reliabilityViewModel.openExactAlarmSettings() }
            )
            StatusRow(
                title = "Battery optimization",
                ok = status?.batteryOptimizationIgnored == true,
                onFix = { reliabilityViewModel.openBatteryOptimizationSettings() }
            )
            StatusRow(
                title = "Full-screen readiness",
                ok = status?.fullScreenReady == true,
                onFix = { reliabilityViewModel.openNotificationSettings() }
            )
            Text(
                "Speaker forcing is best effort on this device. If audio routes externally, unlock and check output route.",
                style = MaterialTheme.typography.bodySmall
            )
            StatusRow(
                title = "DND alarms likely allowed",
                ok = status?.dndAlarmsLikelyAllowed == true,
                onFix = { reliabilityViewModel.openDndSettings() }
            )

            if (!status?.manufacturer.isNullOrBlank()) {
                Text("Manufacturer: ${status?.manufacturer}", style = MaterialTheme.typography.titleSmall)
            }
            if (!status?.oemSteps.isNullOrEmpty()) {
                Text("OEM playbook", style = MaterialTheme.typography.titleSmall)
                status?.oemSteps?.forEach { step ->
                    Text("• ${step.title}: ${step.detail}", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = { reliabilityViewModel.openOemSettings() }) {
                    Text("Open OEM Settings")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { reliabilityViewModel.refresh() }) {
                    Text("Refresh")
                }
                Button(onClick = { reliabilityViewModel.testAlarmIn15Seconds() }) {
                    Text("Test in 15s")
                }
                Button(onClick = onBack) {
                    Text("Back")
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    title: String,
    ok: Boolean,
    onFix: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$title: ${if (ok) "OK" else "Needs Attention"}")
        if (!ok) {
            Button(onClick = onFix) {
                Text("Fix")
            }
        }
    }
}
