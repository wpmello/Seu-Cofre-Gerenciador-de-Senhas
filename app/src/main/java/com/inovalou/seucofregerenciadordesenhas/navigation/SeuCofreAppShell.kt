package com.inovalou.seucofregerenciadordesenhas.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.navigation.AppBottomDestination
import com.inovalou.seucofregerenciadordesenhas.core.navigation.SeuCofreBottomBar
import com.inovalou.seucofregerenciadordesenhas.core.navigation.appBottomDestinationForRoute
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoriesRoute
import com.inovalou.seucofregerenciadordesenhas.feature.common.presentation.PlaceholderTabScreen

@Composable
fun SeuCofreAppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = appBottomDestinationForRoute(
        currentBackStackEntry.value?.destination?.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            SeuCofreBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppBottomDestination.Categories.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppBottomDestination.Vault.route) {
                PlaceholderTabScreen(titleResId = R.string.placeholder_vault)
            }
            composable(AppBottomDestination.Passwords.route) {
                PlaceholderTabScreen(titleResId = R.string.placeholder_passwords)
            }
            composable(AppBottomDestination.Categories.route) {
                CategoriesRoute()
            }
            composable(AppBottomDestination.Settings.route) {
                PlaceholderTabScreen(titleResId = R.string.placeholder_settings)
            }
        }
    }
}
