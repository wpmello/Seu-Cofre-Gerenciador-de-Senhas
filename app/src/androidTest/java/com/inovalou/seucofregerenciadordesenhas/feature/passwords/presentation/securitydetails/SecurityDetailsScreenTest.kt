package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SecurityDetailsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenContentState_whenRendered_thenDisplaysMainSecurityDetailsBlocks() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SecurityDetailsScreen(
                    uiState = sampleUiState(),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("security_details_screen").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.security_details_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("security_details_overall_card").assertIsDisplayed()
        composeRule.onNodeWithText("58%").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.categories_security_moderate)).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.security_details_passwords_analyzed, 28, 28)
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("security_details_calculation_card").assertIsDisplayed()
        composeRule.onNodeWithTag("security_details_tabs").assertIsDisplayed()
        composeRule.onNodeWithText("Banco Aurora").assertIsDisplayed()
    }

    @Test
    fun givenSafeTab_whenClicked_thenEmitsTabSelectionAction() {
        var action: SecurityDetailsAction? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SecurityDetailsScreen(
                    uiState = sampleUiState(),
                    onAction = { action = it }
                )
            }
        }

        composeRule.onNodeWithTag("security_details_tab_safe").performClick()

        assertEquals(SecurityDetailsAction.OnTabSelected(SecurityDetailsTab.Safe), action)
    }

    @Test
    fun givenPasswordItem_whenClicked_thenEmitsOpenPasswordAction() {
        var action: SecurityDetailsAction? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SecurityDetailsScreen(
                    uiState = sampleUiState(),
                    onAction = { action = it }
                )
            }
        }

        composeRule.onNodeWithTag("password_item_1").performClick()

        assertEquals(SecurityDetailsAction.OnPasswordClick(1L), action)
    }

    @Test
    fun givenBackButton_whenClicked_thenEmitsBackAction() {
        var action: SecurityDetailsAction? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SecurityDetailsScreen(
                    uiState = sampleUiState(),
                    onAction = { action = it }
                )
            }
        }

        composeRule.onNodeWithTag("security_details_header_back_button").performClick()

        assertEquals(SecurityDetailsAction.OnBackClick, action)
    }

    @Test
    fun givenEmptyState_whenRendered_thenDisplaysEmptyMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SecurityDetailsScreen(
                    uiState = SecurityDetailsUiState(
                        contentState = SecurityDetailsContentState.Empty
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("security_details_empty").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.security_details_empty_title)).assertIsDisplayed()
    }

    private fun sampleUiState() = SecurityDetailsUiState(
        contentState = SecurityDetailsContentState.Content,
        selectedTab = SecurityDetailsTab.Weak,
        overallScorePercent = 58,
        statusResId = R.string.categories_security_moderate,
        visualState = SecurityDetailsVisualState.Moderate,
        totalPasswords = 28,
        tabs = listOf(
            SecurityDetailsTabUiModel(SecurityDetailsTab.Weak, 4),
            SecurityDetailsTabUiModel(SecurityDetailsTab.Moderate, 7),
            SecurityDetailsTabUiModel(SecurityDetailsTab.Safe, 12)
        ),
        visiblePasswords = listOf(
            SecurityDetailsPasswordUiModel(
                id = 1L,
                title = "Banco Aurora",
                supportingText = "Financeiro",
                initials = "BA",
                scorePercent = 35,
                visualState = SecurityDetailsVisualState.Poor,
                tagResIds = listOf(R.string.edit_password_security_tag_weak)
            )
        )
    )
}
