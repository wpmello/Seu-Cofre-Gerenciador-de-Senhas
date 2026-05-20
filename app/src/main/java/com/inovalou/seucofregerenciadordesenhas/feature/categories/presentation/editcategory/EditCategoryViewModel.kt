package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.icon.VaultIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.DeleteCategoryWithAssociatedPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.GetCategoryByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.TransferPasswordsToCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.TransferPasswordsToCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryIconError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryNameError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.UpdateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObservePasswordsByCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val observePasswordsByCategoryUseCase: ObservePasswordsByCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val deleteCategoryWithAssociatedPasswordsUseCase: DeleteCategoryWithAssociatedPasswordsUseCase,
    private val transferPasswordsToCategoryUseCase: TransferPasswordsToCategoryUseCase,
    private val categoryIconCatalog: VaultIconCatalog,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase
) : ViewModel() {

    private val categoryId: Long? = savedStateHandle.get<Long>(EditCategoryRoute.categoryIdArg)
    private val openedFrom = EditCategoryOpenedFrom.fromRouteValue(
        savedStateHandle.get<String>(EditCategoryRoute.openedFromArg)
    )
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
            EditCategoryAction.OnBackClick -> navigateBackToOrigin()
            is EditCategoryAction.OnNameChanged -> onNameChanged(action.name)
            EditCategoryAction.OnEditIconClick -> {
                _uiState.update { it.copy(isIconPickerVisible = true) }
            }
            EditCategoryAction.OnIconPickerDismissed -> {
                _uiState.update { it.copy(isIconPickerVisible = false) }
            }
            is EditCategoryAction.OnIconSelected -> onIconSelected(action.iconKey)
            is EditCategoryAction.OnPasswordClick -> openPassword(action.passwordId)
            EditCategoryAction.OnSaveClick -> saveCategory()
            EditCategoryAction.OnDeleteButtonClick -> onDeleteButtonClick()
            EditCategoryAction.OnDeleteDialogDismissed -> dismissDeleteFlow()
            EditCategoryAction.OnSimpleDeleteConfirmed -> deleteCategory()
            EditCategoryAction.OnDeleteAllSelected -> showDeleteAllConfirmation()
            EditCategoryAction.OnDeleteAllCancelled -> showAssociatedPasswordsChoice()
            EditCategoryAction.OnDeleteAllConfirmed -> deleteCategoryWithAssociatedPasswords()
            EditCategoryAction.OnTransferSelected -> showTransferSelection()
            EditCategoryAction.OnTransferBackClick -> showAssociatedPasswordsChoice()
            is EditCategoryAction.OnTransferCategorySelected -> selectTransferCategory(action.categoryId)
            EditCategoryAction.OnTransferConfirmed -> transferPasswords()
            EditCategoryAction.OnPostTransferDeleteCancelled -> dismissDeleteFlow()
            EditCategoryAction.OnPostTransferDeleteConfirmed -> deleteCategory()
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

            observeAssociatedPasswords(resolvedCategoryId)
            observerCategoriesFromDatabase()
        }
    }

    private fun navigateBackToOrigin() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        viewModelScope.launch {
            _effects.emit(EditCategoryEffect.NavigateBackToOrigin(openedFrom))
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

    private fun openPassword(passwordId: Long) {
        viewModelScope.launch {
            _effects.emit(EditCategoryEffect.OpenPassword(passwordId))
        }
    }

    private fun saveCategory() {
        val resolvedCategoryId = categoryId ?: return
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
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
                    _effects.emit(EditCategoryEffect.NavigateToCategories)
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
        val currentDeleteFlowState = _uiState.value.deleteFlowState
        if (currentDeleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        if (
            currentDeleteFlowState !is EditCategoryDeleteFlowState.SimpleDeleteConfirmation &&
            currentDeleteFlowState !is EditCategoryDeleteFlowState.PostTransferDeleteConfirmation
        ) {
            return
        }
        if (
            currentDeleteFlowState is EditCategoryDeleteFlowState.SimpleDeleteConfirmation &&
            _uiState.value.associatedPasswordsCount > 0
        ) {
            showAssociatedPasswordsChoice()
            return
        }

        _uiState.update { state ->
            state.copy(
                deleteFlowState = EditCategoryDeleteFlowState.CriticalOperation(
                    titleResId = R.string.edit_category_deleting_category_operation_title,
                    messageResId = R.string.edit_category_deleting_category_operation_message,
                    stepResIds = listOf(R.string.edit_category_critical_step_delete_category),
                    activeStepIndex = 0
                ),
                operationErrorResId = null
            )
        }

        viewModelScope.launch {
            when (deleteCategoryUseCase(resolvedCategoryId)) {
                DeleteCategoryResult.Success -> {
                    _uiState.update { it.copy(deleteFlowState = EditCategoryDeleteFlowState.Idle) }
                    _effects.emit(EditCategoryEffect.NavigateToCategories)
                }
                DeleteCategoryResult.NotFound -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Idle,
                            contentState = EditCategoryContentState.Error(
                                R.string.edit_category_not_found_error
                            )
                        )
                    }
                }
                DeleteCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Error(
                                R.string.edit_category_delete_error
                            ),
                            operationErrorResId = R.string.edit_category_delete_error
                        )
                    }
                }
            }
        }
    }

    private fun deleteCategoryWithAssociatedPasswords() {
        val resolvedCategoryId = categoryId ?: return
        if (_uiState.value.deleteFlowState !is EditCategoryDeleteFlowState.DeleteAllConfirmation) {
            return
        }

        _uiState.update { state ->
            state.copy(
                deleteFlowState = EditCategoryDeleteFlowState.CriticalOperation(
                    titleResId = R.string.edit_category_delete_all_operation_title,
                    messageResId = R.string.edit_category_delete_all_operation_message,
                    stepResIds = listOf(
                        R.string.edit_category_critical_step_delete_passwords,
                        R.string.edit_category_critical_step_delete_category
                    ),
                    activeStepIndex = 0
                ),
                operationErrorResId = null
            )
        }

        viewModelScope.launch {
            when (deleteCategoryWithAssociatedPasswordsUseCase(resolvedCategoryId)) {
                DeleteCategoryResult.Success -> {
                    _uiState.update { it.copy(deleteFlowState = EditCategoryDeleteFlowState.Idle) }
                    _effects.emit(EditCategoryEffect.NavigateToCategories)
                }
                DeleteCategoryResult.NotFound -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Idle,
                            contentState = EditCategoryContentState.Error(
                                R.string.edit_category_not_found_error
                            )
                        )
                    }
                }
                DeleteCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Error(
                                R.string.edit_category_delete_error
                            ),
                            operationErrorResId = R.string.edit_category_delete_error
                        )
                    }
                }
            }
        }
    }

    private fun transferPasswords() {
        val resolvedCategoryId = categoryId ?: return
        val selectedCategoryId = _uiState.value.selectedTransferCategoryId ?: return
        if (_uiState.value.deleteFlowState !is EditCategoryDeleteFlowState.TransferSelection) {
            return
        }
        if (_uiState.value.availableTransferCategories.none { it.id == selectedCategoryId }) {
            return
        }

        _uiState.update { state ->
            state.copy(
                deleteFlowState = EditCategoryDeleteFlowState.CriticalOperation(
                    titleResId = R.string.edit_category_transfer_operation_title,
                    messageResId = R.string.edit_category_transfer_operation_message,
                    stepResIds = listOf(R.string.edit_category_critical_step_transfer_passwords),
                    activeStepIndex = 0
                ),
                operationErrorResId = null
            )
        }

        viewModelScope.launch {
            when (
                transferPasswordsToCategoryUseCase(
                    sourceCategoryId = resolvedCategoryId,
                    targetCategoryId = selectedCategoryId
                )
            ) {
                TransferPasswordsToCategoryResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.PostTransferDeleteConfirmation,
                            selectedTransferCategoryId = null
                        )
                    }
                }
                TransferPasswordsToCategoryResult.SourceNotFound,
                TransferPasswordsToCategoryResult.TargetNotFound -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Error(
                                R.string.edit_category_not_found_error
                            ),
                            operationErrorResId = R.string.edit_category_not_found_error
                        )
                    }
                }
                TransferPasswordsToCategoryResult.SameCategory -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Error(
                                R.string.edit_category_transfer_invalid_target_error
                            ),
                            operationErrorResId = R.string.edit_category_transfer_invalid_target_error
                        )
                    }
                }
                TransferPasswordsToCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteFlowState = EditCategoryDeleteFlowState.Error(
                                R.string.edit_category_transfer_error
                            ),
                            operationErrorResId = R.string.edit_category_transfer_error
                        )
                    }
                }
            }
        }
    }

    private fun onDeleteButtonClick() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        if (_uiState.value.associatedPasswordsCount == 0) {
            _uiState.update {
                it.copy(
                    deleteFlowState = EditCategoryDeleteFlowState.SimpleDeleteConfirmation,
                    operationErrorResId = null
                )
            }
        } else {
            showAssociatedPasswordsChoice()
        }
    }

    private fun dismissDeleteFlow() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        _uiState.update {
            it.copy(
                deleteFlowState = EditCategoryDeleteFlowState.Idle,
                selectedTransferCategoryId = null,
                operationErrorResId = null
            )
        }
    }

    private fun showAssociatedPasswordsChoice() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        _uiState.update { state ->
            state.copy(
                deleteFlowState = EditCategoryDeleteFlowState.AssociatedPasswordsChoice(
                    passwordCount = state.associatedPasswordsCount
                ),
                operationErrorResId = null
            )
        }
    }

    private fun showDeleteAllConfirmation() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        _uiState.update { state ->
            state.copy(
                deleteFlowState = EditCategoryDeleteFlowState.DeleteAllConfirmation(
                    passwordCount = state.associatedPasswordsCount
                ),
                operationErrorResId = null
            )
        }
    }

    private fun showTransferSelection() {
        if (_uiState.value.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation) {
            return
        }
        _uiState.update { state ->
            val selectedCategoryId = state.selectedTransferCategoryId
                ?.takeIf { selectedId ->
                    state.availableTransferCategories.any { category -> category.id == selectedId }
                }
            state.copy(
                selectedTransferCategoryId = selectedCategoryId,
                deleteFlowState = EditCategoryDeleteFlowState.TransferSelection(
                    passwordCount = state.associatedPasswordsCount,
                    categories = state.availableTransferCategories,
                    selectedCategoryId = selectedCategoryId
                ),
                operationErrorResId = null
            )
        }
    }

    private fun selectTransferCategory(categoryId: Long) {
        val currentFlowState = _uiState.value.deleteFlowState
        if (currentFlowState !is EditCategoryDeleteFlowState.TransferSelection) {
            return
        }
        if (_uiState.value.availableTransferCategories.none { it.id == categoryId }) {
            return
        }
        _uiState.update { state ->
            state.copy(
                selectedTransferCategoryId = categoryId,
                deleteFlowState = currentFlowState.copy(selectedCategoryId = categoryId)
            )
        }
    }

    private fun observerCategoriesFromDatabase() {
        viewModelScope.launch {
            observeCategoriesUseCase().collect { categories ->
                _uiState.update { state ->
                    val availableCategories = categories
                        .filter { category -> category.id != categoryId }
                        .map { category -> category.toTransferOptionUiModel() }
                    val selectedCategoryId = state.selectedTransferCategoryId
                        ?.takeIf { selectedId ->
                            availableCategories.any { category -> category.id == selectedId }
                        }
                    state.copy(
                        availableTransferCategories = availableCategories,
                        selectedTransferCategoryId = selectedCategoryId,
                        deleteFlowState = state.deleteFlowState.withUpdatedCategoryOptions(
                            categories = availableCategories,
                            selectedCategoryId = selectedCategoryId
                        )
                    )
                }
            }
        }
    }

    private fun observeAssociatedPasswords(categoryId: Long) {
        viewModelScope.launch {
            observePasswordsByCategoryUseCase(categoryId).collect { passwords ->
                _uiState.update { state ->
                    val passwordCount = passwords.size
                    state.copy(
                        associatedPasswordsCount = passwordCount,
                        passwordsSectionState = passwords.toPasswordsSectionState(),
                        deleteFlowState = state.deleteFlowState.withUpdatedPasswordCount(passwordCount)
                    )
                }
            }
        }
    }

    private fun Category.toTransferOptionUiModel(): CategoryTransferOptionUiModel {
        val icon = categoryIconCatalog.resolve(iconKey)
        return CategoryTransferOptionUiModel(
            id = id,
            name = name,
            iconKey = icon.iconKey,
            iconResId = icon.drawableResId,
            itemCount = itemCount
        )
    }
}

