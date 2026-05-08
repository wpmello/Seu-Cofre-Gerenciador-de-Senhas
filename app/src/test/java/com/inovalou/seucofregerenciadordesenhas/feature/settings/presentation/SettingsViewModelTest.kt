package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

import com.inovalou.seucofregerenciadordesenhas.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsViewModelTest {

    @Test
    fun givenSettingsViewModelCreated_whenReadingInitialState_thenExposesMockedContent() {
        val viewModel = SettingsViewModel()

        val state = viewModel.uiState.value

        assertTrue(state.contentState is SettingsContentState.Content)
        assertEquals("Alex Thompson", state.user.name)
        assertEquals(R.string.vault_encrypted_indicator, state.user.encryptedStatusResId)
        assertEquals(3, state.items.size)
        assertEquals(R.string.settings_language_title, state.items[0].titleResId)
        assertEquals(R.string.settings_theme_title, state.items[1].titleResId)
        assertEquals(R.string.settings_about_title, state.items[2].titleResId)
        assertEquals(R.string.settings_theme_current_dark, state.items[1].trailingLabelResId)
    }
}
