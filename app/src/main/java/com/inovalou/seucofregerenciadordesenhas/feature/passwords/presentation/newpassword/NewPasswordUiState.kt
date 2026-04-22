package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.annotation.StringRes

data class NewPasswordUiState(
    val title: String = "",
    val login: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val isCategoryDialogVisible: Boolean = false,
    val categorySelectionState: NewPasswordCategorySelectionUiState =
        NewPasswordCategorySelectionUiState.Loading,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val categoryErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null
)

sealed interface NewPasswordCategorySelectionUiState {
    data object Loading : NewPasswordCategorySelectionUiState
    data object Empty : NewPasswordCategorySelectionUiState
    data class Content(
        val categories: List<NewPasswordCategoryOptionUiModel>
    ) : NewPasswordCategorySelectionUiState
}

data class NewPasswordCategoryOptionUiModel(
    val id: Long,
    val name: String,
    val isSelected: Boolean
)

sealed interface NewPasswordEffect {
    data object NavigateBack : NewPasswordEffect
}
