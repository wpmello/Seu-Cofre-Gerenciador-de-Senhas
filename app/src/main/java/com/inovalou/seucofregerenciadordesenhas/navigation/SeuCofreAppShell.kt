package com.inovalou.seucofregerenciadordesenhas.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.navigation.AppBottomDestination
import com.inovalou.seucofregerenciadordesenhas.core.navigation.SeuCofreBottomBar
import com.inovalou.seucofregerenciadordesenhas.core.navigation.appBottomDestinationForRoute
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories.AllCategoriesEntry
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories.AllCategoriesRoute
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.CategoriesRoute
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryEntry
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryRoute
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory.NewCategoryDestination
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory.NewCategoryOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory.NewCategoryRoute
import com.inovalou.seucofregerenciadordesenhas.feature.common.presentation.PlaceholderTabScreen
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.PasswordsRoute

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
                PasswordsRoute(
                    onOpenPassword = {}
                )
            }
            composable(AppBottomDestination.Categories.route) {
                CategoriesRoute(
                    onCategoryClick = { categoryId ->
                        navController.navigate(EditCategoryRoute.createRoute(categoryId))
                    },
                    onViewAllClick = {
                        navController.navigate(AllCategoriesRoute.route)
                    },
                    onAddCategoryClick = {
                        navController.navigate(
                            NewCategoryDestination.createRoute(
                                openedFrom = NewCategoryOpenedFrom.Categories
                            )
                        )
                    }
                )
            }
            composable(AllCategoriesRoute.route) {
                AllCategoriesEntry(
                    onBackClick = {
                        navController.popBackStack(
                            AppBottomDestination.Categories.route,
                            inclusive = false
                        )
                    },
                    onEditCategoryClick = { categoryId, openedFrom ->
                        navController.navigate(
                            EditCategoryRoute.createRoute(
                                categoryId = categoryId,
                                openedFrom = openedFrom
                            )
                        )
                    },
                    onAddCategoryClick = {
                        navController.navigate(
                            NewCategoryDestination.createRoute(
                                openedFrom = NewCategoryOpenedFrom.AllCategories
                            )
                        )
                    }
                )
            }
            composable(
                route = NewCategoryDestination.routePattern,
                arguments = listOf(
                    navArgument(NewCategoryDestination.openedFromArg) {
                        type = NavType.StringType
                        defaultValue = NewCategoryOpenedFrom.Categories.routeValue
                    }
                )
            ) {
                NewCategoryRoute(
                    onNavigateBackToOrigin = { openedFrom ->
                        when (openedFrom) {
                            NewCategoryOpenedFrom.Categories -> {
                                navController.popBackStack(
                                    AppBottomDestination.Categories.route,
                                    inclusive = false
                                )
                            }

                            NewCategoryOpenedFrom.AllCategories -> {
                                navController.popBackStack(
                                    AllCategoriesRoute.route,
                                    inclusive = false
                                )
                            }
                        }
                    }
                )
            }
            composable(
                route = EditCategoryRoute.routePattern,
                arguments = listOf(
                    navArgument(EditCategoryRoute.categoryIdArg) {
                        type = NavType.LongType
                    },
                    navArgument(EditCategoryRoute.openedFromArg) {
                        type = NavType.StringType
                        defaultValue = EditCategoryOpenedFrom.Categories.routeValue
                    }
                )
            ) {
                EditCategoryEntry(
                    onNavigateBackToOrigin = { openedFrom ->
                        when (openedFrom) {
                            EditCategoryOpenedFrom.Categories -> {
                                navController.popBackStack(
                                    AppBottomDestination.Categories.route,
                                    inclusive = false
                                )
                            }

                            EditCategoryOpenedFrom.AllCategories -> {
                                navController.popBackStack(
                                    AllCategoriesRoute.route,
                                    inclusive = false
                                )
                            }
                        }
                    },
                    onNavigateToCategories = {
                        navController.popBackStack(
                            AppBottomDestination.Categories.route,
                            inclusive = false
                        )
                    }
                )
            }
            composable(AppBottomDestination.Settings.route) {
                PlaceholderTabScreen(titleResId = R.string.placeholder_settings)
            }
        }
    }
}
