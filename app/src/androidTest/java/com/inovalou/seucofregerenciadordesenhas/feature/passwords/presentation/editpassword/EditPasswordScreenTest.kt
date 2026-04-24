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
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState
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
        composeRule.onNodeWithTag("edit_password_identity_card_mode_button").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_title_text").assertIsDisplayed()
        composeRule.onNodeWithText("SP").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_email_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_password_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_category_field").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_security_section").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_note_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_save_button").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_password_delete_button").assertIsDisplayed()
        composeRule.onNodeWithText("Anotações").assertIsDisplayed()
        composeRule.onNodeWithText("Conta principal da família.").assertIsDisplayed()
        composeRule.onNodeWithText("Sugerir Senha Forte").assertIsDisplayed()
        composeRule.onNodeWithText("Ver Histórico").assertIsDisplayed()
        composeRule.onNodeWithText("Senha fraca").assertIsDisplayed()
        composeRule.onNodeWithText("Senha duplicada").assertIsDisplayed()
    }

    @Test
    fun givenSafeSecuritySection_whenRendered_thenDisplaysSafeStateContent() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditPasswordScreen(
                    uiState = editPasswordUiState().copy(
                        securitySection = EditPasswordSecuritySectionUiState(
                            scorePercent = 100,
                            visualState = EditPasswordSecurityVisualState.Safe,
                            riskTitleResId = R.string.edit_password_security_safe_title,
                            tagResIds = listOf(R.string.edit_password_security_tag_safe),
                            alertResId = R.string.edit_password_security_alert_safe
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithText("Senha Segura").assertIsDisplayed()
        composeRule.onNodeWithText("Senha segura").assertIsDisplayed()
        composeRule.onNodeWithText("A senha atende aos critérios locais atuais do app.").assertIsDisplayed()
    }

    @Test
    fun givenEmptyNote_whenRendered_thenDisplaysFallbackHint() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditPasswordScreen(
                    uiState = editPasswordUiState().copy(note = ""),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithText(
            "Esta credencial ainda não possui anotações. Você pode escrever uma nota aqui."
        ).assertIsDisplayed()
    }

    @Test
    fun givenReadMode_whenTypingNote_thenFieldStaysEditable() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                var uiState by remember { mutableStateOf(editPasswordUiState()) }
                EditPasswordScreen(
                    uiState = uiState,
                    onAction = { action ->
                        if (action is EditPasswordAction.OnNoteChanged) {
                            uiState = uiState.copy(note = action.note)
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_note_input").performTextClearance()
        composeRule.onNodeWithTag("edit_password_note_input").performTextInput("Observação livre")

        composeRule.onNodeWithText("Observação livre").assertIsDisplayed()
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

    @Test
    fun givenTitleEditor_whenStateIsControlled_thenUpdatesVisibleTitleAndInitials() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                var uiState by remember { mutableStateOf(editPasswordUiState()) }
                EditPasswordScreen(
                    uiState = uiState,
                    onAction = { action ->
                        when (action) {
                            EditPasswordAction.OnIdentityCardEditClick -> {
                                uiState = uiState.copy(isIdentityCardEditing = true)
                            }
                            is EditPasswordAction.OnTitleChanged -> {
                                uiState = uiState.copy(title = action.title)
                            }
                            else -> Unit
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_identity_card_mode_button").performClick()
        composeRule.onNodeWithTag("edit_password_title_input").performTextClearance()
        composeRule.onNodeWithTag("edit_password_title_input").performTextInput("Netflix")

        composeRule.onNodeWithText("Netflix").assertIsDisplayed()
        composeRule.onNodeWithText("NE").assertIsDisplayed()
    }

    @Test
    fun givenCategoryFieldClick_whenStateIsControlled_thenDisplaysReusedDialog() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                var uiState by remember { mutableStateOf(editPasswordUiState()) }
                EditPasswordScreen(
                    uiState = uiState,
                    onAction = { action ->
                        when (action) {
                            EditPasswordAction.OnIdentityCardEditClick -> {
                                uiState = uiState.copy(isIdentityCardEditing = true)
                            }
                            EditPasswordAction.OnCategoryFieldClick -> {
                                uiState = uiState.copy(isCategoryDialogVisible = true)
                            }
                            EditPasswordAction.OnCategoryDialogDismissed -> {
                                uiState = uiState.copy(isCategoryDialogVisible = false)
                            }
                            else -> Unit
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_identity_card_mode_button").performClick()
        composeRule.onNodeWithTag("edit_password_category_field").performClick()

        composeRule.onNodeWithTag("password_category_dialog").assertIsDisplayed()
    }

    @Test
    fun givenIdentityCardSaveClick_whenStateIsControlled_thenReturnsToReadModeWithUpdatedTitle() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                var uiState by remember {
                    mutableStateOf(editPasswordUiState().copy(isIdentityCardEditing = true))
                }
                EditPasswordScreen(
                    uiState = uiState,
                    onAction = { action ->
                        when (action) {
                            is EditPasswordAction.OnTitleChanged -> {
                                uiState = uiState.copy(title = action.title)
                            }
                            EditPasswordAction.OnIdentityCardSaveClick -> {
                                uiState = uiState.copy(isIdentityCardEditing = false)
                            }
                            else -> Unit
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("edit_password_title_input").performTextClearance()
        composeRule.onNodeWithTag("edit_password_title_input").performTextInput("Netflix")
        composeRule.onNodeWithTag("edit_password_identity_card_mode_button").performClick()

        composeRule.onNodeWithTag("edit_password_title_text").assertIsDisplayed()
        composeRule.onNodeWithText("Netflix").assertIsDisplayed()
    }

    private fun editPasswordUiState() = EditPasswordUiState(
        title = "Spotify",
        email = "premium@vault.com",
        password = "plain-secret",
        note = "Conta principal da família.",
        selectedCategoryId = 2L,
        selectedCategoryName = "Music",
        categorySelectionState = PasswordCategorySelectionUiState.Content(
            categories = listOf(
                com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategoryOptionUiModel(
                    id = 2L,
                    name = "Music",
                    isSelected = true
                )
            )
        ),
        createdAt = 1_700_000_000_000L,
        updatedAt = 1_710_000_000_000L,
        contentState = EditPasswordContentState.Content,
        securitySection = EditPasswordSecuritySectionUiState(
            scorePercent = 32
        )
    )
}
