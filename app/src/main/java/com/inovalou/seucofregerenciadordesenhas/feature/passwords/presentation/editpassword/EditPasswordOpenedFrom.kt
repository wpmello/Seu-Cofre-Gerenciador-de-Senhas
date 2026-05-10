package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

enum class EditPasswordOpenedFrom(val routeValue: String) {
    Vault("vault"),
    Passwords("passwords"),
    EditCategory("edit_category"),
    SecurityDetails("security_details"),
    GlobalSearch("global_search");

    companion object {
        fun fromRouteValue(routeValue: String?): EditPasswordOpenedFrom =
            entries.firstOrNull { openedFrom -> openedFrom.routeValue == routeValue }
                ?: Passwords
    }
}
