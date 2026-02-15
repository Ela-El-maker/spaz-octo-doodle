package com.spazoodle.guardian.ui.reliability

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmEvent
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.OemReliabilityPlaybooks
import com.spazoodle.guardian.platform.reliability.ReliabilityScanner
import com.spazoodle.guardian.runtime.GuardianRuntime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReliabilityViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(ReliabilityUiState(isLoading = true))
    val uiState: StateFlow<ReliabilityUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            runCatching {
                ReliabilityScanner(appContext).scan()
            }.onSuccess { status ->
                val latestMissed = runCatching {
                    GuardianRuntime.alarmHistoryRepository(appContext)
                        .getRecent(100)
                        .firstOrNull { it.outcome == AlarmEventOutcome.MISSED }
                }.getOrNull()
                val latestMissedSummary = latestMissed?.let {
                    "Last missed alarm: alarmId=${it.alarmId} detail=${it.detail.orEmpty()}"
                }

                _uiState.value = ReliabilityUiState(
                    status = status,
                    isLoading = false,
                    latestMissedSummary = latestMissedSummary
                )
            }.onFailure { error ->
                _uiState.value = ReliabilityUiState(
                    status = null,
                    isLoading = false,
                    message = error.message ?: "Failed to scan reliability"
                )
            }
        }
    }

    fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, appContext.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }

    fun openExactAlarmSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${appContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${appContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        appContext.startActivity(intent)
    }

    fun openBatteryOptimizationSettings() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${appContext.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure {
                val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(fallback)
            }
    }

    fun openDndSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(intent)
    }

    fun openOemSettings() {
        val playbook = OemReliabilityPlaybooks.resolve(Build.MANUFACTURER.orEmpty()) ?: return
        val packageUri = Uri.parse("package:${appContext.packageName}")
        val intent = playbook.settingsIntents(appContext).firstOrNull()?.apply {
            data = packageUri
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent != null && runCatching { appContext.startActivity(intent) }.isSuccess) return
        openBatteryOptimizationSettings()
    }

    fun testAlarmIn15Seconds() {
        viewModelScope.launch {
            val triggerAt = System.currentTimeMillis() + 15_000L
            val scheduler = GuardianRuntime.alarmScheduler(appContext)
            if (!scheduler.canScheduleExactAlarms()) {
                _uiState.update {
                    it.copy(message = "Exact alarms are not allowed. Enable exact alarms, then retry.")
                }
                return@launch
            }

            val alarmId = triggerAt
            val now = System.currentTimeMillis()
            val alarm = Alarm(
                id = alarmId,
                title = "Guardian Test Alarm",
                description = null,
                type = AlarmType.ALARM,
                triggerAtUtcMillis = triggerAt,
                timezoneIdAtCreation = ZoneId.systemDefault().id,
                enabled = true,
                vibrateEnabled = true,
                ringtoneUri = null,
                primaryAction = null,
                policy = AlarmPolicy(
                    preAlerts = emptyList(),
                    nagSpec = null,
                    escalationSpec = null
                ),
                snoozeSpec = SnoozeSpec(
                    durationsMinutes = listOf(5, 10, 15),
                    defaultMinutes = 10
                ),
                createdAtUtcMillis = now,
                updatedAtUtcMillis = now
            )
            val plan = SchedulePlan(
                alarmId = alarmId,
                triggers = listOf(
                    Trigger(
                        alarmId = alarmId,
                        kind = TriggerKind.MAIN,
                        scheduledAtUtcMillis = triggerAt,
                        index = 0,
                        key = "TEST_15S"
                    )
                )
            )

            runCatching {
                GuardianRuntime.createAlarmUseCase(appContext).invoke(alarm)
                scheduler.schedule(plan)
            }.onSuccess {
                _uiState.update { it.copy(message = "Test alarm scheduled in 15 seconds") }
                classifyTestResult(alarmId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(message = error.message ?: "Failed to schedule test alarm")
                }
            }
        }
    }

    private suspend fun classifyTestResult(alarmId: Long) {
        repeat(35) {
            delay(1_000)
            val events = GuardianRuntime.alarmHistoryRepository(appContext).getByAlarmId(alarmId)
            val message = classify(events)
            if (message != null) {
                _uiState.update { it.copy(message = message) }
                return
            }
        }
        _uiState.update { it.copy(message = "Test result pending. Check History for details.") }
    }

    private fun classify(events: List<AlarmEvent>): String? {
        val missed = events.lastOrNull { it.outcome == AlarmEventOutcome.MISSED }
        if (missed != null) return "Test result: missed (${missed.detail.orEmpty()})"

        val recovered = events.lastOrNull { it.deliveryState == DeliveryState.RECOVERED_LATE }
        if (recovered != null) return "Test result: late (${recovered.delayMs ?: 0} ms)"

        val fired = events.lastOrNull { it.outcome == AlarmEventOutcome.FIRED }
        if (fired != null) return "Test result: on-time"

        return null
    }
}
