package com.spazoodle.guardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spazoodle.guardian.data.local.entity.AlarmHistoryEntity

@Dao
interface AlarmHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: AlarmHistoryEntity)

    @Query("SELECT * FROM alarm_history WHERE alarmId = :alarmId ORDER BY eventAtUtcMillis ASC")
    suspend fun getByAlarmId(alarmId: Long): List<AlarmHistoryEntity>

    @Query("SELECT * FROM alarm_history ORDER BY eventAtUtcMillis DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<AlarmHistoryEntity>
}
