package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CompleteOnboardingUseCaseTest {

    @Test
    fun givenOnboardingNotCompleted_whenUseCaseRuns_thenRepositoryPersistsCompletedFlag() = runTest {
        val repository = FakeAppPreferencesRepository()
        val useCase = CompleteOnboardingUseCase(repository)

        useCase()

        assertTrue(repository.current.hasCompletedOnboarding)
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
