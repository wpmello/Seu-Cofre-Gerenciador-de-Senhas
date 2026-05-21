package com.inovalou.seucofregerenciadordesenhas.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.rounded.Security
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppBottomDestinationTest {

    @Test
    fun givenVaultRoute_whenResolvingDestination_thenReturnsVaultTab() {
        assertEquals(
            AppBottomDestination.Vault,
            appBottomDestinationForRoute("vault")
        )
    }

    @Test
    fun givenVaultDestination_whenInspectingIcon_thenUsesSettingsActiveEncryptionIcon() {
        assertEquals(
            Icons.Rounded.Security.name,
            AppBottomDestination.Vault.icon.name
        )
    }

    @Test
    fun givenCategoriesRoute_whenResolvingDestination_thenReturnsCategoriesTab() {
        assertEquals(
            AppBottomDestination.Categories,
            appBottomDestinationForRoute("categories")
        )
    }

    @Test
    fun givenCategoriesDestination_whenInspectingIcon_thenUsesNewPasswordCategoryFieldIcon() {
        assertEquals(
            Icons.Outlined.GridView.name,
            AppBottomDestination.Categories.icon.name
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
    fun givenSettingsRoute_whenResolvingDestination_thenReturnsSettingsTab() {
        assertEquals(
            AppBottomDestination.Settings,
            appBottomDestinationForRoute("settings")
        )
    }

    @Test
    fun givenUnknownRoute_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("unknown"))
    }

    @Test
    fun givenSecurityDetailsRoute_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("security-details"))
    }

    @Test
    fun givenInternalPasswordRoutes_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("passwords/new"))
        assertNull(appBottomDestinationForRoute("passwords/new?openedFrom={openedFrom}"))
        assertNull(appBottomDestinationForRoute("passwords/new?openedFrom=vault"))
        assertNull(appBottomDestinationForRoute("passwords/{passwordId}/edit?openedFrom={openedFrom}"))
        assertNull(appBottomDestinationForRoute("passwords/25/edit?openedFrom=vault"))
    }

    @Test
    fun givenInternalCategoryRoutes_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("categories/all"))
        assertNull(appBottomDestinationForRoute("categories/new?openedFrom={openedFrom}"))
        assertNull(appBottomDestinationForRoute("categories/new?openedFrom=all_categories"))
        assertNull(appBottomDestinationForRoute("categories/{categoryId}/edit?openedFrom={openedFrom}"))
        assertNull(appBottomDestinationForRoute("categories/9/edit?openedFrom=vault"))
    }

    @Test
    fun givenGlobalSearchRoute_whenResolvingDestination_thenReturnsNull() {
        assertNull(appBottomDestinationForRoute("global-search"))
    }
}
