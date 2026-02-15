package com.spazoodle.guardian.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.SnoozeSpec
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.runtime.GuardianRuntime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealAlarmPipelineIntegrationTest {

    @Test
    fun createdAlarm_shouldFireMainTrigger_andBeRecordedInHistory() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        val triggerAt = System.currentTimeMillis() + 20_000L
        val alarmId = triggerAt + 1337L
        val now = System.currentTimeMillis()

        val alarm = Alarm(
            id = alarmId,
            title = "Integration Alarm",
            description = "Integration pipeline validation",
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

        GuardianRuntime.createAlarmUseCase(context).invoke(alarm)
        val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(alarm)
        GuardianRuntime.alarmScheduler(context).schedule(plan)

        var delivered = false
        var missed = false
        repeat(35) {
            delay(1_000)
            val events = GuardianRuntime.alarmHistoryRepository(context).getByAlarmId(alarmId)
            delivered = events.any { event ->
                event.triggerKind == TriggerKind.MAIN &&
                    (event.outcome == AlarmEventOutcome.FIRED || event.outcome == AlarmEventOutcome.RECOVERED_LATE) &&
                    (event.deliveryState == DeliveryState.FIRED || event.deliveryState == DeliveryState.RECOVERED_LATE)
            }
            missed = events.any { event ->
                event.triggerKind == TriggerKind.MAIN && event.outcome == AlarmEventOutcome.MISSED
            }
            if (delivered || missed) return@repeat
        }

        assertTrue("Expected MAIN delivery (FIRED or RECOVERED_LATE), but alarm was missed", !missed)
        assertTrue("Expected MAIN delivery event for created alarm", delivered)
    }
}
