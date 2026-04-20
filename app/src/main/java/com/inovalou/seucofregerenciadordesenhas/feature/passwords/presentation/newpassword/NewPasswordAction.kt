package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

sealed interface NewPasswordAction {
    data object OnBackClick : NewPasswordAction
    data class OnTitleChanged(val title: String) : NewPasswordAction
    data class OnLoginChanged(val login: String) : NewPasswordAction
    data class OnCategoryChanged(val category: String) : NewPasswordAction
    data class OnPasswordChanged(val password: String) : NewPasswordAction
    data object OnTogglePasswordVisibility : NewPasswordAction
    data object OnSaveClick : NewPasswordAction
}
