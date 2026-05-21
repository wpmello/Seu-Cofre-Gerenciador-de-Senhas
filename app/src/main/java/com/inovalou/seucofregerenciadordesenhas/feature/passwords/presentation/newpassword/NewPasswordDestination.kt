package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

object NewPasswordDestination {
    const val openedFromArg = "openedFrom"
    const val route = "passwords/new"
    const val routePattern = "$route?$openedFromArg={$openedFromArg}"

    fun createRoute(
        openedFrom: NewPasswordOpenedFrom = NewPasswordOpenedFrom.Passwords
    ): String = "$route?$openedFromArg=${openedFrom.routeValue}"
}
