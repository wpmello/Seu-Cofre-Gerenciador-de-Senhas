package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

enum class NewCategoryOpenedFrom(val routeValue: String) {
    Categories("categories"),
    AllCategories("all_categories"),
    Vault("vault");

    companion object {
        fun fromRouteValue(routeValue: String?): NewCategoryOpenedFrom =
            entries.firstOrNull { openedFrom -> openedFrom.routeValue == routeValue }
                ?: Categories
    }
}
