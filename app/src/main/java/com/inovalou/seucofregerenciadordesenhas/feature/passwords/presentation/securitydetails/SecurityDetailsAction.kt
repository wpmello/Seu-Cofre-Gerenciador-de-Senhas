package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

sealed interface SecurityDetailsAction {
    data object OnBackClick : SecurityDetailsAction
    data class OnTabSelected(val tab: SecurityDetailsTab) : SecurityDetailsAction
    data class OnPasswordClick(val passwordId: Long) : SecurityDetailsAction
}