private fun EditCategoryDeleteFlowState.withUpdatedPasswordCount(
    passwordCount: Int
): EditCategoryDeleteFlowState = when (this) {
    is EditCategoryDeleteFlowState.AssociatedPasswordsChoice -> copy(passwordCount = passwordCount)
    is EditCategoryDeleteFlowState.DeleteAllConfirmation -> copy(passwordCount = passwordCount)
    is EditCategoryDeleteFlowState.TransferSelection -> copy(passwordCount = passwordCount)
    EditCategoryDeleteFlowState.Idle,
    EditCategoryDeleteFlowState.SimpleDeleteConfirmation,
    EditCategoryDeleteFlowState.PostTransferDeleteConfirmation,
    is EditCategoryDeleteFlowState.CriticalOperation,
    is EditCategoryDeleteFlowState.Error -> this
}

private fun EditCategoryDeleteFlowState.withUpdatedCategoryOptions(
    categories: List<CategoryTransferOptionUiModel>,
    selectedCategoryId: Long?
): EditCategoryDeleteFlowState = when (this) {
    is EditCategoryDeleteFlowState.TransferSelection -> copy(
        categories = categories,
        selectedCategoryId = selectedCategoryId
    )
    EditCategoryDeleteFlowState.Idle,
    EditCategoryDeleteFlowState.SimpleDeleteConfirmation,
    is EditCategoryDeleteFlowState.AssociatedPasswordsChoice,
    is EditCategoryDeleteFlowState.DeleteAllConfirmation,
    EditCategoryDeleteFlowState.PostTransferDeleteConfirmation,
    is EditCategoryDeleteFlowState.CriticalOperation,
    is EditCategoryDeleteFlowState.Error -> this
}

