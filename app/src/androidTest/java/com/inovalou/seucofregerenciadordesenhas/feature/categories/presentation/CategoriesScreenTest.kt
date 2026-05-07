package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertTrue
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
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
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
                            categories = sampleCategories()
                        )
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_create_fab").assertIsDisplayed()
    }

    @Test
    fun givenMoreThanFourCategoriesFlag_whenRendered_thenDisplaysBottomViewAllButton() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Content(
                            categories = sampleCategories()
                        ),
                        shouldShowBottomViewAllButton = true
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_bottom_view_all").assertIsDisplayed()
    }

    @Test
    fun givenNoMoreThanFourCategoriesFlag_whenRendered_thenDoesNotDisplayBottomViewAllButton() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Content(
                            categories = sampleCategories()
                        ),
                        shouldShowBottomViewAllButton = false
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onAllNodesWithTag("categories_bottom_view_all").assertCountEquals(0)
    }

    @Test
    fun givenBottomViewAllButton_whenClicked_thenInvokesSameNavigationCallback() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Content(
                            categories = sampleCategories()
                        ),
                        shouldShowBottomViewAllButton = true
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = { wasClicked = true },
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_bottom_view_all").performClick()

        assertTrue(wasClicked)
    }

    @Test
    fun givenHighlightedCategoryCard_whenClicked_thenInvokesCategoryNavigationCallback() {
        var clickedCategoryId: Long? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        currentCategory = HighlightedCategoryUiModel(
                            id = 7L,
                            name = "Pessoal",
                            itemCount = 3
                        ),
                        categoriesState = CategoriesContentUiState.Content(
                            categories = sampleCategories()
                        )
                    ),
                    onAction = {},
                    onCategoryClick = { clickedCategoryId = it },
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("highlighted_category_card").performClick()

        assertTrue(clickedCategoryId == 7L)
    }

    @Test
    fun givenSecuritySummaryStatus_whenRendered_thenDisplaysMappedStatusText() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        securitySummary = SecuritySummaryUiModel(
                            statusResId = R.string.categories_security_moderate,
                            totalItems = 2,
                            visualState = SecuritySummaryVisualState.Moderate
                        ),
                        categoriesState = CategoriesContentUiState.Empty
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("categories_security_summary_card").assertIsDisplayed()
        composeRule.onNodeWithTag("categories_security_summary_status").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.categories_security_moderate)).assertIsDisplayed()
    }

    @Test
    fun givenSecuritySummaryCard_whenClicked_thenInvokesSecurityDetailsCallback() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                CategoriesScreen(
                    uiState = CategoriesUiState(
                        categoriesState = CategoriesContentUiState.Empty
                    ),
                    onAction = {},
                    onCategoryClick = {},
                    onViewAllClick = {},
                    onAddCategoryClick = {},
                    onSecuritySummaryClick = { wasClicked = true }
                )
            }
        }

        composeRule.onNodeWithTag("categories_security_summary_card").performClick()

        assertTrue(wasClicked)
    }

    private fun sampleCategories() = listOf(
        CategoryCardUiModel(
            id = 1L,
            name = "Trabalho",
            iconKey = "ic_work_bag_add_category",
            iconResId = R.drawable.ic_work_bag_add_category,
            itemCount = 42
        )
    )
}
