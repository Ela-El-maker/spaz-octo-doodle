package com.spazoodle.guardian.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.runtime.GuardianRuntime

class GuardianRescheduleWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val reason = inputData.getString(KEY_REASON) ?: "unknown"
        return runCatching {
            val plans = GuardianRuntime
                .rescheduleAllActiveAlarmsUseCase(applicationContext)
                .invoke()
            GuardianRuntime.alarmScheduler(applicationContext).rescheduleAll(plans)
            GuardianRuntime.recordFireEventUseCase(applicationContext).invoke(
                alarmId = SYSTEM_EVENT_ALARM_ID,
                triggerKind = TriggerKind.MAIN,
                outcome = AlarmEventOutcome.RESCHEDULED,
                deliveryState = DeliveryState.FIRED,
                detail = "rescheduled_after_$reason:count=${plans.size}"
            )
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_PREFIX = "guardian_reschedule_"
        private const val KEY_REASON = "reason"
        private const val SYSTEM_EVENT_ALARM_ID = -2L

        fun enqueue(context: Context, reason: String) {
            val request = OneTimeWorkRequestBuilder<GuardianRescheduleWorker>()
                .setInputData(
                    androidx.work.Data.Builder()
                        .putString(KEY_REASON, reason)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_PREFIX + reason,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
