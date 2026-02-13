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
