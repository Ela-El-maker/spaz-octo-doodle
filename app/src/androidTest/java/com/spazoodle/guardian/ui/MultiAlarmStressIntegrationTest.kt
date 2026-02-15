package com.spazoodle.guardian.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.domain.model.Alarm
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.AlarmPolicy
import com.spazoodle.guardian.domain.model.AlarmType
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
class MultiAlarmStressIntegrationTest {

    @Test
    fun multipleNearTermAlarms_shouldAllFireMain() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val now = System.currentTimeMillis()
        val alarmIds = mutableListOf<Long>()

        listOf(20_000L, 24_000L, 28_000L).forEachIndexed { index, offset ->
            val triggerAt = now + offset
            val alarmId = triggerAt + 7_000L + index
            alarmIds += alarmId
            val alarm = buildAlarm(alarmId = alarmId, triggerAt = triggerAt, now = now, title = "Multi $index")
            GuardianRuntime.createAlarmUseCase(context).invoke(alarm)
            val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(alarm)
            GuardianRuntime.alarmScheduler(context).schedule(plan)
        }

        repeat(60) {
            delay(1_000)
            val allDelivered = alarmIds.all { alarmId ->
                GuardianRuntime.alarmHistoryRepository(context).getByAlarmId(alarmId).any { event ->
                    event.triggerKind == TriggerKind.MAIN &&
                        (event.outcome == AlarmEventOutcome.FIRED || event.outcome == AlarmEventOutcome.RECOVERED_LATE)
                }
            }
            if (allDelivered) return@repeat
        }

        val failedIds = alarmIds.filterNot { alarmId ->
            GuardianRuntime.alarmHistoryRepository(context).getByAlarmId(alarmId).any { event ->
                event.triggerKind == TriggerKind.MAIN &&
                    (event.outcome == AlarmEventOutcome.FIRED || event.outcome == AlarmEventOutcome.RECOVERED_LATE)
            }
        }
        assertTrue("Expected all multi-alarms to deliver MAIN. Missing: $failedIds", failedIds.isEmpty())
    }

    @Test
    fun rapidlyCreatedThenCanceledAlarms_shouldNotFireAfterCancel() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val now = System.currentTimeMillis()

        val canceledIds = (0 until 5).map { index ->
            val triggerAt = now + 20_000L + (index * 1_000L)
            val alarmId = triggerAt + 55_000L + index
            val alarm = buildAlarm(alarmId = alarmId, triggerAt = triggerAt, now = now, title = "Cancel $index")
            GuardianRuntime.createAlarmUseCase(context).invoke(alarm)
            val plan = GuardianRuntime.computeSchedulePlanUseCase().invoke(alarm)
            GuardianRuntime.alarmScheduler(context).schedule(plan)
            GuardianRuntime.disableAlarmUseCase(context).invoke(alarmId)
            GuardianRuntime.alarmScheduler(context).cancelAlarm(alarmId)
            alarmId
        }

        delay(35_000L)

        val firedCanceled = canceledIds.filter { alarmId ->
            GuardianRuntime.alarmHistoryRepository(context).getByAlarmId(alarmId).any { event ->
                event.triggerKind == TriggerKind.MAIN &&
                    (event.outcome == AlarmEventOutcome.FIRED || event.outcome == AlarmEventOutcome.RECOVERED_LATE)
            }
        }

        assertTrue(
            "Canceled alarms should not fire MAIN, but these did: $firedCanceled",
            firedCanceled.isEmpty()
        )
    }

    private fun buildAlarm(alarmId: Long, triggerAt: Long, now: Long, title: String): Alarm {
        return Alarm(
            id = alarmId,
            title = title,
            description = "Stress case $title",
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
    }
}
