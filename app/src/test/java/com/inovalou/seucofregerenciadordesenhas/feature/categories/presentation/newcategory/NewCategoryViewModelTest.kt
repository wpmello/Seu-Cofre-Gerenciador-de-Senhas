package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.CreateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewCategoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenInitialState_whenViewModelIsCreated_thenFirstIconStartsSelected() {
        val viewModel = NewCategoryViewModel(
            createCategoryUseCase = CreateCategoryUseCase(FakeCategoryRepository()),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )

        assertEquals("ic_directory", viewModel.uiState.value.selectedIconKey)
        assertEquals(12, viewModel.uiState.value.availableIcons.size)
        assertTrue(viewModel.uiState.value.availableIcons.first().isSelected)
    }

    @Test
    fun givenBlankName_whenSubmitting_thenExposesNameValidationError() = runTest {
        val viewModel = NewCategoryViewModel(
            createCategoryUseCase = CreateCategoryUseCase(FakeCategoryRepository()),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )

        viewModel.onAction(NewCategoryAction.OnNameChanged("   "))
        viewModel.onAction(NewCategoryAction.OnCreateCategoryClick)

        advanceUntilIdle()

        assertEquals(R.string.new_category_name_error_blank, viewModel.uiState.value.nameErrorResId)
    }

    @Test
    fun givenNoAvailableIcons_whenSubmitting_thenExposesIconValidationError() = runTest {
        val viewModel = NewCategoryViewModel(
            createCategoryUseCase = CreateCategoryUseCase(FakeCategoryRepository()),
            categoryIconCatalog = EmptyCategoryIconCatalog()
        )

        viewModel.onAction(NewCategoryAction.OnNameChanged("Pessoal"))
        viewModel.onAction(NewCategoryAction.OnCreateCategoryClick)

        advanceUntilIdle()

        assertEquals(R.string.new_category_icon_error_missing, viewModel.uiState.value.iconErrorResId)
    }

    @Test
    fun givenValidInput_whenSubmitting_thenEmitsNavigateBackEffect() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = NewCategoryViewModel(
            createCategoryUseCase = CreateCategoryUseCase(repository),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )
        val emittedEffect = async { viewModel.effects.first() }

        viewModel.onAction(NewCategoryAction.OnNameChanged("Trabalho"))
        viewModel.onAction(NewCategoryAction.OnIconSelected("ic_work_bag_add_category"))
        viewModel.onAction(NewCategoryAction.OnCreateCategoryClick)

        advanceUntilIdle()

        assertEquals(NewCategoryEffect.NavigateBack, emittedEffect.await())
        assertEquals("Trabalho", repository.createdName)
        assertEquals("ic_work_bag_add_category", repository.createdIconKey)
    }

    @Test
    fun givenRepositoryFailure_whenSubmitting_thenExposesSubmitError() = runTest {
        val viewModel = NewCategoryViewModel(
            createCategoryUseCase = CreateCategoryUseCase(
                FakeCategoryRepository(shouldFailOnCreate = true)
            ),
            categoryIconCatalog = FakeCategoryIconCatalog()
        )

        viewModel.onAction(NewCategoryAction.OnNameChanged("Saúde"))
        viewModel.onAction(NewCategoryAction.OnCreateCategoryClick)

        advanceUntilIdle()

        assertEquals(R.string.new_category_create_error, viewModel.uiState.value.submitErrorResId)
    }

    private class FakeCategoryRepository(
        private val shouldFailOnCreate: Boolean = false
    ) : CategoryRepository {

        var createdName: String? = null
        var createdIconKey: String? = null

        override suspend fun createCategory(name: String, iconKey: String): Long {
            if (shouldFailOnCreate) error("repository failure")
            createdName = name
            createdIconKey = iconKey
            return 1L
        }

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        private val icons = listOf(
            CategoryIconOption("ic_directory", R.drawable.ic_directory),
            CategoryIconOption("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category),
            CategoryIconOption("ic_global", R.drawable.ic_global),
            CategoryIconOption("ic_favorite", R.drawable.ic_favorite),
            CategoryIconOption("ic_home_2", R.drawable.ic_home_2),
            CategoryIconOption("ic_work_bag", R.drawable.ic_work_bag),
            CategoryIconOption("ic_home", R.drawable.ic_home),
            CategoryIconOption("ic_padlock", R.drawable.ic_padlock),
            CategoryIconOption("ic_star", R.drawable.ic_star),
            CategoryIconOption("ic_cloud", R.drawable.ic_cloud),
            CategoryIconOption("ic_user_profile", R.drawable.ic_user_profile),
            CategoryIconOption("ic_device", R.drawable.ic_device)
        )

        override fun all(): List<CategoryIconOption> = icons

        override fun resolve(iconKey: String): CategoryIconOption =
            icons.first { it.iconKey == iconKey }

        override fun default(): CategoryIconOption = icons.first()
    }

    private class EmptyCategoryIconCatalog : CategoryIconCatalog {
        override fun all(): List<CategoryIconOption> = emptyList()

        override fun resolve(iconKey: String): CategoryIconOption =
            CategoryIconOption("ic_directory", R.drawable.ic_directory)

        override fun default(): CategoryIconOption =
            CategoryIconOption("ic_directory", R.drawable.ic_directory)
    }
}
