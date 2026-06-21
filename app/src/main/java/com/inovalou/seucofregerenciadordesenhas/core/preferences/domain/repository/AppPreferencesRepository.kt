package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository

import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {

    fun observePreferences(): Flow<AppPreferences>

    suspend fun updateUserName(userName: String)

    suspend fun updateLanguage(language: AppLanguage)

    suspend fun updateThemePreference(themePreference: AppThemePreference)

    suspend fun completeOnboarding()
}
