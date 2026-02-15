package com.spazoodle.guardian.runtime

import android.content.Context
import com.spazoodle.guardian.data.local.GuardianDatabase
import com.spazoodle.guardian.data.local.GuardianDatabaseFactory
import com.spazoodle.guardian.data.repository.AlarmHistoryRepositoryImpl
import com.spazoodle.guardian.data.repository.AlarmRepositoryImpl
import com.spazoodle.guardian.domain.repository.AlarmHistoryRepository
import com.spazoodle.guardian.domain.repository.AlarmRepository
import com.spazoodle.guardian.domain.usecase.AcknowledgeAlarmUseCase
import com.spazoodle.guardian.domain.usecase.ComputeSchedulePlanUseCase
import com.spazoodle.guardian.domain.usecase.CreateAlarmUseCase
import com.spazoodle.guardian.domain.usecase.DeleteAlarmUseCase
import com.spazoodle.guardian.domain.usecase.DisableAlarmUseCase
import com.spazoodle.guardian.domain.usecase.EnableAlarmUseCase
import com.spazoodle.guardian.domain.usecase.FinalizeOneTimeAlarmUseCase
import com.spazoodle.guardian.domain.usecase.RecordFireEventUseCase
import com.spazoodle.guardian.domain.usecase.ReconcileEnabledAlarmsUseCase
import com.spazoodle.guardian.domain.usecase.RescheduleAllActiveAlarmsUseCase
import com.spazoodle.guardian.domain.usecase.UpdateAlarmUseCase
import com.spazoodle.guardian.platform.scheduler.AlarmScheduler
import com.spazoodle.guardian.platform.scheduler.AndroidAlarmScheduler
import com.spazoodle.guardian.platform.reliability.GuardianPolicyConfig
import com.spazoodle.guardian.platform.reliability.TriggerDeduper
import com.spazoodle.guardian.platform.time.SystemUtcClock

object GuardianRuntime {

    data class RetentionPruneResult(
        val historyRowsPruned: Int,
        val triggerRowsPruned: Int
    )

    @Volatile
    private var database: GuardianDatabase? = null

    fun alarmScheduler(context: Context): AlarmScheduler {
        return AndroidAlarmScheduler(context.applicationContext)
    }

    fun alarmRepository(context: Context): AlarmRepository {
        return AlarmRepositoryImpl(getDatabase(context).alarmDao())
    }

    fun alarmHistoryRepository(context: Context): AlarmHistoryRepository {
        return AlarmHistoryRepositoryImpl(getDatabase(context).alarmHistoryDao())
    }

    fun triggerDeduper(context: Context): TriggerDeduper {
        return TriggerDeduper(
            triggerExecutionDao = getDatabase(context).triggerExecutionDao(),
            dedupeWindowMs = GuardianPolicyConfig.dedupeWindowMs
        )
    }

    fun recordFireEventUseCase(context: Context): RecordFireEventUseCase {
        return RecordFireEventUseCase(
            alarmHistoryRepository = alarmHistoryRepository(context),
            clock = SystemUtcClock
        )
    }

    fun createAlarmUseCase(context: Context): CreateAlarmUseCase {
        return CreateAlarmUseCase(
            alarmRepository = alarmRepository(context),
            clock = SystemUtcClock
        )
    }

    fun deleteAlarmUseCase(context: Context): DeleteAlarmUseCase {
        return DeleteAlarmUseCase(
            alarmRepository = alarmRepository(context)
        )
    }

    fun updateAlarmUseCase(context: Context): UpdateAlarmUseCase {
        return UpdateAlarmUseCase(
            alarmRepository = alarmRepository(context),
            clock = SystemUtcClock
        )
    }

    fun enableAlarmUseCase(context: Context): EnableAlarmUseCase {
        return EnableAlarmUseCase(alarmRepository(context))
    }

    fun disableAlarmUseCase(context: Context): DisableAlarmUseCase {
        return DisableAlarmUseCase(alarmRepository(context))
    }

    fun finalizeOneTimeAlarmUseCase(context: Context): FinalizeOneTimeAlarmUseCase {
        return FinalizeOneTimeAlarmUseCase(alarmRepository(context))
    }

    fun computeSchedulePlanUseCase(): ComputeSchedulePlanUseCase {
        return ComputeSchedulePlanUseCase(SystemUtcClock)
    }

    fun acknowledgeAlarmUseCase(context: Context): AcknowledgeAlarmUseCase {
        return AcknowledgeAlarmUseCase(
            alarmHistoryRepository = alarmHistoryRepository(context),
            clock = SystemUtcClock
        )
    }

    fun rescheduleAllActiveAlarmsUseCase(context: Context): RescheduleAllActiveAlarmsUseCase {
        return RescheduleAllActiveAlarmsUseCase(
            alarmRepository = alarmRepository(context),
            computeSchedulePlanUseCase = ComputeSchedulePlanUseCase(SystemUtcClock)
        )
    }

    fun reconcileEnabledAlarmsUseCase(context: Context): ReconcileEnabledAlarmsUseCase {
        return ReconcileEnabledAlarmsUseCase(
            alarmRepository = alarmRepository(context),
            computeSchedulePlanUseCase = ComputeSchedulePlanUseCase(SystemUtcClock),
            clock = SystemUtcClock
        )
    }

    suspend fun pruneRetention(
        context: Context,
        nowUtcMillis: Long,
        retentionMs: Long
    ): RetentionPruneResult {
        val cutoff = nowUtcMillis - retentionMs
        val history = getDatabase(context).alarmHistoryDao().pruneOlderThan(cutoff)
        val triggers = getDatabase(context).triggerExecutionDao().pruneOlderThan(cutoff)
        return RetentionPruneResult(
            historyRowsPruned = history,
            triggerRowsPruned = triggers
        )
    }

    private fun getDatabase(context: Context): GuardianDatabase {
        val existing = database
        if (existing != null) return existing

        return synchronized(this) {
            database ?: GuardianDatabaseFactory.create(context.applicationContext).also {
                database = it
            }
        }
    }
}
