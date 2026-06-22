package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

sealed interface EditPasswordAction {
    data object OnBackClick : EditPasswordAction
    data class OnTitleChanged(val title: String) : EditPasswordAction
    data class OnEmailChanged(val email: String) : EditPasswordAction
    data object OnCategoryFieldClick : EditPasswordAction
    data object OnCategoryDialogDismissed : EditPasswordAction
    data class OnCategorySelected(val categoryId: Long) : EditPasswordAction
    data class OnPasswordChanged(val password: String) : EditPasswordAction
    data class OnNoteChanged(val note: String) : EditPasswordAction
    data object OnTogglePasswordVisibility : EditPasswordAction
    data object OnCopyEmailClick : EditPasswordAction
    data object OnCopyPasswordClick : EditPasswordAction
    data object OnSuggestStrongPasswordClick : EditPasswordAction
    data object OnPasswordHistoryClick : EditPasswordAction
    data object OnComingSoonDialogDismissed : EditPasswordAction
    data object OnSaveClick : EditPasswordAction
    data object OnDeleteClick : EditPasswordAction
    data object OnDeleteDialogDismissed : EditPasswordAction
    data object OnDeleteConfirmed : EditPasswordAction
    data object OnLocalAuthenticationSucceeded : EditPasswordAction
    data object OnLocalAuthenticationCancelled : EditPasswordAction
    data object OnLocalAuthenticationFailed : EditPasswordAction
    data object OnLocalAuthenticationUnavailable : EditPasswordAction
    data object OnLocalAuthenticationRetryClick : EditPasswordAction
}
