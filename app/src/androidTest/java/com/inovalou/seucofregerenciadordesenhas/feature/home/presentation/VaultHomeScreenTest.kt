package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class VaultHomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenContentState_whenRendered_thenDisplaysSummaryCategoriesRecentPasswordsAndFab() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_home_summary_card").assertIsDisplayed()
        composeRule.onNodeWithText("4").assertIsDisplayed()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.vault_home_weak_passwords, 1, 1)
        ).assertIsDisplayed()
        composeRule.onNodeWithTag("vault_home_security_summary_tags").assert(hasScrollAction())
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.vault_home_moderate_passwords, 2, 2)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.vault_home_strong_passwords, 1, 1)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.categories_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("vault_home_other_categories_card").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.vault_home_recent_title)).assertIsDisplayed()
        composeRule.onNodeWithTag("password_item_11").assertIsDisplayed()
        composeRule.onNodeWithTag("vault_home_create_password_fab").assertIsDisplayed()
    }

    @Test
    fun givenFewerThanThreeCategories_whenRendered_thenDoesNotShowOtherCard() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        categories = listOf(
                            VaultHomeCategoryUiModel(
                                id = 1L,
                                name = "Social",
                                iconKey = "ic_global",
                                iconResId = R.drawable.ic_global,
                                itemCount = 1
                            )
                        ),
                        showOtherCategories = false
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onAllNodesWithTag("vault_home_other_categories_card").assertCountEquals(0)
    }

    @Test
    fun givenNoRecentPasswords_whenRendered_thenDisplaysEmptyRecentFallback() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(recentPasswords = emptyList()),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_recent_empty").assertIsDisplayed()
    }

    @Test
    fun givenSafeRecentPassword_whenRendered_thenDoesNotDisplayWeakFlag() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        recentPasswords = listOf(
                            VaultHomeRecentPasswordUiModel(
                                id = 22L,
                                title = "Banco",
                                supportingText = "conta@email.com",
                                initials = "B",
                                securityLevel = VaultPasswordListSecurityLevel.Safe
                            )
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithTag("password_item_22").assertIsDisplayed()
        composeRule.onAllNodesWithTag("password_strength_flag_22").assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.passwords_strength_weak_flag)).assertCountEquals(0)
    }

    @Test
    fun givenCategoryCard_whenClicked_thenEmitsCategoryAction() {
        var clickedId: Long? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action is VaultHomeAction.OnCategoryClick) {
                            clickedId = action.categoryId
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_category_1").performClick()

        assertEquals(1L, clickedId)
    }

    @Test
    fun givenOtherCard_whenClicked_thenEmitsOtherCategoriesAction() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action == VaultHomeAction.OnOtherCategoriesClick) {
                            wasClicked = true
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_other_categories_card").performClick()

        assertTrue(wasClicked)
    }

    @Test
    fun givenViewAllPasswords_whenClicked_thenEmitsViewAllAction() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action == VaultHomeAction.OnViewAllPasswordsClick) {
                            wasClicked = true
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_recent_view_all").performClick()

        assertTrue(wasClicked)
    }

    @Test
    fun givenRecentPassword_whenClicked_thenEmitsRecentPasswordAction() {
        var clickedId: Long? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action is VaultHomeAction.OnRecentPasswordClick) {
                            clickedId = action.passwordId
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("password_item_11").performClick()

        assertEquals(11L, clickedId)
    }

    @Test
    fun givenSecuritySummaryTag_whenClicked_thenEmitsTagAction() {
        var selectedFilter: VaultHomeSecurityFilter? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action is VaultHomeAction.OnSecuritySummaryTagClick) {
                            selectedFilter = action.filter
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_weak_passwords").performClick()

        assertEquals(VaultHomeSecurityFilter.Weak, selectedFilter)
    }

    @Test
    fun givenSecuritySummaryList_whenRendered_thenShowsTitleListAndHidesOverviewContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        summaryCardState = VaultHomeSummaryCardState.Content(
                            filter = VaultHomeSecurityFilter.Weak,
                            passwords = listOf(
                                VaultHomeSummaryPasswordUiModel(
                                    id = 31L,
                                    title = "Banco",
                                    supportingText = "conta@email.com",
                                    initials = "B",
                                    bucket = VaultHomeSecurityFilter.Weak.bucket,
                                    securityLevel = VaultPasswordListSecurityLevel.Weak,
                                    scorePercent = 24
                                )
                            )
                        )
                    ),
                    onAction = {}
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.security_details_tab_weak)).assertIsDisplayed()
        composeRule.onNodeWithTag("vault_home_summary_password_list").assertIsDisplayed()
        composeRule.onNodeWithTag("password_item_31").assertIsDisplayed()
        composeRule.onAllNodesWithTag("vault_home_total_passwords").assertCountEquals(0)
        composeRule.onAllNodesWithTag("vault_home_security_summary_tags").assertCountEquals(0)
    }

    @Test
    fun givenSecuritySummaryListActions_whenClicked_thenEmitsBackAndPasswordActions() {
        var didClickBack = false
        var clickedPasswordId: Long? = null

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        summaryCardState = VaultHomeSummaryCardState.Content(
                            filter = VaultHomeSecurityFilter.Moderate,
                            passwords = listOf(
                                VaultHomeSummaryPasswordUiModel(
                                    id = 41L,
                                    title = "Email",
                                    supportingText = "usuario@email.com",
                                    initials = "E",
                                    bucket = VaultHomeSecurityFilter.Moderate.bucket,
                                    securityLevel = VaultPasswordListSecurityLevel.Moderate,
                                    scorePercent = 70
                                )
                            )
                        )
                    ),
                    onAction = { action ->
                        when (action) {
                            VaultHomeAction.OnSecuritySummaryBackClick -> didClickBack = true
                            is VaultHomeAction.OnSecuritySummaryPasswordClick -> {
                                clickedPasswordId = action.passwordId
                            }
                            else -> Unit
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_summary_list_back").performClick()
        composeRule.onNodeWithTag("password_item_41").performClick()

        assertTrue(didClickBack)
        assertEquals(41L, clickedPasswordId)
    }

    @Test
    fun givenSecuritySummaryEmptyLoadingAndErrorStates_whenRendered_thenDisplaysFallbacks() {
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        summaryCardState = VaultHomeSummaryCardState.Empty(
                            filter = VaultHomeSecurityFilter.Safe
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onNodeWithTag("vault_home_summary_passwords_empty").assertIsDisplayed()

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        summaryCardState = VaultHomeSummaryCardState.Loading(
                            filter = VaultHomeSecurityFilter.Safe
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onNodeWithTag("vault_home_summary_passwords_loading").assertIsDisplayed()

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(
                        summaryCardState = VaultHomeSummaryCardState.Error(
                            filter = VaultHomeSecurityFilter.Safe,
                            messageResId = R.string.vault_home_summary_passwords_error
                        )
                    ),
                    onAction = {}
                )
            }
        }
        composeRule.onNodeWithTag("vault_home_summary_passwords_error").assertIsDisplayed()
    }

    @Test
    fun givenFab_whenClicked_thenEmitsAddPasswordAction() {
        var wasClicked = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                VaultHomeScreen(
                    uiState = contentState(),
                    onAction = { action ->
                        if (action == VaultHomeAction.OnAddPasswordClick) {
                            wasClicked = true
                        }
                    }
                )
            }
        }

        composeRule.onNodeWithTag("vault_home_create_password_fab").performClick()

        assertTrue(wasClicked)
    }

    private fun contentState(
        categories: List<VaultHomeCategoryUiModel> = listOf(
            VaultHomeCategoryUiModel(1L, "Social", "ic_global", R.drawable.ic_global, 2),
            VaultHomeCategoryUiModel(2L, "Compras", "ic_work_bag", R.drawable.ic_work_bag, 1),
            VaultHomeCategoryUiModel(3L, "Bancos", "ic_padlock", R.drawable.ic_padlock, 3)
        ),
        showOtherCategories: Boolean = true,
        recentPasswords: List<VaultHomeRecentPasswordUiModel> = listOf(
            VaultHomeRecentPasswordUiModel(
                id = 11L,
                title = "Facebook",
                supportingText = "usuario@email.com",
                initials = "F",
                securityLevel = VaultPasswordListSecurityLevel.Weak
            )
        ),
        summaryCardState: VaultHomeSummaryCardState = VaultHomeSummaryCardState.Overview
    ) = VaultHomeUiState(
        totalPasswords = 4,
        weakPasswords = 1,
        moderatePasswords = 2,
        strongPasswords = 1,
        categories = categories,
        showOtherCategories = showOtherCategories,
        recentPasswords = recentPasswords,
        summaryCardState = summaryCardState,
        contentState = VaultHomeContentState.Content
    )
}
