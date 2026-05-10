package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GlobalSearchScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenAwaitingQueryState_whenRendered_thenDisplaysInitialFallback() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                GlobalSearchScreen(
                    uiState = GlobalSearchUiState(
                        contentState = GlobalSearchContentState.AwaitingQuery
                    ),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("global_search_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_awaiting_query").assertIsDisplayed()
    }

    @Test
    fun givenContentState_whenRendered_thenDisplaysMinimalCategoryAndPasswordItems() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                GlobalSearchScreen(
                    uiState = GlobalSearchUiState(
                        query = "ban",
                        categories = listOf(
                            GlobalSearchCategoryUiModel(
                                id = 1L,
                                name = "Banco",
                                iconResId = R.drawable.ic_padlock
                            )
                        ),
                        passwords = listOf(
                            GlobalSearchPasswordUiModel(
                                id = 2L,
                                title = "Banco Digital",
                                initials = "BD"
                            )
                        ),
                        contentState = GlobalSearchContentState.Content
                    ),
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("global_search_category_item_1").assertIsDisplayed()
        composeRule.onNodeWithTag("global_search_password_item_2").assertIsDisplayed()
        composeRule.onNodeWithText("Banco").assertIsDisplayed()
        composeRule.onNodeWithText("Banco Digital").assertIsDisplayed()
        composeRule.onAllNodesWithText("FRACA").assertCountEquals(0)
        composeRule.onAllNodesWithText("MODERADA").assertCountEquals(0)
        composeRule.onAllNodesWithText("user@email.com").assertCountEquals(0)
    }

    @Test
    fun givenSearchItems_whenClicked_thenDispatchesTypedActions() {
        val actions = mutableListOf<GlobalSearchAction>()

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                GlobalSearchScreen(
                    uiState = GlobalSearchUiState(
                        categories = listOf(
                            GlobalSearchCategoryUiModel(
                                id = 11L,
                                name = "Trabalho",
                                iconResId = R.drawable.ic_work_bag
                            )
                        ),
                        passwords = listOf(
                            GlobalSearchPasswordUiModel(
                                id = 22L,
                                title = "GitHub",
                                initials = "G"
                            )
                        ),
                        contentState = GlobalSearchContentState.Content
                    ),
                    onAction = { action -> actions += action },
                    onBackClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("global_search_category_item_11").performClick()
        composeRule.onNodeWithTag("global_search_password_item_22").performClick()

        assertEquals(
            listOf(
                GlobalSearchAction.OnCategoryClick(11L),
                GlobalSearchAction.OnPasswordClick(22L)
            ),
            actions
        )
    }
}
