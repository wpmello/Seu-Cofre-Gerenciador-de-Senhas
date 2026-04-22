package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordCategoryError
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
    private val createPasswordUseCase: CreatePasswordUseCase,
    observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<NewPasswordEffect>()
    val effects: SharedFlow<NewPasswordEffect> = _effects.asSharedFlow()

    init {
        observeCategoriesUseCase()
            .collectIntoUiState()
    }

    fun onAction(action: NewPasswordAction) {
        when (action) {
            NewPasswordAction.OnBackClick -> navigateBack()
            is NewPasswordAction.OnTitleChanged -> updateState(title = action.title)
            is NewPasswordAction.OnLoginChanged -> updateState(login = action.login)
            NewPasswordAction.OnCategoryFieldClick -> openCategoryDialog()
            NewPasswordAction.OnCategoryDialogDismissed -> closeCategoryDialog()
            is NewPasswordAction.OnCategorySelected -> selectCategory(action.categoryId)
            is NewPasswordAction.OnPasswordChanged -> updateState(password = action.password)
            NewPasswordAction.OnTogglePasswordVisibility -> togglePasswordVisibility()
            NewPasswordAction.OnSaveClick -> savePassword()
        }
    }

    private fun updateState(
        title: String = _uiState.value.title,
        login: String = _uiState.value.login,
        password: String = _uiState.value.password
    ) {
        _uiState.update { state ->
            state.copy(
                title = title,
                login = login,
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
                    categoryErrorResId = null,
                    passwordErrorResId = null,
                    submitErrorResId = null
                )
            }

            when (
                val result = createPasswordUseCase(
                    title = _uiState.value.title,
                    login = _uiState.value.login,
                    categoryId = _uiState.value.selectedCategoryId,
                    categoryName = _uiState.value.selectedCategoryName,
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
                            categoryErrorResId = result.validation.categoryError.toCategoryErrorResId(),
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

    private fun openCategoryDialog() {
        _uiState.update { state ->
            state.copy(isCategoryDialogVisible = true)
        }
    }

    private fun closeCategoryDialog() {
        _uiState.update { state ->
            state.copy(isCategoryDialogVisible = false)
        }
    }

    private fun selectCategory(categoryId: Long) {
        val selectionState = _uiState.value.categorySelectionState
        if (selectionState !is NewPasswordCategorySelectionUiState.Content) {
            return
        }

        val selectedCategory = selectionState.categories.firstOrNull { it.id == categoryId } ?: return
        _uiState.update { state ->
            state.copy(
                selectedCategoryId = selectedCategory.id,
                selectedCategoryName = selectedCategory.name,
                isCategoryDialogVisible = false,
                categorySelectionState = selectionState.withSelection(selectedCategory.id),
                categoryErrorResId = null,
                submitErrorResId = null
            )
        }
    }

    private fun kotlinx.coroutines.flow.Flow<List<Category>>.collectIntoUiState() {
        viewModelScope.launch {
            collect { categories ->
                _uiState.update { state ->
                    val resolvedSelectedCategory = categories.firstOrNull {
                        it.id == state.selectedCategoryId
                    }
                    state.copy(
                        selectedCategoryId = resolvedSelectedCategory?.id,
                        selectedCategoryName = resolvedSelectedCategory?.name,
                        categorySelectionState = categories.toSelectionState(
                            selectedCategoryId = resolvedSelectedCategory?.id
                        )
                    )
                }
            }
        }
    }
}

private fun CreatePasswordCategoryError?.toCategoryErrorResId(): Int? = when (this) {
    CreatePasswordCategoryError.Missing -> R.string.new_password_category_error_missing
    CreatePasswordCategoryError.Invalid -> R.string.new_password_category_error_invalid
    null -> null
}

private fun CreatePasswordPasswordError?.toPasswordErrorResId(): Int? = when (this) {
    CreatePasswordPasswordError.Blank -> R.string.new_password_password_error_blank
    null -> null
}

private fun List<Category>.toSelectionState(
    selectedCategoryId: Long?
): NewPasswordCategorySelectionUiState {
    if (isEmpty()) {
        return NewPasswordCategorySelectionUiState.Empty
    }

    return NewPasswordCategorySelectionUiState.Content(
        categories = map { category ->
            NewPasswordCategoryOptionUiModel(
                id = category.id,
                name = category.name,
                isSelected = category.id == selectedCategoryId
            )
        }
    )
}

private fun NewPasswordCategorySelectionUiState.Content.withSelection(
    selectedCategoryId: Long
): NewPasswordCategorySelectionUiState.Content = copy(
    categories = categories.map { category ->
        category.copy(isSelected = category.id == selectedCategoryId)
    }
)
