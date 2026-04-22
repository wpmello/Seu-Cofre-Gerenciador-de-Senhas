package com.inovalou.seucofregerenciadordesenhas.core.navigation

import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SeuCofreBottomBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenPasswordsDestination_whenRendered_thenMarksPasswordsTabAsSelected() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SeuCofreBottomBar(
                    currentDestination = AppBottomDestination.Passwords,
                    onDestinationSelected = {}
                )
            }
        }

        composeRule.onNodeWithText("PASSWORDS").assertIsSelected()
    }

    @Test
    fun givenBottomBar_whenPasswordsTabClicked_thenReturnsPasswordsDestination() {
        var selectedDestination: AppBottomDestination? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SeuCofreBottomBar(
                    currentDestination = AppBottomDestination.Categories,
                    onDestinationSelected = { selectedDestination = it }
                )
            }
        }

        composeRule.onNodeWithText("PASSWORDS").performClick()

        assertEquals(AppBottomDestination.Passwords, selectedDestination)
    }
}
