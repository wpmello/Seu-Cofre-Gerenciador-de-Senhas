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
            CategoryEntity(id = 1L, name = "Educação", iconKey = "ic_home_2", itemCount = 15),
            CategoryEntity(id = 2L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42)
        )
        val dataSource = RoomCategoriesLocalDataSource(categoryDao = FakeCategoryDao(flowOf(expected)))

        val observed = dataSource.observeCategories().first()

        assertEquals(expected, observed)
    }

    @Test
    fun givenCategory_whenInserting_thenDelegatesToDao() = runTest {
        val category = CategoryEntity(
            name = "Pessoal",
            iconKey = "ic_user_profile",
            itemCount = 0
        )
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = RoomCategoriesLocalDataSource(dao)

        val insertedId = dataSource.insertCategory(category)

        assertEquals(99L, insertedId)
        assertEquals(category, dao.insertedCategory)
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
        private val categoriesFlow: Flow<List<CategoryEntity>>
    ) : CategoryDao {

        var insertedCategory: CategoryEntity? = null

        override suspend fun insertCategory(category: CategoryEntity): Long {
            insertedCategory = category
            return 99L
        }

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow
    }
}
