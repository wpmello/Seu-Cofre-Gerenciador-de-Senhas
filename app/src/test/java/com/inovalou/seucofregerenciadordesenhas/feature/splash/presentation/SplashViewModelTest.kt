package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.ObserveAppPreferencesUseCase
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenOnboardingNotCompleted_whenSplashResolves_thenNavigatesToOnboarding() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = SplashViewModel(ObserveAppPreferencesUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceTimeBy(SplashScreenSpec.animationDurationMillis.toLong() - 1L)
        assertNull(viewModel.uiState.value.destination)

        advanceTimeBy(1L)
        advanceUntilIdle()

        assertEquals(SplashLaunchDestination.Onboarding, viewModel.uiState.value.destination)
    }

    @Test
    fun givenCompletedOnboarding_whenSplashResolves_thenNavigatesToApp() = runTest {
        val repository = FakeAppPreferencesRepository(
            initialPreferences = AppPreferences(hasCompletedOnboarding = true)
        )
        val viewModel = SplashViewModel(ObserveAppPreferencesUseCase(repository))
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceTimeBy(SplashScreenSpec.animationDurationMillis.toLong())
        advanceUntilIdle()

        assertEquals(SplashLaunchDestination.App, viewModel.uiState.value.destination)
    }

    private class FakeAppPreferencesRepository(
        initialPreferences: AppPreferences = AppPreferences()
    ) : AppPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)

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
