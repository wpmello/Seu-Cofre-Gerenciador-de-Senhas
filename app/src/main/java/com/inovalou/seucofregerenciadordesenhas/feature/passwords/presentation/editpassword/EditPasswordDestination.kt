package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

object EditPasswordDestination {
    const val passwordIdArg = "passwordId"
    const val routePattern = "passwords/{$passwordIdArg}/edit"

    fun createRoute(passwordId: Long): String = "passwords/$passwordId/edit"
}
