package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class EditPasswordScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenContentState_whenRendered_thenDisplaysExpectedSections() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditPasswordScreen(
                    uiState = editPasswordUiState(),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_header").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_email_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_password_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_save_button").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_delete_button").assertIsDisplayed()
        composeRule.onNodeWithText("Notas seguras").assertIsDisplayed()
        composeRule.onNodeWithText("Sugerir Senha Forte").assertIsDisplayed()
        composeRule.onNodeWithText("Ver Histórico").assertIsDisplayed()
    }

    @Test
    fun givenVisibilityToggle_whenStateIsControlled_thenPasswordFieldRemainsInteractive() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                var uiState by remember { mutableStateOf(editPasswordUiState()) }
                EditPasswordScreen(
                    uiState = uiState,
                    onAction = { action ->
                        if (action == EditPasswordAction.OnTogglePasswordVisibility) {
                            uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_visibility_toggle").performClick()

        composeRule.onNodeWithTag("edit_password_password_input").assertIsDisplayed()
    }

    private fun editPasswordUiState() = EditPasswordUiState(
        title = "Spotify",
        email = "premium@vault.com",
        password = "plain-secret",
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_710_000_000_000L,
        contentState = EditPasswordContentState.Content,
        securitySection = EditPasswordSecuritySectionUiState(
            scorePercent = 32
        ),
        notesResId = R.string.edit_password_notes_mock
    )
}
