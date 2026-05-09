package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateUserNameUseCaseTest {

    @Test
    fun givenNameWithOuterSpaces_whenUpdating_thenStoresTrimmedName() = runTest {
        val repository = FakeAppPreferencesRepository()
        val useCase = UpdateUserNameUseCase(repository)

        useCase("  Maria Silva  ")

        assertEquals("Maria Silva", repository.current.userName)
    }

    @Test
    fun givenBlankName_whenUpdating_thenStoresBlankPreferenceForFallback() = runTest {
        val repository = FakeAppPreferencesRepository(
            initialPreferences = AppPreferences(userName = "Maria Silva")
        )
        val useCase = UpdateUserNameUseCase(repository)

        useCase("   ")

        assertEquals("", repository.current.userName)
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
    }
}
