package com.inovalou.seucofregerenciadordesenhas.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Security
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
        icon = Icons.Rounded.Security
    ),
    Passwords(
        route = "passwords",
        labelResId = R.string.bottom_nav_passwords,
        icon = Icons.Outlined.Key
    ),
    Categories(
        route = "categories",
        labelResId = R.string.bottom_nav_categories,
        icon = Icons.Outlined.GridView
    ),
    Settings(
        route = "settings",
        labelResId = R.string.bottom_nav_settings,
        icon = Icons.Outlined.Settings
    )
}

fun appBottomDestinationForRoute(route: String?): AppBottomDestination? =
    AppBottomDestination.entries.firstOrNull { destination ->
        route == destination.route
    }
