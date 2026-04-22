package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

sealed interface EditPasswordAction {
    data object OnBackClick : EditPasswordAction
    data class OnTitleChanged(val title: String) : EditPasswordAction
    data class OnEmailChanged(val email: String) : EditPasswordAction
    data class OnPasswordChanged(val password: String) : EditPasswordAction
    data object OnTogglePasswordVisibility : EditPasswordAction
    data object OnCopyEmailClick : EditPasswordAction
    data object OnCopyPasswordClick : EditPasswordAction
    data object OnSaveClick : EditPasswordAction
    data object OnDeleteClick : EditPasswordAction
}
