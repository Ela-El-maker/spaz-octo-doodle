package com.spazoodle.guardian.ui.home

import android.app.Application
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DefaultPreAlertOffsets
import com.spazoodle.guardian.domain.model.EscalationSpec
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.NagSpec
import com.spazoodle.guardian.domain.model.PrimaryAction
import com.spazoodle.guardian.domain.model.PrimaryActionType
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.runtime.GuardianRuntime
import com.spazoodle.guardian.platform.time.AlarmIdGenerator
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.platform.reliability.ReliabilityScanner
import com.spazoodle.guardian.platform.reliability.RiskReason
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val alarmIdGenerator = AlarmIdGenerator(appContext)

    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    private val _editorUiState = MutableStateFlow(EditorUiState())
    val editorUiState: StateFlow<EditorUiState> = _editorUiState.asStateFlow()
    private var previewPlayer: MediaPlayer? = null
    private var previewStopJob: Job? = null
    private var latestAlarms: List<Alarm> = emptyList()

    init {
        observeAlarms()
    }

    fun refreshAlarms() {
        viewModelScope.launch {
            runCatching { buildHomeUiState(latestAlarms) }
                .onSuccess { _homeUiState.value = it }
                .onFailure { error ->
                    _homeUiState.update {
                        it.copy(errorMessage = error.message ?: "Failed to refresh alarms")
                    }
                }
        }
    }

    private fun observeAlarms() {
        viewModelScope.launch {
            _homeUiState.update { it.copy(isLoading = true, errorMessage = null) }
            GuardianRuntime.alarmRepository(appContext)
                .observeAllAlarms()
                .collect { alarms ->
                    latestAlarms = alarms
                    runCatching { buildHomeUiState(alarms) }
                        .onSuccess { _homeUiState.value = it }
                        .onFailure { error ->
                            _homeUiState.update {
                                it.copy(
                                    alarms = alarms,
                                    isLoading = false,
                                    errorMessage = error.message ?: "Failed to update alarms"
                                )
                            }
                        }
                }
        }
    }

    private suspend fun buildHomeUiState(alarms: List<Alarm>): HomeUiState {
        val history = runCatching {
            GuardianRuntime.alarmHistoryRepository(appContext).getRecent(400)
        }.getOrDefault(emptyList())
        val latestByAlarm = history
            .groupBy { it.alarmId }
            .mapValues { (_, events) -> events.maxByOrNull { it.eventAtUtcMillis } }

        val status = ReliabilityScanner(appContext).scan()
        val riskMap = alarms.associate { alarm ->
            val reasons = mutableListOf<RiskReason>()
            if (alarm.enabled && !status.exactAlarmAllowed && GuardianFeatureFlags.strictExactGate) {
                reasons += RiskReason.EXACT_ALARM_BLOCKED
            }
            if (!status.notificationsEnabled) reasons += RiskReason.NOTIFICATION_DISABLED
            if (!status.batteryOptimizationIgnored) reasons += RiskReason.BATTERY_OPTIMIZED
            if (!status.fullScreenReady) reasons += RiskReason.FULL_SCREEN_NOT_READY
            if (!status.dndAlarmsLikelyAllowed) reasons += RiskReason.DND_MAY_SUPPRESS
            if (status.oemSteps.isNotEmpty()) reasons += RiskReason.OEM_BACKGROUND_RESTRICTIONS

            val label = when {
                reasons.contains(RiskReason.EXACT_ALARM_BLOCKED) -> "BLOCKED"
                reasons.size >= 3 -> "HIGH"
                reasons.isNotEmpty() -> "MEDIUM"
                else -> "LOW"
            }
            alarm.id to PlanRisk(label = label, reasons = reasons)
        }
        val alarmStateMap = alarms.associate { alarm ->
            val latest = latestByAlarm[alarm.id]
            alarm.id to deriveAlarmState(alarm, latest)
        }
        return HomeUiState(
            alarms = alarms,
            planRisk = riskMap,
            alarmState = alarmStateMap,
            isLoading = false
        )
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
                        description = alarm.description.orEmpty(),
                        templateId = AlarmTemplatePreset.STANDARD.id,
                        dateText = zoned.toLocalDate().toString(),
                        timeText = zoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        vibrateEnabled = alarm.vibrateEnabled,
                        ringtoneUri = alarm.ringtoneUri.orEmpty(),
                        ringtoneLabel = resolveRingtoneLabel(alarm.ringtoneUri),
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

    fun setRingtoneSelection(uriString: String?) {
        val cleanUri = uriString.orEmpty()
        updateDraft {
            it.copy(
                ringtoneUri = cleanUri,
                ringtoneLabel = resolveRingtoneLabel(cleanUri.ifBlank { null })
            )
        }
    }

    fun toggleRingtonePreview() {
        val current = _editorUiState.value.draft
        if (previewPlayer?.isPlaying == true) {
            stopRingtonePreview()
            return
        }
        val toneUri = runCatching {
            current.ringtoneUri.takeIf { it.isNotBlank() }?.let(Uri::parse)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }.getOrNull()

        if (toneUri == null) {
            _editorUiState.update { it.copy(errorMessage = "No ringtone available for preview") }
            return
        }

        stopRingtonePreview()
        runCatching {
            MediaPlayer().apply {
                setDataSource(appContext, toneUri)
                isLooping = false
                prepare()
                start()
            }
        }.onSuccess { player ->
            previewPlayer = player
            previewStopJob = viewModelScope.launch {
                delay(8_000L)
                stopRingtonePreview()
            }
        }.onFailure { error ->
            stopRingtonePreview()
            _editorUiState.update {
                it.copy(errorMessage = error.message ?: "Unable to preview ringtone")
            }
        }
    }

    fun stopRingtonePreview() {
        previewStopJob?.cancel()
        previewStopJob = null
        previewPlayer?.let { player ->
            runCatching {
                if (player.isPlaying) player.stop()
            }
            runCatching { player.release() }
        }
        previewPlayer = null
    }

    fun saveDraft(editingAlarmId: Long?, onDone: () -> Unit) {
        viewModelScope.launch {
            val draft = _editorUiState.value.draft
            runCatching {
                val alarm = buildAlarmFromDraft(draft, editingAlarmId)
                enforceStrictExactPolicyIfNeeded(alarm)
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
                    enforceStrictExactPolicyIfNeeded(alarm.copy(enabled = true))
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
        }
    }

    fun deleteAlarm(alarmId: Long, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            runCatching {
                GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarmId)
                GuardianRuntime.deleteAlarmUseCase(appContext).invoke(alarmId)
            }.onSuccess {
                onDone?.invoke()
            }.onFailure { error ->
                _homeUiState.update { it.copy(errorMessage = error.message ?: "Failed to delete alarm") }
                _editorUiState.update { it.copy(errorMessage = error.message ?: "Failed to delete alarm") }
            }
        }
    }

    fun deleteAlarmByIdWithUndo(
        alarmId: Long,
        onDeleted: (Alarm) -> Unit
    ) {
        viewModelScope.launch {
            runCatching {
                val alarm = GuardianRuntime.alarmRepository(appContext).getById(alarmId)
                    ?: error("Alarm not found")
                GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarm.id)
                GuardianRuntime.deleteAlarmUseCase(appContext).invoke(alarm.id)
                alarm
            }.onSuccess { deleted ->
                onDeleted(deleted)
            }.onFailure { error ->
                _homeUiState.update { it.copy(errorMessage = error.message ?: "Failed to delete alarm") }
                _editorUiState.update { it.copy(errorMessage = error.message ?: "Failed to delete alarm") }
            }
        }
    }

    fun deleteAlarm(
        alarm: Alarm,
        onDeleted: ((Alarm) -> Unit)? = null
    ) {
        viewModelScope.launch {
            runCatching {
                GuardianRuntime.alarmScheduler(appContext).cancelAlarm(alarm.id)
                GuardianRuntime.deleteAlarmUseCase(appContext).invoke(alarm.id)
            }.onSuccess {
                onDeleted?.invoke(alarm)
            }.onFailure { error ->
                _homeUiState.update { it.copy(errorMessage = error.message ?: "Failed to delete alarm") }
            }
        }
    }

    fun restoreDeletedAlarm(
        alarm: Alarm,
        onDone: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            runCatching {
                val now = System.currentTimeMillis()
                val restored = if (alarm.enabled && alarm.triggerAtUtcMillis <= now) {
                    alarm.copy(enabled = false, updatedAtUtcMillis = now)
                } else {
                    alarm.copy(updatedAtUtcMillis = now)
                }

                GuardianRuntime.alarmRepository(appContext).upsert(restored)
                if (restored.enabled) {
                    val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(restored)
                    GuardianRuntime.alarmScheduler(appContext).schedule(plan)
                } else {
                    GuardianRuntime.alarmScheduler(appContext).cancelAlarm(restored.id)
                }
            }.onSuccess {
                onDone?.invoke()
            }.onFailure { error ->
                _homeUiState.update { it.copy(errorMessage = error.message ?: "Failed to restore alarm") }
            }
        }
    }

    private fun enforceStrictExactPolicyIfNeeded(alarm: Alarm) {
        if (!alarm.enabled) return
        if (!GuardianFeatureFlags.strictExactGate) return
        val exactAllowed = GuardianRuntime.alarmScheduler(appContext).canScheduleExactAlarms()
        require(exactAllowed) {
            "Guardian strict mode blocked: Exact alarms are required for reliable delivery. Enable Exact alarms in settings."
        }
    }

    private suspend fun buildAlarmFromDraft(draft: AlarmDraft, editingAlarmId: Long?): Alarm {
        val date = LocalDate.parse(draft.dateText)
        val time = LocalTime.parse(draft.timeText)
        val zone = ZoneId.systemDefault()
        val triggerAtMillis = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
        require(draft.description.length <= 500) { "Description must be 500 characters or less." }

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
            id = editingAlarmId ?: alarmIdGenerator.nextId(),
            title = draft.title.ifBlank { "Guardian alarm" },
            description = draft.description.trim().ifBlank { null },
            type = AlarmType.ALARM,
            triggerAtUtcMillis = triggerAtMillis,
            timezoneIdAtCreation = zone.id,
            enabled = draft.enabled,
            vibrateEnabled = draft.vibrateEnabled,
            ringtoneUri = draft.ringtoneUri.trim().ifBlank { null },
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

    private fun resolveRingtoneLabel(uriString: String?): String {
        val uri = runCatching {
            uriString?.takeIf { it.isNotBlank() }?.let(Uri::parse)
        }.getOrNull()
        if (uri == null) return "Default alarm tone"
        return runCatching {
            val ringtone = RingtoneManager.getRingtone(appContext, uri)
            ringtone?.getTitle(appContext).takeUnless { it.isNullOrBlank() } ?: "Custom ringtone"
        }.getOrDefault("Custom ringtone")
    }

    override fun onCleared() {
        stopRingtonePreview()
        super.onCleared()
    }

    private fun deriveAlarmState(
        alarm: Alarm,
        latestEvent: com.spazoodle.guardian.domain.model.AlarmEvent?
    ): AlarmState {
        if (alarm.enabled) return AlarmState.ACTIVE
        if (latestEvent == null) return AlarmState.COMPLETED

        return when {
            latestEvent.outcome == AlarmEventOutcome.MISSED ||
                latestEvent.deliveryState == DeliveryState.MISSED -> AlarmState.MISSED

            latestEvent.outcome == AlarmEventOutcome.DISMISSED ||
                latestEvent.outcome == AlarmEventOutcome.ACTION_LAUNCHED ||
                latestEvent.deliveryState == DeliveryState.DISMISSED ||
                latestEvent.deliveryState == DeliveryState.ACTION_LAUNCHED -> AlarmState.COMPLETED

            else -> AlarmState.COMPLETED
        }
    }
}
