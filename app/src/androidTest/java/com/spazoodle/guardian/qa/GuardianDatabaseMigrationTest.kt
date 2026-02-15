package com.spazoodle.guardian.qa

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spazoodle.guardian.data.local.GuardianDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuardianDatabaseMigrationTest {

    @Test
    fun migration3To4_addsAlarmDefaultsWithoutDataLoss() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "guardian_migration_test.db"
        context.deleteDatabase(dbName)

        val helper = openHelper(context, dbName, 3)
        helper.writableDatabase.use { db ->
            createVersion3Schema(db)
            insertVersion3AlarmRow(db)
            GuardianDatabase.MIGRATION_3_4.migrate(db)

            db.query(
                "SELECT description, vibrateEnabled, ringtoneUri, title, triggerAtUtcMillis FROM alarms WHERE id = 1"
            ).use { cursor ->
                assertEquals("Expected migrated row", 1, cursor.count)
                cursor.moveToFirst()
                assertNull(cursor.getString(0))
                assertEquals(1, cursor.getInt(1))
                assertNull(cursor.getString(2))
                assertEquals("Legacy Alarm", cursor.getString(3))
                assertEquals(1_777_777_777_000L, cursor.getLong(4))
            }
        }
        helper.close()
        context.deleteDatabase(dbName)
    }

    private fun openHelper(context: Context, dbName: String, version: Int): SupportSQLiteOpenHelper {
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onCreate(db: SupportSQLiteDatabase) = Unit

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
    }

    private fun createVersion3Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS alarms (" +
                "id INTEGER NOT NULL PRIMARY KEY, " +
                "title TEXT NOT NULL, " +
                "type TEXT NOT NULL, " +
                "triggerAtUtcMillis INTEGER NOT NULL, " +
                "timezoneIdAtCreation TEXT NOT NULL, " +
                "enabled INTEGER NOT NULL, " +
                "meetingUrl TEXT, " +
                "primaryActionType TEXT, " +
                "primaryActionValue TEXT, " +
                "primaryActionLabel TEXT, " +
                "preAlertKeysCsv TEXT NOT NULL, " +
                "preAlertOffsetsCsv TEXT NOT NULL, " +
                "nagEnabled INTEGER NOT NULL, " +
                "nagRepeatMinutesCsv TEXT NOT NULL, " +
                "nagMaxCount INTEGER NOT NULL, " +
                "nagMaxWindowMinutes INTEGER NOT NULL, " +
                "escalationEnabled INTEGER NOT NULL, " +
                "escalationStepUpAfterNagCount INTEGER NOT NULL, " +
                "escalationToneSequenceCsv TEXT NOT NULL, " +
                "snoozeDurationsCsv TEXT NOT NULL, " +
                "snoozeDefaultMinutes INTEGER NOT NULL, " +
                "createdAtUtcMillis INTEGER NOT NULL, " +
                "updatedAtUtcMillis INTEGER NOT NULL)"
        )
    }

    private fun insertVersion3AlarmRow(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT INTO alarms (" +
                "id, title, type, triggerAtUtcMillis, timezoneIdAtCreation, enabled, " +
                "meetingUrl, primaryActionType, primaryActionValue, primaryActionLabel, " +
                "preAlertKeysCsv, preAlertOffsetsCsv, nagEnabled, nagRepeatMinutesCsv, " +
                "nagMaxCount, nagMaxWindowMinutes, escalationEnabled, escalationStepUpAfterNagCount, " +
                "escalationToneSequenceCsv, snoozeDurationsCsv, snoozeDefaultMinutes, " +
                "createdAtUtcMillis, updatedAtUtcMillis" +
                ") VALUES (" +
                "1, 'Legacy Alarm', 'ALARM', 1777777777000, 'Africa/Nairobi', 1, " +
                "NULL, NULL, NULL, NULL, " +
                "'', '', 0, '', " +
                "0, 0, 0, 0, " +
                "'', '5,10,15', 10, " +
                "1700000000000, 1700000000000" +
                ")"
        )
    }
}
