package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveCategoriesUseCaseTest {

    @Test
    fun givenRepositoryFlow_whenInvokingUseCase_thenReturnsRepositoryEmission() = runTest {
        val expected = listOf(
            Category(id = 1L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 0L),
            Category(id = 2L, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 0L)
        )
        val useCase = ObserveCategoriesUseCase(
            categoryRepository = FakeCategoryRepository(flowOf(expected))
        )

        val observed = useCase().first()

        assertEquals(expected, observed)
    }

    @Test
    fun givenRepositoryFailure_whenInvokingUseCase_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("repository failure")
        val useCase = ObserveCategoriesUseCase(
            categoryRepository = FakeCategoryRepository(
                flow {
                    throw expected
                }
            )
        )

        val thrown = try {
            useCase().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    private class FakeCategoryRepository(
        private val categoriesFlow: Flow<List<Category>>
    ) : CategoryRepository {
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit


        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }
}
