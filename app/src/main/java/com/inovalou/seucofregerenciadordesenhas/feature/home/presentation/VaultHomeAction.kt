package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

sealed interface VaultHomeAction {
    data object OnSearchClick : VaultHomeAction
    data class OnSecuritySummaryTagClick(val filter: VaultHomeSecurityFilter) : VaultHomeAction
    data object OnSecuritySummaryBackClick : VaultHomeAction
    data class OnSecuritySummaryPasswordClick(val passwordId: Long) : VaultHomeAction
    data class OnCategoryClick(val categoryId: Long) : VaultHomeAction
    data object OnOtherCategoriesClick : VaultHomeAction
    data object OnViewAllPasswordsClick : VaultHomeAction
    data class OnRecentPasswordClick(val passwordId: Long) : VaultHomeAction
}
