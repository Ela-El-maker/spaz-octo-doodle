package com.spazoodle.guardian.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import com.spazoodle.guardian.data.local.dao.AlarmDao
import com.spazoodle.guardian.data.local.dao.AlarmHistoryDao
import com.spazoodle.guardian.data.local.entity.AlarmEntity
import com.spazoodle.guardian.data.local.entity.AlarmHistoryEntity

@Database(
    entities = [AlarmEntity::class, AlarmHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GuardianDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN type TEXT NOT NULL DEFAULT 'ALARM'"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN preAlertKeysCsv TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN primaryActionType TEXT"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN primaryActionValue TEXT"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN primaryActionLabel TEXT"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN preAlertOffsetsCsv TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN nagEnabled INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN nagRepeatMinutesCsv TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN nagMaxCount INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN nagMaxWindowMinutes INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN escalationEnabled INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN escalationStepUpAfterNagCount INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN escalationToneSequenceCsv TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN snoozeDurationsCsv TEXT NOT NULL DEFAULT '5,10,15'"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN snoozeDefaultMinutes INTEGER NOT NULL DEFAULT 10"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN createdAtUtcMillis INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN updatedAtUtcMillis INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_alarms_enabled_triggerAtUtcMillis " +
                        "ON alarms(enabled, triggerAtUtcMillis)"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS alarm_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "alarmId INTEGER NOT NULL, " +
                        "triggerKind TEXT NOT NULL, " +
                        "outcome TEXT NOT NULL, " +
                        "eventAtUtcMillis INTEGER NOT NULL, " +
                        "detail TEXT)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_alarm_history_alarmId ON alarm_history(alarmId)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_alarm_history_eventAtUtcMillis ON alarm_history(eventAtUtcMillis)"
                )
            }
        }
    }
}
