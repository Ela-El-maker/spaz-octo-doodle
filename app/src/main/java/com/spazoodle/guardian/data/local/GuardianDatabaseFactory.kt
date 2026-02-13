package com.spazoodle.guardian.data.local

import android.content.Context
import androidx.room.Room

object GuardianDatabaseFactory {
    private const val DB_NAME = "guardian.db"

    fun create(context: Context): GuardianDatabase {
        return Room.databaseBuilder(
            context,
            GuardianDatabase::class.java,
            DB_NAME
        )
            .addMigrations(GuardianDatabase.MIGRATION_1_2)
            .build()
    }
}
