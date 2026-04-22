package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabaseMigrations
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategoryDatabaseMigrationTest {

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SeuCofreDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2_addsIconKeyColumnWithDefaultValue() {
        migrationTestHelper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO categories(id, name, item_count) VALUES (1, 'Legacy', 4)"
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            2,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_1_2
        )

        val cursor = migratedDb.query(
            "SELECT name, icon_key, item_count FROM categories WHERE id = 1"
        )
        cursor.moveToFirst()

        assertEquals("Legacy", cursor.getString(0))
        assertEquals("ic_directory", cursor.getString(1))
        assertEquals(4, cursor.getInt(2))

        cursor.close()
    }

    @Test
    fun migrate5To6_addsLastModifiedAtColumnWithDefaultValue() {
        migrationTestHelper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                INSERT INTO categories(id, name, icon_key, item_count)
                VALUES (1, 'Legacy', 'ic_directory', 4)
                """.trimIndent()
            )
            close()
        }

        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            TEST_DB,
            6,
            true,
            SeuCofreDatabaseMigrations.MIGRATION_5_6
        )

        val cursor = migratedDb.query(
            "SELECT name, icon_key, item_count, last_modified_at FROM categories WHERE id = 1"
        )
        cursor.moveToFirst()

        assertEquals("Legacy", cursor.getString(0))
        assertEquals("ic_directory", cursor.getString(1))
        assertEquals(4, cursor.getInt(2))
        assertEquals(0L, cursor.getLong(3))

        cursor.close()
    }

    private companion object {
        const val TEST_DB = "category-migration-test"
    }
}
