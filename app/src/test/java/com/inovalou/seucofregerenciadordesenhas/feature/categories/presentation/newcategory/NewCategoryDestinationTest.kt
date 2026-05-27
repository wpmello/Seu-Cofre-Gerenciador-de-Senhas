package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import org.junit.Assert.assertEquals
import org.junit.Test

class NewCategoryDestinationTest {

    @Test
    fun givenAllCategoriesOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "categories/new?openedFrom=all_categories",
            NewCategoryDestination.createRoute(openedFrom = NewCategoryOpenedFrom.AllCategories)
        )
    }

    @Test
    fun givenVaultOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "categories/new?openedFrom=vault",
            NewCategoryDestination.createRoute(openedFrom = NewCategoryOpenedFrom.Vault)
        )
    }

    @Test
    fun givenNoOriginOverride_whenCreatingRoute_thenDefaultsToCategoriesOrigin() {
        assertEquals(
            "categories/new?openedFrom=categories",
            NewCategoryDestination.createRoute()
        )
    }

    @Test
    fun givenVaultRouteValue_whenResolvingOpenedFrom_thenReturnsVaultOrigin() {
        assertEquals(
            NewCategoryOpenedFrom.Vault,
            NewCategoryOpenedFrom.fromRouteValue("vault")
        )
    }
}
