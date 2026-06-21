package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.CompleteOnboardingUseCase
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingRouteTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenOnboardingRoute_whenAdvancingThroughAllSteps_thenShowsThreeStepsWithoutSignInAndCompletes() {
        val repository = FakeAppPreferencesRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        var completed = false

        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                OnboardingRoute(
                    onOnboardingCompleted = { completed = true },
                    viewModel = viewModel
                )
            }
        }

        composeRule.onAllNodesWithText("Já tem uma conta? ").assertCountEquals(0)
        composeRule.onAllNodesWithText("Entrar").assertCountEquals(0)

        composeRule.onNodeWithText("Proteja todas as suas senhas em um só lugar").assertIsDisplayed()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Suas senhas protegidas").assertIsDisplayed()
        composeRule.onNodeWithText("Próximo").performClick()

        composeRule.onNodeWithText("Acesso rápido e seguro").assertIsDisplayed()
        composeRule.onNodeWithText("Começar").performClick()

        composeRule.waitForIdle()

        assertTrue(repository.current.hasCompletedOnboarding)
        assertTrue(completed)
    }

    private class FakeAppPreferencesRepository(
        initialPreferences: AppPreferences = AppPreferences()
    ) : AppPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)
        val current: AppPreferences get() = preferencesFlow.value

        override fun observePreferences(): Flow<AppPreferences> = preferencesFlow

        override suspend fun updateUserName(userName: String) {
            preferencesFlow.value = preferencesFlow.value.copy(userName = userName)
        }

        override suspend fun updateLanguage(language: AppLanguage) {
            preferencesFlow.value = preferencesFlow.value.copy(language = language)
        }

        override suspend fun updateThemePreference(themePreference: AppThemePreference) {
            preferencesFlow.value = preferencesFlow.value.copy(themePreference = themePreference)
        }

        override suspend fun completeOnboarding() {
            preferencesFlow.value = preferencesFlow.value.copy(hasCompletedOnboarding = true)
        }
    }
}
