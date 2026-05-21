package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

enum class NewPasswordOpenedFrom(val routeValue: String) {
    Vault("vault"),
    Passwords("passwords");

    companion object {
        fun fromRouteValue(routeValue: String?): NewPasswordOpenedFrom =
            entries.firstOrNull { openedFrom -> openedFrom.routeValue == routeValue }
                ?: Passwords
    }
}
