package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryWithAssociatedPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.GetCategoryByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.TransferPasswordsToCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconOption
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObservePasswordsByCategoryUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
            currentCategory = category(id = 9L, name = "Trabalho", iconKey = "ic_work_bag_add_category")
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is EditCategoryContentState.Content)
        assertEquals("Trabalho", state.name)
        assertEquals("ic_work_bag_add_category", state.selectedIconKey)
        assertTrue(state.availableIcons.any { it.iconKey == "ic_work_bag_add_category" && it.isSelected })
    }

    @Test
    fun givenAssociatedPasswords_whenViewModelLoads_thenExposesPasswordsSectionContentAndCount() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(
                passwordSummary(id = 100L, title = "GitHub", login = "dev@empresa.com"),
                passwordSummary(id = 101L, title = "Banco", login = "conta@banco.com")
            ),
            passwordSecuritySnapshots = listOf(
                PasswordSecuritySnapshot(
                    passwordId = 100L,
                    password = "Qr7!Lp2@Mz9#",
                    fingerprint = "github"
                ),
                PasswordSecuritySnapshot(
                    passwordId = 101L,
                    password = "VeryStrongCredential!2026",
                    fingerprint = "bank"
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.associatedPasswordsCount)
        assertTrue(state.passwordsSectionState is CategoryPasswordsSectionUiState.Content)
        val passwords = (state.passwordsSectionState as CategoryPasswordsSectionUiState.Content).passwords
        assertEquals("GitHub", passwords.first().title)
        assertEquals("dev@empresa.com", passwords.first().supportingText)
        assertEquals(
            listOf(
                CategoryPasswordItemSecurityLevel.Moderate,
                CategoryPasswordItemSecurityLevel.Safe
            ),
            passwords.map { password -> password.securityLevel }
        )
    }

    @Test
    fun givenAssociatedPasswordWithoutLogin_whenViewModelLoads_thenKeepsSupportingTextEmpty() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(passwordSummary(id = 100L, title = "GitHub", login = ""))
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value.passwordsSectionState
        assertTrue(state is CategoryPasswordsSectionUiState.Content)
        assertEquals("", (state as CategoryPasswordsSectionUiState.Content).passwords.single().supportingText)
    }

    @Test
    fun givenAssociatedPasswordClick_whenHandled_thenEmitsOpenPasswordEffect() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnPasswordClick(100L))

        assertEquals(EditCategoryEffect.OpenPassword(100L), effect.await())
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
        val repository = FakeCategoryRepository()
        val viewModel = buildViewModel(categoryRepository = repository)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnNameChanged("Corporativo"))
        viewModel.onAction(EditCategoryAction.OnIconSelected("ic_directory"))
        viewModel.onAction(EditCategoryAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(9L, repository.updatedCategory?.id)
        assertEquals("Corporativo", repository.updatedCategory?.name)
        assertEquals("ic_directory", repository.updatedCategory?.iconKey)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenSaveAlreadyInProgress_whenSaveClickedAgain_thenUpdatesCategoryOnlyOnce() = runTest {
        val updateGate = CompletableDeferred<Unit>()
        val repository = FakeCategoryRepository(updateGate = updateGate)
        val viewModel = buildViewModel(categoryRepository = repository)
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnNameChanged("Corporativo"))
        viewModel.onAction(EditCategoryAction.OnSaveClick)
        advanceUntilIdle()
        viewModel.onAction(EditCategoryAction.OnSaveClick)

        assertTrue(viewModel.uiState.value.isSaving)
        assertEquals(1, repository.updateCalls)

        updateGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenCategoryWithoutPasswords_whenDeleteButtonClick_thenShowsSimpleDeleteConfirmation() = runTest {
        val viewModel = buildViewModel(passwords = emptyList())
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)

        assertEquals(
            EditCategoryDeleteFlowState.SimpleDeleteConfirmation,
            viewModel.uiState.value.deleteFlowState
        )
    }

    @Test
    fun givenSimpleDeleteDialog_whenCancelled_thenKeepsCategoryWithoutDeleting() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = buildViewModel(categoryRepository = repository, passwords = emptyList())
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnDeleteDialogDismissed)

        assertEquals(EditCategoryDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertEquals(null, repository.deletedCategoryId)
    }

    @Test
    fun givenCategoryWithoutPasswords_whenSimpleDeleteIsConfirmed_thenDeletesCategoryAndNavigates() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = buildViewModel(categoryRepository = repository, passwords = emptyList())
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnSimpleDeleteConfirmed)
        advanceUntilIdle()

        assertEquals(9L, repository.deletedCategoryId)
        assertEquals(null, repository.deletedCategoryWithAssociatedPasswordsId)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenPasswordsAppearBeforeSimpleDeleteConfirmation_whenConfirmed_thenShowsAssociatedPasswordChoice() = runTest {
        val repository = FakeCategoryRepository()
        val passwordRepository = FakePasswordRepository(passwords = emptyList(), snapshots = emptyList())
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwordRepository = passwordRepository
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        passwordRepository.emitPasswords(listOf(passwordSummary(id = 1L)))
        advanceUntilIdle()
        viewModel.onAction(EditCategoryAction.OnSimpleDeleteConfirmed)

        val state = viewModel.uiState.value.deleteFlowState
        assertTrue(state is EditCategoryDeleteFlowState.AssociatedPasswordsChoice)
        assertEquals(1, (state as EditCategoryDeleteFlowState.AssociatedPasswordsChoice).passwordCount)
        assertEquals(null, repository.deletedCategoryId)
    }

    @Test
    fun givenCategoryWithPasswords_whenDeleteButtonClick_thenShowsAssociatedPasswordsChoice() = runTest {
        val viewModel = buildViewModel(
            passwords = listOf(passwordSummary(id = 1L), passwordSummary(id = 2L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)

        val state = viewModel.uiState.value.deleteFlowState
        assertTrue(state is EditCategoryDeleteFlowState.AssociatedPasswordsChoice)
        assertEquals(2, (state as EditCategoryDeleteFlowState.AssociatedPasswordsChoice).passwordCount)
    }

    @Test
    fun givenDeleteAllSelected_whenCancelled_thenReturnsToAssociatedPasswordsChoice() = runTest {
        val viewModel = buildViewModel(passwords = listOf(passwordSummary(id = 1L)))
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnDeleteAllSelected)
        viewModel.onAction(EditCategoryAction.OnDeleteAllCancelled)

        assertTrue(viewModel.uiState.value.deleteFlowState is EditCategoryDeleteFlowState.AssociatedPasswordsChoice)
    }

    @Test
    fun givenDeleteAllConfirmed_whenOperationSucceeds_thenDeletesPasswordsAndCategoryTransactionallyAndNavigates() = runTest {
        val repository = FakeCategoryRepository()
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnDeleteAllSelected)
        viewModel.onAction(EditCategoryAction.OnDeleteAllConfirmed)
        advanceUntilIdle()

        assertEquals(9L, repository.deletedCategoryWithAssociatedPasswordsId)
        assertEquals(null, repository.deletedCategoryId)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenTransferSelected_whenSelectionOpens_thenCurrentCategoryIsNotAvailableAsDestination() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(
                category(id = 9L, name = "Trabalho"),
                category(id = 10L, name = "Pessoal")
            ),
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)

        val state = viewModel.uiState.value.deleteFlowState
        assertTrue(state is EditCategoryDeleteFlowState.TransferSelection)
        val transferSelection = state as EditCategoryDeleteFlowState.TransferSelection
        assertEquals(listOf(10L), transferSelection.categories.map { it.id })
        assertEquals(null, transferSelection.selectedCategoryId)
    }

    @Test
    fun givenNoTransferDestination_whenSelectionOpens_thenExposesEmptyDestinationList() = runTest {
        val viewModel = buildViewModel(
            categories = listOf(category(id = 9L, name = "Trabalho")),
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)

        val state = viewModel.uiState.value.deleteFlowState
        assertTrue(state is EditCategoryDeleteFlowState.TransferSelection)
        assertTrue((state as EditCategoryDeleteFlowState.TransferSelection).categories.isEmpty())
        assertEquals(null, state.selectedCategoryId)
    }

    @Test
    fun givenDestinationSelected_whenTransferConfirmed_thenTransfersInBatchAndAsksWhetherToDeleteOriginalCategory() = runTest {
        val repository = FakeCategoryRepository(
            categories = listOf(
                category(id = 9L, name = "Trabalho"),
                category(id = 10L, name = "Pessoal")
            )
        )
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)
        viewModel.onAction(EditCategoryAction.OnTransferCategorySelected(10L))
        viewModel.onAction(EditCategoryAction.OnTransferConfirmed)
        advanceUntilIdle()

        assertEquals(9L to 10L, repository.transfers.single())
        assertEquals(
            EditCategoryDeleteFlowState.PostTransferDeleteConfirmation,
            viewModel.uiState.value.deleteFlowState
        )
    }

    @Test
    fun givenTransferCompleted_whenPostTransferDeleteIsCancelled_thenKeepsOriginalCategory() = runTest {
        val repository = FakeCategoryRepository(
            categories = listOf(
                category(id = 9L, name = "Trabalho"),
                category(id = 10L, name = "Pessoal")
            )
        )
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)
        viewModel.onAction(EditCategoryAction.OnTransferCategorySelected(10L))
        viewModel.onAction(EditCategoryAction.OnTransferConfirmed)
        advanceUntilIdle()
        viewModel.onAction(EditCategoryAction.OnPostTransferDeleteCancelled)

        assertEquals(EditCategoryDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertEquals(null, repository.deletedCategoryId)
        assertEquals(9L to 10L, repository.transfers.single())
    }

    @Test
    fun givenTransferCompleted_whenPostTransferDeleteIsConfirmed_thenDeletesOriginalCategoryAndNavigates() = runTest {
        val repository = FakeCategoryRepository(
            categories = listOf(
                category(id = 9L, name = "Trabalho"),
                category(id = 10L, name = "Pessoal")
            )
        )
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)
        viewModel.onAction(EditCategoryAction.OnTransferCategorySelected(10L))
        viewModel.onAction(EditCategoryAction.OnTransferConfirmed)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditCategoryAction.OnPostTransferDeleteConfirmed)
        advanceUntilIdle()

        assertEquals(9L, repository.deletedCategoryId)
        assertEquals(EditCategoryEffect.NavigateToCategories, effect.await())
    }

    @Test
    fun givenCriticalOperationInProgress_whenConfirmIsClickedAgain_thenDoesNotDuplicateTransfer() = runTest {
        val transferGate = CompletableDeferred<Unit>()
        val repository = FakeCategoryRepository(
            categories = listOf(
                category(id = 9L, name = "Trabalho"),
                category(id = 10L, name = "Pessoal")
            ),
            transferGate = transferGate
        )
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnTransferSelected)
        viewModel.onAction(EditCategoryAction.OnTransferCategorySelected(10L))
        viewModel.onAction(EditCategoryAction.OnTransferConfirmed)
        advanceUntilIdle()
        viewModel.onAction(EditCategoryAction.OnTransferConfirmed)

        assertTrue(viewModel.uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation)
        assertEquals(1, repository.transferCalls)

        transferGate.complete(Unit)
        advanceUntilIdle()

        assertEquals(
            EditCategoryDeleteFlowState.PostTransferDeleteConfirmation,
            viewModel.uiState.value.deleteFlowState
        )
    }

    @Test
    fun givenRepositoryFailure_whenDeleteAllIsConfirmed_thenExposesErrorAndDoesNotNavigate() = runTest {
        val repository = FakeCategoryRepository(failDeleteWithAssociatedPasswords = true)
        val viewModel = buildViewModel(
            categoryRepository = repository,
            passwords = listOf(passwordSummary(id = 1L))
        )
        advanceUntilIdle()

        viewModel.onAction(EditCategoryAction.OnDeleteButtonClick)
        viewModel.onAction(EditCategoryAction.OnDeleteAllSelected)
        viewModel.onAction(EditCategoryAction.OnDeleteAllConfirmed)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.deleteFlowState is EditCategoryDeleteFlowState.Error)
        assertEquals(R.string.edit_category_delete_error, state.operationErrorResId)
    }

    @Test
    fun givenCategoryNotFound_whenViewModelLoads_thenExposesErrorState() = runTest {
        val viewModel = buildViewModel(currentCategory = null, categories = emptyList())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is EditCategoryContentState.Error)
    }

    private fun buildViewModel(
        currentCategory: Category? = category(id = 9L, name = "Trabalho", iconKey = "ic_work_bag_add_category"),
        categories: List<Category> = listOf(
            category(id = 9L, name = "Trabalho", iconKey = "ic_work_bag_add_category"),
            category(id = 10L, name = "Pessoal", iconKey = "ic_directory")
        ),
        passwords: List<PasswordSummary> = emptyList(),
        passwordSecuritySnapshots: List<PasswordSecuritySnapshot> = emptyList(),
        openedFrom: EditCategoryOpenedFrom = EditCategoryOpenedFrom.Categories,
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(
            currentCategory = currentCategory,
            categories = categories
        ),
        passwordRepository: FakePasswordRepository = FakePasswordRepository(
            passwords = passwords,
            snapshots = passwordSecuritySnapshots
        )
    ): EditCategoryViewModel {
        return EditCategoryViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    EditCategoryRoute.categoryIdArg to 9L,
                    EditCategoryRoute.openedFromArg to openedFrom.routeValue
                )
            ),
            getCategoryByIdUseCase = GetCategoryByIdUseCase(categoryRepository),
            observePasswordsByCategoryUseCase = ObservePasswordsByCategoryUseCase(
                repository = passwordRepository,
                evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
            ),
            updateCategoryUseCase = UpdateCategoryUseCase(categoryRepository),
            deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository),
            deleteCategoryWithAssociatedPasswordsUseCase = DeleteCategoryWithAssociatedPasswordsUseCase(
                categoryRepository
            ),
            transferPasswordsToCategoryUseCase = TransferPasswordsToCategoryUseCase(categoryRepository),
            categoryIconCatalog = FakeCategoryIconCatalog(),
            observeCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository)
        )
    }

    private class FakeCategoryRepository(
        currentCategory: Category? = category(id = 9L, name = "Trabalho", iconKey = "ic_work_bag_add_category"),
        categories: List<Category> = listOf(
            category(id = 9L, name = "Trabalho", iconKey = "ic_work_bag_add_category"),
            category(id = 10L, name = "Pessoal", iconKey = "ic_directory")
        ),
        private val failDeleteWithAssociatedPasswords: Boolean = false,
        private val failSimpleDelete: Boolean = false,
        private val transferGate: CompletableDeferred<Unit>? = null,
        private val updateGate: CompletableDeferred<Unit>? = null
    ) : CategoryRepository {

        private val categoryById = (categories + listOfNotNull(currentCategory)).associateBy { it.id }
        private val categoriesFlow = MutableStateFlow(categories)
        var updatedCategory: Category? = null
        var deletedCategoryId: Long? = null
        var deletedCategoryWithAssociatedPasswordsId: Long? = null
        val transfers = mutableListOf<Pair<Long, Long>>()
        var transferCalls: Int = 0
        var updateCalls: Int = 0

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? = categoryById[categoryId]

        override suspend fun updateCategory(category: Category) {
            updateCalls += 1
            updateGate?.await()
            updatedCategory = category
        }

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) {
            if (failSimpleDelete) error("simple delete failure")
            deletedCategoryId = categoryId
        }

        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) {
            if (failDeleteWithAssociatedPasswords) error("delete with associated passwords failure")
            deletedCategoryWithAssociatedPasswordsId = categoryId
        }

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) {
            transferCalls += 1
            transferGate?.await()
            transfers += sourceCategoryId to targetCategoryId
        }

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
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
        passwords: List<PasswordSummary>,
        private val snapshots: List<PasswordSecuritySnapshot>
    ) : PasswordRepository {


        private val passwordsFlow = MutableStateFlow(passwords)

        override fun observePasswords(): Flow<List<PasswordSummary>> = passwordsFlow

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            passwordsFlow

        override fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> =
            flowOf(snapshots)

        override suspend fun getPasswordCount(): Int = passwordsFlow.value.size

        override suspend fun createPassword(password: NewPassword): Long = 1L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false

        fun emitPasswords(passwords: List<PasswordSummary>) {
            passwordsFlow.value = passwords
        }
    }
}

private fun category(
    id: Long,
    name: String,
    iconKey: String = "ic_directory",
    itemCount: Int = 0,
    lastModifiedAt: Long = 0L
) = Category(
    id = id,
    name = name,
    iconKey = iconKey,
    itemCount = itemCount,
    lastModifiedAt = lastModifiedAt
)

private fun passwordSummary(
    id: Long,
    title: String = "GitHub",
    login: String = "dev@empresa.com"
) = PasswordSummary(
    id = id,
    title = title,
    login = login,
    categoryId = 9L,
    categoryName = "Trabalho"
)
