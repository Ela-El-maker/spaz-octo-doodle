package com.spazoodle.guardian.ui.ringing

import android.content.Intent
import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.spazoodle.guardian.platform.reliability.GuardianFeatureFlags
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.receiver.AlarmActionReceiver
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver
import kotlinx.coroutines.delay

class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(KeyguardManager::class.java)
            runCatching { keyguardManager?.requestDismissKeyguard(this, null) }
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val alarmId = intent.getLongExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, -1L)
        val title = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE).orEmpty()
        val description = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_DESCRIPTION).orEmpty()
        val primaryActionType = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_TYPE)
        val primaryActionValue = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_VALUE)
        val primaryActionLabel = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_LABEL)
        val routeWarning = intent.getBooleanExtra(AlarmTriggerReceiver.EXTRA_AUDIO_ROUTE_WARNING, false)
        val holdConfig = GuardianPolicyConfig.holdToStopConfig

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val stopInteraction = remember { MutableInteractionSource() }
                    val stopPressed by stopInteraction.collectIsPressedAsState()
                    var holdProgress by remember { mutableFloatStateOf(0f) }
                    var guardStartedLogged by remember { mutableStateOf(false) }

                    LaunchedEffect(stopPressed) {
                        if (!GuardianFeatureFlags.holdToStopGuard || !holdConfig.enabled) return@LaunchedEffect
                        if (!stopPressed) {
                            holdProgress = 0f
                            guardStartedLogged = false
                            return@LaunchedEffect
                        }
                        if (!guardStartedLogged) {
                            action(alarmId, AlarmActionReceiver.ACTION_STOP_GUARD_STARTED)
                            guardStartedLogged = true
                        }
                        val holdMs = holdConfig.holdDurationMs.coerceAtLeast(500L)
                        val tickMs = 50L
                        val totalTicks = (holdMs / tickMs).toInt().coerceAtLeast(1)
                        for (tick in 1..totalTicks) {
                            if (!stopPressed) return@LaunchedEffect
                            holdProgress = tick.toFloat() / totalTicks.toFloat()
                            if (holdProgress >= 1f) {
                                action(alarmId, AlarmActionReceiver.ACTION_STOP)
                                return@LaunchedEffect
                            }
                            delay(tickMs)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Alarm", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = if (title.isBlank()) "Guardian" else title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        if (routeWarning) {
                            Text(
                                text = "Audio may still route to Bluetooth/headset. Speaker forcing is best effort.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                modifier = Modifier
                                    .sizeIn(minHeight = 56.dp)
                                    .semantics { contentDescription = "Hold for 2 seconds to stop alarm" },
                                interactionSource = stopInteraction,
                                onClick = {}
                            ) {
                                Text("Stop")
                            }
                            Button(
                                modifier = Modifier
                                    .sizeIn(minHeight = 56.dp)
                                    .semantics { contentDescription = "Snooze alarm for 10 minutes" },
                                onClick = {
                                    action(
                                        alarmId,
                                        AlarmActionReceiver.ACTION_SNOOZE,
                                        snoozeMinutes = 10
                                    )
                                }
                            ) {
                                Text("Snooze 10")
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                modifier = Modifier
                                    .sizeIn(minHeight = 56.dp)
                                    .semantics { contentDescription = "Snooze alarm for 5 minutes" },
                                onClick = {
                                    action(
                                        alarmId,
                                        AlarmActionReceiver.ACTION_SNOOZE,
                                        snoozeMinutes = 5
                                    )
                                }
                            ) {
                                Text("Snooze 5")
                            }
                            Button(
                                modifier = Modifier
                                    .sizeIn(minHeight = 56.dp)
                                    .semantics { contentDescription = "Snooze alarm for 15 minutes" },
                                onClick = {
                                    action(
                                        alarmId,
                                        AlarmActionReceiver.ACTION_SNOOZE,
                                        snoozeMinutes = 15
                                    )
                                }
                            ) {
                                Text("Snooze 15")
                            }
                        }
                        if (GuardianFeatureFlags.holdToStopGuard && holdConfig.enabled) {
                            LinearProgressIndicator(
                                progress = { holdProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                            Text(
                                text = "Hold Stop for ${holdConfig.holdDurationMs / 1000.0} seconds to confirm.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (!primaryActionValue.isNullOrBlank()) {
                            Button(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .sizeIn(minHeight = 56.dp)
                                    .semantics { contentDescription = "Perform primary action" },
                                onClick = {
                                    action(
                                        alarmId,
                                        AlarmActionReceiver.ACTION_DO,
                                        primaryActionType = primaryActionType,
                                        primaryActionValue = primaryActionValue,
                                        primaryActionLabel = primaryActionLabel
                                    )
                                }
                            ) {
                                Text(primaryActionLabel.takeUnless { it.isNullOrBlank() } ?: "Do")
                            }
                            Text(
                                text = "If device is locked, unlock first then tap Do.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun action(
        alarmId: Long,
        action: String,
        snoozeMinutes: Int? = null,
        primaryActionType: String? = null,
        primaryActionValue: String? = null,
        primaryActionLabel: String? = null
    ) {
        val intent = Intent(this, AlarmActionReceiver::class.java).apply {
            this.action = action
            putExtra(AlarmActionReceiver.EXTRA_ALARM_ID, alarmId)
            snoozeMinutes?.let { putExtra(AlarmActionReceiver.EXTRA_SNOOZE_MINUTES, it) }
            primaryActionType?.let { putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_TYPE, it) }
            primaryActionValue?.let { putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_VALUE, it) }
            primaryActionLabel?.let { putExtra(AlarmActionReceiver.EXTRA_PRIMARY_ACTION_LABEL, it) }
        }
        sendBroadcast(intent)
        finish()
    }
}
