package com.inovalou.seucofregerenciadordesenhas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation.OnboardingRoute
import com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation.SplashLaunchDestination
import com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation.SplashRoute

internal object SeuCofreRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val APP = "app"
}

@Composable
fun SeuCofreNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SeuCofreRoutes.SPLASH,
        modifier = modifier
    ) {
        composable(SeuCofreRoutes.SPLASH) {
            SplashRoute(
                onLaunchResolved = { destination ->
                    val route = when (destination) {
                        SplashLaunchDestination.Onboarding -> SeuCofreRoutes.ONBOARDING
                        SplashLaunchDestination.App -> SeuCofreRoutes.APP
                    }
                    navController.navigateToRoot(route)
                }
            )
        }

        composable(SeuCofreRoutes.ONBOARDING) {
            OnboardingRoute(
                onOnboardingCompleted = {
                    navController.navigateToRoot(SeuCofreRoutes.APP)
                }
            )
        }

        composable(SeuCofreRoutes.APP) {
            SeuCofreAppShell()
        }
    }
}

private fun androidx.navigation.NavHostController.navigateToRoot(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
