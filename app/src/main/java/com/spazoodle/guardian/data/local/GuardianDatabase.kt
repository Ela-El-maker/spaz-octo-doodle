package com.spazoodle.guardian.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spazoodle.guardian.data.local.dao.AlarmDao
import com.spazoodle.guardian.data.local.entity.AlarmEntity

@Database(
    entities = [AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GuardianDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
