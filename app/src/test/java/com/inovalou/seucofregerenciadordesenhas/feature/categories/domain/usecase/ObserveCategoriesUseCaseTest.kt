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
            Category(id = 1L, name = "Trabalho", itemCount = 42),
            Category(id = 2L, name = "Educação", itemCount = 15)
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

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }
}
