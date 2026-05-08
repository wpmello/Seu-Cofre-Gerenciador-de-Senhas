package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

sealed interface VaultHomeAction {
    data object OnSearchClick : VaultHomeAction
    data class OnCategoryClick(val categoryId: Long) : VaultHomeAction
    data object OnOtherCategoriesClick : VaultHomeAction
    data object OnViewAllPasswordsClick : VaultHomeAction
    data class OnRecentPasswordClick(val passwordId: Long) : VaultHomeAction
    data object OnAddPasswordClick : VaultHomeAction
}
