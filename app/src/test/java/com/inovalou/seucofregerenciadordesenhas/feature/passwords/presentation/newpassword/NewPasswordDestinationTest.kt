package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import org.junit.Assert.assertEquals
import org.junit.Test

class NewPasswordDestinationTest {

    @Test
    fun givenDestinationRequested_whenCreatingRoute_thenReturnsStaticPasswordsCreationRoute() {
        assertEquals("passwords/new", NewPasswordDestination.route)
    }
}
