package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.ObserveAppPreferencesUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateAppLanguageUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateAppThemePreferenceUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateUserNameUseCase
import com.inovalou.seucofregerenciadordesenhas.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun givenNoSavedUserName_whenObserved_thenExposesFallbackNameAndDefaultPreferences() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = buildViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }

        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertTrue(state.contentState is SettingsContentState.Content)
        assertNull(state.user.name)
        assertEquals(R.string.settings_user_name_fallback, state.user.fallbackNameResId)
        assertEquals(R.string.vault_encrypted_indicator, state.user.encryptedStatusResId)
        assertEquals(3, state.items.size)
        assertEquals(R.string.settings_language_title, state.items[0].titleResId)
        assertEquals(R.string.settings_theme_title, state.items[1].titleResId)
        assertEquals(R.string.settings_about_title, state.items[2].titleResId)
        assertEquals(R.string.settings_theme_current_dark, state.items[1].trailingLabelResId)
    }

    @Test
    fun givenUserEditsName_whenTypingBeforeSave_thenCardNameDoesNotChangeUntilSaved() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = buildViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.OnUserCardClick)
        viewModel.onAction(SettingsAction.OnUserNameDraftChange("Maria Silva"))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.user.name)
        assertEquals("Maria Silva", viewModel.uiState.value.nameEditor?.draftName)

        viewModel.onAction(SettingsAction.OnSaveUserNameClick)
        advanceUntilIdle()

        assertEquals("Maria Silva", viewModel.uiState.value.user.name)
        assertNull(viewModel.uiState.value.nameEditor)
        assertEquals("Maria Silva", repository.current.userName)
    }

    @Test
    fun givenLanguageDialog_whenEnglishIsSaved_thenRepositoryAndStateUseEnglish() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = buildViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.OnItemClick(SettingsItemKind.Language))
        viewModel.onAction(SettingsAction.OnLanguageDraftSelected(AppLanguage.English))
        viewModel.onAction(SettingsAction.OnSaveLanguageClick)
        advanceUntilIdle()

        assertEquals(AppLanguage.English, repository.current.language)
        assertEquals(AppLanguage.English, viewModel.uiState.value.selectedLanguage)
        assertNull(viewModel.uiState.value.languageDialog)
    }

    @Test
    fun givenThemeDialog_whenLightThemeIsSaved_thenThemeTrailingLabelReflectsChoice() = runTest {
        val repository = FakeAppPreferencesRepository()
        val viewModel = buildViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.OnItemClick(SettingsItemKind.Theme))
        viewModel.onAction(SettingsAction.OnThemeDraftSelected(AppThemePreference.Light))
        viewModel.onAction(SettingsAction.OnSaveThemeClick)
        advanceUntilIdle()

        assertEquals(AppThemePreference.Light, repository.current.themePreference)
        assertEquals(AppThemePreference.Light, viewModel.uiState.value.selectedTheme)
        assertEquals(R.string.settings_theme_current_light, viewModel.uiState.value.items[1].trailingLabelResId)
        assertNull(viewModel.uiState.value.themeDialog)
    }

    @Test
    fun givenAboutItemClicked_whenHandled_thenShowsAboutDialogUntilDismissed() = runTest {
        val viewModel = buildViewModel(FakeAppPreferencesRepository())
        backgroundScope.launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.onAction(SettingsAction.OnItemClick(SettingsItemKind.About))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.aboutDialogVisible)

        viewModel.onAction(SettingsAction.OnDismissAboutDialog)
        advanceUntilIdle()

        assertTrue(!viewModel.uiState.value.aboutDialogVisible)
    }

    private fun buildViewModel(
        repository: FakeAppPreferencesRepository
    ): SettingsViewModel =
        SettingsViewModel(
            observeAppPreferencesUseCase = ObserveAppPreferencesUseCase(repository),
            updateUserNameUseCase = UpdateUserNameUseCase(repository),
            updateAppLanguageUseCase = UpdateAppLanguageUseCase(repository),
            updateAppThemePreferenceUseCase = UpdateAppThemePreferenceUseCase(repository)
        )

    private class FakeAppPreferencesRepository(
        initialPreferences: AppPreferences = AppPreferences()
    ) : AppPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)
        val current: AppPreferences get() = preferencesFlow.value

        override fun observePreferences(): Flow<AppPreferences> = preferencesFlow

        override suspend fun updateUserName(userName: String) {
            preferencesFlow.value = preferencesFlow.value.copy(userName = userName.trim())
        }

        override suspend fun updateLanguage(language: AppLanguage) {
            preferencesFlow.value = preferencesFlow.value.copy(language = language)
        }

        override suspend fun updateThemePreference(themePreference: AppThemePreference) {
            preferencesFlow.value = preferencesFlow.value.copy(themePreference = themePreference)
        }
    }
}
