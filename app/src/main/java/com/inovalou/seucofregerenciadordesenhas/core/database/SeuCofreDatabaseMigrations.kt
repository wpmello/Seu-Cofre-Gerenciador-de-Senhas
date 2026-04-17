package com.inovalou.seucofregerenciadordesenhas.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object SeuCofreDatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE categories
                ADD COLUMN icon_key TEXT NOT NULL DEFAULT 'ic_directory'
                """.trimIndent()
            )
        }
    }
}
