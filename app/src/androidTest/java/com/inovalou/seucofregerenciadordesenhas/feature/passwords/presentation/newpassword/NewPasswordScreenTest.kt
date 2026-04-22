package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.inovalou.seucofregerenciadordesenhas.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewPasswordScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenInitialUiState_whenScreenIsRendered_thenShowsExpectedFormSections() {
        composeRule.setContent {
            NewPasswordScreen(
                uiState = NewPasswordUiState(),
                onAction = {}
            )
        }

        composeRule.onNodeWithTag("new_password_header").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Proteja sua identidade digital com criptografia de nível militar."
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_title_input").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_email_input").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_category_field").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_password_input").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_visibility_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_save_button").assertIsDisplayed()
    }

    @Test
    fun givenCategoryDialogVisibleWithEmptyState_whenScreenIsRendered_thenShowsFallbackMessage() {
        composeRule.setContent {
            NewPasswordScreen(
                uiState = NewPasswordUiState(
                    isCategoryDialogVisible = true,
                    categorySelectionState = NewPasswordCategorySelectionUiState.Empty
                ),
                onAction = {}
            )
        }

        composeRule.onNodeWithText("Escolha uma categoria").assertIsDisplayed()
        composeRule.onNodeWithText("Ainda não existem categorias disponíveis.").assertIsDisplayed()
    }

    @Test
    fun givenCategoryFieldClicked_whenScreenControlsItsState_thenShowsCategoryDialog() {
        composeRule.setContent {
            var uiState by remember { mutableStateOf(NewPasswordUiState()) }

            NewPasswordScreen(
                uiState = uiState,
                onAction = { action ->
                    if (action == NewPasswordAction.OnCategoryFieldClick) {
                        uiState = uiState.copy(
                            isCategoryDialogVisible = true,
                            categorySelectionState = NewPasswordCategorySelectionUiState.Empty
                        )
                    }
                }
            )
        }

        composeRule.onNodeWithTag("new_password_category_field").performClick()

        composeRule.onNodeWithText("Escolha uma categoria").assertIsDisplayed()
    }

    @Test
    fun givenCategorySelectionCallbacks_whenCategoryFieldAndOptionAreClicked_thenDispatchesActions() {
        val actions = mutableListOf<NewPasswordAction>()
        composeRule.setContent {
            NewPasswordScreen(
                uiState = NewPasswordUiState(
                    isCategoryDialogVisible = true,
                    categorySelectionState = NewPasswordCategorySelectionUiState.Content(
                        categories = listOf(
                            NewPasswordCategoryOptionUiModel(
                                id = 3L,
                                name = "Trabalho",
                                isSelected = false
                            )
                        )
                    )
                ),
                onAction = actions::add
            )
        }

        composeRule.onNodeWithTag("new_password_category_field").performClick()
        composeRule.onNodeWithText("Trabalho").performClick()

        assertTrue(actions.contains(NewPasswordAction.OnCategoryFieldClick))
        assertTrue(actions.contains(NewPasswordAction.OnCategorySelected(3L)))
    }

    @Test
    fun givenCategoryValidationError_whenScreenIsRendered_thenDisplaysErrorMessage() {
        composeRule.setContent {
            NewPasswordScreen(
                uiState = NewPasswordUiState(
                    categoryErrorResId = R.string.new_password_category_error_invalid
                ),
                onAction = {}
            )
        }

        composeRule.onNodeWithText("Selecione uma categoria válida para continuar.").assertIsDisplayed()
    }
}
