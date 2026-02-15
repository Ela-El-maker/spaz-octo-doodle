package com.spazoodle.guardian.platform.reliability

import com.spazoodle.guardian.data.local.dao.TriggerExecutionDao
import com.spazoodle.guardian.data.local.entity.TriggerExecutionEntity

class TriggerDeduper(
    private val triggerExecutionDao: TriggerExecutionDao,
    private val dedupeWindowMs: Long = 8_000L
) {
    suspend fun shouldProcess(executionKey: String, nowUtcMillis: Long): Boolean {
        val existing = triggerExecutionDao.getByKey(executionKey)
        if (existing == null) {
            triggerExecutionDao.insert(
                TriggerExecutionEntity(
                    executionKey = executionKey,
                    firstSeenAtUtcMillis = nowUtcMillis,
                    lastSeenAtUtcMillis = nowUtcMillis,
                    handled = true
                )
            )
            return true
        }

        val delta = nowUtcMillis - existing.lastSeenAtUtcMillis
        val process = delta > dedupeWindowMs
        triggerExecutionDao.update(
            existing.copy(
                lastSeenAtUtcMillis = nowUtcMillis,
                handled = existing.handled || process
            )
        )
        return process
    }

    suspend fun prune(retentionMs: Long, nowUtcMillis: Long) {
        val cutoff = nowUtcMillis - retentionMs
        triggerExecutionDao.pruneOlderThan(cutoff)
    }
}
