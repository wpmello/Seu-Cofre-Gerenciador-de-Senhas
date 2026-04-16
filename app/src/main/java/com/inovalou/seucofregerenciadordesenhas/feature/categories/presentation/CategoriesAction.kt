package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

sealed interface CategoriesAction {
    data object OnViewAllClick : CategoriesAction
    data object OnSearchClick : CategoriesAction
}
