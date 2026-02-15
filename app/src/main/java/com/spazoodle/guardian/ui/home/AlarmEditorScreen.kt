package com.spazoodle.guardian.ui.home

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorScreen(
    alarmId: Long?,
    onBack: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    LaunchedEffect(alarmId) {
        if (alarmId == null) {
            homeViewModel.loadCreateDraft()
        } else {
            homeViewModel.loadEditDraft(alarmId)
        }
    }

    val editorState by homeViewModel.editorUiState.collectAsState()
    val draft = editorState.draft
    val scrollState = rememberScrollState()
    val templateRowScroll = rememberScrollState()
    val actionRowScroll = rememberScrollState()

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            @Suppress("DEPRECATION")
            val pickedUri = result.data?.getParcelableExtra<android.net.Uri>(
                RingtoneManager.EXTRA_RINGTONE_PICKED_URI
            )
            homeViewModel.setRingtoneSelection(pickedUri?.toString())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            homeViewModel.stopRingtonePreview()
        }
    }

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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionCard(title = "Details") {
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
                    value = draft.description,
                    onValueChange = { value ->
                        if (value.length <= 500) {
                            homeViewModel.updateDraft { current -> current.copy(description = value) }
                        }
                    },
                    label = { Text("Description (optional)") },
                    supportingText = { Text("${draft.description.length}/500") },
                    minLines = 2,
                    maxLines = 4
                )
            }

            SectionCard(title = "Schedule") {
                DateField(
                    dateText = draft.dateText,
                    onDateChange = { value ->
                        homeViewModel.updateDraft { current -> current.copy(dateText = value) }
                    }
                )
                TimeField(
                    timeText = draft.timeText,
                    onTimeChange = { value ->
                        homeViewModel.updateDraft { current -> current.copy(timeText = value) }
                    }
                )
                Text(
                    text = formatPreviewDateTime(draft.dateText, draft.timeText),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard(title = "Ringtone & Vibration") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                if (draft.ringtoneUri.isNotBlank()) {
                                    putExtra(
                                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                        android.net.Uri.parse(draft.ringtoneUri)
                                    )
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                    ) {
                        Text("Choose ringtone")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { homeViewModel.toggleRingtonePreview() }
                    ) {
                        Text("Preview")
                    }
                }
                Text(
                    draft.ringtoneLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LabeledToggle(
                    text = "Vibrate",
                    checked = draft.vibrateEnabled,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(vibrateEnabled = checked) } }
                )
            }

            SectionCard(title = "Template") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(templateRowScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AlarmTemplatePreset.entries.forEach { preset ->
                        AssistChip(onClick = { homeViewModel.applyTemplate(preset) }, label = {
                            Text(preset.label)
                        })
                    }
                }
            }

            SectionCard(title = "Primary Action") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(actionRowScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "OPEN_URL",
                        "OPEN_DEEPLINK",
                        "CALL_NUMBER",
                        "OPEN_MAP_NAVIGATION"
                    ).forEach { actionType ->
                        AssistChip(onClick = { homeViewModel.setPrimaryActionType(actionType) }, label = {
                            Text(actionType.removePrefix("OPEN_").lowercase().replace("_", " "))
                        })
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
            }

            SectionCard(title = "Delivery") {
                LabeledToggle(
                    text = "Enabled",
                    checked = draft.enabled,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(enabled = checked) } }
                )

                Text("Pre-alerts", style = MaterialTheme.typography.titleSmall)
                LabeledToggle(
                    text = "1 day before",
                    checked = draft.preAlert1Day,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert1Day = checked) } }
                )
                LabeledToggle(
                    text = "1 hour before",
                    checked = draft.preAlert1Hour,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert1Hour = checked) } }
                )
                LabeledToggle(
                    text = "10 minutes before",
                    checked = draft.preAlert10Min,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert10Min = checked) } }
                )
                LabeledToggle(
                    text = "2 minutes before",
                    checked = draft.preAlert2Min,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(preAlert2Min = checked) } }
                )

                LabeledToggle(
                    text = "Nag mode",
                    checked = draft.nagEnabled,
                    onChange = { checked -> homeViewModel.updateDraft { it.copy(nagEnabled = checked) } }
                )

                AnimatedVisibility(visible = draft.nagEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        LabeledToggle(
                            text = "Escalation",
                            checked = draft.escalationEnabled,
                            onChange = { checked ->
                                homeViewModel.updateDraft { current -> current.copy(escalationEnabled = checked) }
                            }
                        )
                        AnimatedVisibility(visible = draft.escalationEnabled) {
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
                }
            }

            if (editorState.errorMessage != null) {
                HorizontalDivider()
                Text(
                    text = editorState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBack) {
                    Text("Cancel")
                }
                Button(
                    onClick = { homeViewModel.saveDraft(alarmId) { onBack() } },
                    modifier = Modifier.semantics { contentDescription = "Save alarm" }
                ) {
                    Text("Save")
                }
            }

            Text(
                text = "Time shown in your local timezone (${ZoneId.systemDefault().id})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    dateText: String,
    onDateChange: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    val initialDate = runCatching { LocalDate.parse(dateText) }.getOrNull() ?: LocalDate.now()
    val initialMillis = initialDate
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    OutlinedButton(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Date: ${formatDisplayDate(dateText)}")
    }

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis ?: initialMillis
                        val newDate = Instant.ofEpochMilli(picked)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateChange(newDate.toString())
                        openDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    timeText: String,
    onTimeChange: (String) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }
    val parsed = runCatching { LocalTime.parse(timeText) }.getOrNull() ?: LocalTime.of(9, 0)
    val timePickerState = rememberTimePickerState(
        initialHour = parsed.hour,
        initialMinute = parsed.minute,
        is24Hour = false
    )

    OutlinedButton(onClick = { openDialog = true }, modifier = Modifier.fillMaxWidth()) {
        Text("Time: ${formatDisplayTime(timeText)}")
    }

    if (openDialog) {
        AlertDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChange("%02d:%02d".format(timePickerState.hour, timePickerState.minute))
                        openDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { openDialog = false }) { Text("Cancel") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun LabeledToggle(
    text: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun formatDisplayDate(raw: String): String {
    return runCatching {
        LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))
    }.getOrDefault(raw.ifBlank { "Select date" })
}

private fun formatDisplayTime(raw: String): String {
    return runCatching {
        LocalTime.parse(raw).format(DateTimeFormatter.ofPattern("h:mm a"))
    }.getOrDefault(raw.ifBlank { "Select time" })
}

private fun formatPreviewDateTime(dateRaw: String, timeRaw: String): String {
    return runCatching {
        val localDate = LocalDate.parse(dateRaw)
        val localTime = LocalTime.parse(timeRaw)
        val zoned = localDate.atTime(localTime).atZone(ZoneId.systemDefault())
        zoned.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy • h:mm a"))
    }.getOrDefault("Select date and time")
}
