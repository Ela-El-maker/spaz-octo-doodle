package com.spazoodle.guardian.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AlarmEditorScreen(
    alarmId: Long?,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val editorState by homeViewModel.editorUiState.collectAsState()
    val draft = editorState.draft

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (alarmId == null) "Create Alarm" else "Edit Alarm")
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.title,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(title = value) }
                },
                label = { Text("Title") }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.dateText,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(dateText = value) }
                },
                label = { Text("Date (yyyy-MM-dd)") }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.timeText,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(timeText = value) }
                },
                label = { Text("Time (HH:mm)") }
            )

            Text("Template", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AlarmTemplatePreset.entries.forEach { preset ->
                    OutlinedButton(onClick = { homeViewModel.applyTemplate(preset) }) {
                        Text(preset.label)
                    }
                }
            }

            Text("Primary Action", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "OPEN_URL",
                    "OPEN_DEEPLINK",
                    "CALL_NUMBER",
                    "OPEN_MAP_NAVIGATION"
                ).forEach { actionType ->
                    OutlinedButton(onClick = { homeViewModel.setPrimaryActionType(actionType) }) {
                        Text(actionType.removePrefix("OPEN_").lowercase().replace("_", " "))
                    }
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.primaryActionType,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(primaryActionType = value) }
                },
                label = { Text("Action Type (optional)") }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.primaryActionValue,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(primaryActionValue = value) }
                },
                label = { Text("Action Value (optional)") }
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.primaryActionLabel,
                onValueChange = { value ->
                    homeViewModel.updateDraft { current -> current.copy(primaryActionLabel = value) }
                },
                label = { Text("Action Label (optional)") }
            )

            LabeledCheck(
                text = "Enabled",
                checked = draft.enabled,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(enabled = checked) } }
            )

            Text("Pre-alerts", style = MaterialTheme.typography.titleMedium)
            LabeledCheck(
                text = "1 day before",
                checked = draft.preAlert1Day,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert1Day = checked) } }
            )
            LabeledCheck(
                text = "1 hour before",
                checked = draft.preAlert1Hour,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert1Hour = checked) } }
            )
            LabeledCheck(
                text = "10 minutes before",
                checked = draft.preAlert10Min,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert10Min = checked) } }
            )
            LabeledCheck(
                text = "2 minutes before",
                checked = draft.preAlert2Min,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert2Min = checked) } }
            )
            LabeledCheck(
                text = "Nag mode",
                checked = draft.nagEnabled,
                onChange = { checked -> homeViewModel.updateDraft { it.copy(nagEnabled = checked) } }
            )
            if (draft.nagEnabled) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.nagRepeatMinutesCsv,
                    onValueChange = { value ->
                        homeViewModel.updateDraft { current -> current.copy(nagRepeatMinutesCsv = value) }
                    },
                    label = { Text("Nag repeat minutes (csv)") }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.nagMaxCount,
                    onValueChange = { value ->
                        homeViewModel.updateDraft { current -> current.copy(nagMaxCount = value) }
                    },
                    label = { Text("Nag max count") }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = draft.nagMaxWindowMinutes,
                    onValueChange = { value ->
                        homeViewModel.updateDraft { current -> current.copy(nagMaxWindowMinutes = value) }
                    },
                    label = { Text("Nag max window (minutes)") }
                )
                LabeledCheck(
                    text = "Escalation",
                    checked = draft.escalationEnabled,
                    onChange = { checked ->
                        homeViewModel.updateDraft { current -> current.copy(escalationEnabled = checked) }
                    }
                )
                if (draft.escalationEnabled) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft.escalationStepAfterCount,
                        onValueChange = { value ->
                            homeViewModel.updateDraft { current ->
                                current.copy(escalationStepAfterCount = value)
                            }
                        },
                        label = { Text("Escalate after nag count") }
                    )
                }
            }

            if (editorState.errorMessage != null) {
                Text(
                    text = editorState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBack) {
                    Text("Cancel")
                }
                Button(onClick = {
                    homeViewModel.saveDraft(alarmId) { onBack() }
                }) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun LabeledCheck(
    text: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text)
        Checkbox(checked = checked, onCheckedChange = onChange)
    }
}
