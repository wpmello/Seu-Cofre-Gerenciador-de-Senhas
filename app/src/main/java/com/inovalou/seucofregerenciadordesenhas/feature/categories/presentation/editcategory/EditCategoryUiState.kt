package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel

data class EditCategoryUiState(
    val name: String = "",
    val availableIcons: List<CategorySelectableIconUiModel> = emptyList(),
    val selectedIconKey: String? = null,
    val contentState: EditCategoryContentState = EditCategoryContentState.Loading,
    val isIconPickerVisible: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
    @StringRes val nameErrorResId: Int? = null,
    @StringRes val iconErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    @StringRes val deleteErrorResId: Int? = null,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val passwordsSectionState: CategoryPasswordsSectionUiState = CategoryPasswordsSectionUiState.Empty
)

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
    val supportingText: String
)

sealed interface EditCategoryEffect {
    data class NavigateBackToOrigin(val openedFrom: EditCategoryOpenedFrom) : EditCategoryEffect
    data object NavigateToCategories : EditCategoryEffect
}
