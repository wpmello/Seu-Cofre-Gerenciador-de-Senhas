package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

sealed interface AllCategoriesAction {
    data class OnSearchQueryChanged(val query: String) : AllCategoriesAction
    data class OnCategoryClick(val categoryId: Long) : AllCategoriesAction
}
