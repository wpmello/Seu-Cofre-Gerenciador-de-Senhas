package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import org.junit.Assert.assertEquals
import org.junit.Test

class EditCategoryRouteTest {

    @Test
    fun givenVaultOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "categories/9/edit?openedFrom=vault",
            EditCategoryRoute.createRoute(
                categoryId = 9L,
                openedFrom = EditCategoryOpenedFrom.Vault
            )
        )
    }

    @Test
    fun givenAllCategoriesOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "categories/9/edit?openedFrom=all_categories",
            EditCategoryRoute.createRoute(
                categoryId = 9L,
                openedFrom = EditCategoryOpenedFrom.AllCategories
            )
        )
    }

    @Test
    fun givenNoOriginOverride_whenCreatingRoute_thenDefaultsToCategoriesOrigin() {
        assertEquals(
            "categories/9/edit?openedFrom=categories",
            EditCategoryRoute.createRoute(categoryId = 9L)
        )
    }
}
