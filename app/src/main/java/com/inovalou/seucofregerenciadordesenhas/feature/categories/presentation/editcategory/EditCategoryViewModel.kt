package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.GetCategoryByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryIconError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryNameError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
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
class EditCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    private val categoryId: Long? = savedStateHandle.get<Long>(EditCategoryRoute.categoryIdArg)
    private val iconOptions = categoryIconCatalog.all()
    private val defaultIconKey = categoryIconCatalog.default().iconKey

    private val _uiState = MutableStateFlow(
        EditCategoryUiState(
            availableIcons = iconOptions.map { option ->
                CategorySelectableIconUiModel(
                    iconKey = option.iconKey,
                    iconResId = option.drawableResId,
                    isSelected = option.iconKey == defaultIconKey
                )
            },
            selectedIconKey = defaultIconKey
        )
    )
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EditCategoryEffect>()
    val effects: SharedFlow<EditCategoryEffect> = _effects.asSharedFlow()

    init {
        loadCategory()
    }

    fun onAction(action: EditCategoryAction) {
        when (action) {
            is EditCategoryAction.OnNameChanged -> onNameChanged(action.name)
            EditCategoryAction.OnEditIconClick -> {
                _uiState.update { it.copy(isIconPickerVisible = true) }
            }
            EditCategoryAction.OnIconPickerDismissed -> {
                _uiState.update { it.copy(isIconPickerVisible = false) }
            }
            is EditCategoryAction.OnIconSelected -> onIconSelected(action.iconKey)
            EditCategoryAction.OnSaveClick -> saveCategory()
            EditCategoryAction.OnDeleteClick -> {
                _uiState.update {
                    it.copy(
                        isDeleteConfirmationVisible = true,
                        deleteErrorResId = null
                    )
                }
            }
            EditCategoryAction.OnDeleteDismissed -> {
                _uiState.update { it.copy(isDeleteConfirmationVisible = false) }
            }
            EditCategoryAction.OnDeleteConfirmed -> deleteCategory()
        }
    }

    private fun loadCategory() {
        val resolvedCategoryId = categoryId
        if (resolvedCategoryId == null) {
            _uiState.update {
                it.copy(
                    contentState = EditCategoryContentState.Error(R.string.edit_category_load_error)
                )
            }
            return
        }

        viewModelScope.launch {
            val category = getCategoryByIdUseCase(resolvedCategoryId)
            if (category == null) {
                _uiState.update {
                    it.copy(
                        contentState = EditCategoryContentState.Error(
                            R.string.edit_category_not_found_error
                        )
                    )
                }
                return@launch
            }

            _uiState.update { state ->
                state.copy(
                    contentState = EditCategoryContentState.Content,
                    name = category.name,
                    selectedIconKey = category.iconKey,
                    availableIcons = state.availableIcons.map { icon ->
                        icon.copy(isSelected = icon.iconKey == category.iconKey)
                    }
                )
            }
        }
    }

    private fun onNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name,
                nameErrorResId = null,
                submitErrorResId = null
            )
        }
    }

    private fun onIconSelected(iconKey: String) {
        _uiState.update { state ->
            state.copy(
                selectedIconKey = iconKey,
                iconErrorResId = null,
                submitErrorResId = null,
                isIconPickerVisible = false,
                availableIcons = state.availableIcons.map { icon ->
                    icon.copy(isSelected = icon.iconKey == iconKey)
                }
            )
        }
    }

    private fun saveCategory() {
        val resolvedCategoryId = categoryId ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isSaving = true,
                    nameErrorResId = null,
                    iconErrorResId = null,
                    submitErrorResId = null
                )
            }

            when (
                val result = updateCategoryUseCase(
                    categoryId = resolvedCategoryId,
                    name = _uiState.value.name,
                    iconKey = _uiState.value.selectedIconKey
                )
            ) {
                UpdateCategoryResult.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    _effects.emit(EditCategoryEffect.NavigateBack)
                }
                is UpdateCategoryResult.ValidationError -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            nameErrorResId = result.validation.nameError.toNameErrorResId(),
                            iconErrorResId = result.validation.iconError.toIconErrorResId()
                        )
                    }
                }
                UpdateCategoryResult.NotFound -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            contentState = EditCategoryContentState.Error(
                                R.string.edit_category_not_found_error
                            )
                        )
                    }
                }
                UpdateCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            submitErrorResId = R.string.edit_category_update_error
                        )
                    }
                }
            }
        }
    }

    private fun deleteCategory() {
        val resolvedCategoryId = categoryId ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isDeleting = true,
                    deleteErrorResId = null
                )
            }

            when (deleteCategoryUseCase(resolvedCategoryId)) {
                DeleteCategoryResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _effects.emit(EditCategoryEffect.NavigateBack)
                }
                DeleteCategoryResult.NotFound -> {
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            isDeleteConfirmationVisible = false,
                            contentState = EditCategoryContentState.Error(
                                R.string.edit_category_not_found_error
                            )
                        )
                    }
                }
                DeleteCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            deleteErrorResId = R.string.edit_category_delete_error
                        )
                    }
                }
            }
        }
    }
}

private fun UpdateCategoryNameError?.toNameErrorResId(): Int? = when (this) {
    UpdateCategoryNameError.Blank -> R.string.edit_category_name_error_blank
    null -> null
}

private fun UpdateCategoryIconError?.toIconErrorResId(): Int? = when (this) {
    UpdateCategoryIconError.Missing -> R.string.edit_category_icon_error_missing
    null -> null
}
