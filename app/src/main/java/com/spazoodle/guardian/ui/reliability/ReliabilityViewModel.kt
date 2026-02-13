package com.spazoodle.guardian.ui.reliability

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spazoodle.guardian.domain.model.SchedulePlan
import com.spazoodle.guardian.domain.model.Trigger
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.ReliabilityScanner
import com.spazoodle.guardian.runtime.GuardianRuntime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
                _uiState.value = ReliabilityUiState(status = status, isLoading = false)
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

    fun testAlarmIn15Seconds() {
        viewModelScope.launch {
            val triggerAt = System.currentTimeMillis() + 15_000L
            val alarmId = triggerAt
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
            GuardianRuntime.alarmScheduler(appContext).schedule(plan)
            _uiState.update { it.copy(message = "Test alarm scheduled in 15 seconds") }
        }
    }
}
