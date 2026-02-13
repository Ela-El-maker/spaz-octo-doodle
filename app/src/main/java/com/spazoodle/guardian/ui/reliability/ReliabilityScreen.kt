package com.spazoodle.guardian.ui.reliability

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@Composable
fun ReliabilityScreen(
    onBack: () -> Unit,
    reliabilityViewModel: ReliabilityViewModel = viewModel()
) {
    val uiState by reliabilityViewModel.uiState.collectAsState()
    val status = uiState.status

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reliability Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Health Score: ${status?.healthScore ?: 0}%", style = MaterialTheme.typography.headlineSmall)

            if (uiState.message != null) {
                Text(uiState.message ?: "", color = MaterialTheme.colorScheme.primary)
            }

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
            StatusRow(
                title = "DND alarms likely allowed",
                ok = status?.dndAlarmsLikelyAllowed == true,
                onFix = { reliabilityViewModel.openDndSettings() }
            )

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
