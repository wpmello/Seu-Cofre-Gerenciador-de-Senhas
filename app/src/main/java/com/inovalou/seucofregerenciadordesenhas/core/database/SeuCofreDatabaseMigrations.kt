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

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS passwords_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    login TEXT NOT NULL,
                    category TEXT NOT NULL,
                    category_id INTEGER,
                    encrypted_password TEXT NOT NULL,
                    password_iv TEXT NOT NULL,
                    password_cipher_version INTEGER NOT NULL,
                    icon_key TEXT NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES categories(id) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO passwords_new(
                    id,
                    title,
                    login,
                    category,
                    category_id,
                    encrypted_password,
                    password_iv,
                    password_cipher_version,
                    icon_key
                )
                SELECT
                    id,
                    title,
                    login,
                    category,
                    NULL,
                    encrypted_password,
                    password_iv,
                    password_cipher_version,
                    icon_key
                FROM passwords
                """.trimIndent()
            )
            database.execSQL("DROP TABLE passwords")
            database.execSQL("ALTER TABLE passwords_new RENAME TO passwords")
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_passwords_category_id ON passwords(category_id)"
            )
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                ALTER TABLE categories
                ADD COLUMN last_modified_at INTEGER NOT NULL DEFAULT 0
                """.trimIndent()
            )
        }
    }
}
