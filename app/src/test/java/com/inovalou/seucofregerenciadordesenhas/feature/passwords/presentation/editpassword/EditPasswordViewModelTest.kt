package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordCategoryError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.AnalyzePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GetPasswordDetailsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GeneratePasswordTitleUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordValidation
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordPasswordError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState
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
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenPersistedPassword_whenViewModelLoads_thenExposesEditableFieldsAndDates() = runTest {
        val viewModel = buildViewModel(passwordDetails = persistedPassword(login = "mail@vault.com"))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.contentState is EditPasswordContentState.Content)
        assertEquals("Spotify", state.title)
        assertEquals("mail@vault.com", state.email)
        assertEquals("plain-secret", state.password)
        assertEquals("Conta principal da família.", state.note)
        assertEquals(2L, state.selectedCategoryId)
        assertEquals("Music", state.selectedCategoryName)
        assertFalse(state.isIdentityCardEditing)
        assertEquals(1_700_000_000_000L, state.createdAt)
        assertEquals(1_710_000_000_000L, state.updatedAt)
    }

    @Test
    fun givenPersistedPassword_whenViewModelLoads_thenExposesAnalyzedSecuritySection() = runTest {
        val viewModel = buildViewModel()

        advanceUntilIdle()

        val securitySection = viewModel.uiState.value.securitySection
        assertEquals(35, securitySection.scorePercent)
        assertEquals(
            EditPasswordSecurityVisualState.HighRisk,
            securitySection.visualState
        )
        assertEquals(
            R.string.edit_password_security_title,
            securitySection.riskTitleResId
        )
        assertEquals(listOf(R.string.edit_password_security_tag_weak), securitySection.tagResIds)
        assertEquals(
            R.string.edit_password_security_alert_high,
            securitySection.alertResId
        )
    }

    @Test
    fun givenLegacyInvalidCategory_whenViewModelLoads_thenExposesInvalidCategoryError() = runTest {
        val viewModel = buildViewModel(
            passwordDetails = persistedPassword(categoryId = null, categoryName = "Legacy")
        )

        advanceUntilIdle()

        assertEquals("Legacy", viewModel.uiState.value.selectedCategoryName)
        assertEquals(
            R.string.edit_password_category_error_invalid,
            viewModel.uiState.value.categoryErrorResId
        )
    }

    @Test
    fun givenEmptyCategory_whenDialogOpens_thenKeepsSelectionEmpty() = runTest {
        val viewModel = buildViewModel(
            passwordDetails = persistedPassword(categoryId = null, categoryName = null)
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnCategoryFieldClick)

        assertTrue(viewModel.uiState.value.isCategoryDialogVisible)
        val selectionState = viewModel.uiState.value.categorySelectionState
        assertTrue(selectionState is PasswordCategorySelectionUiState.Content)
        assertFalse((selectionState as PasswordCategorySelectionUiState.Content).categories.any { it.isSelected })
    }

    @Test
    fun givenValidCategory_whenDialogOpens_thenMarksExistingSelection() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnCategoryFieldClick)

        val selectionState = viewModel.uiState.value.categorySelectionState
        assertTrue(selectionState is PasswordCategorySelectionUiState.Content)
        val selected = (selectionState as PasswordCategorySelectionUiState.Content)
            .categories
            .single { it.id == 2L }
        assertTrue(selected.isSelected)
    }

    @Test
    fun givenCategorySelection_whenHandled_thenUpdatesCategoryAndClearsError() = runTest {
        val viewModel = buildViewModel(
            passwordDetails = persistedPassword(categoryId = null, categoryName = "Legacy")
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnCategorySelected(2L))

        assertEquals(2L, viewModel.uiState.value.selectedCategoryId)
        assertEquals("Music", viewModel.uiState.value.selectedCategoryName)
        assertEquals(null, viewModel.uiState.value.categoryErrorResId)
        assertFalse(viewModel.uiState.value.isCategoryDialogVisible)
    }

    @Test
    fun givenTitleChange_whenHandled_thenUpdatesEditableTitleState() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))

        assertEquals("Spotify Family", viewModel.uiState.value.title)
    }

    @Test
    fun givenIdentityCardReadMode_whenProtectedFieldsAndNoteAreHandled_thenKeepsProtectedFieldsReadOnlyAndAllowsNote() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Netflix"))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("new@vault.com"))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret"))
        viewModel.onAction(EditPasswordAction.OnNoteChanged("Nova anotação"))
        viewModel.onAction(EditPasswordAction.OnCategoryFieldClick)

        val state = viewModel.uiState.value
        assertEquals("Spotify", state.title)
        assertEquals("premium@vault.com", state.email)
        assertEquals("plain-secret", state.password)
        assertEquals("Nova anotação", state.note)
        assertFalse(state.isCategoryDialogVisible)
    }

    @Test
    fun givenIdentityCardEditMode_whenCardSaveIsHandled_thenKeepsDraftInStateAndExitsEditMode() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnTitleChanged("Netflix"))
        viewModel.onAction(EditPasswordAction.OnIdentityCardSaveClick)

        assertEquals("Netflix", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.isIdentityCardEditing)
    }

    @Test
    fun givenPersistedPasswordWithoutEmail_whenViewModelLoads_thenKeepsEditableEmailEmpty() = runTest {
        val viewModel = buildViewModel(passwordDetails = persistedPassword(login = ""))

        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.email)
    }

    @Test
    fun givenMissingPasswordId_whenViewModelLoads_thenExposesErrorState() = runTest {
        val viewModel = buildViewModel(passwordId = null)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is EditPasswordContentState.Error)
    }

    @Test
    fun givenVisibilityAction_whenHandled_thenTogglesPasswordVisibility() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTogglePasswordVisibility)
        assertTrue(viewModel.uiState.value.isPasswordVisible)

        viewModel.onAction(EditPasswordAction.OnTogglePasswordVisibility)
        assertFalse(viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun givenPasswordEdition_whenHandled_thenRefreshesSecuritySectionFromRealAnalysis() = runTest {
        val viewModel = buildViewModel(duplicatePasswords = setOf("S7!mQ2#vN9@tL4\$z"))
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("S7!mQ2#vN9@tL4\$z"))
        advanceUntilIdle()

        val securitySection = viewModel.uiState.value.securitySection
        assertEquals(80, securitySection.scorePercent)
        assertEquals(
            EditPasswordSecurityVisualState.MediumRisk,
            securitySection.visualState
        )
        assertEquals(
            listOf(R.string.edit_password_security_tag_duplicate),
            securitySection.tagResIds
        )
        assertEquals(
            R.string.edit_password_security_alert_medium,
            securitySection.alertResId
        )
    }

    @Test
    fun givenCopyEmailAction_whenHandled_thenEmitsCopyEffectAndMarksState() = runTest {
        val viewModel = buildViewModel(passwordDetails = persistedPassword(login = "copy@vault.com"))
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnCopyEmailClick)

        assertEquals(
            EditPasswordEffect.CopyToClipboard(
                value = "copy@vault.com",
                isSensitive = false
            ),
            effect.await()
        )
        assertEquals(EditPasswordCopiedField.Email, viewModel.uiState.value.lastCopiedField)
    }

    @Test
    fun givenCopyPasswordAction_whenHandled_thenEmitsSensitiveClipboardEffect() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnCopyPasswordClick)

        assertEquals(
            EditPasswordEffect.CopyToClipboard(
                value = "plain-secret",
                isSensitive = true
            ),
            effect.await()
        )
        assertEquals(EditPasswordCopiedField.Password, viewModel.uiState.value.lastCopiedField)
    }

    @Test
    fun givenBackAction_whenHandled_thenEmitsNavigateBackEffect() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnBackClick)

        assertEquals(EditPasswordEffect.NavigateBack, effect.await())
    }

    @Test
    fun givenValidChanges_whenSaving_thenDelegatesRealUpdateAndNavigatesBack() = runTest {
        val updateUseCase = FakeUpdatePasswordUseCase()
        val viewModel = buildViewModel(updatePasswordUseCase = updateUseCase)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnTitleChanged("  Spotify Family  "))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("  updated@vault.com  "))
        viewModel.onAction(EditPasswordAction.OnCategorySelected(2L))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret"))
        viewModel.onAction(EditPasswordAction.OnNoteChanged("  Atualizar após renovação anual.  "))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(8L, updateUseCase.lastPasswordId)
        assertEquals("Spotify Family", updateUseCase.lastTitle)
        assertEquals("updated@vault.com", updateUseCase.lastLogin)
        assertEquals(2L, updateUseCase.lastCategoryId)
        assertEquals("Music", updateUseCase.lastCategoryName)
        assertEquals("new-secret", updateUseCase.lastPassword)
        assertEquals("Atualizar após renovação anual.", updateUseCase.lastNote)
        assertEquals(EditPasswordEffect.NavigateBack, effect.await())
    }

    @Test
    fun givenValidationFailure_whenSaving_thenExposesFieldError() = runTest {
        val viewModel = buildViewModel(
            passwordDetails = persistedPassword(categoryId = null, categoryName = "Legacy")
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnIdentityCardEditClick)
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("   "))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(
            R.string.edit_password_password_error_blank,
            viewModel.uiState.value.passwordErrorResId
        )
        assertEquals(
            R.string.edit_password_category_error_invalid,
            viewModel.uiState.value.categoryErrorResId
        )
    }

    @Test
    fun givenRepositoryNotFound_whenSaving_thenExposesNotFoundErrorState() = runTest {
        val viewModel = buildViewModel(
            updatePasswordUseCase = FakeUpdatePasswordUseCase(passwordDetails = null)
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.contentState is EditPasswordContentState.Error)
    }

    @Test
    fun givenDeleteAction_whenHandled_thenKeepsFlowNonDestructive() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)

        assertEquals(null, viewModel.uiState.value.submitErrorResId)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    private fun buildViewModel(
        passwordId: Long? = 8L,
        passwordDetails: PasswordDetails? = persistedPassword(),
        updatePasswordUseCase: FakeUpdatePasswordUseCase = FakeUpdatePasswordUseCase(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
        duplicatePasswords: Set<String> = emptySet()
    ): EditPasswordViewModel = EditPasswordViewModel(
        savedStateHandle = SavedStateHandle(
            buildMap {
                if (passwordId != null) {
                    put(EditPasswordDestination.passwordIdArg, passwordId)
                }
            }
        ),
        getPasswordDetailsUseCase = GetPasswordDetailsUseCase(
            FakePasswordRepository(
                passwordDetails = passwordDetails,
                duplicatePasswords = duplicatePasswords
            )
        ),
        observeCategoriesUseCase = ObserveCategoriesUseCase(categoryRepository),
        analyzePasswordSecurityUseCase = AnalyzePasswordSecurityUseCase(
            passwordRepository = FakePasswordRepository(
                passwordDetails = passwordDetails,
                duplicatePasswords = duplicatePasswords
            ),
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase()
        ),
        updatePasswordUseCase = UpdatePasswordUseCase(
            passwordRepository = updatePasswordUseCase,
            categoryRepository = categoryRepository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(updatePasswordUseCase),
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )
    )

    private fun persistedPassword(
        login: String = "premium@vault.com",
        categoryId: Long? = 2L,
        categoryName: String? = "Music",
        note: String? = "Conta principal da família."
    ) = PasswordDetails(
        id = 8L,
        title = "Spotify",
        login = login,
        password = "plain-secret",
        note = note,
        categoryId = categoryId,
        categoryName = categoryName,
        iconKey = "sp",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_710_000_000_000L
    )

    private class FakePasswordRepository(
        private val passwordDetails: PasswordDetails?,
        private val duplicatePasswords: Set<String> = emptySet()
    ) : PasswordRepository {

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = duplicatePasswords.contains(password)
    }

    private class FakeUpdatePasswordUseCase(
        private val result: UpdatePasswordResult = UpdatePasswordResult.Success,
        private val passwordDetails: PasswordDetails? = PasswordDetails(
            id = 8L,
            title = "Spotify",
            login = "premium@vault.com",
            password = "plain-secret",
            note = "Conta principal da família.",
            categoryId = 2L,
            categoryName = "Music",
            iconKey = "sp",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_710_000_000_000L
        )
    ) : PasswordRepository {

        var lastPasswordId: Long? = null
        var lastTitle: String? = null
        var lastLogin: String? = null
        var lastCategoryId: Long? = null
        var lastCategoryName: String? = null
        var lastPassword: String? = null
        var lastNote: String? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) {
            lastPasswordId = password.id
            lastTitle = password.title
            lastLogin = password.login
            lastCategoryId = password.categoryId
            lastCategoryName = password.categoryName
            lastPassword = password.password
            lastNote = password.note
            if (result is UpdatePasswordResult.Failure) {
                error("update failure")
            }
        }

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }

    private class FakeCategoryRepository(
        initialCategories: List<Category> = listOf(
            Category(
                id = 2L,
                name = "Music",
                iconKey = "music",
                itemCount = 4,
                lastModifiedAt = 1_700_000_000_000L
            ),
            Category(
                id = 3L,
                name = "Work",
                iconKey = "work",
                itemCount = 2,
                lastModifiedAt = 1_710_000_000_000L
            )
        )
    ) : CategoryRepository {

        private val categoriesFlow = MutableStateFlow(initialCategories)

        override suspend fun createCategory(name: String, iconKey: String): Long = 0L

        override suspend fun getCategoryById(categoryId: Long): Category? =
            categoriesFlow.value.firstOrNull { it.id == categoryId }

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
