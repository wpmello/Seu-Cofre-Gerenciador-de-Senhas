package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenSettingsContent_whenRendered_thenDisplaysFigmaContentWithoutLogoutButton() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content(),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_title)).assertIsDisplayed()
        composeRule.onNodeWithText("Alex Thompson").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_user_card").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_crypto_card").assertIsDisplayed()
        composeRule.onAllNodesWithTag("settings_item").assertCountEquals(3)
        composeRule.onAllNodesWithTag("settings_logout_button").assertCountEquals(0)
    }
}
