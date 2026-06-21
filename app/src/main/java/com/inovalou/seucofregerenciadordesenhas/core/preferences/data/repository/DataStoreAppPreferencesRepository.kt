package com.inovalou.seucofregerenciadordesenhas.core.preferences.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreAppPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferencesRepository {

    override fun observePreferences(): Flow<AppPreferences> =
        dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                AppPreferences(
                    userName = preferences[UserNameKey].orEmpty(),
                    language = AppLanguage.fromStorageValue(preferences[LanguageKey]),
                    themePreference = AppThemePreference.fromStorageValue(preferences[ThemePreferenceKey]),
                    hasCompletedOnboarding = preferences[HasCompletedOnboardingKey] ?: false
                )
            }

    override suspend fun updateUserName(userName: String) {
        dataStore.edit { preferences ->
            preferences[UserNameKey] = userName
        }
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[LanguageKey] = language.storageValue
        }
    }

    override suspend fun updateThemePreference(themePreference: AppThemePreference) {
        dataStore.edit { preferences ->
            preferences[ThemePreferenceKey] = themePreference.storageValue
        }
    }

    override suspend fun completeOnboarding() {
        dataStore.edit { preferences ->
            preferences[HasCompletedOnboardingKey] = true
        }
    }

    private companion object {
        val UserNameKey = stringPreferencesKey("user_name")
        val LanguageKey = stringPreferencesKey("language")
        val ThemePreferenceKey = stringPreferencesKey("theme_preference")
        val HasCompletedOnboardingKey = booleanPreferencesKey("has_completed_onboarding")
    }
}
