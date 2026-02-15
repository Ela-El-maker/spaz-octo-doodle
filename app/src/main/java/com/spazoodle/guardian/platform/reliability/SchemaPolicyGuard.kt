package com.spazoodle.guardian.platform.reliability

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

class SchemaPolicyGuard(
    private val context: Context,
    private val dbName: String = "guardian.db",
    private val expectedVersion: Int = 4
) {
    fun check() {
        val dbFile: File = context.getDatabasePath(dbName)
        if (!dbFile.exists()) return
        val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
        db.use {
            val current = it.version
            require(current <= expectedVersion) {
                "Schema version $current is newer than app supports ($expectedVersion)."
            }
        }
    }
}
