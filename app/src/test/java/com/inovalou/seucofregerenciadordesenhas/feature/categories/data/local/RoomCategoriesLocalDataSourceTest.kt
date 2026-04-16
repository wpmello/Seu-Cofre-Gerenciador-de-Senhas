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
            CategoryEntity(id = 1L, name = "Educação", itemCount = 15),
            CategoryEntity(id = 2L, name = "Trabalho", itemCount = 42)
        )
        val dataSource = RoomCategoriesLocalDataSource(
            categoryDao = FakeCategoryDao(flowOf(expected))
        )

        val observed = dataSource.observeCategories().first()

        assertEquals(expected, observed)
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

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow
    }
}
