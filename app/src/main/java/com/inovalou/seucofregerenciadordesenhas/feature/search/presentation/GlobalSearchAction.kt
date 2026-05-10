package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

sealed interface GlobalSearchAction {
    data class OnQueryChanged(val query: String) : GlobalSearchAction
    data class OnCategoryClick(val categoryId: Long) : GlobalSearchAction
    data class OnPasswordClick(val passwordId: Long) : GlobalSearchAction
}
