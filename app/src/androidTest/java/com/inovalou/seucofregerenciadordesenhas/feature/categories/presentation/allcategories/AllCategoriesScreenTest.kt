package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoryCardUiModel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class AllCategoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenContentState_whenRendered_thenDisplaysSearchAndGrid() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                AllCategoriesScreen(
                    uiState = AllCategoriesUiState(
                        filteredCategories = listOf(
                            CategoryCardUiModel(
                                id = 1L,
                                name = "Trabalho",
                                iconKey = "ic_work_bag_add_category",
                                iconResId = com.inovalou.seucofregerenciadordesenhas.R.drawable.ic_work_bag_add_category,
                                itemCount = 42
                            )
                        ),
                        contentState = AllCategoriesContentState.Content
                    ),
                    onAction = {},
                    onBackClick = {},
                    onAddCategoryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("all_categories_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("all_categories_search_input").assertIsDisplayed()
        composeRule.onNodeWithTag("all_categories_create_fab").assertIsDisplayed()
    }

    @Test
    fun givenSearchEmptyState_whenRendered_thenDisplaysSearchEmptyFeedback() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                AllCategoriesScreen(
                    uiState = AllCategoriesUiState(
                        query = "xyz",
                        contentState = AllCategoriesContentState.EmptySearchResult
                    ),
                    onAction = {},
                    onBackClick = {},
                    onAddCategoryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("all_categories_search_empty").assertIsDisplayed()
    }
}
