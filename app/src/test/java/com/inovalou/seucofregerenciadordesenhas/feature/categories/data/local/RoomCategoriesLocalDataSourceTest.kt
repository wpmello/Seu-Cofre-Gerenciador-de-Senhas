package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomCategoriesLocalDataSourceTest {

    @Test
    fun givenDaoFlow_whenObservingCategories_thenDelegatesToDao() = runTest {
        val expected = listOf(
            CategoryEntity(id = 1L, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 10L),
            CategoryEntity(id = 2L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 20L)
        )
        val dataSource = RoomCategoriesLocalDataSource(categoryDao = FakeCategoryDao(flowOf(expected)))

        val observed = dataSource.observeCategories().first()

        assertEquals(expected, observed)
    }

    @Test
    fun givenRawQuery_whenObservingMatchingCategories_thenEscapesLikeWildcardsAndDelegatesPatternToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(categoryDao = dao)

        dataSource.observeCategoriesMatchingQuery("50%_safe").first()

        assertEquals("%50\\%\\_safe%", dao.lastSearchPattern)
    }

    @Test
    fun givenCategory_whenInserting_thenDelegatesToDao() = runTest {
        val category = CategoryEntity(
            name = "Pessoal",
            iconKey = "ic_user_profile",
            itemCount = 0,
            lastModifiedAt = 30L
        )
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(dao)

        val insertedId = dataSource.insertCategory(category)

        assertEquals(99L, insertedId)
        assertEquals(category, dao.insertedCategory)
    }

    @Test
    fun givenPersistedCategoryId_whenGettingCategory_thenDelegatesToDao() = runTest {
        val expected = CategoryEntity(
            id = 7L,
            name = "Trabalho",
            iconKey = "ic_work_bag_add_category",
            itemCount = 4,
            lastModifiedAt = 40L
        )
        val dataSource = RoomCategoriesLocalDataSource(
            categoryDao = FakeCategoryDao(
                categoriesFlow = flowOf(emptyList()),
                categoryById = expected
            )
        )

        val category = dataSource.getCategoryById(7L)

        assertEquals(expected, category)
    }

    @Test
    fun givenCategory_whenUpdating_thenDelegatesToDao() = runTest {
        val category = CategoryEntity(
            id = 9L,
            name = "Atualizada",
            iconKey = "ic_star",
            itemCount = 1,
            lastModifiedAt = 50L
        )
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(dao)

        dataSource.updateCategory(category)

        assertEquals(category, dao.updatedCategory)
    }

    @Test
    fun givenCategoryId_whenDeleting_thenDelegatesToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(dao)

        dataSource.deleteCategoryById(12L)

        assertEquals(12L, dao.deletedCategoryId)
    }

    @Test
    fun givenCategoryIdAndTimestamp_whenTouchingCategory_thenDelegatesToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(dao)

        dataSource.updateCategoryLastModifiedAt(categoryId = 13L, lastModifiedAt = 77L)

        assertEquals(13L, dao.touchedCategoryId)
        assertEquals(77L, dao.touchedLastModifiedAt)
    }

    @Test
    fun givenDaoFailure_whenObservingCategories_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("database unavailable")
        val dataSource = RoomCategoriesLocalDataSource(
            categoryDao = FakeCategoryDao(
                flow {
                    throw expected
                }
            )
        )

        val thrown = try {
            dataSource.observeCategories().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    private class FakeCategoryDao(
        private val categoriesFlow: Flow<List<CategoryEntity>>,
        private val categoryById: CategoryEntity? = null
    ) : CategoryDao {

        var insertedCategory: CategoryEntity? = null
        var updatedCategory: CategoryEntity? = null
        var touchedCategoryId: Long? = null
        var touchedLastModifiedAt: Long? = null
        var deletedCategoryId: Long? = null
        var lastSearchPattern: String? = null

        override suspend fun insertCategory(category: CategoryEntity): Long {
            insertedCategory = category
            return 99L
        }

        override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = categoryById

        override suspend fun updateCategory(category: CategoryEntity) {
            updatedCategory = category
        }

        override suspend fun updateCategoryLastModifiedAt(categoryId: Long, lastModifiedAt: Long) {
            touchedCategoryId = categoryId
            touchedLastModifiedAt = lastModifiedAt
        }

        override suspend fun deleteCategoryById(categoryId: Long) {
            deletedCategoryId = categoryId
        }

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        override fun observeCategoriesMatchingQuery(searchPattern: String): Flow<List<CategoryEntity>> {
            lastSearchPattern = searchPattern
            return flowOf(emptyList())
        }
    }
}
