package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GetPasswordDetailsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordPasswordError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordUseCase
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
class EditPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPasswordDetailsUseCase: GetPasswordDetailsUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {

    private val passwordId = savedStateHandle.get<Long>(EditPasswordDestination.passwordIdArg)

    private val _uiState = MutableStateFlow(EditPasswordUiState())
    val uiState: StateFlow<EditPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EditPasswordEffect>()
    val effects: SharedFlow<EditPasswordEffect> = _effects.asSharedFlow()

    init {
        loadPassword()
    }

    fun onAction(action: EditPasswordAction) {
        when (action) {
            EditPasswordAction.OnBackClick -> navigateBack()
            is EditPasswordAction.OnTitleChanged -> updateTitle(action.title)
            is EditPasswordAction.OnEmailChanged -> updateEmail(action.email)
            is EditPasswordAction.OnPasswordChanged -> updatePassword(action.password)
            EditPasswordAction.OnTogglePasswordVisibility -> togglePasswordVisibility()
            EditPasswordAction.OnCopyEmailClick -> copyEmail()
            EditPasswordAction.OnCopyPasswordClick -> copyPassword()
            EditPasswordAction.OnSaveClick -> saveChanges()
            EditPasswordAction.OnDeleteClick -> Unit
        }
    }

    private fun loadPassword() {
        val resolvedPasswordId = passwordId
        if (resolvedPasswordId == null) {
            _uiState.update {
                it.copy(
                    contentState = EditPasswordContentState.Error(
                        R.string.edit_password_load_error
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            val password = getPasswordDetailsUseCase(resolvedPasswordId)
            if (password == null) {
                _uiState.update {
                    it.copy(
                        contentState = EditPasswordContentState.Error(
                            R.string.edit_password_not_found_error
                        )
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    title = password.title,
                    email = password.login,
                    password = password.password,
                    createdAt = password.createdAt,
                    updatedAt = password.updatedAt,
                    contentState = EditPasswordContentState.Content
                )
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.emit(EditPasswordEffect.NavigateBack)
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                submitErrorResId = null
            )
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                submitErrorResId = null
            )
        }
    }

    private fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
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

    private fun copyEmail() {
        val email = _uiState.value.email
        if (email.isBlank()) {
            return
        }

        _uiState.update { it.copy(lastCopiedField = EditPasswordCopiedField.Email) }
        viewModelScope.launch {
            _effects.emit(EditPasswordEffect.CopyToClipboard(email, isSensitive = false))
        }
    }

    private fun copyPassword() {
        val password = _uiState.value.password
        if (password.isBlank()) {
            return
        }

        _uiState.update { it.copy(lastCopiedField = EditPasswordCopiedField.Password) }
        viewModelScope.launch {
            _effects.emit(EditPasswordEffect.CopyToClipboard(password, isSensitive = true))
        }
    }

    private fun saveChanges() {
        val resolvedPasswordId = passwordId ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    passwordErrorResId = null,
                    submitErrorResId = null
                )
            }

            when (
                val result = updatePasswordUseCase(
                    passwordId = resolvedPasswordId,
                    title = _uiState.value.title,
                    login = _uiState.value.email,
                    password = _uiState.value.password
                )
            ) {
                UpdatePasswordResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _effects.emit(EditPasswordEffect.NavigateBack)
                }

                UpdatePasswordResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            contentState = EditPasswordContentState.Error(
                                R.string.edit_password_not_found_error
                            )
                        )
                    }
                }

                UpdatePasswordResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            submitErrorResId = R.string.edit_password_save_error
                        )
                    }
                }

                is UpdatePasswordResult.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            passwordErrorResId = result.validation.passwordError.toPasswordErrorResId()
                        )
                    }
                }
            }
        }
    }
}

private fun UpdatePasswordPasswordError?.toPasswordErrorResId(): Int? = when (this) {
    UpdatePasswordPasswordError.Blank -> R.string.edit_password_password_error_blank
    null -> null
}
