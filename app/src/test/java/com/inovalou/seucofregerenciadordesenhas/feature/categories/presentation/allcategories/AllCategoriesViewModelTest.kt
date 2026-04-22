package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AllCategoriesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenStateIsObserved_thenStartsInLoadingState() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.contentState is AllCategoriesContentState.Loading)
    }

    @Test
    fun givenNoPersistedCategories_whenObservingState_thenShowsEmptyCategoriesState() = runTest {
        val viewModel = buildViewModel(categories = emptyList())
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is AllCategoriesContentState.EmptyCategories)
        assertTrue(state.allCategories.isEmpty())
        assertTrue(state.filteredCategories.isEmpty())
    }

    @Test
    fun givenPersistedCategories_whenObservingState_thenShowsFullListFromRepository() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 0L),
                Category(id = 2L, name = "Bancos", iconKey = "ic_directory", itemCount = 8, lastModifiedAt = 0L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is AllCategoriesContentState.Content)
        assertEquals(2, state.allCategories.size)
        assertEquals(2, state.filteredCategories.size)
        assertEquals("Trabalho", state.filteredCategories.first().name)
    }

    @Test
    fun givenSearchQueryMatchesCategories_whenQueryChanges_thenFiltersListIgnoringCase() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 0L),
                Category(id = 2L, name = "Bancos", iconKey = "ic_directory", itemCount = 8, lastModifiedAt = 0L),
                Category(id = 3L, name = "Compras", iconKey = "ic_home", itemCount = 24, lastModifiedAt = 0L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(AllCategoriesAction.OnSearchQueryChanged("comp"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("comp", state.query)
        assertEquals(listOf("Compras"), state.filteredCategories.map { it.name })
    }

    @Test
    fun givenSearchQueryCleared_whenQueryBecomesBlank_thenRestoresFullList() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1L, name = "Social", iconKey = "ic_global", itemCount = 18, lastModifiedAt = 0L),
                Category(id = 2L, name = "Privado", iconKey = "ic_padlock", itemCount = 3, lastModifiedAt = 0L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(AllCategoriesAction.OnSearchQueryChanged("pri"))
        advanceUntilIdle()
        viewModel.onAction(AllCategoriesAction.OnSearchQueryChanged(""))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is AllCategoriesContentState.Content)
        assertEquals(2, state.filteredCategories.size)
    }

    @Test
    fun givenSearchQueryHasNoMatches_whenQueryChanges_thenShowsSearchEmptyState() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1L, name = "Saude", iconKey = "ic_favorite", itemCount = 5, lastModifiedAt = 0L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(AllCategoriesAction.OnSearchQueryChanged("xyz"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is AllCategoriesContentState.EmptySearchResult)
        assertTrue(state.filteredCategories.isEmpty())
    }

    @Test
    fun givenUnknownIconKey_whenObservingState_thenUsesSafeFallbackIcon() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 7L, name = "Legado", iconKey = "ic_unknown_legacy", itemCount = 2, lastModifiedAt = 0L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals(R.drawable.ic_directory, viewModel.uiState.value.filteredCategories.single().iconResId)
    }

    @Test
    fun givenCategoryCardClicked_whenActionIsHandled_thenEmitsNavigationEffectWithAllCategoriesOrigin() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(AllCategoriesAction.OnCategoryClick(categoryId = 25L))

        assertEquals(
            AllCategoriesEffect.NavigateToEditCategory(
                categoryId = 25L,
                openedFrom = EditCategoryOpenedFrom.AllCategories
            ),
            effect.await()
        )
    }

    @Test
    fun givenRepositoryFails_whenObservingState_thenShowsErrorState() = runTest {
        val repository = object : CategoryRepository {
            override suspend fun createCategory(name: String, iconKey: String): Long = 1L

            override suspend fun getCategoryById(categoryId: Long): Category? = null

            override suspend fun updateCategory(category: Category) = Unit

            override suspend fun touchCategory(categoryId: Long) = Unit

            override suspend fun deleteCategoryById(categoryId: Long) = Unit

            override fun observeCategories(): Flow<List<Category>> = flow {
                throw IllegalStateException("repository failure")
            }
        }
        val viewModel = AllCategoriesViewModel(
            observeCategoriesUseCase = ObserveCategoriesUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is AllCategoriesContentState.Error)
    }

    private fun buildViewModel(
        categories: List<Category> = listOf(
            Category(id = 1L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 0L)
        )
    ): AllCategoriesViewModel = AllCategoriesViewModel(
        observeCategoriesUseCase = ObserveCategoriesUseCase(FakeCategoryRepository(categories)),
        categoryIconCatalog = FakeCategoryIconCatalog()
    )

    private class FakeCategoryRepository(
        categories: List<Category>
    ) : CategoryRepository {

        private val categoriesFlow = MutableStateFlow(categories)

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        private val icons = listOf(
            CategoryIconOption("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category),
            CategoryIconOption("ic_directory", R.drawable.ic_directory),
            CategoryIconOption("ic_home", R.drawable.ic_home),
            CategoryIconOption("ic_global", R.drawable.ic_global),
            CategoryIconOption("ic_padlock", R.drawable.ic_padlock),
            CategoryIconOption("ic_favorite", R.drawable.ic_favorite)
        )

        override fun all(): List<CategoryIconOption> = icons

        override fun resolve(iconKey: String): CategoryIconOption =
            icons.firstOrNull { icon -> icon.iconKey == iconKey } ?: icons[1]

        override fun default(): CategoryIconOption = icons.first()
    }
}
