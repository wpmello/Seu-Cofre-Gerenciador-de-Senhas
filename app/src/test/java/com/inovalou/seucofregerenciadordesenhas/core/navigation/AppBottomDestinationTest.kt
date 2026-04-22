package com.inovalou.seucofregerenciadordesenhas.core.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppBottomDestinationTest {

    @Test
    fun givenCategoriesRoute_whenResolvingDestination_thenReturnsCategoriesTab() {
        assertEquals(
            AppBottomDestination.Categories,
            appBottomDestinationForRoute("categories")
        )
    }

    @Test
    fun givenPasswordsRoute_whenResolvingDestination_thenReturnsPasswordsTab() {
        assertEquals(
            AppBottomDestination.Passwords,
            appBottomDestinationForRoute("passwords")
        )
    }

    @Test
    fun givenUnknownRoute_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("unknown"))
    }
}
