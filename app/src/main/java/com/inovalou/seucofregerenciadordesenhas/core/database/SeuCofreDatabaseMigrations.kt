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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS passwords (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    login TEXT NOT NULL,
                    icon_key TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE passwords
                ADD COLUMN category TEXT NOT NULL DEFAULT ''
                """.trimIndent()
            )
            database.execSQL(
                """
                ALTER TABLE passwords
                ADD COLUMN encrypted_password TEXT NOT NULL DEFAULT ''
                """.trimIndent()
            )
            database.execSQL(
                """
                ALTER TABLE passwords
                ADD COLUMN password_iv TEXT NOT NULL DEFAULT ''
                """.trimIndent()
            )
            database.execSQL(
                """
                ALTER TABLE passwords
                ADD COLUMN password_cipher_version INTEGER NOT NULL DEFAULT 1
                """.trimIndent()
            )
        }
    }
}
