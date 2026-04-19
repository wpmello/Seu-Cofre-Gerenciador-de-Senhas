package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoryCardUiModel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Rule
import org.junit.Test

class CategoriesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenCategoriesScreen_whenRenderedWithEmptyState_thenDisplaysWithoutCrashing() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Empty
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_screen").assertIsDisplayed()
    }

    @Test
    fun givenContentState_whenRendered_thenDisplaysCreateFab() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Content(
                            categories = listOf(
                                CategoryCardUiModel(
                                    id = 1L,
                                    name = "Trabalho",
                                    iconKey = "ic_work_bag_add_category",
                                    iconResId = R.drawable.ic_work_bag_add_category,
                                    itemCount = 42
                                )
                            )
                        )
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_create_fab").assertIsDisplayed()
    }
}
