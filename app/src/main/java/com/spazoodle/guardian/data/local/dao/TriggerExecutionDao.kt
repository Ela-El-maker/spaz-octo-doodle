package com.spazoodle.guardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.spazoodle.guardian.data.local.entity.TriggerExecutionEntity

@Dao
interface TriggerExecutionDao {
    @Query("SELECT * FROM trigger_executions WHERE executionKey = :executionKey LIMIT 1")
    suspend fun getByKey(executionKey: String): TriggerExecutionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TriggerExecutionEntity)

    @Update
    suspend fun update(entity: TriggerExecutionEntity)

    @Query("DELETE FROM trigger_executions WHERE firstSeenAtUtcMillis < :olderThanUtcMillis")
    suspend fun pruneOlderThan(olderThanUtcMillis: Long): Int
}
