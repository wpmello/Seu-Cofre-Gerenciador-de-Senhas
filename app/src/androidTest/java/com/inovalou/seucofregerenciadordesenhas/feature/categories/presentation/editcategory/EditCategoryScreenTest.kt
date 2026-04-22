package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class EditCategoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

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
                                    supportingText = "dev@empresa.com"
                                )
                            )
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("password_item_9").assertIsDisplayed()
        composeRule.onNodeWithText("GitHub").assertIsDisplayed()
        composeRule.onNodeWithText("dev@empresa.com").assertIsDisplayed()
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
