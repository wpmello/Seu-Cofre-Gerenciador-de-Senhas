package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

enum class EditPasswordOpenedFrom(val routeValue: String) {
    Passwords("passwords"),
    EditCategory("edit_category");

    companion object {
        fun fromRouteValue(routeValue: String?): EditPasswordOpenedFrom =
            entries.firstOrNull { openedFrom -> openedFrom.routeValue == routeValue }
                ?: Passwords
    }
}
