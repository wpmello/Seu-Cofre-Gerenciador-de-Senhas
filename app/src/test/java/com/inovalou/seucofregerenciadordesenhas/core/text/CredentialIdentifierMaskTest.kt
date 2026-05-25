package com.inovalou.seucofregerenciadordesenhas.core.text

import org.junit.Assert.assertEquals
import org.junit.Test

class CredentialIdentifierMaskTest {

    @Test
    fun givenEmailIdentifier_whenMaskedForDisplay_thenMasksLocalPartDomainAndTopLevelDomain() {
        assertEquals(
            "j***@e***.c***",
            "joao@email.com".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenShortEmailIdentifier_whenMaskedForDisplay_thenDoesNotExposeLocalPart() {
        assertEquals(
            "***@e***.c***",
            "ab@email.com".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenUsernameIdentifier_whenMaskedForDisplay_thenMasksIdentifierWithMinimalExposure() {
        assertEquals(
            "j***",
            "jsilva_dev".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenBlankIdentifier_whenMaskedForDisplay_thenReturnsEmptyValue() {
        assertEquals("", "   ".maskCredentialIdentifierForDisplay())
    }

    @Test
    fun givenEmailWithShortLocalPart_whenMaskedForDisplay_thenFullyMasksLocalPart() {
        assertEquals(
            "***@e***.c***",
            "a@email.com".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenEmailWithInvalidDomain_whenMaskedForDisplay_thenMasksDomainCompletely() {
        assertEquals(
            "j***@***",
            "joao@email".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenIdentifierWithMultipleAtSigns_whenMaskedForDisplay_thenTreatsAsGenericIdentifier() {
        assertEquals(
            "j***",
            "joao@email@com".maskCredentialIdentifierForDisplay()
        )
    }

    @Test
    fun givenShortUsernameIdentifier_whenMaskedForDisplay_thenMasksCompletely() {
        assertEquals(
            "***",
            "ab".maskCredentialIdentifierForDisplay()
        )
    }
}
