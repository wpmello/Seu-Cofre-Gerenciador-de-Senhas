package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R

data class PasswordsUiState(
    val query: String = "",
    val allPasswords: List<PasswordListItemUiModel> = emptyList(),
    val filteredPasswords: List<PasswordListItemUiModel> = emptyList(),
    val totalPasswords: Int = 0,
    val contentState: PasswordsContentState = PasswordsContentState.Loading
)

data class PasswordListItemUiModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val initials: String
)

sealed interface PasswordsContentState {
    data object Loading : PasswordsContentState
    data object Content : PasswordsContentState
    data object EmptyPasswords : PasswordsContentState
    data object EmptySearchResult : PasswordsContentState
    data class Error(@StringRes val messageResId: Int) : PasswordsContentState
}

sealed interface PasswordsEffect {
    data class OpenPasswordDetails(val passwordId: Long) : PasswordsEffect
    data object NavigateToNewPassword : PasswordsEffect
}
