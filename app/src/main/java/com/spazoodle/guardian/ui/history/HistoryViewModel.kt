package com.spazoodle.guardian.ui.history

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spazoodle.guardian.runtime.GuardianRuntime
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching {
                GuardianRuntime.alarmHistoryRepository(appContext).getRecent(200)
            }.onSuccess { events ->
                val alarmRepo = GuardianRuntime.alarmRepository(appContext)
                val descriptions = events
                    .map { it.alarmId }
                    .distinct()
                    .associateWith { alarmId ->
                        runCatching { alarmRepo.getById(alarmId)?.description.orEmpty() }
                            .getOrDefault("")
                    }
                _uiState.value = HistoryUiState(
                    events = events,
                    alarmDescriptions = descriptions,
                    isLoading = false
                )
            }.onFailure { error ->
                _uiState.value = HistoryUiState(
                    events = emptyList(),
                    alarmDescriptions = emptyMap(),
                    isLoading = false,
                    message = error.message ?: "Failed to load history"
                )
            }
        }
    }

    fun copyDiagnostics() {
        val text = buildDiagnosticsPayload()
        val clipboard = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("guardian-diagnostics", text))
        _uiState.update { it.copy(message = "Diagnostics copied to clipboard") }
    }

    fun shareDiagnostics() {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Guardian Diagnostics")
            putExtra(Intent.EXTRA_TEXT, buildDiagnosticsPayload())
        }
        val chooser = Intent.createChooser(sendIntent, "Share diagnostics").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(chooser)
    }

    private fun buildDiagnosticsPayload(): String {
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        val lines = mutableListOf<String>()
        lines += "Guardian diagnostics"
        lines += "generated_at=${formatter.format(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zone))}"

        if (uiState.value.events.isEmpty()) {
            lines += "events=none"
            return lines.joinToString("\n")
        }

        uiState.value.events.forEach { event ->
            val at = formatter.format(Instant.ofEpochMilli(event.eventAtUtcMillis).atZone(zone))
            lines += "alarmId=${event.alarmId} at=$at trigger=${event.triggerKind} outcome=${event.outcome} " +
                "state=${event.deliveryState} delayMs=${event.delayMs ?: -1} deduped=${event.wasDeduped} " +
                "detail=${event.detail.orEmpty()}"
        }

        return lines.joinToString("\n")
    }
}
