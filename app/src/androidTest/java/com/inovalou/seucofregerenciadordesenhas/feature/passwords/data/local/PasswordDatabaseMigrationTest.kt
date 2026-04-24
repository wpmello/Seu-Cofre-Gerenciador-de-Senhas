package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PasswordDatabaseMigrationTest {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SeuCofreDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate2To3_addsPasswordsTable() {
        migrationTestHelper.createDatabase(TEST_DB, 2).apply {
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            3,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_2_3
        )
        migratedDb.execSQL(
            "INSERT INTO passwords(id, title, login, icon_key) VALUES (1, 'Netflix', 'joao@email.com', 'ic_home')"
        )

        val cursor = migratedDb.query(
            "SELECT title, login, icon_key FROM passwords WHERE id = 1"
        )
        cursor.moveToFirst()

        assertEquals("Netflix", cursor.getString(0))
        assertEquals("joao@email.com", cursor.getString(1))
        assertEquals("ic_home", cursor.getString(2))

        cursor.close()
    }

    @Test
    fun migrate3To4_addsEncryptedPasswordColumns() {
        migrationTestHelper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO passwords(id, title, login, icon_key) VALUES (1, 'Netflix', 'joao@email.com', 'ic_home')"
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            4,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_3_4
        )

        val cursor = migratedDb.query(
            """
            SELECT title, login, category, encrypted_password, password_iv, password_cipher_version, icon_key
            FROM passwords
            WHERE id = 1
            """.trimIndent()
        )
        cursor.moveToFirst()

        assertEquals("Netflix", cursor.getString(0))
        assertEquals("joao@email.com", cursor.getString(1))
        assertEquals("", cursor.getString(2))
        assertEquals("", cursor.getString(3))
        assertEquals("", cursor.getString(4))
        assertEquals(1, cursor.getInt(5))
        assertEquals("ic_home", cursor.getString(6))

        cursor.close()
    }

    @Test
    fun migrate4To5_addsNullableCategoryReferenceWithoutBreakingLegacyPasswords() {
        migrationTestHelper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                INSERT INTO passwords(
                    id, title, login, category, encrypted_password, password_iv, password_cipher_version, icon_key
                ) VALUES (1, 'Netflix', 'joao@email.com', 'Legacy', 'cipher', 'iv', 1, 'ic_home')
                """.trimIndent()
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            5,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_4_5
        )

        val cursor = migratedDb.query(
            """
            SELECT title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key
            FROM passwords
            WHERE id = 1
            """.trimIndent()
        )
        cursor.moveToFirst()

        assertEquals("Netflix", cursor.getString(0))
        assertEquals("joao@email.com", cursor.getString(1))
        assertEquals("Legacy", cursor.getString(2))
        assertEquals(true, cursor.isNull(3))
        assertEquals("cipher", cursor.getString(4))
        assertEquals("iv", cursor.getString(5))
        assertEquals(1, cursor.getInt(6))
        assertEquals("ic_home", cursor.getString(7))

        cursor.close()
    }

    @Test
    fun migrate6To7_addsCreatedAtAndUpdatedAtAndBackfillsLegacyPasswords() {
        migrationTestHelper.createDatabase(TEST_DB, 6).apply {
            execSQL(
                """
                INSERT INTO passwords(
                    id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key
                ) VALUES (1, 'Netflix', 'joao@email.com', 'Legacy', NULL, 'cipher', 'iv', 1, 'ic_home')
                """.trimIndent()
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            7,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_6_7
        )

        val cursor = migratedDb.query(
            """
            SELECT created_at, updated_at
            FROM passwords
            WHERE id = 1
            """.trimIndent()
        )
        cursor.moveToFirst()

        val createdAt = cursor.getLong(0)
        val updatedAt = cursor.getLong(1)

        assertTrue(createdAt > 0L)
        assertTrue(updatedAt > 0L)
        assertEquals(createdAt, updatedAt)

        cursor.close()
    }

    @Test
    fun migrate7To8_addsOptionalPlainTextNoteColumnWithoutBreakingExistingPasswords() {
        migrationTestHelper.createDatabase(TEST_DB, 7).apply {
            execSQL(
                """
                INSERT INTO passwords(
                    id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at
                ) VALUES (1, 'Netflix', 'joao@email.com', 'Legacy', NULL, 'cipher', 'iv', 1, 'ic_home', 100, 200)
                """.trimIndent()
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            8,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_7_8
        )

        migratedDb.execSQL(
            "UPDATE passwords SET note = 'Cobrança no cartão final 1234' WHERE id = 1"
        )

        val cursor = migratedDb.query(
            """
            SELECT note, created_at, updated_at
            FROM passwords
            WHERE id = 1
            """.trimIndent()
        )
        cursor.moveToFirst()

        assertEquals("Cobrança no cartão final 1234", cursor.getString(0))
        assertEquals(100L, cursor.getLong(1))
        assertEquals(200L, cursor.getLong(2))

        cursor.close()
    }

    @Test
    fun migrate8To9_addsNullablePasswordFingerprintColumnWithoutBreakingExistingPasswords() {
        migrationTestHelper.createDatabase(TEST_DB, 8).apply {
            execSQL(
                """
                INSERT INTO passwords(
                    id, title, login, category, category_id, encrypted_password, password_iv, password_cipher_version, icon_key, created_at, updated_at, note
                ) VALUES (1, 'Netflix', 'joao@email.com', 'Legacy', NULL, 'cipher', 'iv', 1, 'ic_home', 100, 200, NULL)
                """.trimIndent()
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            9,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_8_9
        )

        migratedDb.execSQL(
            "UPDATE passwords SET password_fingerprint = 'fp-1' WHERE id = 1"
        )

        val cursor = migratedDb.query(
            """
            SELECT password_fingerprint, created_at, updated_at
            FROM passwords
            WHERE id = 1
            """.trimIndent()
        )
        cursor.moveToFirst()

        assertEquals("fp-1", cursor.getString(0))
        assertEquals(100L, cursor.getLong(1))
        assertEquals(200L, cursor.getLong(2))

        cursor.close()
    }

    private companion object {
        const val TEST_DB = "password-migration-test"
    }
}
