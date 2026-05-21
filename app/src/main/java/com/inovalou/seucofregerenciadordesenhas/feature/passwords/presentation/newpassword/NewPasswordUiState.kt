package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState

data class NewPasswordUiState(
    val title: String = "",
    val login: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val isCategoryDialogVisible: Boolean = false,
    val categorySelectionState: PasswordCategorySelectionUiState =
        PasswordCategorySelectionUiState.Loading,
    val password: String = "",
    val note: String = "",
    val isPasswordVisible: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val categoryErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null
)

sealed interface NewPasswordEffect {
    data object NavigateBack : NewPasswordEffect
    data class NavigateBackToOrigin(
        val openedFrom: NewPasswordOpenedFrom
    ) : NewPasswordEffect
}
