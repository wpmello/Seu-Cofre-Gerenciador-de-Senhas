package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.lifecycle.SavedStateHandle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GetPasswordDetailsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GeneratePasswordTitleUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordValidation
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordPasswordError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
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
        assertEquals(1_700_000_000_000L, state.createdAt)
        assertEquals(1_710_000_000_000L, state.updatedAt)
    }

    @Test
    fun givenTitleChange_whenHandled_thenUpdatesEditableTitleState() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnTitleChanged("Spotify Family"))

        assertEquals("Spotify Family", viewModel.uiState.value.title)
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

        viewModel.onAction(EditPasswordAction.OnTitleChanged("  Spotify Family  "))
        viewModel.onAction(EditPasswordAction.OnEmailChanged("  updated@vault.com  "))
        viewModel.onAction(EditPasswordAction.OnPasswordChanged("new-secret"))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(8L, updateUseCase.lastPasswordId)
        assertEquals("Spotify Family", updateUseCase.lastTitle)
        assertEquals("updated@vault.com", updateUseCase.lastLogin)
        assertEquals("new-secret", updateUseCase.lastPassword)
        assertEquals(EditPasswordEffect.NavigateBack, effect.await())
    }

    @Test
    fun givenValidationFailure_whenSaving_thenExposesFieldError() = runTest {
        val viewModel = buildViewModel(
            updatePasswordUseCase = FakeUpdatePasswordUseCase(
                result = UpdatePasswordResult.ValidationError(
                    UpdatePasswordValidation(passwordError = UpdatePasswordPasswordError.Blank)
                )
            )
        )
        advanceUntilIdle()

        viewModel.onAction(EditPasswordAction.OnPasswordChanged("   "))
        viewModel.onAction(EditPasswordAction.OnSaveClick)
        advanceUntilIdle()

        assertEquals(
            R.string.edit_password_password_error_blank,
            viewModel.uiState.value.passwordErrorResId
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
        updatePasswordUseCase: FakeUpdatePasswordUseCase = FakeUpdatePasswordUseCase()
    ): EditPasswordViewModel = EditPasswordViewModel(
        savedStateHandle = SavedStateHandle(
            buildMap {
                if (passwordId != null) {
                    put(EditPasswordDestination.passwordIdArg, passwordId)
                }
            }
        ),
        getPasswordDetailsUseCase = GetPasswordDetailsUseCase(
            FakePasswordRepository(passwordDetails)
        ),
        updatePasswordUseCase = UpdatePasswordUseCase(
            passwordRepository = updatePasswordUseCase,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(updatePasswordUseCase),
            timeProvider = FixedTimeProvider(1_750_000_000_000L)
        )
    )

    private fun persistedPassword(login: String = "premium@vault.com") = PasswordDetails(
        id = 8L,
        title = "Spotify",
        login = login,
        password = "plain-secret",
        categoryId = 2L,
        categoryName = "Music",
        iconKey = "sp",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_710_000_000_000L
    )

    private class FakePasswordRepository(
        private val passwordDetails: PasswordDetails?
    ) : PasswordRepository {

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = passwordDetails

        override suspend fun updatePassword(password: PasswordDetails) = Unit
    }

    private class FakeUpdatePasswordUseCase(
        private val result: UpdatePasswordResult = UpdatePasswordResult.Success,
        private val passwordDetails: PasswordDetails? = PasswordDetails(
            id = 8L,
            title = "Spotify",
            login = "premium@vault.com",
            password = "plain-secret",
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
        var lastPassword: String? = null

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
            lastPassword = password.password
            if (result is UpdatePasswordResult.Failure) {
                error("update failure")
            }
        }
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
