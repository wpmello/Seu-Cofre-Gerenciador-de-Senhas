package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PasswordsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenContentState_whenRendered_thenDisplaysHeaderSearchListAndFab() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                PasswordsScreen(
                    uiState = PasswordsUiState(
                        allPasswords = listOf(
                            PasswordListItemUiModel(
                                id = 1L,
                                title = "Netflix",
                                supportingText = "joao@email.com",
                                initials = "N"
                            )
                        ),
                        filteredPasswords = listOf(
                            PasswordListItemUiModel(
                                id = 1L,
                                title = "Netflix",
                                supportingText = "joao@email.com",
                                initials = "N"
                            )
                        ),
                        totalPasswords = 1,
                        contentState = PasswordsContentState.Content
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("passwords_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("passwords_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("passwords_list").assertIsDisplayed()
        composeRule.onNodeWithTag("passwords_create_fab").assertIsDisplayed()
        composeRule.onNodeWithText("Gerencie sua 1 credencial segura").assertIsDisplayed()
    }

    @Test
    fun givenEmptyPasswordsState_whenRendered_thenDisplaysEmptyFeedbackWithoutSubtitle() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                PasswordsScreen(
                    uiState = PasswordsUiState(
                        contentState = PasswordsContentState.EmptyPasswords
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("passwords_empty").assertIsDisplayed()
        composeRule.onAllNodesWithText("Gerencie sua 0 credencial segura").assertCountEquals(0)
        composeRule.onAllNodesWithText("Gerencie suas 0 credenciais seguras").assertCountEquals(0)
    }

    @Test
    fun givenSearchEmptyState_whenRendered_thenDisplaysSearchEmptyFeedback() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                PasswordsScreen(
                    uiState = PasswordsUiState(
                        query = "xyz",
                        contentState = PasswordsContentState.EmptySearchResult
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("passwords_search_empty").assertIsDisplayed()
    }

    @Test
    fun givenPasswordItem_whenClicked_thenInvokesActionCallback() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                PasswordsScreen(
                    uiState = PasswordsUiState(
                        filteredPasswords = listOf(
                            PasswordListItemUiModel(
                                id = 11L,
                                title = "GitHub",
                                supportingText = "jsilva_dev",
                                initials = "G"
                            )
                        ),
                        totalPasswords = 1,
                        contentState = PasswordsContentState.Content
                    ),
                    onAction = { action ->
                        if (action == PasswordsAction.OnPasswordClick(11L)) {
                            wasClicked = true
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("password_item_11").performClick()

        assertTrue(wasClicked)
    }
}
