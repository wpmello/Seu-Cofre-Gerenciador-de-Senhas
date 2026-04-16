package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenNoCategories_whenObservingState_thenEmitsEmptyState() = runTest {
        val repository = FakeCategoryRepository(emptyList())
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository)
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Empty)
    }

    @Test
    fun givenCategoriesFlow_whenObservingState_thenMapsToContentUiState() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                Category(id = 1, name = "Trabalho", itemCount = 42),
                Category(id = 2, name = "Educação", itemCount = 15)
            )
        )
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository)
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Content)
        categoriesState as CategoriesContentUiState.Content
        assertEquals(2, categoriesState.categories.size)
        assertEquals("Trabalho", categoriesState.categories.first().name)
        assertEquals(42, categoriesState.categories.first().itemCount)
    }

    @Test
    fun givenRepositoryFailure_whenObservingState_thenEmitsErrorState() = runTest {
        val repository = object : CategoryRepository {
            override fun observeCategories(): Flow<List<Category>> = flow {
                throw IllegalStateException("repository failure")
            }
        }
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository)
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Error)
    }

    private class FakeCategoryRepository(
        categories: List<Category>
    ) : CategoryRepository {

        private val categoriesFlow = MutableStateFlow(categories)

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }
}
