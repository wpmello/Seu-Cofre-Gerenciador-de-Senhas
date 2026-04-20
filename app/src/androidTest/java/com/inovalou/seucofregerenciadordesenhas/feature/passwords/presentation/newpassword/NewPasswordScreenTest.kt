package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.onNodeWithTag("new_password_category_input").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_password_input").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_visibility_toggle").assertIsDisplayed()
        composeRule.onNodeWithTag("new_password_save_button").assertIsDisplayed()
    }
}
