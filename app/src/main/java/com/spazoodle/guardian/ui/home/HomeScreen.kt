package com.spazoodle.guardian.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spazoodle.guardian.domain.model.Alarm
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onCreateAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onOpenReliability: () -> Unit,
    onOpenHistory: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.homeUiState.collectAsState()
    val topRowScroll = rememberScrollState()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var knownAlarmIds by remember { mutableStateOf(emptySet<Long>()) }
    var initializedIds by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.alarms) {
        val currentIds = uiState.alarms.map { it.id }.toSet()
        if (!initializedIds) {
            knownAlarmIds = currentIds
            initializedIds = true
            return@LaunchedEffect
        }

        val newId = uiState.alarms.firstOrNull { it.id !in knownAlarmIds }?.id
        if (newId != null) {
            val index = uiState.alarms.indexOfFirst { it.id == newId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
        knownAlarmIds = currentIds
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onCreateAlarm()
            }, modifier = Modifier.semantics {
                contentDescription = "Create Alarm"
            }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Guardian Alarms",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(topRowScroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onOpenReliability) {
                    Text("Reliability Dashboard")
                }
                OutlinedButton(onClick = onOpenHistory) {
                    Text("History & Proof")
                }
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Crossfade(
                targetState = uiState.alarms.isEmpty(),
                label = "alarm-list-state"
            ) { isEmpty ->
                if (isEmpty) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        tonalElevation = 2.dp,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("No alarms yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Tap + to create your first alarm.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.alarms, key = { it.id }) { alarm ->
                            AnimatedAlarmListItem(
                                key = alarm.id
                            ) {
                                AlarmRow(
                                    alarm = alarm,
                                    state = uiState.alarmState[alarm.id] ?: if (alarm.enabled) AlarmState.ACTIVE else AlarmState.COMPLETED,
                                    risk = uiState.planRisk[alarm.id],
                                    onToggle = { enabled -> homeViewModel.toggleEnabled(alarm, enabled) },
                                    onDelete = {
                                        homeViewModel.deleteAlarm(alarm) { deleted ->
                                            scope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Alarm deleted",
                                                    actionLabel = "Undo",
                                                    withDismissAction = true
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    homeViewModel.restoreDeletedAlarm(deleted)
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        onEditAlarm(alarm.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedAlarmListItem(
    key: Long,
    content: @Composable () -> Unit
) {
    var visible by remember(key) { mutableStateOf(false) }
    LaunchedEffect(key) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically()
    ) {
        content()
    }
}

@Composable
private fun AlarmRow(
    modifier: Modifier = Modifier,
    alarm: Alarm,
    state: AlarmState,
    risk: PlanRisk?,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateTime = Instant.ofEpochMilli(alarm.triggerAtUtcMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEE, d MMM • h:mm a"))
    val cardElevation by animateDpAsState(
        targetValue = if (alarm.enabled) 3.dp else 1.dp,
        label = "alarm-card-elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alarm.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.padding(2.dp))
                AssistChip(
                    onClick = onClick,
                    label = {
                        Text(
                            when (state) {
                                AlarmState.ACTIVE -> "Active"
                                AlarmState.MISSED -> "Missed"
                                AlarmState.COMPLETED -> "Completed"
                            }
                        )
                    }
                )
                val primaryAction = alarm.primaryAction
                if (primaryAction != null) {
                    Text(
                        text = "Action: ${primaryAction.label ?: primaryAction.type.name.lowercase(Locale.US).replace("_", " ")}",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                if (risk != null) {
                    val riskColor = when (risk.label) {
                        "LOW" -> MaterialTheme.colorScheme.primary
                        "MEDIUM" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    Text(
                        text = "Risk: ${risk.label}${if (risk.reasons.isNotEmpty()) " • ${risk.reasons.joinToString(", ")}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = riskColor
                    )
                }
            }
            Switch(
                checked = alarm.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics {
                    contentDescription = "Toggle alarm ${alarm.title}"
                    stateDescription = if (alarm.enabled) "On" else "Off"
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onDelete) {
                Text("Delete")
            }
        }
    }
}
