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
            "INSERT INTO categories(id, name, item_count) VALUES (1, 'zeta', 2)"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, item_count) VALUES (2, 'Alpha', 1)"
        )
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories(id, name, item_count) VALUES (3, 'bravo', 3)"
        )

        val categories = categoryDao.observeCategories().first()

        assertEquals(listOf("Alpha", "bravo", "zeta"), categories.map { it.name })
        assertEquals(listOf(1, 3, 2), categories.map { it.itemCount })
    }
}
