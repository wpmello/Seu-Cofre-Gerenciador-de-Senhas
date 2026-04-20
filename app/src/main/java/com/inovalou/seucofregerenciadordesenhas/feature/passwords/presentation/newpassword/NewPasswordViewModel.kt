package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordPasswordError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    private val createPasswordUseCase: CreatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<NewPasswordEffect>()
    val effects: SharedFlow<NewPasswordEffect> = _effects.asSharedFlow()

    fun onAction(action: NewPasswordAction) {
        when (action) {
            NewPasswordAction.OnBackClick -> navigateBack()
            is NewPasswordAction.OnTitleChanged -> updateState(title = action.title)
            is NewPasswordAction.OnLoginChanged -> updateState(login = action.login)
            is NewPasswordAction.OnCategoryChanged -> updateState(category = action.category)
            is NewPasswordAction.OnPasswordChanged -> updateState(password = action.password)
            NewPasswordAction.OnTogglePasswordVisibility -> togglePasswordVisibility()
            NewPasswordAction.OnSaveClick -> savePassword()
        }
    }

    private fun updateState(
        title: String = _uiState.value.title,
        login: String = _uiState.value.login,
        category: String = _uiState.value.category,
        password: String = _uiState.value.password
    ) {
        _uiState.update { state ->
            state.copy(
                title = title,
                login = login,
                category = category,
                password = password,
                passwordErrorResId = null,
                submitErrorResId = null
            )
        }
    }

    private fun togglePasswordVisibility() {
        _uiState.update { state ->
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }

    private fun savePassword() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSaving = true,
                    passwordErrorResId = null,
                    submitErrorResId = null
                )
            }

            when (
                val result = createPasswordUseCase(
                    title = _uiState.value.title,
                    login = _uiState.value.login,
                    category = _uiState.value.category,
                    password = _uiState.value.password
                )
            ) {
                CreatePasswordResult.Success -> {
                    _uiState.update { state -> state.copy(isSaving = false) }
                    _effects.emit(NewPasswordEffect.NavigateBack)
                }

                CreatePasswordResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            submitErrorResId = R.string.new_password_save_error
                        )
                    }
                }

                is CreatePasswordResult.ValidationError -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            passwordErrorResId = result.validation.passwordError.toPasswordErrorResId()
                        )
                    }
                }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(NewPasswordEffect.NavigateBack)
        }
    }
}

private fun CreatePasswordPasswordError?.toPasswordErrorResId(): Int? = when (this) {
    CreatePasswordPasswordError.Blank -> R.string.new_password_password_error_blank
    null -> null
}
