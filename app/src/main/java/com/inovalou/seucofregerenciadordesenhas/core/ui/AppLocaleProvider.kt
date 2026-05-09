package com.inovalou.seucofregerenciadordesenhas.core.ui

import android.content.res.Configuration
import android.os.LocaleList
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage

@Composable
fun AppLocaleProvider(
    language: AppLanguage,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(language, currentConfiguration) {
        Configuration(currentConfiguration).apply {
            setLocales(LocaleList.forLanguageTags(language.languageTag))
        }
    }
    val localizedContext = remember(context, localizedConfiguration) {
        ContextThemeWrapper(context, context.theme).apply {
            applyOverrideConfiguration(localizedConfiguration)
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfiguration,
        content = content
    )
}
