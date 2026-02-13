package com.spazoodle.guardian.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DefaultPreAlertOffsets
import com.spazoodle.guardian.domain.model.EscalationSpec
import com.spazoodle.guardian.domain.model.NagSpec
import com.spazoodle.guardian.domain.model.PrimaryAction
import com.spazoodle.guardian.domain.model.PrimaryActionType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.runtime.GuardianRuntime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _editorUiState = MutableStateFlow(EditorUiState())
    val editorUiState: StateFlow<EditorUiState> = _editorUiState.asStateFlow()

    init {
        refreshAlarms()
    }

    fun refreshAlarms() {
        viewModelScope.launch {
            _homeUiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                GuardianRuntime.alarmRepository(appContext).getAllAlarms()
            }.onSuccess { alarms ->
                _homeUiState.update { HomeUiState(alarms = alarms, isLoading = false) }
            }.onFailure { error ->
                _homeUiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load alarms")
                }
            }
        }
    }

    fun loadCreateDraft() {
        val now = Instant.ofEpochMilli(System.currentTimeMillis())
            .atZone(ZoneId.systemDefault())
        _editorUiState.value = EditorUiState(
            draft = AlarmDraft(
                dateText = now.toLocalDate().toString(),
                timeText = now.toLocalTime().withSecond(0).withNano(0).toString()
            )
        )
    }

    fun loadEditDraft(alarmId: Long) {
        viewModelScope.launch {
            _editorUiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                GuardianRuntime.alarmRepository(appContext).getById(alarmId)
            }.onSuccess { alarm ->
                if (alarm == null) {
                    _editorUiState.update {
                        it.copy(isLoading = false, errorMessage = "Alarm not found")
                    }
                    return@onSuccess
                }

                val zoned = Instant.ofEpochMilli(alarm.triggerAtUtcMillis)
                    .atZone(ZoneId.systemDefault())
                val preKeys = alarm.policy.preAlerts.map { it.key }.toSet()

                _editorUiState.value = EditorUiState(
                    draft = AlarmDraft(
                        title = alarm.title,
                        templateId = AlarmTemplatePreset.STANDARD.id,
                        dateText = zoned.toLocalDate().toString(),
                        timeText = zoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        primaryActionType = alarm.primaryAction?.type?.name.orEmpty(),
                        primaryActionValue = alarm.primaryAction?.value.orEmpty(),
                        primaryActionLabel = alarm.primaryAction?.label.orEmpty(),
                        enabled = alarm.enabled,
                        preAlert1Day = preKeys.contains(DefaultPreAlertOffsets.ONE_DAY.key),
                        preAlert1Hour = preKeys.contains(DefaultPreAlertOffsets.ONE_HOUR.key),
                        preAlert10Min = preKeys.contains(DefaultPreAlertOffsets.TEN_MINUTES.key),
                        preAlert2Min = preKeys.contains(DefaultPreAlertOffsets.TWO_MINUTES.key),
                        nagEnabled = alarm.policy.nagSpec?.enabled == true,
                        nagRepeatMinutesCsv = alarm.policy.nagSpec?.repeatMinutes
                            ?.joinToString(",")
                            ?: "2,3,5,10",
                        nagMaxCount = (alarm.policy.nagSpec?.maxNagCount ?: 20).toString(),
                        nagMaxWindowMinutes = (alarm.policy.nagSpec?.maxNagWindowMinutes ?: 120).toString(),
                        escalationEnabled = alarm.policy.escalationSpec?.enabled == true,
                        escalationStepAfterCount = (alarm.policy.escalationSpec?.stepUpAfterNagCount ?: 3).toString()
                    ),
                    isLoading = false
                )
            }.onFailure { error ->
                _editorUiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load alarm")
                }
            }
        }
    }

    fun updateDraft(transform: (AlarmDraft) -> AlarmDraft) {
        _editorUiState.update { it.copy(draft = transform(it.draft), errorMessage = null) }
    }

    fun applyTemplate(template: AlarmTemplatePreset) {
        updateDraft { current ->
            when (template) {
                AlarmTemplatePreset.STANDARD -> current.copy(
                    templateId = template.id,
                    preAlert1Day = true,
                    preAlert1Hour = true,
                    preAlert10Min = true,
                    preAlert2Min = true,
                    nagEnabled = false,
                    escalationEnabled = false
                )

                AlarmTemplatePreset.CRITICAL -> current.copy(
                    templateId = template.id,
                    preAlert1Day = true,
                    preAlert1Hour = true,
                    preAlert10Min = true,
                    preAlert2Min = true,
                    nagEnabled = true,
                    nagRepeatMinutesCsv = "1,2,3,5,10",
                    nagMaxCount = "30",
                    nagMaxWindowMinutes = "180",
                    escalationEnabled = true,
                    escalationStepAfterCount = "2"
                )

                AlarmTemplatePreset.TRAVEL -> current.copy(
                    templateId = template.id,
                    preAlert1Day = true,
                    preAlert1Hour = true,
                    preAlert10Min = true,
                    preAlert2Min = false,
                    nagEnabled = true,
                    nagRepeatMinutesCsv = "3,5,10",
                    nagMaxCount = "12",
                    nagMaxWindowMinutes = "120",
                    escalationEnabled = false
                )

                AlarmTemplatePreset.QUIET -> current.copy(
                    templateId = template.id,
                    preAlert1Day = false,
                    preAlert1Hour = true,
                    preAlert10Min = true,
                    preAlert2Min = false,
                    nagEnabled = false,
                    escalationEnabled = false
                )
            }
        }
    }

    fun setPrimaryActionType(typeName: String) {
        val label = when (typeName) {
            PrimaryActionType.OPEN_URL.name -> "Open"
            PrimaryActionType.OPEN_DEEPLINK.name -> "Open"
            PrimaryActionType.CALL_NUMBER.name -> "Call"
            PrimaryActionType.OPEN_MAP_NAVIGATION.name -> "Navigate"
            PrimaryActionType.OPEN_NOTE.name -> "Open Note"
            PrimaryActionType.OPEN_CHECKLIST.name -> "Checklist"
            PrimaryActionType.OPEN_FILE.name -> "Open File"
            else -> ""
        }
        updateDraft { it.copy(primaryActionType = typeName, primaryActionLabel = label) }
    }

    fun saveDraft(editingAlarmId: Long?, onDone: () -> Unit) {
        viewModelScope.launch {
            val draft = _editorUiState.value.draft
            runCatching {
                val alarm = buildAlarmFromDraft(draft, editingAlarmId)
                if (editingAlarmId == null) {
                    GuardianRuntime.createAlarmUseCase(appContext).invoke(alarm)
                } else {
                    GuardianRuntime.updateAlarmUseCase(appContext).invoke(alarm)
                }

                if (alarm.enabled) {
                    val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(alarm)
                    GuardianRuntime.alarmScheduler(appContext).schedule(plan)
                } else {
                    GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarm.id)
                }
            }.onSuccess {
                refreshAlarms()
                onDone()
            }.onFailure { error ->
                _editorUiState.update {
                    it.copy(errorMessage = error.message ?: "Failed to save alarm")
                }
            }
        }
    }

    fun toggleEnabled(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch {
            runCatching {
                if (enabled) {
                    GuardianRuntime.enableAlarmUseCase(appContext).invoke(alarm.id)
                    val updated = alarm.copy(enabled = true, updatedAtUtcMillis = System.currentTimeMillis())
                    val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(updated)
                    GuardianRuntime.alarmScheduler(appContext).schedule(plan)
                } else {
                    GuardianRuntime.disableAlarmUseCase(appContext).invoke(alarm.id)
                    GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarm.id)
                }
            }.onFailure { error ->
                _homeUiState.update { it.copy(errorMessage = error.message ?: "Failed to toggle alarm") }
            }
            refreshAlarms()
        }
    }

    private suspend fun buildAlarmFromDraft(draft: AlarmDraft, editingAlarmId: Long?): Alarm {
        val date = LocalDate.parse(draft.dateText)
        val time = LocalTime.parse(draft.timeText)
        val zone = ZoneId.systemDefault()
        val triggerAtMillis = date.atTime(time).atZone(zone).toInstant().toEpochMilli()

        val existing = editingAlarmId?.let { GuardianRuntime.alarmRepository(appContext).getById(it) }
        val now = System.currentTimeMillis()
        val preAlerts = buildList {
            if (draft.preAlert1Day) add(DefaultPreAlertOffsets.ONE_DAY)
            if (draft.preAlert1Hour) add(DefaultPreAlertOffsets.ONE_HOUR)
            if (draft.preAlert10Min) add(DefaultPreAlertOffsets.TEN_MINUTES)
            if (draft.preAlert2Min) add(DefaultPreAlertOffsets.TWO_MINUTES)
        }

        val nagRepeatMinutes = draft.nagRepeatMinutesCsv
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
            .ifEmpty { listOf(2, 3, 5, 10) }
        val nagMaxCount = draft.nagMaxCount.toIntOrNull()?.coerceAtLeast(1) ?: 20
        val nagMaxWindow = draft.nagMaxWindowMinutes.toIntOrNull()?.coerceAtLeast(1) ?: 120

        val nagSpec = if (draft.nagEnabled) {
            NagSpec(
                enabled = true,
                repeatMinutes = nagRepeatMinutes,
                maxNagCount = nagMaxCount,
                maxNagWindowMinutes = nagMaxWindow
            )
        } else {
            null
        }

        val actionType = runCatching {
            if (draft.primaryActionType.isBlank()) null
            else PrimaryActionType.valueOf(draft.primaryActionType.trim())
        }.getOrNull()

        val primaryAction = if (actionType != null && draft.primaryActionValue.isNotBlank()) {
            PrimaryAction(
                type = actionType,
                value = draft.primaryActionValue.trim(),
                label = draft.primaryActionLabel.trim().ifBlank { null }
            )
        } else {
            null
        }

        return Alarm(
            id = editingAlarmId ?: now,
            title = draft.title.ifBlank { "Guardian alarm" },
            type = AlarmType.ALARM,
            triggerAtUtcMillis = triggerAtMillis,
            timezoneIdAtCreation = zone.id,
            enabled = draft.enabled,
            primaryAction = primaryAction,
            policy = AlarmPolicy(
                preAlerts = preAlerts,
                nagSpec = nagSpec,
                escalationSpec = if (draft.nagEnabled && draft.escalationEnabled) {
                    EscalationSpec(
                        enabled = true,
                        stepUpAfterNagCount = draft.escalationStepAfterCount.toIntOrNull()?.coerceAtLeast(1) ?: 3,
                        toneSequence = listOf("soft", "standard", "loud")
                    )
                } else {
                    null
                }
            ),
            snoozeSpec = SnoozeSpec(
                durationsMinutes = listOf(5, 10, 15),
                defaultMinutes = 10
            ),
            createdAtUtcMillis = existing?.createdAtUtcMillis ?: now,
            updatedAtUtcMillis = now
        )
    }
}
