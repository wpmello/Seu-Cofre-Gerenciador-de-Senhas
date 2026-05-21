package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import org.junit.Assert.assertEquals
import org.junit.Test

class NewPasswordDestinationTest {

    @Test
    fun givenDestinationRequested_whenCreatingRoute_thenReturnsStaticPasswordsCreationRoute() {
        assertEquals("passwords/new", NewPasswordDestination.route)
    }

    @Test
    fun givenVaultOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "passwords/new?openedFrom=vault",
            NewPasswordDestination.createRoute(openedFrom = NewPasswordOpenedFrom.Vault)
        )
    }

    @Test
    fun givenNoOriginOverride_whenCreatingRoute_thenDefaultsToPasswordsOrigin() {
        assertEquals(
            "passwords/new?openedFrom=passwords",
            NewPasswordDestination.createRoute()
        )
    }
}
