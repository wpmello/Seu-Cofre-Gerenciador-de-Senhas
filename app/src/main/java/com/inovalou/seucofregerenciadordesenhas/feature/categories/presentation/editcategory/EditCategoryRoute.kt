package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

object EditCategoryRoute {
    const val categoryIdArg = "categoryId"
    const val openedFromArg = "openedFrom"
    const val routePattern = "categories/{$categoryIdArg}/edit?$openedFromArg={$openedFromArg}"

    fun createRoute(
        categoryId: Long,
        openedFrom: EditCategoryOpenedFrom = EditCategoryOpenedFrom.Categories
    ): String = "categories/$categoryId/edit?openedFrom=${openedFrom.routeValue}"
}
