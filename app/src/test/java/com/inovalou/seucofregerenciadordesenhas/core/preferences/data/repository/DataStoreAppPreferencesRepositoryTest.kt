package com.inovalou.seucofregerenciadordesenhas.core.preferences.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import java.io.File
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAppPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val dataStoreScopes = mutableListOf<TestScope>()

    @After
    fun tearDown() {
        dataStoreScopes.forEach { it.cancel() }
        dataStoreScopes.clear()
    }

    @Test
    fun givenEmptyDataStore_whenObserved_thenEmitsDefaultPreferences() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        assertEquals(AppPreferences(), repository.observePreferences().first())
    }

    @Test
    fun givenUserNameUpdate_whenObserved_thenPersistsUserName() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        repository.updateUserName("Maria Silva")

        assertEquals(
            AppPreferences(userName = "Maria Silva"),
            repository.observePreferences().first()
        )
    }

    @Test
    fun givenLanguageUpdate_whenObserved_thenPersistsLanguage() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        repository.updateLanguage(AppLanguage.English)

        assertEquals(
            AppPreferences(language = AppLanguage.English),
            repository.observePreferences().first()
        )
    }

    @Test
    fun givenThemeUpdate_whenObserved_thenPersistsTheme() = runTest {
        val repository = DataStoreAppPreferencesRepository(createDataStore())

        repository.updateThemePreference(AppThemePreference.Light)

        assertEquals(
            AppPreferences(themePreference = AppThemePreference.Light),
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

    private fun createDataStore(): DataStore<Preferences> {
        val file = File(temporaryFolder.newFolder(), "settings.preferences_pb")
        val dataStoreScope = TestScope(UnconfinedTestDispatcher())
        dataStoreScopes += dataStoreScope

        return PreferenceDataStoreFactory.create(scope = dataStoreScope) { file }
    }
}
