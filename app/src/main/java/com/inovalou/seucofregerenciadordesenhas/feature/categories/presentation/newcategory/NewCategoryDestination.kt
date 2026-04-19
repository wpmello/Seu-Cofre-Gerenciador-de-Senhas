package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

object NewCategoryDestination {
    const val openedFromArg = "openedFrom"
    private const val baseRoute = "categories/new"
    const val routePattern = "$baseRoute?$openedFromArg={$openedFromArg}"

    fun createRoute(
        openedFrom: NewCategoryOpenedFrom = NewCategoryOpenedFrom.Categories
    ): String = "$baseRoute?$openedFromArg=${openedFrom.routeValue}"
}
