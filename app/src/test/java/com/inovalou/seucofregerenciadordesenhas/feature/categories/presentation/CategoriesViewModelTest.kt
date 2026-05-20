package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecuritySummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
        val viewModel = buildViewModel(categories = emptyList())
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Empty)
    }

    @Test
    fun givenCategoriesFlow_whenObservingState_thenMapsToContentUiState() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 10L),
                Category(id = 2, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 20L)
            )
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
    fun givenMoreThanFourCategories_whenObservingState_thenKeepsOnlyFourPreviewCards() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 10L),
                Category(id = 2, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 20L),
                Category(id = 3, name = "Saúde", iconKey = "ic_directory", itemCount = 12, lastModifiedAt = 30L),
                Category(id = 4, name = "Viagens", iconKey = "ic_directory", itemCount = 21, lastModifiedAt = 40L),
                Category(id = 5, name = "Privado", iconKey = "ic_directory", itemCount = 3, lastModifiedAt = 50L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Content)
        categoriesState as CategoriesContentUiState.Content
        assertEquals(4, categoriesState.categories.size)
        assertEquals(
            listOf("Trabalho", "Educação", "Saúde", "Viagens"),
            categoriesState.categories.map { it.name }
        )
        assertTrue(viewModel.uiState.value.shouldShowBottomViewAllButton)
    }

    @Test
    fun givenFourOrFewerCategories_whenObservingState_thenHidesBottomViewAllButton() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 10L),
                Category(id = 2, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 20L),
                Category(id = 3, name = "Saúde", iconKey = "ic_directory", itemCount = 12, lastModifiedAt = 30L),
                Category(id = 4, name = "Viagens", iconKey = "ic_directory", itemCount = 21, lastModifiedAt = 40L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Content)
        assertTrue(!viewModel.uiState.value.shouldShowBottomViewAllButton)
    }

    @Test
    fun givenUnknownIconKey_whenObservingState_thenUsesSafeFallbackIcon() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 3, name = "Legado", iconKey = "ic_unknown_legacy", itemCount = 2, lastModifiedAt = 10L)
            )
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
                Category(id = 5, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 8, lastModifiedAt = 10L)
            )
        )
        val viewModel = buildViewModel(categoryRepository = repository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        repository.emit(
            listOf(
                Category(id = 5, name = "Corporativo", iconKey = "ic_directory", itemCount = 8, lastModifiedAt = 20L)
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
                Category(id = 6, name = "Pessoal", iconKey = "ic_directory", itemCount = 1, lastModifiedAt = 10L)
            )
        )
        val viewModel = buildViewModel(categoryRepository = repository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        repository.emit(emptyList())
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.categoriesState is CategoriesContentUiState.Empty)
    }

    @Test
    fun givenRepositoryFailure_whenObservingState_thenEmitsErrorState() = runTest {
        val repository = object : CategoryRepository {
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

            override fun observeCategories(): Flow<List<Category>> = flow {
                throw IllegalStateException("repository failure")
            }
        }
        val viewModel = buildViewModel(categoryRepository = repository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val categoriesState = viewModel.uiState.value.categoriesState
        assertTrue(categoriesState is CategoriesContentUiState.Error)
    }

    @Test
    fun givenCategoriesWithDifferentLastModifiedAt_whenObservingState_thenHighlightsMostRecentlyChangedCategory() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                Category(id = 1, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 10L),
                Category(id = 7, name = "Pessoal", iconKey = "ic_directory", itemCount = 3, lastModifiedAt = 200L),
                Category(id = 2, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 20L)
            )
        )
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals(7L, viewModel.uiState.value.currentCategory?.id)
        assertEquals("Pessoal", viewModel.uiState.value.currentCategory?.name)
    }

    @Test
    fun givenVaultSecuritySummary_whenObservingState_thenMapsStatusAndTotalToSecurityCard() = runTest {
        val passwordRepository = FakePasswordRepository(
            listOf(
                PasswordSecuritySnapshot(password = "R8!kLm2@Qp7#", fingerprint = "fp-1"),
                PasswordSecuritySnapshot(password = "Ab1!cd2@", fingerprint = "fp-2")
            )
        )
        val viewModel = buildViewModel(passwordRepository = passwordRepository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.securitySummary.totalItems)
        assertEquals(R.string.categories_security_moderate, viewModel.uiState.value.securitySummary.statusResId)
        assertEquals(SecuritySummaryVisualState.Moderate, viewModel.uiState.value.securitySummary.visualState)
    }

    private fun buildViewModel(
        categories: List<Category> = emptyList(),
        categoryRepository: CategoryRepository = FakeCategoryRepository(categories),
        passwordRepository: PasswordRepository = FakePasswordRepository(emptyList())
    ) = CategoriesViewModel(
        observeCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository),
        observeVaultSecuritySummaryUseCase = ObserveVaultSecuritySummaryUseCase(
            repository = passwordRepository,
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
        ),
        categoryIconCatalog = FakeCategoryIconCatalog()
    )

    private class FakeCategoryRepository(
        categories: List<Category>
    ) : CategoryRepository {
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit


        private val categoriesFlow = MutableStateFlow(categories)

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow

        fun emit(categories: List<Category>) {
            categoriesFlow.value = categories
        }
    }

    private class FakePasswordRepository(
        snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {


        private val snapshotsFlow = MutableStateFlow(snapshots)

        override fun observePasswords(): Flow<List<PasswordSummary>> = flowOf(emptyList())

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            flowOf(emptyList())

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            snapshotsFlow

        override suspend fun getPasswordCount(): Int = snapshotsFlow.value.size

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long?): Boolean =
            false
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        override fun all(): List<CategoryIconOption> = emptyList()

        override fun resolve(iconKey: String): CategoryIconOption = when (iconKey) {
            "ic_work_bag_add_category" -> CategoryIconOption(
                iconKey = iconKey,
                drawableResId = R.drawable.ic_work_bag_add_category,
            )

            "ic_home_2" -> CategoryIconOption(
                iconKey = iconKey,
                drawableResId = R.drawable.ic_home_2,
            )

            else -> CategoryIconOption(
                iconKey = iconKey,
                drawableResId = R.drawable.ic_directory,
            )
        }

        override fun default(): CategoryIconOption = CategoryIconOption(
            iconKey = "ic_directory",
            drawableResId = R.drawable.ic_directory
        )
    }
}
