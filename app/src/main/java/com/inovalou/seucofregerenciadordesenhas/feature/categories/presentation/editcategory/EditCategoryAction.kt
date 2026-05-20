package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

sealed interface EditCategoryAction {
    data object OnBackClick : EditCategoryAction
    data class OnNameChanged(val name: String) : EditCategoryAction
    data object OnEditIconClick : EditCategoryAction
    data object OnIconPickerDismissed : EditCategoryAction
    data class OnIconSelected(val iconKey: String) : EditCategoryAction
    data class OnPasswordClick(val passwordId: Long) : EditCategoryAction
    data object OnSaveClick : EditCategoryAction
    data object OnDeleteButtonClick : EditCategoryAction
    data object OnDeleteDialogDismissed : EditCategoryAction
    data object OnSimpleDeleteConfirmed : EditCategoryAction
    data object OnDeleteAllSelected : EditCategoryAction
    data object OnDeleteAllCancelled : EditCategoryAction
    data object OnDeleteAllConfirmed : EditCategoryAction
    data object OnTransferSelected : EditCategoryAction
    data object OnTransferBackClick : EditCategoryAction
    data class OnTransferCategorySelected(val categoryId: Long) : EditCategoryAction
    data object OnTransferConfirmed : EditCategoryAction
    data object OnPostTransferDeleteCancelled : EditCategoryAction
    data object OnPostTransferDeleteConfirmed : EditCategoryAction
}
