package com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model

enum class AppLanguage(val storageValue: String, val languageTag: String) {
    PortugueseBrazil(storageValue = "pt-BR", languageTag = "pt-BR"),
    English(storageValue = "en", languageTag = "en"),
    Spanish(storageValue = "es", languageTag = "es");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage =
            entries.firstOrNull { language -> language.storageValue == value } ?: PortugueseBrazil
    }
}
