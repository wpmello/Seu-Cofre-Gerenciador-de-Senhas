package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.CompleteOnboardingUseCase
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenUserIsOnFirstStep_whenAdvancing_thenMovesToNextStepWithoutPersisting() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))

        viewModel.onAction(OnboardingAction.OnNextClick)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentStep)
        assertEquals(0, repository.completeOnboardingCalls)
    }

    @Test
    fun givenUserIsOnLastStep_whenCompleting_thenPersistsFlagAndNavigatesToApp() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        val effect = async { viewModel.effects.first() }

        repeat(OnboardingUiState.LAST_STEP_INDEX) {
            viewModel.onAction(OnboardingAction.OnNextClick)
        }
        viewModel.onAction(OnboardingAction.OnNextClick)
        advanceUntilIdle()

        assertTrue(repository.current.hasCompletedOnboarding)
        assertEquals(1, repository.completeOnboardingCalls)
        assertEquals(OnboardingEffect.NavigateToApp, effect.await())
    }

    private class FakeAppPreferencesRepository(
        initialPreferences: AppPreferences = AppPreferences()
    ) : AppPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)
        val current: AppPreferences get() = preferencesFlow.value
        var completeOnboardingCalls: Int = 0

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
            completeOnboardingCalls += 1
            preferencesFlow.value = preferencesFlow.value.copy(hasCompletedOnboarding = true)
        }
    }
}
