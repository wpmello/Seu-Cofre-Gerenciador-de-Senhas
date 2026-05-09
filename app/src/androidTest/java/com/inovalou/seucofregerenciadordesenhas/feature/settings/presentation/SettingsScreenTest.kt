package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        composeRule.onNodeWithText(context.getString(R.string.settings_user_name_fallback)).assertIsDisplayed()
        composeRule.onNodeWithTag("settings_user_card").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_crypto_card").assertIsDisplayed()
        composeRule.onAllNodesWithTag("settings_item").assertCountEquals(3)
        composeRule.onAllNodesWithTag("settings_logout_button").assertCountEquals(0)
    }

    @Test
    fun givenSettingsItemWithTrailingLabel_whenRendered_thenTrailingLabelUsesSubtitleLine() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content(),
                    onAction = {}
                )
            }
        }

        val titleBounds = composeRule
            .onNodeWithTag("settings_item_title_Language")
            .fetchSemanticsNode()
            .boundsInRoot
        val subtitleBounds = composeRule
            .onNodeWithTag("settings_item_subtitle_Language")
            .fetchSemanticsNode()
            .boundsInRoot
        val trailingBounds = composeRule
            .onNodeWithTag("settings_item_trailing_Language")
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(titleBounds.right > trailingBounds.left)
        assertTrue(trailingBounds.top >= titleBounds.bottom - 1f)
        assertTrue(trailingBounds.top < subtitleBounds.bottom)
        assertTrue(trailingBounds.bottom > subtitleBounds.top)
    }

    @Test
    fun givenNameEditorVisible_whenRendered_thenDisplaysBottomSheetActions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content().copy(
                        nameEditor = SettingsNameEditorUiState(draftName = "")
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_user_name_sheet").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_user_name_sheet_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("settings_user_name_save").assertIsDisplayed()
    }

    @Test
    fun givenLanguageDialogVisible_whenRendered_thenDisplaysLanguageOptions() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content().copy(
                        languageDialog = SettingsLanguageDialogUiState(
                            selectedLanguage = AppLanguage.PortugueseBrazil,
                            draftLanguage = AppLanguage.English
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_language_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("English").assertIsDisplayed()
    }

    @Test
    fun givenThemeDialogVisible_whenRendered_thenDisplaysThemeOptions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content().copy(
                        themeDialog = SettingsThemeDialogUiState(
                            selectedTheme = AppThemePreference.Dark,
                            draftTheme = AppThemePreference.System
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_theme_dialog").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_current_system)).assertIsDisplayed()
    }

    @Test
    fun givenAboutDialogVisible_whenRendered_thenDisplaysDeveloperMessage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content().copy(aboutDialogVisible = true),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("settings_about_dialog").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_about_dialog_message)).assertIsDisplayed()
    }

    @Test
    fun givenUserCardClicked_whenRendered_thenDispatchesOpenNameEditorAction() {
        var action: SettingsAction? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SettingsScreen(
                    uiState = SettingsUiState.content(),
                    onAction = { action = it }
                )
            }
        }

        composeRule.onNodeWithTag("settings_user_card").performClick()

        assertEquals(SettingsAction.OnUserCardClick, action)
    }
}
