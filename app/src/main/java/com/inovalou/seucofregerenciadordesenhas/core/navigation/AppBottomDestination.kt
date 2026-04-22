package com.inovalou.seucofregerenciadordesenhas.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import com.inovalou.seucofregerenciadordesenhas.R

enum class AppBottomDestination(
    val route: String,
    @StringRes val labelResId: Int,
    val icon: ImageVector
) {
    Vault(
        route = "vault",
        labelResId = R.string.bottom_nav_vault,
        icon = Icons.Outlined.Shield
    ),
    Passwords(
        route = "passwords",
        labelResId = R.string.bottom_nav_passwords,
        icon = Icons.Outlined.Key
    ),
    Categories(
        route = "categories",
        labelResId = R.string.bottom_nav_categories,
        icon = Icons.Outlined.Category
    ),
    Settings(
        route = "settings",
        labelResId = R.string.bottom_nav_settings,
        icon = Icons.Outlined.Settings
    )
}

fun appBottomDestinationForRoute(route: String?): AppBottomDestination? =
    AppBottomDestination.entries.firstOrNull { destination ->
        route?.substringBefore("/") == destination.route
    }
