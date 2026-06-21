package com.inovalou.seucofregerenciadordesenhas

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.fragment.app.FragmentActivity
import com.inovalou.seucofregerenciadordesenhas.core.preferences.presentation.AppPreferencesViewModel
import com.inovalou.seucofregerenciadordesenhas.core.ui.AppLocaleProvider
import com.inovalou.seucofregerenciadordesenhas.navigation.SeuCofreNavHost
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appPreferencesViewModel: AppPreferencesViewModel = hiltViewModel()
            val preferences by appPreferencesViewModel.preferences.collectAsStateWithLifecycle()

            AppLocaleProvider(language = preferences.language) {
                SeuCofreGerenciadorDeSenhasTheme(themePreference = preferences.themePreference) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        SeuCofreNavHost()
                    }
                }
            }
        }
    }
}
