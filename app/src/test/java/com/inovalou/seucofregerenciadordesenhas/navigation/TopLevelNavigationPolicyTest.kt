package com.inovalou.seucofregerenciadordesenhas.navigation

import com.inovalou.seucofregerenciadordesenhas.core.navigation.AppBottomDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TopLevelNavigationPolicyTest {

    @Test
    fun givenBottomDestinationSelectionForNonStartDestination_whenResolvingOptions_thenPreservesTabState() {
        val options = topLevelNavigationOptionsFor(
            intent = TopLevelNavigationIntent.BottomDestinationSelection,
            destination = AppBottomDestination.Passwords
        )

        assertTrue(options.saveState)
        assertTrue(options.restoreState)
    }

    @Test
    fun givenBottomDestinationSelectionForVault_whenResolvingOptions_thenDoesNotRestoreStartDestinationState() {
        val options = topLevelNavigationOptionsFor(
            intent = TopLevelNavigationIntent.BottomDestinationSelection,
            destination = AppBottomDestination.Vault
        )

        assertFalse(options.saveState)
        assertFalse(options.restoreState)
    }

    @Test
    fun givenInternalFlowCompletion_whenResolvingOptions_thenClearsInternalStackState() {
        val options = topLevelNavigationOptionsFor(
            intent = TopLevelNavigationIntent.InternalFlowCompletion,
            destination = AppBottomDestination.Categories
        )

        assertFalse(options.saveState)
        assertFalse(options.restoreState)
    }
}
