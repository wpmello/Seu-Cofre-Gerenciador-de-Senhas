package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import org.junit.Assert.assertEquals
import org.junit.Test

class EditPasswordDestinationTest {

    @Test
    fun givenPasswordId_whenCreatingRoute_thenEmbedsThatArgument() {
        assertEquals(
            "passwords/25/edit",
            EditPasswordDestination.createRoute(passwordId = 25L)
        )
    }
}
