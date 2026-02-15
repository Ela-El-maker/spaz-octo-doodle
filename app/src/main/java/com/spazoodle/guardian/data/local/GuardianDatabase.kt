package com.spazoodle.guardian.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.RoomDatabase
import com.spazoodle.guardian.data.local.dao.AlarmDao
import com.spazoodle.guardian.data.local.dao.AlarmHistoryDao
import com.spazoodle.guardian.data.local.dao.AlarmRiskCacheDao
import com.spazoodle.guardian.data.local.dao.TriggerExecutionDao
import com.spazoodle.guardian.data.local.entity.AlarmEntity
import com.spazoodle.guardian.data.local.entity.AlarmHistoryEntity
import com.spazoodle.guardian.data.local.entity.AlarmRiskCacheEntity
import com.spazoodle.guardian.data.local.entity.TriggerExecutionEntity

@Database(
    entities = [
        AlarmEntity::class,
        AlarmHistoryEntity::class,
        TriggerExecutionEntity::class,
        AlarmRiskCacheEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class GuardianDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao
    abstract fun triggerExecutionDao(): TriggerExecutionDao
    abstract fun alarmRiskCacheDao(): AlarmRiskCacheDao

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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE alarm_history ADD COLUMN scheduledAtUtcMillis INTEGER"
                )
                database.execSQL(
                    "ALTER TABLE alarm_history ADD COLUMN firedAtUtcMillis INTEGER"
                )
                database.execSQL(
                    "ALTER TABLE alarm_history ADD COLUMN delayMs INTEGER"
                )
                database.execSQL(
                    "ALTER TABLE alarm_history ADD COLUMN wasDeduped INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE alarm_history ADD COLUMN deliveryState TEXT NOT NULL DEFAULT 'FIRED'"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS trigger_executions (" +
                        "executionKey TEXT NOT NULL PRIMARY KEY, " +
                        "firstSeenAtUtcMillis INTEGER NOT NULL, " +
                        "lastSeenAtUtcMillis INTEGER NOT NULL, " +
                        "handled INTEGER NOT NULL)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_trigger_executions_firstSeenAtUtcMillis " +
                        "ON trigger_executions(firstSeenAtUtcMillis)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_trigger_executions_handled " +
                        "ON trigger_executions(handled)"
                )

                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS alarm_risk_cache (" +
                        "alarmId INTEGER NOT NULL PRIMARY KEY, " +
                        "riskLevel TEXT NOT NULL, " +
                        "reasonsCsv TEXT NOT NULL, " +
                        "updatedAtUtcMillis INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN description TEXT"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN vibrateEnabled INTEGER NOT NULL DEFAULT 1"
                )
                database.execSQL(
                    "ALTER TABLE alarms ADD COLUMN ringtoneUri TEXT"
                )
            }
        }
    }
}
