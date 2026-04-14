package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenSplashRoute_whenAnimationCompletes_thenInvokesCompletionCallback() {
        composeRule.mainClock.autoAdvance = false
        var splashFinished by mutableStateOf(false)

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                SplashRoute(
                    onSplashFinished = { splashFinished = true }
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(SplashScreenSpec.animationDurationMillis.toLong() - 1L)
        composeRule.waitForIdle()
        assertFalse(splashFinished)

        composeRule.mainClock.advanceTimeBy(1L)
        composeRule.waitForIdle()
        assertTrue(splashFinished)
    }
}
