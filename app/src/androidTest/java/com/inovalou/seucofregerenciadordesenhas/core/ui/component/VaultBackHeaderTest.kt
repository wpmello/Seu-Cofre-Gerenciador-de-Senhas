package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VaultBackHeaderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenHeaderContent_whenRendered_thenShowsTitleAndBackAction() {
        var clicks = 0

        composeRule.setContent {
            VaultBackHeader(
                title = "Nova Senha",
                navigationContentDescription = "Voltar",
                onBackClick = { clicks += 1 },
                testTag = "vault_back_header"
            )
        }

        composeRule.onNodeWithText("Nova Senha").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_back_header_back_button").performClick()

        assertEquals(1, clicks)
    }
}
