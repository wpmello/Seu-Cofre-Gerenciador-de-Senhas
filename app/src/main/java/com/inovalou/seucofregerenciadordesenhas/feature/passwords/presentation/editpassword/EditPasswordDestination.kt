package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

object EditPasswordDestination {
    const val passwordIdArg = "passwordId"
    const val openedFromArg = "openedFrom"
    private const val baseRoute = "passwords/{$passwordIdArg}/edit"
    const val routePattern = "$baseRoute?$openedFromArg={$openedFromArg}"

    fun createRoute(
        passwordId: Long,
        openedFrom: EditPasswordOpenedFrom = EditPasswordOpenedFrom.Passwords
    ): String = "passwords/$passwordId/edit?$openedFromArg=${openedFrom.routeValue}"
}
