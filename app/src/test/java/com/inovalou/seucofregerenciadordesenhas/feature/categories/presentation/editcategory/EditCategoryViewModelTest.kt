package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.GetCategoryByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObservePasswordsByCategoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditCategoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenExistingCategoryId_whenViewModelLoads_thenFillsInitialStateWithPersistedValues() = runTest {
        val viewModel = buildViewModel(
            category = Category(
                id = 9L,
                name = "Trabalho",
                iconKey = "ic_work_bag_add_category",
                itemCount = 4,
                lastModifiedAt = 0L
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is EditCategoryContentState.Content)
        assertEquals("Trabalho", state.name)
        assertEquals("ic_work_bag_add_category", state.selectedIconKey)
        assertTrue(state.availableIcons.any { it.iconKey == "ic_work_bag_add_category" && it.isSelected })
    }

    @Test
    fun givenAssociatedPasswords_whenViewModelLoads_thenExposesPasswordsSectionContent() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(
                    id = 100L,
                    title = "GitHub",
                    login = "dev@empresa.com",
                    categoryId = 9L,
                    categoryName = "Trabalho"
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value.passwordsSectionState
        assertTrue(state is CategoryPasswordsSectionUiState.Content)
        state as CategoryPasswordsSectionUiState.Content
        assertEquals("GitHub", state.passwords.single().title)
        assertEquals("dev@empresa.com", state.passwords.single().supportingText)
    }

    @Test
    fun givenAssociatedPasswordWithoutLogin_whenViewModelLoads_thenKeepsSupportingTextEmpty() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                PasswordSummary(
                    id = 100L,
                    title = "GitHub",
                    login = "",
                    categoryId = 9L,
                    categoryName = "Trabalho"
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value.passwordsSectionState
        assertTrue(state is CategoryPasswordsSectionUiState.Content)
        state as CategoryPasswordsSectionUiState.Content
        assertEquals("", state.passwords.single().supportingText)
    }

    @Test
    fun givenTypedName_whenNameChanges_thenUpdatesEditableState() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnNameChanged("Corporativo"))

        assertEquals("Corporativo", viewModel.uiState.value.name)
    }

    @Test
    fun givenScreenOpenedFromCategories_whenBackIsClicked_thenNavigatesBackToCategoriesOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditCategoryOpenedFrom.Categories)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnBackClick)

        assertEquals(
            EditCategoryEffect.NavigateBackToOrigin(EditCategoryOpenedFrom.Categories),
            effect.await()
        )
    }

    @Test
    fun givenScreenOpenedFromAllCategories_whenBackIsClicked_thenNavigatesBackToAllCategoriesOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditCategoryOpenedFrom.AllCategories)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnBackClick)

        assertEquals(
            EditCategoryEffect.NavigateBackToOrigin(EditCategoryOpenedFrom.AllCategories),
            effect.await()
        )
    }

    @Test
    fun givenIconPickerOpened_whenIconSelected_thenUpdatesSelectedIconAndClosesDialog() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnEditIconClick)
        viewModel.onAction(EditCategoryAction.OnIconSelected("ic_directory"))

        val state = viewModel.uiState.value
        assertEquals("ic_directory", state.selectedIconKey)
        assertFalse(state.isIconPickerVisible)
        assertTrue(state.availableIcons.any { it.iconKey == "ic_directory" && it.isSelected })
    }

    @Test
    fun givenBlankName_whenSaving_thenExposesValidationError() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnNameChanged("   "))
        viewModel.onAction(EditCategoryAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(R.string.edit_category_name_error_blank, viewModel.uiState.value.nameErrorResId)
    }

    @Test
    fun givenValidChanges_whenSaving_thenPersistsNameAndIconAndNavigatesBack() = runTest {
        val fakeUpdateUseCase = FakeUpdateCategoryUseCase()
        val viewModel = buildViewModel(updateCategoryUseCase = fakeUpdateUseCase)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnNameChanged("Corporativo"))
        viewModel.onAction(EditCategoryAction.OnIconSelected("ic_directory"))
        viewModel.onAction(EditCategoryAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(9L, fakeUpdateUseCase.lastCategoryId)
        assertEquals("Corporativo", fakeUpdateUseCase.lastName)
        assertEquals("ic_directory", fakeUpdateUseCase.lastIconKey)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenDeleteClick_whenDialogIsCancelled_thenKeepsCategoryWithoutDeleting() = runTest {
        val fakeDeleteUseCase = FakeDeleteCategoryUseCase()
        val viewModel = buildViewModel(deleteCategoryUseCase = fakeDeleteUseCase)
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteClick)
        assertTrue(viewModel.uiState.value.isDeleteConfirmationVisible)

        viewModel.onAction(EditCategoryAction.OnDeleteDismissed)

        assertFalse(viewModel.uiState.value.isDeleteConfirmationVisible)
        assertEquals(null, fakeDeleteUseCase.deletedCategoryId)
    }

    @Test
    fun givenDeleteConfirmed_whenDeletionSucceeds_thenNavigatesBack() = runTest {
        val fakeDeleteUseCase = FakeDeleteCategoryUseCase()
        val viewModel = buildViewModel(deleteCategoryUseCase = fakeDeleteUseCase)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnDeleteClick)
        viewModel.onAction(EditCategoryAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertEquals(9L, fakeDeleteUseCase.deletedCategoryId)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenCategoryNotFound_whenViewModelLoads_thenExposesErrorState() = runTest {
        val viewModel = buildViewModel(category = null)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is EditCategoryContentState.Error)
    }

    @Test
    fun givenUpdateFailure_whenSaving_thenExposesSubmitError() = runTest {
        val viewModel = buildViewModel(
            updateCategoryUseCase = FakeUpdateCategoryUseCase(
                result = UpdateCategoryResult.Failure
            )
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(R.string.edit_category_update_error, viewModel.uiState.value.submitErrorResId)
    }

    @Test
    fun givenDeleteFailure_whenDeleteIsConfirmed_thenExposesDeleteError() = runTest {
        val viewModel = buildViewModel(
            deleteCategoryUseCase = FakeDeleteCategoryUseCase(
                result = DeleteCategoryResult.Failure
            )
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteClick)
        viewModel.onAction(EditCategoryAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertEquals(R.string.edit_category_delete_error, viewModel.uiState.value.deleteErrorResId)
        assertTrue(viewModel.uiState.value.isDeleteConfirmationVisible)
    }

    private fun buildViewModel(
        category: Category? = Category(
            id = 9L,
            name = "Trabalho",
            iconKey = "ic_work_bag_add_category",
            itemCount = 4,
            lastModifiedAt = 0L
        ),
        passwords: List<PasswordSummary> = emptyList(),
        openedFrom: EditCategoryOpenedFrom = EditCategoryOpenedFrom.Categories,
        updateCategoryUseCase: FakeUpdateCategoryUseCase = FakeUpdateCategoryUseCase(),
        deleteCategoryUseCase: FakeDeleteCategoryUseCase = FakeDeleteCategoryUseCase()
    ): EditCategoryViewModel = EditCategoryViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(
                EditCategoryRoute.categoryIdArg to 9L,
                EditCategoryRoute.openedFromArg to openedFrom.routeValue
            )
        ),
        getCategoryByIdUseCase = GetCategoryByIdUseCase(FakeCategoryLookupRepository(category)),
        observePasswordsByCategoryUseCase = ObservePasswordsByCategoryUseCase(
            FakePasswordRepository(passwords)
        ),
        updateCategoryUseCase = UpdateCategoryUseCase(updateCategoryUseCase),
        deleteCategoryUseCase = DeleteCategoryUseCase(deleteCategoryUseCase),
        categoryIconCatalog = FakeCategoryIconCatalog()
    )

    private class FakeCategoryLookupRepository(
        private val category: Category?
    ) : com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository {
        override suspend fun createCategory(name: String, iconKey: String): Long = 1L
        override suspend fun getCategoryById(categoryId: Long): Category? = category
        override suspend fun updateCategory(category: Category) = Unit
        override suspend fun touchCategory(categoryId: Long) = Unit
        override suspend fun deleteCategoryById(categoryId: Long) = Unit
        override fun observeCategories() = kotlinx.coroutines.flow.emptyFlow<List<Category>>()
    }

    private class FakeUpdateCategoryUseCase(
        private val result: UpdateCategoryResult = UpdateCategoryResult.Success
    ) : com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository {

        var lastCategoryId: Long? = null
        var lastName: String? = null
        var lastIconKey: String? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? =
            Category(id = categoryId, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 4, lastModifiedAt = 0L)

        override suspend fun updateCategory(category: Category) {
            lastCategoryId = category.id
            lastName = category.name
            lastIconKey = category.iconKey
            if (result is UpdateCategoryResult.Failure) error("update failure")
        }

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories() = kotlinx.coroutines.flow.emptyFlow<List<Category>>()
    }

    private class FakeDeleteCategoryUseCase(
        private val result: DeleteCategoryResult = DeleteCategoryResult.Success
    ) : com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository {

        var deletedCategoryId: Long? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? =
            Category(id = categoryId, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 4, lastModifiedAt = 0L)

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) {
            if (result is DeleteCategoryResult.Failure) error("delete failure")
            deletedCategoryId = categoryId
        }

        override fun observeCategories() = kotlinx.coroutines.flow.emptyFlow<List<Category>>()
    }

    private class FakeCategoryIconCatalog : CategoryIconCatalog {
        private val icons = listOf(
            CategoryIconOption("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category),
            CategoryIconOption("ic_directory", R.drawable.ic_directory),
            CategoryIconOption("ic_star", R.drawable.ic_star)
        )

        override fun all(): List<CategoryIconOption> = icons

        override fun resolve(iconKey: String): CategoryIconOption =
            icons.firstOrNull { it.iconKey == iconKey } ?: icons.first()

        override fun default(): CategoryIconOption = icons.first()
    }

    private class FakePasswordRepository(
        passwords: List<PasswordSummary>
    ) : PasswordRepository {

        private val passwordsFlow = MutableStateFlow(passwords)

        override fun observePasswords(): Flow<List<PasswordSummary>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            passwordsFlow

        override suspend fun getPasswordCount(): Int = passwordsFlow.value.size

        override suspend fun createPassword(password: com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword): Long =
            1L
    }
}
