package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.ObserveAppPreferencesUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateAppLanguageUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateAppThemePreferenceUseCase
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.UpdateUserNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeAppPreferencesUseCase: ObserveAppPreferencesUseCase,
    private val updateUserNameUseCase: UpdateUserNameUseCase,
    private val updateAppLanguageUseCase: UpdateAppLanguageUseCase,
    private val updateAppThemePreferenceUseCase: UpdateAppThemePreferenceUseCase
) : ViewModel() {

    private val transientState = MutableStateFlow(SettingsTransientUiState())

    val uiState: StateFlow<SettingsUiState> = observeAppPreferencesUseCase()
        .catch {
            emit(AppPreferences())
            transientState.update { state ->
                state.copy(
                    nameEditor = null,
                    languageDialog = null,
                    themeDialog = null,
                    aboutDialogVisible = false
                )
            }
        }
        .combine(transientState) { preferences, transient ->
            SettingsUiState.content(
                preferences = preferences,
                transientState = transient
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState()
        )

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnSearchClick -> Unit
            SettingsAction.OnUserCardClick -> openUserNameEditor()
            is SettingsAction.OnUserNameDraftChange -> updateUserNameDraft(action.userName)
            SettingsAction.OnSaveUserNameClick -> saveUserName()
            SettingsAction.OnDismissUserNameEditor -> closeUserNameEditor()
            is SettingsAction.OnItemClick -> handleItemClick(action.item)
            is SettingsAction.OnLanguageDraftSelected -> updateLanguageDraft(action.language)
            SettingsAction.OnSaveLanguageClick -> saveLanguage()
            SettingsAction.OnDismissLanguageDialog -> closeLanguageDialog()
            is SettingsAction.OnThemeDraftSelected -> updateThemeDraft(action.themePreference)
            SettingsAction.OnSaveThemeClick -> saveTheme()
            SettingsAction.OnDismissThemeDialog -> closeThemeDialog()
            SettingsAction.OnDismissAboutDialog -> closeAboutDialog()
        }
    }

    private fun openUserNameEditor() {
        val currentName = uiState.value.user.name.orEmpty()
        transientState.update { state ->
            state.copy(nameEditor = SettingsNameEditorUiState(draftName = currentName))
        }
    }

    private fun updateUserNameDraft(userName: String) {
        transientState.update { state ->
            state.copy(nameEditor = state.nameEditor?.copy(draftName = userName))
        }
    }

    private fun saveUserName() {
        val editor = transientState.value.nameEditor ?: return
        if (editor.isSaving) {
            return
        }
        transientState.update { state ->
            state.copy(nameEditor = state.nameEditor?.copy(isSaving = true))
        }
        viewModelScope.launch {
            try {
                updateUserNameUseCase(editor.draftName)
                closeUserNameEditor()
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }
                transientState.update { state ->
                    state.copy(nameEditor = state.nameEditor?.copy(isSaving = false))
                }
                throw exception
            }
        }
    }

    private fun closeUserNameEditor() {
        transientState.update { state -> state.copy(nameEditor = null) }
    }

    private fun handleItemClick(item: SettingsItemKind) {
        when (item) {
            SettingsItemKind.Language -> openLanguageDialog()
            SettingsItemKind.Theme -> openThemeDialog()
            SettingsItemKind.About -> openAboutDialog()
        }
    }

    private fun openLanguageDialog() {
        val currentLanguage = uiState.value.selectedLanguage
        transientState.update { state ->
            state.copy(
                languageDialog = SettingsLanguageDialogUiState(
                    selectedLanguage = currentLanguage,
                    draftLanguage = currentLanguage
                )
            )
        }
    }

    private fun updateLanguageDraft(language: AppLanguage) {
        transientState.update { state ->
            state.copy(languageDialog = state.languageDialog?.copy(draftLanguage = language))
        }
    }

    private fun saveLanguage() {
        val dialog = transientState.value.languageDialog ?: return
        if (dialog.isSaving) {
            return
        }
        transientState.update { state ->
            state.copy(languageDialog = state.languageDialog?.copy(isSaving = true))
        }
        viewModelScope.launch {
            try {
                updateAppLanguageUseCase(dialog.draftLanguage)
                closeLanguageDialog()
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }
                transientState.update { state ->
                    state.copy(languageDialog = state.languageDialog?.copy(isSaving = false))
                }
                throw exception
            }
        }
    }

    private fun closeLanguageDialog() {
        transientState.update { state -> state.copy(languageDialog = null) }
    }

    private fun openThemeDialog() {
        val currentTheme = uiState.value.selectedTheme
        transientState.update { state ->
            state.copy(
                themeDialog = SettingsThemeDialogUiState(
                    selectedTheme = currentTheme,
                    draftTheme = currentTheme
                )
            )
        }
    }

    private fun updateThemeDraft(themePreference: AppThemePreference) {
        transientState.update { state ->
            state.copy(themeDialog = state.themeDialog?.copy(draftTheme = themePreference))
        }
    }

    private fun saveTheme() {
        val dialog = transientState.value.themeDialog ?: return
        if (dialog.isSaving) {
            return
        }
        transientState.update { state ->
            state.copy(themeDialog = state.themeDialog?.copy(isSaving = true))
        }
        viewModelScope.launch {
            try {
                updateAppThemePreferenceUseCase(dialog.draftTheme)
                closeThemeDialog()
            } catch (exception: Exception) {
                if (exception is CancellationException) {
                    throw exception
                }
                transientState.update { state ->
                    state.copy(themeDialog = state.themeDialog?.copy(isSaving = false))
                }
                throw exception
            }
        }
    }

    private fun closeThemeDialog() {
        transientState.update { state -> state.copy(themeDialog = null) }
    }

    private fun openAboutDialog() {
        transientState.update { state -> state.copy(aboutDialogVisible = true) }
    }

    private fun closeAboutDialog() {
        transientState.update { state -> state.copy(aboutDialogVisible = false) }
    }
}
