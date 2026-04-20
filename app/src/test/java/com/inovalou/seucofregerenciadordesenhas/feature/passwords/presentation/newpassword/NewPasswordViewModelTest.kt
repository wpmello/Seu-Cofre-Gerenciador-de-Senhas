package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GeneratePasswordTitleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
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
        assertEquals("", viewModel.uiState.value.category)
        assertEquals("", viewModel.uiState.value.password)
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
    }

    @Test
    fun givenValidForm_whenSaving_thenEmitsNavigateBackAndPersistsPassword() = runTest {
        val repository = FakePasswordRepository(passwordCount = 0)
        val viewModel = buildViewModel(repository)
        val effect = async { viewModel.effects.first() }

        viewModel.onAction(NewPasswordAction.OnLoginChanged("user@email.com"))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals(NewPasswordEffect.NavigateBack, effect.await())
        assertEquals("App 1", repository.createdPassword?.title)
        assertEquals("user@email.com", repository.createdPassword?.login)
    }

    @Test
    fun givenExplicitTitle_whenSaving_thenKeepsUserValue() = runTest {
        val repository = FakePasswordRepository(passwordCount = 9)
        val viewModel = buildViewModel(repository)

        viewModel.onAction(NewPasswordAction.OnTitleChanged("  GitHub  "))
        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))
        viewModel.onAction(NewPasswordAction.OnSaveClick)

        advanceUntilIdle()

        assertEquals("GitHub", repository.createdPassword?.title)
    }

    @Test
    fun givenRepositoryFailure_whenSaving_thenShowsSubmitError() = runTest {
        val viewModel = buildViewModel(
            repository = FakePasswordRepository(shouldFailOnCreate = true)
        )

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
        val viewModel = buildViewModel()

        viewModel.onAction(NewPasswordAction.OnSaveClick)
        advanceUntilIdle()
        assertEquals(
            R.string.new_password_password_error_blank,
            viewModel.uiState.value.passwordErrorResId
        )

        viewModel.onAction(NewPasswordAction.OnPasswordChanged("abc123"))

        assertNull(viewModel.uiState.value.passwordErrorResId)
        assertNull(viewModel.uiState.value.submitErrorResId)
    }

    private fun buildViewModel(
        repository: FakePasswordRepository = FakePasswordRepository()
    ): NewPasswordViewModel = NewPasswordViewModel(
        createPasswordUseCase = CreatePasswordUseCase(
            passwordRepository = repository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository)
        )
    )

    private class FakePasswordRepository(
        private val passwordCount: Int = 0,
        private val shouldFailOnCreate: Boolean = false
    ) : PasswordRepository {

        var createdPassword: NewPassword? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun createPassword(password: NewPassword): Long {
            if (shouldFailOnCreate) {
                error("repository failure")
            }
            createdPassword = password
            return 1L
        }
    }
}
