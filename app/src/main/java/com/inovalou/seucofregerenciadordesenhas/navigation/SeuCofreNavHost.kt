package com.inovalou.seucofregerenciadordesenhas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation.OnboardingRoute
import com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation.SplashRoute

private object SeuCofreRoutes {
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
                onSplashFinished = {
                    navController.navigate(SeuCofreRoutes.ONBOARDING) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(SeuCofreRoutes.ONBOARDING) {
            OnboardingRoute(
                onOnboardingFinished = {
                    navController.navigate(SeuCofreRoutes.APP) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSignInClick = {
                    navController.navigate(SeuCofreRoutes.APP) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(SeuCofreRoutes.APP) {
            SeuCofreAppShell()
        }
    }
}
