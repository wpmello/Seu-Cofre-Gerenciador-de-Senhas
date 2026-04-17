package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var database: SeuCofreDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            SeuCofreDatabase::class.java
        ).build()
        categoryDao = database.categoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun givenNoPersistedCategories_whenObserving_thenReturnsEmptyList() = runTest {
        val categories = categoryDao.observeCategories().first()

        assertTrue(categories.isEmpty())
    }

    @Test
    fun givenPersistedCategories_whenObserving_thenReturnsNameOrderedListIgnoringCase() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (1, 'zeta', 'ic_star', 2)"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (2, 'Alpha', 'ic_directory', 1)"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (3, 'bravo', 'ic_padlock', 3)"
        )

        val categories = categoryDao.observeCategories().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), categories.map { it.name })
        assertEquals(listOf("ic_directory", "ic_padlock", "ic_star"), categories.map { it.iconKey })
        assertEquals(listOf(1, 3, 2), categories.map { it.itemCount })
    }

    @Test
    fun givenPersistedCategoryId_whenGettingById_thenReturnsMatchingCategory() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (9, 'Pessoal', 'ic_directory', 5)"
        )

        val category = categoryDao.getCategoryById(9L)

        assertEquals(9L, category?.id)
        assertEquals("Pessoal", category?.name)
        assertEquals("ic_directory", category?.iconKey)
        assertEquals(5, category?.itemCount)
    }

    @Test
    fun givenPersistedCategory_whenUpdating_thenPersistsNewNameAndIconKey() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (11, 'Trabalho', 'ic_work_bag', 3)"
        )

        categoryDao.updateCategory(
            CategoryEntity(
                id = 11L,
                name = "Corporativo",
                iconKey = "ic_directory",
                itemCount = 3
            )
        )

        val updated = categoryDao.getCategoryById(11L)

        assertEquals("Corporativo", updated?.name)
        assertEquals("ic_directory", updated?.iconKey)
        assertEquals(3, updated?.itemCount)
    }

    @Test
    fun givenPersistedCategory_whenDeleting_thenRemovesItFromDatabase() = runTest {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, icon_key, item_count) VALUES (15, 'Viagens', 'ic_global', 1)"
        )

        categoryDao.deleteCategoryById(15L)

        val deleted = categoryDao.getCategoryById(15L)

        assertEquals(null, deleted)
    }
}
