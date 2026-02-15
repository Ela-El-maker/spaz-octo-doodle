package com.spazoodle.guardian.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.spazoodle.guardian.domain.model.AlarmEventOutcome
import com.spazoodle.guardian.domain.model.DeliveryState
import com.spazoodle.guardian.domain.model.TriggerKind
import com.spazoodle.guardian.platform.reliability.GuardianDiagnosticTags
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.runtime.GuardianRuntime

class GuardianRetentionWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        val retentionMs = GuardianPolicyConfig.retentionConfig.historyRetentionDays * DAY_MS
        return runCatching {
            val result = GuardianRuntime.pruneRetention(
                context = applicationContext,
                nowUtcMillis = now,
                retentionMs = retentionMs
            )
            GuardianRuntime.recordFireEventUseCase(applicationContext).invoke(
                alarmId = SYSTEM_EVENT_ALARM_ID,
                triggerKind = TriggerKind.MAIN,
                outcome = AlarmEventOutcome.FIRED,
                deliveryState = DeliveryState.FIRED,
                detail = "${GuardianDiagnosticTags.RETENTION_PRUNE_RUN}:history=${result.historyRowsPruned},triggers=${result.triggerRowsPruned}"
            )
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "guardian_retention_prune"
        private const val SYSTEM_EVENT_ALARM_ID = -1L
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
