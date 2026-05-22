package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GeneratePasswordTitleUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenViewModelCreated_whenObserved_thenExposesInitialState() {
        val viewModel = buildViewModel()

        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.login)
        assertEquals(null, viewModel.uiState.value.selectedCategoryId)
        assertEquals(null, viewModel.uiState.value.selectedCategoryName)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals("", viewModel.uiState.value.note)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun givenPasswordVisibilityAction_whenHandled_thenTogglesPasswordVisibility() {
        val viewModel = buildViewModel()

        viewModel.onAction(NewPasswordAction.OnTogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.onAction(NewPasswordAction.OnTogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun givenBlankPassword_whenSaving_thenShowsValidationError() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(NewPasswordAction.OnTitleChanged("Netflix"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(
            R.string.new_password_password_error_blank,
            viewModel.uiState.value.passwordErrorResId
        )
        assertEquals(
            R.string.new_password_category_error_missing,
            viewModel.uiState.value.categoryErrorResId
        )
    }

    @Test
    fun givenCategoryFieldClicked_whenHandled_thenOpensSelectionDialog() = runTest {
        val viewModel = buildViewModel()

        viewModel.onAction(NewPasswordAction.OnCategoryFieldClick)

        assertTrue(viewModel.uiState.value.isCategoryDialogVisible)
    }

    @Test
    fun givenNoPersistedCategories_whenCategoriesAreObserved_thenExposesEmptyDialogState() = runTest {
        val viewModel = buildViewModel(categoryRepository = FakeCategoryRepository(emptyList()))

        advanceUntilIdle()

        assertEquals(
            PasswordCategorySelectionUiState.Empty,
            viewModel.uiState.value.categorySelectionState
        )
    }

    @Test
    fun givenPersistedCategories_whenCategoriesAreObserved_thenExposesRealSelectionOptions() = runTest {
        val viewModel = buildViewModel(
            categoryRepository = FakeCategoryRepository(
                listOf(
                    Category(id = 1L, name = "Trabalho", iconKey = "ic_work", itemCount = 2, lastModifiedAt = 0L),
                    Category(id = 2L, name = "Pessoal", iconKey = "ic_home", itemCount = 1, lastModifiedAt = 0L)
                )
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value.categorySelectionState
        assertTrue(state is PasswordCategorySelectionUiState.Content)
        state as PasswordCategorySelectionUiState.Content
        assertEquals(listOf("Trabalho", "Pessoal"), state.categories.map { it.name })
        assertTrue(state.categories.none { it.isSelected })
    }

    @Test
    fun givenCategorySelected_whenHandled_thenUpdatesFieldAndClosesDialog() = runTest {
        val viewModel = buildViewModel(
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 9L, name = "Work", iconKey = "ic_work", itemCount = 1, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategoryFieldClick)
        viewModel.onAction(NewPasswordAction.OnCategorySelected(categoryId = 9L))

        assertFalse(viewModel.uiState.value.isCategoryDialogVisible)
        assertEquals(9L, viewModel.uiState.value.selectedCategoryId)
        assertEquals("Work", viewModel.uiState.value.selectedCategoryName)
    }

    @Test
    fun givenSelectedCategory_whenDialogIsReopened_thenKeepsMatchingOptionSelected() = runTest {
        val categoryRepository = FakeCategoryRepository(
                listOf(
                    Category(id = 4L, name = "Streaming", iconKey = "ic_tv", itemCount = 1, lastModifiedAt = 0L),
                    Category(id = 8L, name = "Financeiro", iconKey = "ic_bank", itemCount = 2, lastModifiedAt = 0L)
                )
            )
        val viewModel = buildViewModel(categoryRepository = categoryRepository)
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategorySelected(categoryId = 8L))
        viewModel.onAction(NewPasswordAction.OnCategoryFieldClick)

        val state = viewModel.uiState.value.categorySelectionState
        assertTrue(state is PasswordCategorySelectionUiState.Content)
        state as PasswordCategorySelectionUiState.Content
        assertEquals(8L, state.categories.single { it.isSelected }.id)
    }

    @Test
    fun givenSelectedCategoryRemovedFromDatabase_whenCategoriesRefresh_thenClearsSelection() = runTest {
        val categoryRepository = FakeCategoryRepository(
            listOf(Category(id = 8L, name = "Financeiro", iconKey = "ic_bank", itemCount = 2, lastModifiedAt = 0L))
        )
        val viewModel = buildViewModel(categoryRepository = categoryRepository)
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategorySelected(categoryId = 8L))
        categoryRepository.emit(emptyList())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.selectedCategoryId)
        assertNull(viewModel.uiState.value.selectedCategoryName)
        assertEquals(
            PasswordCategorySelectionUiState.Empty,
            viewModel.uiState.value.categorySelectionState
        )
    }

    @Test
    fun givenValidForm_whenSaving_thenEmitsNavigateBackToPasswordsOriginAndPersistsPassword() = runTest {
        val repository = FakePasswordRepository(passwordCount = 0)
        val viewModel = buildViewModel(
            repository = repository,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 2L, name = "Pessoal", iconKey = "ic_home", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(NewPasswordAction.OnLoginChanged("user@email.com"))
        viewModel.onAction(NewPasswordAction.OnCategorySelected(2L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnNoteChanged("Conta principal do streaming"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(
            NewPasswordEffect.NavigateBackToOrigin(NewPasswordOpenedFrom.Passwords),
            effect.await()
        )
        assertEquals("App 1", repository.createdPassword?.title)
        assertEquals("user@email.com", repository.createdPassword?.login)
        assertEquals(2L, repository.createdPassword?.categoryId)
        assertEquals("Pessoal", repository.createdPassword?.categoryName)
        assertEquals("Conta principal do streaming", repository.createdPassword?.note)
    }

    @Test
    fun givenSaveAlreadyInProgress_whenSaveClickedAgain_thenPersistsPasswordOnlyOnce() = runTest {
        val createGate = CompletableDeferred<Unit>()
        val repository = FakePasswordRepository(createGate = createGate)
        val viewModel = buildViewModel(
            repository = repository,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 2L, name = "Pessoal", iconKey = "ic_home", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategorySelected(2L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)
        advanceUntilIdle()
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        assertTrue(viewModel.uiState.value.isSaving)
        assertEquals(1, repository.createCalls)

        createGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenVaultOrigin_whenSavingValidForm_thenEmitsNavigateBackToVaultOrigin() = runTest {
        val viewModel = buildViewModel(
            openedFrom = NewPasswordOpenedFrom.Vault,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 2L, name = "Pessoal", iconKey = "ic_home", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(NewPasswordAction.OnCategorySelected(2L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(
            NewPasswordEffect.NavigateBackToOrigin(NewPasswordOpenedFrom.Vault),
            effect.await()
        )
    }

    @Test
    fun givenSelectedCategory_whenSaving_thenPersistsCategoryAssociation() = runTest {
        val repository = FakePasswordRepository(passwordCount = 0)
        val viewModel = buildViewModel(
            repository = repository,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 7L, name = "Work", iconKey = "ic_work", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategorySelected(categoryId = 7L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(7L, repository.createdPassword?.categoryId)
        assertEquals("Work", repository.createdPassword?.categoryName)
    }

    @Test
    fun givenExplicitTitle_whenSaving_thenKeepsUserValue() = runTest {
        val repository = FakePasswordRepository(passwordCount = 9)
        val viewModel = buildViewModel(
            repository = repository,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 5L, name = "Dev", iconKey = "ic_work", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnTitleChanged("  GitHub  "))
        viewModel.onAction(NewPasswordAction.OnCategorySelected(5L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals("GitHub", repository.createdPassword?.title)
    }

    @Test
    fun givenRepositoryFailure_whenSaving_thenShowsSubmitError() = runTest {
        val viewModel = buildViewModel(
            repository = FakePasswordRepository(shouldFailOnCreate = true),
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 4L, name = "Streaming", iconKey = "ic_tv", itemCount = 0, lastModifiedAt = 0L))
            )
        )
        advanceUntilIdle()

        viewModel.onAction(NewPasswordAction.OnCategorySelected(4L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(R.string.new_password_save_error, viewModel.uiState.value.submitErrorResId)
    }

    @Test
    fun givenBackAction_whenHandled_thenEmitsNavigateBackEffect() = runTest {
        val viewModel = buildViewModel()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(NewPasswordAction.OnBackClick)

        assertEquals(NewPasswordEffect.NavigateBack, effect.await())
    }

    @Test
    fun givenFieldChanged_whenHandled_thenClearsPreviousErrors() = runTest {
        val viewModel = buildViewModel(
            categoryRepository = FakeCategoryRepository(
                listOf(Category(id = 4L, name = "Streaming", iconKey = "ic_tv", itemCount = 0, lastModifiedAt = 0L))
            )
        )

        viewModel.onAction(NewPasswordAction.OnSaveClick)
        advanceUntilIdle()
        assertEquals(
            R.string.new_password_password_error_blank,
            viewModel.uiState.value.passwordErrorResId
        )
        assertEquals(
            R.string.new_password_category_error_missing,
            viewModel.uiState.value.categoryErrorResId
        )

        viewModel.onAction(NewPasswordAction.OnCategorySelected(4L))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnNoteChanged("Observação"))

        assertNull(viewModel.uiState.value.passwordErrorResId)
        assertNull(viewModel.uiState.value.categoryErrorResId)
        assertNull(viewModel.uiState.value.submitErrorResId)
    }

    private fun buildViewModel(
        repository: FakePasswordRepository = FakePasswordRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(emptyList()),
        openedFrom: NewPasswordOpenedFrom = NewPasswordOpenedFrom.Passwords
    ): NewPasswordViewModel = NewPasswordViewModel(
        savedStateHandle = SavedStateHandle(
            mapOf(NewPasswordDestination.openedFromArg to openedFrom.routeValue)
        ),
        createPasswordUseCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = categoryRepository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        ),
        observeCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository)
    )

    private class FakePasswordRepository(
        private val passwordCount: Int = 0,
        private val shouldFailOnCreate: Boolean = false,
        private val createGate: CompletableDeferred<Unit>? = null
    ) : PasswordRepository {


        var createdPassword: NewPassword? = null
        var createCalls: Int = 0

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> = emptyFlow()

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun createPassword(password: NewPassword): Long {
            createCalls += 1
            createGate?.await()
            if (shouldFailOnCreate) {
                error("repository failure")
            }
            createdPassword = password
            return 1L
        }

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }

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

        override suspend fun getCategoryById(categoryId: Long): Category? =
            categoriesFlow.value.firstOrNull { it.id == categoryId }

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow

        fun emit(categories: List<Category>) {
            categoriesFlow.value = categories
        }
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
