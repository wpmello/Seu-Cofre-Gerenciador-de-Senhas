package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

sealed interface NewCategoryAction {
    data object OnBackClick : NewCategoryAction
    data class OnNameChanged(val name: String) : NewCategoryAction
    data class OnIconSelected(val iconKey: String) : NewCategoryAction
    data object OnCreateCategoryClick : NewCategoryAction
}
