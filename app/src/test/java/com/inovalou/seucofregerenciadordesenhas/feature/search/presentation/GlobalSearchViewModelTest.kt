package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.search.domain.usecase.SearchVaultItemsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GlobalSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenObserved_thenStartsAwaitingQuery() {
        val viewModel = buildViewModel()

        assertTrue(viewModel.uiState.value.contentState is GlobalSearchContentState.AwaitingQuery)
    }

    @Test
    fun givenQueryChanges_whenDebounceHasNotFinished_thenDoesNotSearchImmediately() = runTest {
        val categoryRepository = FakeCategoryRepository(emptyList())
        val viewModel = buildViewModel(categoryRepository = categoryRepository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onAction(GlobalSearchAction.OnQueryChanged("ban"))
        advanceTimeBy(GlobalSearchViewModel.SEARCH_DEBOUNCE_MILLIS - 1)

        assertEquals(null, categoryRepository.lastObservedQuery)
        assertEquals("ban", viewModel.uiState.value.query)
    }

    @Test
    fun givenDebouncedQuery_whenResultsExist_thenEmitsCategoriesBeforePasswords() = runTest {
        val viewModel = buildViewModel(
            categoryRepository = FakeCategoryRepository(
                listOf(
                    Category(
                        id = 2L,
                        name = "Banco",
                        iconKey = "ic_padlock",
                        itemCount = 8,
                        lastModifiedAt = 20L
                    )
                )
            ),
            passwordRepository = FakePasswordRepository(
                listOf(
                    PasswordSearchResult(
                        id = 9L,
                        title = "Banco Digital",
                        iconKey = ""
                    )
                )
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onAction(GlobalSearchAction.OnQueryChanged("ban"))
        advanceTimeBy(GlobalSearchViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is GlobalSearchContentState.Content)
        assertEquals("ban", state.query)
        assertEquals(listOf("Banco"), state.categories.map { category -> category.name })
        assertEquals(R.drawable.ic_padlock, state.categories.single().iconResId)
        assertEquals(listOf("Banco Digital"), state.passwords.map { password -> password.title })
        assertEquals("BD", state.passwords.single().initials)
    }

    @Test
    fun givenDebouncedQuery_whenNoResults_thenEmitsEmptySearchState() = runTest {
        val viewModel = buildViewModel()
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onAction(GlobalSearchAction.OnQueryChanged("xyz"))
        advanceTimeBy(GlobalSearchViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is GlobalSearchContentState.Empty)
    }

    @Test
    fun givenSearchFailure_whenQueryIsObserved_thenEmitsErrorState() = runTest {
        val viewModel = buildViewModel(categoryRepository = FailingCategoryRepository())
        backgroundScope.launch { viewModel.uiState.collect { } }

        viewModel.onAction(GlobalSearchAction.OnQueryChanged("bank"))
        advanceTimeBy(GlobalSearchViewModel.SEARCH_DEBOUNCE_MILLIS)
        advanceUntilIdle()

        val contentState = viewModel.uiState.value.contentState
        assertTrue(contentState is GlobalSearchContentState.Error)
        contentState as GlobalSearchContentState.Error
        assertEquals(R.string.global_search_load_error, contentState.messageResId)
    }

    @Test
    fun givenCategoryClicked_whenActionHandled_thenEmitsOpenCategoryEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(GlobalSearchAction.OnCategoryClick(41L))

        assertEquals(GlobalSearchEffect.OpenCategoryDetails(41L), effect.await())
    }

    @Test
    fun givenPasswordClicked_whenActionHandled_thenEmitsOpenPasswordEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(GlobalSearchAction.OnPasswordClick(42L))

        assertEquals(GlobalSearchEffect.OpenPasswordDetails(42L), effect.await())
    }

    private fun buildViewModel(
        categoryRepository: CategoryRepository = FakeCategoryRepository(emptyList()),
        passwordRepository: PasswordRepository = FakePasswordRepository(emptyList())
    ) = GlobalSearchViewModel(
        searchVaultItemsUseCase = SearchVaultItemsUseCase(
            categoryRepository = categoryRepository,
            passwordRepository = passwordRepository
        ),
        categoryIconCatalog = FakeCategoryIconCatalog()
    )

    private class FakeCategoryRepository(
        private val categories: List<Category>
    ) : CategoryRepository {
        var lastObservedQuery: String? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 0L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()

        override fun observeCategoriesMatchingQuery(query: String): Flow<List<Category>> {
            lastObservedQuery = query
            return MutableStateFlow(categories)
        }
    }

    private class FailingCategoryRepository : CategoryRepository {
        override suspend fun createCategory(name: String, iconKey: String): Long = 0L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()

        override fun observeCategoriesMatchingQuery(query: String): Flow<List<Category>> = flow {
            throw IllegalStateException("search failure")
        }
    }

    private class FakePasswordRepository(
        private val passwords: List<PasswordSearchResult>
    ) : PasswordRepository {
        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override fun observePasswordSearchResults(query: String): Flow<List<PasswordSearchResult>> =
            MutableStateFlow(passwords)

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        override fun all(): List<CategoryIconOption> = emptyList()

        override fun resolve(iconKey: String): CategoryIconOption = when (iconKey) {
            "ic_padlock" -> CategoryIconOption(
                iconKey = iconKey,
                drawableResId = R.drawable.ic_padlock
            )

            else -> default()
        }

        override fun default(): CategoryIconOption = CategoryIconOption(
            iconKey = "ic_directory",
            drawableResId = R.drawable.ic_directory
        )
    }
}
