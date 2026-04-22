package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

sealed interface NewPasswordAction {
    data object OnBackClick : NewPasswordAction
    data class OnTitleChanged(val title: String) : NewPasswordAction
    data class OnLoginChanged(val login: String) : NewPasswordAction
    data object OnCategoryFieldClick : NewPasswordAction
    data object OnCategoryDialogDismissed : NewPasswordAction
    data class OnCategorySelected(val categoryId: Long) : NewPasswordAction
    data class OnPasswordChanged(val password: String) : NewPasswordAction
    data object OnTogglePasswordVisibility : NewPasswordAction
    data object OnSaveClick : NewPasswordAction
}
