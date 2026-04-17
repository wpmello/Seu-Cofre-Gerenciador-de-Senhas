package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
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
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Empty)
    }

    @Test
    fun givenCategoriesFlow_whenObservingState_thenMapsToContentUiState() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                Category(id = 1, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42),
                Category(id = 2, name = "Educação", iconKey = "ic_home_2", itemCount = 15)
            )
        )
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Content)
        categoriesState as CategoriesContentUiState.Content
        assertEquals(2, categoriesState.categories.size)
        assertEquals("Trabalho", categoriesState.categories.first().name)
        assertEquals("ic_work_bag_add_category", categoriesState.categories.first().iconKey)
        assertEquals(R.drawable.ic_work_bag_add_category, categoriesState.categories.first().iconResId)
        assertEquals(42, categoriesState.categories.first().itemCount)
    }

    @Test
    fun givenUnknownIconKey_whenObservingState_thenUsesSafeFallbackIcon() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                Category(id = 3, name = "Legado", iconKey = "ic_unknown_legacy", itemCount = 2)
            )
        )
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Content)
        categoriesState as CategoriesContentUiState.Content
        assertEquals("ic_unknown_legacy", categoriesState.categories.first().iconKey)
        assertEquals(R.drawable.ic_directory, categoriesState.categories.first().iconResId)
    }

    @Test
    fun givenCategoriesAreUpdatedAfterEdit_whenObservingState_thenListReflectsNewNameAndIcon() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                Category(id = 5, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 8)
            )
        )
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        repository.emit(
            listOf(
                Category(id = 5, name = "Corporativo", iconKey = "ic_directory", itemCount = 8)
            )
        )
        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Content)
        categoriesState as CategoriesContentUiState.Content
        assertEquals("Corporativo", categoriesState.categories.single().name)
        assertEquals("ic_directory", categoriesState.categories.single().iconKey)
        assertEquals(R.drawable.ic_directory, categoriesState.categories.single().iconResId)
    }

    @Test
    fun givenCategoriesBecomeEmptyAfterDelete_whenObservingState_thenListReflectsEmptyState() = runTest {
        val repository = FakeCategoryRepository(
            listOf(
                Category(id = 6, name = "Pessoal", iconKey = "ic_directory", itemCount = 1)
            )
        )
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        repository.emit(emptyList())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Empty)
    }

    @Test
    fun givenRepositoryFailure_whenObservingState_thenEmitsErrorState() = runTest {
        val repository = object : CategoryRepository {
            override suspend fun createCategory(name: String, iconKey: String): Long = 1L

            override suspend fun getCategoryById(categoryId: Long): Category? = null

            override suspend fun updateCategory(category: Category) = Unit

            override suspend fun deleteCategoryById(categoryId: Long) = Unit

            override fun observeCategories(): Flow<List<Category>> = flow {
                throw IllegalStateException("repository failure")
            }
        }
        val viewModel = CategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
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

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow

        fun emit(categories: List<Category>) {
            categoriesFlow.value = categories
        }
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        private val icons = listOf(
            CategoryIconOption("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category),
            CategoryIconOption("ic_home_2", R.drawable.ic_home_2),
            CategoryIconOption("ic_directory", R.drawable.ic_directory)
        )

        override fun all(): List<CategoryIconOption> = icons

        override fun resolve(iconKey: String): CategoryIconOption =
            icons.firstOrNull { icon -> icon.iconKey == iconKey } ?: icons.last()

        override fun default(): CategoryIconOption = icons.first()
    }
}
