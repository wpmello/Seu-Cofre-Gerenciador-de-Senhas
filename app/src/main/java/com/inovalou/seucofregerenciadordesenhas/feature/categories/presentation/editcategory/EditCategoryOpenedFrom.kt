package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

enum class EditCategoryOpenedFrom(val routeValue: String) {
    Vault("vault"),
    Categories("categories"),
    AllCategories("all_categories");

    companion object {
        fun fromRouteValue(routeValue: String?): EditCategoryOpenedFrom =
            entries.firstOrNull { openedFrom -> openedFrom.routeValue == routeValue } ?: Categories
    }
}
