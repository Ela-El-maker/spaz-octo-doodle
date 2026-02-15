package com.spazoodle.guardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spazoodle.guardian.data.local.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alarmEntity: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :alarmId")
    suspend fun delete(alarmId: Long)

    @Query("SELECT * FROM alarms ORDER BY triggerAtUtcMillis ASC")
    suspend fun getAll(): List<AlarmEntity>

    @Query("SELECT * FROM alarms ORDER BY triggerAtUtcMillis ASC")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE enabled = 1")
    suspend fun getEnabled(): List<AlarmEntity>

    @Query(
        "SELECT * FROM alarms " +
            "WHERE enabled = 1 AND triggerAtUtcMillis >= :fromUtcMillis " +
            "ORDER BY triggerAtUtcMillis ASC LIMIT :limit"
    )
    suspend fun getUpcoming(fromUtcMillis: Long, limit: Int): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :alarmId LIMIT 1")
    suspend fun getById(alarmId: Long): AlarmEntity?

    @Query("UPDATE alarms SET enabled = :enabled, updatedAtUtcMillis = :updatedAtUtcMillis WHERE id = :alarmId")
    suspend fun setEnabled(alarmId: Long, enabled: Boolean, updatedAtUtcMillis: Long)
}
