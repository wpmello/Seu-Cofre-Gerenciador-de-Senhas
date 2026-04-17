package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

object EditCategoryRoute {
    const val categoryIdArg = "categoryId"
    const val routePattern = "categories/{$categoryIdArg}/edit"

    fun createRoute(categoryId: Long): String = "categories/$categoryId/edit"
}
