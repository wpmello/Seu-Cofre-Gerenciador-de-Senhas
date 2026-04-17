package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoryCardUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryOpenedFrom

data class AllCategoriesUiState(
    val query: String = "",
    val allCategories: List<CategoryCardUiModel> = emptyList(),
    val filteredCategories: List<CategoryCardUiModel> = emptyList(),
    val contentState: AllCategoriesContentState = AllCategoriesContentState.Loading
)

sealed interface AllCategoriesContentState {
    data object Loading : AllCategoriesContentState
    data object Content : AllCategoriesContentState
    data object EmptyCategories : AllCategoriesContentState
    data object EmptySearchResult : AllCategoriesContentState
    data class Error(@StringRes val messageResId: Int) : AllCategoriesContentState
}

sealed interface AllCategoriesEffect {
    data class NavigateToEditCategory(
        val categoryId: Long,
        val openedFrom: EditCategoryOpenedFrom
    ) : AllCategoriesEffect
}
