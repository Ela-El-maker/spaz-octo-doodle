package com.spazoodle.guardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spazoodle.guardian.data.local.entity.AlarmRiskCacheEntity

@Dao
interface AlarmRiskCacheDao {
    @Query("SELECT * FROM alarm_risk_cache WHERE alarmId = :alarmId LIMIT 1")
    suspend fun get(alarmId: Long): AlarmRiskCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AlarmRiskCacheEntity)

    @Query("DELETE FROM alarm_risk_cache")
    suspend fun clear()
}