private fun UpdateCategoryNameError?.toNameErrorResId(): Int? = when (this) {
    UpdateCategoryNameError.Blank -> R.string.edit_category_name_error_blank
    null -> null
}

private fun UpdateCategoryIconError?.toIconErrorResId(): Int? = when (this) {
    UpdateCategoryIconError.Missing -> R.string.edit_category_icon_error_missing
    null -> null
}

private fun List<PasswordSummary>.toPasswordsSectionState(): CategoryPasswordsSectionUiState {
    if (isEmpty()) {
        return CategoryPasswordsSectionUiState.Empty
    }

    return CategoryPasswordsSectionUiState.Content(
        passwords = map { password ->
            CategoryPasswordItemUiModel(
                id = password.id,
                title = password.title,
                supportingText = password.login,
                securityLevel = password.securityRiskLevel.toCategoryPasswordItemSecurityLevel()
            )
        }
    )
}

private fun PasswordSecurityRiskLevel.toCategoryPasswordItemSecurityLevel(): CategoryPasswordItemSecurityLevel =
    when (this) {
        PasswordSecurityRiskLevel.High -> CategoryPasswordItemSecurityLevel.Weak
        PasswordSecurityRiskLevel.Medium -> CategoryPasswordItemSecurityLevel.Moderate
        PasswordSecurityRiskLevel.Low -> CategoryPasswordItemSecurityLevel.Safe
    }
