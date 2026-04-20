package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabaseMigrations
import org.junit.Assert.assertEquals
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

    private companion object {
        const val TEST_DB = "password-migration-test"
    }
}
