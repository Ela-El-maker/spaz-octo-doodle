package com.spazoodle.guardian.ui.ringing

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spazoodle.guardian.receiver.AlarmActionReceiver
import com.spazoodle.guardian.receiver.AlarmTriggerReceiver

class AlarmRingingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val alarmId = intent.getLongExtra(AlarmTriggerReceiver.EXTRA_ALARM_ID, -1L)
        val title = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_ALARM_TITLE).orEmpty()
        val primaryActionType = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_TYPE)
        val primaryActionValue = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_VALUE)
        val primaryActionLabel = intent.getStringExtra(AlarmTriggerReceiver.EXTRA_PRIMARY_ACTION_LABEL)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(onClick = { action(alarmId, AlarmActionReceiver.ACTION_STOP) }) {
                                Text("Stop")
                            }
                            Button(
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

                        if (!primaryActionValue.isNullOrBlank()) {
                            Button(
                                modifier = Modifier.padding(top = 12.dp),
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
