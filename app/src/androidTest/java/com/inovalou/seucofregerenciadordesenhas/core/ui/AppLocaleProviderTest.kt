package com.inovalou.seucofregerenciadordesenhas.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class AppLocaleProviderTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun givenLocalizedContext_whenProvided_thenKeepsActivityContextChainForHiltNavigation() {
        var providedActivity: Activity? = null

        composeRule.setContent {
            AppLocaleProvider(language = AppLanguage.English) {
                val context = LocalContext.current
                SideEffect {
                    providedActivity = context.findActivity()
                }
            }
        }

        composeRule.runOnIdle {
            assertSame(composeRule.activity, providedActivity)
        }
    }

    @Test
    fun givenEnglishLanguage_whenProvided_thenUsesLocalizedResources() {
        var localizedTitle: String? = null

        composeRule.setContent {
            AppLocaleProvider(language = AppLanguage.English) {
                val context = LocalContext.current
                SideEffect {
                    localizedTitle = context.getString(R.string.settings_title)
                }
            }
        }

        composeRule.runOnIdle {
            assertEquals("Settings", localizedTitle)
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
