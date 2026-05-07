package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import org.junit.Assert.assertEquals
import org.junit.Test

class EditPasswordDestinationTest {

    @Test
    fun givenPasswordId_whenCreatingRoute_thenEmbedsThatArgument() {
        assertEquals(
            "passwords/25/edit?openedFrom=passwords",
            EditPasswordDestination.createRoute(passwordId = 25L)
        )
    }

    @Test
    fun givenEditCategoryOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "passwords/25/edit?openedFrom=edit_category",
            EditPasswordDestination.createRoute(
                passwordId = 25L,
                openedFrom = EditPasswordOpenedFrom.EditCategory
            )
        )
    }

    @Test
    fun givenSecurityDetailsOrigin_whenCreatingRoute_thenEmbedsOpenedFromArgument() {
        assertEquals(
            "passwords/25/edit?openedFrom=security_details",
            EditPasswordDestination.createRoute(
                passwordId = 25L,
                openedFrom = EditPasswordOpenedFrom.SecurityDetails
            )
        )
    }
}
