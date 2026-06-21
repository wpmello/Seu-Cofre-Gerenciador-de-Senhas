package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class EditCategoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenLockedState_whenRendered_thenDoesNotDisplaySensitiveFieldsAndRetryDispatchesAction() {
        var lastAction: EditCategoryAction? = null
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = EditCategoryUiState(
                        localAuthenticationState = EditCategoryLocalAuthenticationState.Failed
                    ),
                    onAction = { action -> lastAction = action }
                )
            }
        }

        composeRule.onNodeWithTag("edit_category_local_auth_gate").assertIsDisplayed()
        composeRule.onAllNodesWithTag("edit_category_name_input").assertCountEquals(0)
        composeRule.onAllNodesWithTag("edit_category_save_button").assertCountEquals(0)
        composeRule.onNodeWithTag("edit_category_local_auth_gate_retry_button").performClick()

        org.junit.Assert.assertEquals(EditCategoryAction.OnLocalAuthenticationRetryClick, lastAction)
    }

    @Test
    fun givenContentState_whenRendered_thenDisplaysMainEditingElements() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("edit_category_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_category_name_input").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_category_save_button").assertIsDisplayed()
        composeRule.onNodeWithText("Senhas nesta categoria").assertIsDisplayed()
    }

    @Test
    fun givenIconDialogVisible_whenRendered_thenDisplaysIconPickerDialog() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(isIconPickerVisible = true),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("edit_category_icon_dialog").assertIsDisplayed()
    }

    @Test
    fun givenPasswordsSectionContent_whenRendered_thenDisplaysAssociatedPasswords() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(
                        passwordsSectionState = CategoryPasswordsSectionUiState.Content(
                            passwords = listOf(
                                CategoryPasswordItemUiModel(
                                    id = 9L,
                                    title = "GitHub",
                                    supportingText = "dev@empresa.com",
                                    securityLevel = CategoryPasswordItemSecurityLevel.Moderate
                                )
                            )
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("password_item_9").assertIsDisplayed()
        composeRule.onNodeWithTag("password_strength_dot_9").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub").assertIsDisplayed()
        composeRule.onNodeWithText("dev@empresa.com").assertIsDisplayed()
    }

    @Test
    fun givenSafeAssociatedPassword_whenRendered_thenDoesNotDisplayWeakStrengthFlag() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(
                        passwordsSectionState = CategoryPasswordsSectionUiState.Content(
                            passwords = listOf(
                                CategoryPasswordItemUiModel(
                                    id = 9L,
                                    title = "Banco",
                                    supportingText = "conta@banco.com",
                                    securityLevel = CategoryPasswordItemSecurityLevel.Safe
                                )
                            )
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("password_strength_dot_9").assertIsDisplayed()
        composeRule.onAllNodesWithTag("password_strength_flag_9").assertCountEquals(0)
    }

    @Test
    fun givenMoreThanSevenAssociatedPasswords_whenRendered_thenPasswordListScrollsInternally() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(
                        passwordsSectionState = CategoryPasswordsSectionUiState.Content(
                            passwords = (1L..12L).map { id ->
                                CategoryPasswordItemUiModel(
                                    id = id,
                                    title = "Senha $id",
                                    supportingText = "login$id@empresa.com"
                                )
                            }
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("edit_category_passwords_list").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_category_passwords_list").performScrollToIndex(11)
        composeRule.onNodeWithTag("password_item_12").assertIsDisplayed()
        composeRule.onNodeWithTag("edit_category_passwords_list").performTouchInput {
            swipeUp()
        }
        composeRule.onNodeWithTag("edit_category_save_button").assertIsDisplayed()
    }

    @Test
    fun givenAssociatedPasswordClick_whenRendered_thenDispatchesPasswordClickAction() {
        var clickedPasswordId: Long? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                EditCategoryScreen(
                    uiState = editCategoryUiState(
                        passwordsSectionState = CategoryPasswordsSectionUiState.Content(
                            passwords = listOf(
                                CategoryPasswordItemUiModel(
                                    id = 9L,
                                    title = "GitHub",
                                    supportingText = "dev@empresa.com"
                                )
                            )
                        )
                    ),
                    onAction = { action ->
                        if (action is EditCategoryAction.OnPasswordClick) {
                            clickedPasswordId = action.passwordId
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("password_item_9").performClick()

        org.junit.Assert.assertEquals(9L, clickedPasswordId)
    }

    private fun editCategoryUiState(
        isIconPickerVisible: Boolean = false,
        passwordsSectionState: CategoryPasswordsSectionUiState = CategoryPasswordsSectionUiState.Empty
    ) = EditCategoryUiState(
        name = "Trabalho",
        availableIcons = listOf(
            CategorySelectableIconUiModel("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category, true),
            CategorySelectableIconUiModel("ic_directory", R.drawable.ic_directory, false)
        ),
        selectedIconKey = "ic_work_bag_add_category",
        contentState = EditCategoryContentState.Content,
        isIconPickerVisible = isIconPickerVisible,
        passwordsSectionState = passwordsSectionState
    )
}
