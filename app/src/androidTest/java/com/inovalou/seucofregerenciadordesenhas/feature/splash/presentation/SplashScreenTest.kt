package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenSplashState_whenScreenIsRendered_thenDisplaysBrandAndLoadingIndicators() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SplashScreen(uiState = SplashUiState())
            }
        }

        composeRule.onNodeWithText("CS: Cofre de Senhas").assertIsDisplayed()
        composeRule.onNodeWithText("SEU COFRE DIGITAL SEGURO").assertIsDisplayed()
        composeRule.onNodeWithText("Carregando...").assertIsDisplayed()
        composeRule.onNodeWithText("ENCRYPTED END-TO-END").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_star_field_background").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_progress_indicator").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_logo").assertIsDisplayed()
    }
}
