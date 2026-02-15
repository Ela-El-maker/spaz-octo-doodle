package com.spazoodle.guardian.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.spazoodle.guardian.domain.model.AlarmEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    historyViewModel: HistoryViewModel = viewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()
    val actionsRowScroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("History & Proof") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.message != null) {
                Text(uiState.message ?: "", color = MaterialTheme.colorScheme.primary)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(actionsRowScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { historyViewModel.refresh() }) { Text("Refresh") }
                Button(onClick = { historyViewModel.copyDiagnostics() }) { Text("Copy Diagnostics") }
                Button(onClick = { historyViewModel.shareDiagnostics() }) { Text("Share") }
                Button(onClick = onBack) { Text("Back") }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.events, key = { "${it.alarmId}_${it.eventAtUtcMillis}_${it.outcome}" }) { event ->
                    HistoryRow(event, uiState.alarmDescriptions[event.alarmId].orEmpty())
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(event: AlarmEvent, description: String) {
    val dateTime = Instant.ofEpochMilli(event.eventAtUtcMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "alarmId=${event.alarmId} ${event.outcome}",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "trigger=${event.triggerKind} at=$dateTime state=${event.deliveryState}",
            style = MaterialTheme.typography.bodySmall
        )
        if (event.delayMs != null) {
            Text(text = "delayMs=${event.delayMs}", style = MaterialTheme.typography.bodySmall)
        }
        if (event.wasDeduped) {
            Text(text = "deduped=true", style = MaterialTheme.typography.bodySmall)
        }
        if (!event.detail.isNullOrBlank()) {
            Text(text = event.detail.orEmpty(), style = MaterialTheme.typography.bodySmall)
        }
        if (description.isNotBlank()) {
            val preview = if (description.length > 120) {
                description.take(120) + "..."
            } else {
                description
            }
            Text(text = "description=$preview", style = MaterialTheme.typography.bodySmall)
        }
    }
}
