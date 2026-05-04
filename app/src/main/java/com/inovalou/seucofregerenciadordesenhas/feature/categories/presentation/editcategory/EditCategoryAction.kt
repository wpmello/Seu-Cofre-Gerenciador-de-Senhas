package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

sealed interface EditCategoryAction {
    data object OnBackClick : EditCategoryAction
    data class OnNameChanged(val name: String) : EditCategoryAction
    data object OnEditIconClick : EditCategoryAction
    data object OnIconPickerDismissed : EditCategoryAction
    data class OnIconSelected(val iconKey: String) : EditCategoryAction
    data class OnPasswordClick(val passwordId: Long) : EditCategoryAction
    data object OnSaveClick : EditCategoryAction
    data object OnDeleteClick : EditCategoryAction
    data object OnDeleteDismissed : EditCategoryAction
    data object OnDeleteConfirmed : EditCategoryAction
}
