package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.annotation.StringRes

data class NewPasswordUiState(
    val title: String = "",
    val login: String = "",
    val category: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null
)

sealed interface NewPasswordEffect {
    data object NavigateBack : NewPasswordEffect
}
