package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
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
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.DeletePasswordByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.EvaluatePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GetPasswordDetailsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GeneratePasswordTitleUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordValidation
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordPasswordError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))

        assertEquals("Spotify Family", viewModel.uiState.value.title)
    }

    @Test
    fun givenEditableIdentityCardFields_whenHandled_thenUpdatesFieldsAndOpensCategoryDialog() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Netflix"))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("new@vault.com"))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret"))
        viewModel.onAction(EditPasswordAction.OnNoteChanged("Nova anotação"))
        viewModel.onAction(EditPasswordAction.OnCategoryFieldClick)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Netflix", state.title)
        assertEquals("new@vault.com", state.email)
        assertEquals("new-secret", state.password)
        assertEquals("Nova anotação", state.note)
        assertTrue(state.isCategoryDialogVisible)
    }

    @Test
    fun givenUserEditsFields_whenCategoriesRefresh_thenKeepsEditableFields() = runTest {
        val categoryRepository = FakeCategoryRepository()
        val viewModel = buildViewModel(categoryRepository = categoryRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Netflix"))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("new@vault.com"))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret"))
        viewModel.onAction(EditPasswordAction.OnNoteChanged("Nova anotação"))
        viewModel.onAction(EditPasswordAction.OnTogglePasswordVisibility)

        categoryRepository.emit(
            listOf(
                Category(
                    id = 2L,
                    name = "Music",
                    iconKey = "music",
                    itemCount = 5,
                    lastModifiedAt = 1_720_000_000_000L
                ),
                Category(
                    id = 3L,
                    name = "Work",
                    iconKey = "work",
                    itemCount = 2,
                    lastModifiedAt = 1_720_000_000_000L
                )
            )
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Netflix", state.title)
        assertEquals("new@vault.com", state.email)
        assertEquals("new-secret", state.password)
        assertEquals("Nova anotação", state.note)
        assertTrue(state.isPasswordVisible)
    }

    @Test
    fun givenPersistedPasswordLoads_whenCategoriesRefresh_thenDoesNotReinitializeForm() = runTest {
        val categoryRepository = FakeCategoryRepository()
        val viewModel = buildViewModel(categoryRepository = categoryRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("family@vault.com"))
        categoryRepository.emit(
            listOf(
                Category(
                    id = 2L,
                    name = "Music atualizada",
                    iconKey = "music",
                    itemCount = 5,
                    lastModifiedAt = 1_720_000_000_000L
                )
            )
        )
        advanceUntilIdle()

        assertEquals("Spotify Family", viewModel.uiState.value.title)
        assertEquals("family@vault.com", viewModel.uiState.value.email)
        assertEquals("plain-secret", viewModel.uiState.value.password)
        assertEquals("Conta principal da família.", viewModel.uiState.value.note)
        assertEquals(2L, viewModel.uiState.value.selectedCategoryId)
        assertEquals("Music atualizada", viewModel.uiState.value.selectedCategoryName)
    }

    @Test
    fun givenTextFieldsExceedLimits_whenHandled_thenLimitsEditableState() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("a".repeat(101)))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("b".repeat(255)))
        viewModel.onAction(EditPasswordAction.OnNoteChanged("c".repeat(1_501)))

        assertEquals(100, viewModel.uiState.value.title.length)
        assertEquals(254, viewModel.uiState.value.email.length)
        assertEquals(1_500, viewModel.uiState.value.note.length)
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
    fun givenRapidPasswordChanges_whenDebounceWindowIsActive_thenAnalyzesOnlyLatestPassword() = runTest {
        val analysisRepository = FakePasswordRepository(
            passwordDetails = persistedPassword(),
            duplicatePasswords = setOf("S7!mQ2#vN9@tL4\$z")
        )
        val viewModel = buildViewModel(analysisPasswordRepository = analysisRepository)
        advanceUntilIdle()
        val callsAfterLoad = analysisRepository.duplicateLookupCalls

        viewModel.onAction(EditPasswordAction.OnPasswordChanged("short-one"))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("short-two"))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("S7!mQ2#vN9@tL4\$z"))
        advanceTimeBy(299)
        runCurrent()

        assertEquals(callsAfterLoad, analysisRepository.duplicateLookupCalls)

        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(callsAfterLoad + 1, analysisRepository.duplicateLookupCalls)
        assertEquals("S7!mQ2#vN9@tL4\$z", analysisRepository.lastDuplicateLookupPassword)
        assertEquals(
            listOf(R.string.edit_password_security_tag_duplicate),
            viewModel.uiState.value.securitySection.tagResIds
        )
    }

    @Test
    fun givenPasswordChangedAndSavedBeforeDebounce_whenSaveRuns_thenUsesLatestPasswordImmediately() = runTest {
        val updateUseCase = FakeUpdatePasswordUseCase()
        val analysisRepository = FakePasswordRepository(passwordDetails = persistedPassword())
        val viewModel = buildViewModel(
            updatePasswordUseCase = updateUseCase,
            analysisPasswordRepository = analysisRepository
        )
        advanceUntilIdle()
        val callsAfterLoad = analysisRepository.duplicateLookupCalls

        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret-without-analysis"))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        runCurrent()

        assertEquals("new-secret-without-analysis", updateUseCase.lastPassword)
        assertEquals(callsAfterLoad, analysisRepository.duplicateLookupCalls)
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

        assertEquals(
            EditPasswordEffect.NavigateBackToOrigin(EditPasswordOpenedFrom.Passwords),
            effect.await()
        )
    }

    @Test
    fun givenEditCategoryOrigin_whenBackActionIsHandled_thenNavigatesBackToEditCategoryOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditPasswordOpenedFrom.EditCategory)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnBackClick)

        assertEquals(
            EditPasswordEffect.NavigateBackToOrigin(EditPasswordOpenedFrom.EditCategory),
            effect.await()
        )
    }

    @Test
    fun givenVaultOrigin_whenBackActionIsHandled_thenNavigatesBackToVaultOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditPasswordOpenedFrom.Vault)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnBackClick)

        assertEquals(
            EditPasswordEffect.NavigateBackToOrigin(EditPasswordOpenedFrom.Vault),
            effect.await()
        )
    }

    @Test
    fun givenValidChanges_whenSaving_thenDelegatesRealUpdateAndNavigatesBack() = runTest {
        val updateUseCase = FakeUpdatePasswordUseCase()
        val viewModel = buildViewModel(updatePasswordUseCase = updateUseCase)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

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
        assertEquals(
            EditPasswordEffect.NavigateAfterSave(EditPasswordOpenedFrom.Passwords),
            effect.await()
        )
    }

    @Test
    fun givenSaveCompletesBeforeEffectCollectorStarts_whenCollectorStarts_thenReceivesNavigateOnce() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(
            EditPasswordEffect.NavigateAfterSave(EditPasswordOpenedFrom.Passwords),
            withTimeout(1_000) { viewModel.effects.first() }
        )
        assertEquals(null, withTimeoutOrNull(100) { viewModel.effects.first() })
    }

    @Test
    fun givenSaveAlreadyInProgress_whenSaveClickedAgain_thenUpdatesPasswordOnlyOnce() = runTest {
        val updateGate = CompletableDeferred<Unit>()
        val updateUseCase = FakeUpdatePasswordUseCase(updateGate = updateGate)
        val viewModel = buildViewModel(updatePasswordUseCase = updateUseCase)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()
        viewModel.onAction(EditPasswordAction.OnSaveClick)

        assertTrue(viewModel.uiState.value.isSaving)
        assertEquals(1, updateUseCase.updateCalls)

        updateGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenEditCategoryOrigin_whenSavingValidChanges_thenNavigatesBackToEditCategoryOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditPasswordOpenedFrom.EditCategory)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(
            EditPasswordEffect.NavigateAfterSave(EditPasswordOpenedFrom.EditCategory),
            effect.await()
        )
    }

    @Test
    fun givenVaultOrigin_whenSavingValidChanges_thenNavigatesAfterSaveWithVaultOrigin() = runTest {
        val viewModel = buildViewModel(openedFrom = EditPasswordOpenedFrom.Vault)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(
            EditPasswordEffect.NavigateAfterSave(EditPasswordOpenedFrom.Vault),
            effect.await()
        )
    }

    @Test
    fun givenValidationFailure_whenSaving_thenExposesFieldError() = runTest {
        val viewModel = buildViewModel(
            passwordDetails = persistedPassword(categoryId = null, categoryName = "Legacy")
        )
        advanceUntilIdle()

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
    fun givenDeleteAction_whenHandled_thenShowsDeleteConfirmationWithoutDeleting() = runTest {
        val deleteRepository = FakeDeletePasswordRepository()
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        advanceUntilIdle()

        assertEquals(EditPasswordDeleteFlowState.Confirmation, viewModel.uiState.value.deleteFlowState)
        assertEquals(0, deleteRepository.deleteCalls)
        assertEquals(null, withTimeoutOrNull(100) { viewModel.effects.first() })
    }

    @Test
    fun givenDeleteConfirmation_whenDismissed_thenKeepsPasswordWithoutDeleting() = runTest {
        val deleteRepository = FakeDeletePasswordRepository()
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteDialogDismissed)
        advanceUntilIdle()

        assertEquals(EditPasswordDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertEquals(0, deleteRepository.deleteCalls)
        assertEquals(null, withTimeoutOrNull(100) { viewModel.effects.first() })
    }

    @Test
    fun givenDeleteConfirmed_whenHandled_thenDeletesPasswordAndNavigatesBackToOrigin() = runTest {
        val deleteRepository = FakeDeletePasswordRepository()
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertEquals(8L, deleteRepository.deletedPasswordId)
        assertFalse(viewModel.uiState.value.isDeleting)
        assertEquals(EditPasswordDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertEquals(
            EditPasswordEffect.NavigateBackToOrigin(EditPasswordOpenedFrom.Passwords),
            effect.await()
        )
    }

    @Test
    fun givenDeleteAlreadyInProgress_whenDeleteClickedAgain_thenDeletesPasswordOnlyOnce() = runTest {
        val deleteGate = CompletableDeferred<Unit>()
        val deleteRepository = FakeDeletePasswordRepository(deleteGate = deleteGate)
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)
        advanceUntilIdle()
        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)

        assertTrue(viewModel.uiState.value.isDeleting)
        assertEquals(1, deleteRepository.deleteCalls)

        deleteGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenDeleteInProgress_whenSaveClicked_thenDoesNotPersistConcurrentUpdate() = runTest {
        val deleteGate = CompletableDeferred<Unit>()
        val deleteRepository = FakeDeletePasswordRepository(deleteGate = deleteGate)
        val updateUseCase = FakeUpdatePasswordUseCase()
        val viewModel = buildViewModel(
            updatePasswordUseCase = updateUseCase,
            deletePasswordRepository = deleteRepository
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)
        advanceUntilIdle()
        viewModel.onAction(EditPasswordAction.OnSaveClick)

        assertTrue(viewModel.uiState.value.isDeleting)
        assertEquals(0, updateUseCase.updateCalls)

        deleteGate.complete(Unit)
        advanceUntilIdle()
    }

    @Test
    fun givenDeleteFailure_whenDeleteCompletes_thenShowsDeleteErrorAndKeepsContentVisible() = runTest {
        val deleteRepository = FakeDeletePasswordRepository(failDelete = true)
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isDeleting)
        assertEquals(EditPasswordDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertTrue(viewModel.uiState.value.contentState is EditPasswordContentState.Content)
        assertEquals(R.string.edit_password_delete_error, viewModel.uiState.value.submitErrorResId)
        assertEquals(null, withTimeoutOrNull(100) { viewModel.effects.first() })
    }

    @Test
    fun givenMissingPasswordDuringDelete_whenDeleteCompletes_thenShowsNotFoundErrorState() = runTest {
        val deleteRepository = FakeDeletePasswordRepository(deleteResult = false)
        val viewModel = buildViewModel(deletePasswordRepository = deleteRepository)
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnDeleteClick)
        viewModel.onAction(EditPasswordAction.OnDeleteConfirmed)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isDeleting)
        assertEquals(EditPasswordDeleteFlowState.Idle, viewModel.uiState.value.deleteFlowState)
        assertTrue(viewModel.uiState.value.contentState is EditPasswordContentState.Error)
        assertEquals(null, viewModel.uiState.value.submitErrorResId)
    }

    private fun buildViewModel(
        passwordId: Long? = 8L,
        openedFrom: EditPasswordOpenedFrom = EditPasswordOpenedFrom.Passwords,
        passwordDetails: PasswordDetails? = persistedPassword(),
        updatePasswordUseCase: FakeUpdatePasswordUseCase = FakeUpdatePasswordUseCase(),
        deletePasswordRepository: FakeDeletePasswordRepository = FakeDeletePasswordRepository(),
        categoryRepository: FakeCategoryRepository = FakeCategoryRepository(),
        duplicatePasswords: Set<String> = emptySet(),
        analysisPasswordRepository: FakePasswordRepository = FakePasswordRepository(
            passwordDetails = passwordDetails,
            duplicatePasswords = duplicatePasswords
        )
    ): EditPasswordViewModel = EditPasswordViewModel(
        savedStateHandle = SavedStateHandle(
            buildMap {
                if (passwordId != null) {
                    put(EditPasswordDestination.passwordIdArg, passwordId)
                }
                put(EditPasswordDestination.openedFromArg, openedFrom.routeValue)
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
            passwordRepository = analysisPasswordRepository,
            evaluatePasswordSecurityUseCase = EvaluatePasswordSecurityUseCase(),
            dispatchers = testDispatchers()
        ),
        updatePasswordUseCase = UpdatePasswordUseCase(
            passwordRepository = updatePasswordUseCase,
            categoryRepository = categoryRepository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(updatePasswordUseCase),
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        ),
        deletePasswordByIdUseCase = DeletePasswordByIdUseCase(deletePasswordRepository)
    )

    private fun testDispatchers() = AppDispatchers(
        default = mainDispatcherRule.dispatcher,
        io = mainDispatcherRule.dispatcher
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


        var duplicateLookupCalls: Int = 0
        var lastDuplicateLookupPassword: String? = null

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
        ): Boolean {
            duplicateLookupCalls += 1
            lastDuplicateLookupPassword = password
            return duplicatePasswords.contains(password)
        }

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
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
        ),
        private val updateGate: CompletableDeferred<Unit>? = null
    ) : PasswordRepository {


        var lastPasswordId: Long? = null
        var lastTitle: String? = null
        var lastLogin: String? = null
        var lastCategoryId: Long? = null
        var lastCategoryName: String? = null
        var lastPassword: String? = null
        var lastNote: String? = null
        var updateCalls: Int = 0

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) {
            updateCalls += 1
            updateGate?.await()
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

        override suspend fun deletePasswordById(passwordId: Long): Boolean = false
    }

    private class FakeDeletePasswordRepository(
        private val deleteResult: Boolean = true,
        private val failDelete: Boolean = false,
        private val deleteGate: CompletableDeferred<Unit>? = null
    ) : PasswordRepository {

        var deletedPasswordId: Long? = null
        var deleteCalls: Int = 0

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false

        override suspend fun deletePasswordById(passwordId: Long): Boolean {
            deleteCalls += 1
            deleteGate?.await()
            deletedPasswordId = passwordId
            if (failDelete) {
                error("delete failure")
            }
            return deleteResult
        }
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
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit


        private val categoriesFlow = MutableStateFlow(initialCategories)

        override suspend fun createCategory(name: String, iconKey: String): Long = 0L

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
