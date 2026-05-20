package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel

data class EditCategoryUiState(
    val name: String = "",
    val availableIcons: List<CategorySelectableIconUiModel> = emptyList(),
    val selectedIconKey: String? = null,
    val contentState: EditCategoryContentState = EditCategoryContentState.Loading,
    val isIconPickerVisible: Boolean = false,
    val deleteFlowState: EditCategoryDeleteFlowState = EditCategoryDeleteFlowState.Idle,
    val associatedPasswordsCount: Int = 0,
    val availableTransferCategories: List<CategoryTransferOptionUiModel> = emptyList(),
    val selectedTransferCategoryId: Long? = null,
    @StringRes val nameErrorResId: Int? = null,
    @StringRes val iconErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    @StringRes val operationErrorResId: Int? = null,
    val isSaving: Boolean = false,
    val passwordsSectionState: CategoryPasswordsSectionUiState = CategoryPasswordsSectionUiState.Empty
)

sealed interface EditCategoryDeleteFlowState {
    data object Idle : EditCategoryDeleteFlowState
    data object SimpleDeleteConfirmation : EditCategoryDeleteFlowState
    data class AssociatedPasswordsChoice(
        val passwordCount: Int
    ) : EditCategoryDeleteFlowState
    data class DeleteAllConfirmation(
        val passwordCount: Int
    ) : EditCategoryDeleteFlowState
    data class TransferSelection(
        val passwordCount: Int,
        val categories: List<CategoryTransferOptionUiModel>,
        val selectedCategoryId: Long?
    ) : EditCategoryDeleteFlowState
    data object PostTransferDeleteConfirmation : EditCategoryDeleteFlowState
    data class CriticalOperation(
        @StringRes val titleResId: Int,
        @StringRes val messageResId: Int,
        @StringRes val stepResIds: List<Int>,
        val activeStepIndex: Int
    ) : EditCategoryDeleteFlowState
    data class Error(@StringRes val messageResId: Int) : EditCategoryDeleteFlowState
}

sealed interface EditCategoryContentState {
    data object Loading : EditCategoryContentState
    data object Content : EditCategoryContentState
    data class Error(@StringRes val messageResId: Int) : EditCategoryContentState
}

sealed interface CategoryPasswordsSectionUiState {
    data object Empty : CategoryPasswordsSectionUiState
    data class Content(
        val passwords: List<CategoryPasswordItemUiModel>
    ) : CategoryPasswordsSectionUiState
}

data class CategoryPasswordItemUiModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val securityLevel: CategoryPasswordItemSecurityLevel = CategoryPasswordItemSecurityLevel.Weak
)

data class CategoryTransferOptionUiModel(
    val id: Long,
    val name: String,
    val iconKey: String,
    val iconResId: Int,
    val itemCount: Int
)

enum class CategoryPasswordItemSecurityLevel {
    Weak,
    Moderate,
    Safe
}

sealed interface EditCategoryEffect {
    data class NavigateBackToOrigin(val openedFrom: EditCategoryOpenedFrom) : EditCategoryEffect
    data class OpenPassword(val passwordId: Long) : EditCategoryEffect
    data object NavigateToCategories : EditCategoryEffect
}
