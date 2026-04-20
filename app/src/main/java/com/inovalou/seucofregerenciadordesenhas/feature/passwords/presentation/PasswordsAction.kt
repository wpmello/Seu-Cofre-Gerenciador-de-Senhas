package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

sealed interface PasswordsAction {
    data class OnSearchQueryChanged(val query: String) : PasswordsAction
    data class OnPasswordClick(val passwordId: Long) : PasswordsAction
}
