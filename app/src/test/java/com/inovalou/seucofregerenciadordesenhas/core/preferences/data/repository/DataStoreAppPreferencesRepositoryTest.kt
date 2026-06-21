package com.inovalou.seucofregerenciadordesenhas.core.preferences.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAppPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenEmptyDataStore_whenObserved_thenEmitsDefaultPreferences() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        assertEquals(AppPreferences(), repository.observePreferences().first())
    }

    @Test
    fun givenUpdates_whenObserved_thenPersistsUserNameLanguageAndTheme() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        repository.updateUserName("Maria Silva")
        repository.updateLanguage(AppLanguage.English)
        repository.updateThemePreference(AppThemePreference.Light)

        assertEquals(
            AppPreferences(
                userName = "Maria Silva",
                language = AppLanguage.English,
                themePreference = AppThemePreference.Light
            ),
            repository.observePreferences().first()
        )
    }

    @Test
    fun givenOnboardingIsCompleted_whenObserved_thenPersistsCompletedFlag() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        repository.completeOnboarding()

        assertEquals(
            AppPreferences(hasCompletedOnboarding = true),
            repository.observePreferences().first()
        )
    }

    private fun TestScope.createDataStore(): DataStore<Preferences> {
        val file = File(temporaryFolder.newFolder(), "settings.preferences_pb")
        return PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
    }
}
