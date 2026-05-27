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
import com.inovalou.seucofregerenciadordesenhas.feature.home.presentation.VaultHomeRoute
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.PasswordsRoute
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword.EditPasswordDestination
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword.EditPasswordOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword.EditPasswordRoute
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword.NewPasswordDestination
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword.NewPasswordOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword.NewPasswordRoute
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails.SecurityDetailsDestination
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails.SecurityDetailsRoute
import com.inovalou.seucofregerenciadordesenhas.feature.search.presentation.GlobalSearchRoute
import com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation.SettingsRoute

private object GlobalSearchDestination {
    const val route = "global-search"
}

@Composable
fun SeuCofreAppShell(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = appBottomDestinationForRoute(
        currentBackStackEntry.value?.destination?.route
    )

    fun navigateToTopLevel(
        destination: AppBottomDestination,
        intent: TopLevelNavigationIntent = TopLevelNavigationIntent.BottomDestinationSelection
    ) {
        val navigationOptions = topLevelNavigationOptionsFor(
            intent = intent,
            destination = destination
        )
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = navigationOptions.saveState
            }
            launchSingleTop = true
            restoreState = navigationOptions.restoreState
        }
    }

    fun navigateAfterInternalFlowToTopLevel(destination: AppBottomDestination) {
        navigateToTopLevel(
            destination = destination,
            intent = TopLevelNavigationIntent.InternalFlowCompletion
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            currentDestination?.let { destination ->
                SeuCofreBottomBar(
                    currentDestination = destination,
                    onDestinationSelected = { selectedDestination ->
                        navigateToTopLevel(selectedDestination)
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppBottomDestination.Vault.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppBottomDestination.Vault.route) {
                VaultHomeRoute(
                    onOpenCategory = { categoryId ->
                        navController.navigate(
                            EditCategoryRoute.createRoute(
                                categoryId = categoryId,
                                openedFrom = EditCategoryOpenedFrom.Vault
                            )
                        )
                    },
                    onOpenAllCategories = {
                        navController.navigate(AllCategoriesRoute.route)
                    },
                    onOpenPasswords = {
                        navigateToTopLevel(AppBottomDestination.Passwords)
                    },
                    onOpenPassword = { passwordId ->
                        navController.navigate(
                            EditPasswordDestination.createRoute(
                                passwordId = passwordId,
                                openedFrom = EditPasswordOpenedFrom.Vault
                            )
                        )
                    },
                    onAddPassword = {
                        navController.navigate(
                            NewPasswordDestination.createRoute(
                                openedFrom = NewPasswordOpenedFrom.Vault
                            )
                        )
                    },
                    onAddCategory = {
                        navController.navigate(
                            NewCategoryDestination.createRoute(
                                openedFrom = NewCategoryOpenedFrom.Vault
                            )
                        )
                    },
                    onOpenGlobalSearch = {
                        navController.navigate(GlobalSearchDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppBottomDestination.Passwords.route) {
                PasswordsRoute(
                    onOpenPassword = { passwordId ->
                        navController.navigate(EditPasswordDestination.createRoute(passwordId))
                    },
                    onAddPassword = {
                        navController.navigate(
                            NewPasswordDestination.createRoute(
                                openedFrom = NewPasswordOpenedFrom.Passwords
                            )
                        )
                    },
                    onOpenGlobalSearch = {
                        navController.navigate(GlobalSearchDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = NewPasswordDestination.routePattern,
                arguments = listOf(
                    navArgument(NewPasswordDestination.openedFromArg) {
                        type = NavType.StringType
                        defaultValue = NewPasswordOpenedFrom.Passwords.routeValue
                    }
                )
            ) {
                NewPasswordRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateBackToOrigin = { openedFrom ->
                        when (openedFrom) {
                            NewPasswordOpenedFrom.Vault -> {
                                navigateAfterInternalFlowToTopLevel(AppBottomDestination.Passwords)
                            }

                            NewPasswordOpenedFrom.Passwords -> {
                                val returnedToPasswords = navController.popBackStack(
                                    AppBottomDestination.Passwords.route,
                                    inclusive = false
                                )
                                if (!returnedToPasswords) {
                                    navigateAfterInternalFlowToTopLevel(
                                        AppBottomDestination.Passwords
                                    )
                                }
                            }
                        }
                    }
                )
            }
            composable(
                route = EditPasswordDestination.routePattern,
                arguments = listOf(
                    navArgument(EditPasswordDestination.passwordIdArg) {
                        type = NavType.LongType
                    },
                    navArgument(EditPasswordDestination.openedFromArg) {
                        type = NavType.StringType
                        defaultValue = EditPasswordOpenedFrom.Passwords.routeValue
                    }
                )
            ) {
                EditPasswordRoute(
                    onNavigateBackToOrigin = { openedFrom ->
                        when (openedFrom) {
                            EditPasswordOpenedFrom.Vault -> {
                                navController.popBackStack(
                                    AppBottomDestination.Vault.route,
                                    inclusive = false
                                )
                            }

                            EditPasswordOpenedFrom.Passwords -> {
                                navController.popBackStack(
                                    AppBottomDestination.Passwords.route,
                                    inclusive = false
                                )
                            }

                            EditPasswordOpenedFrom.EditCategory -> {
                                navController.popBackStack()
                            }

                            EditPasswordOpenedFrom.SecurityDetails -> {
                                navController.popBackStack(
                                    SecurityDetailsDestination.route,
                                    inclusive = false
                                )
                            }

                            EditPasswordOpenedFrom.GlobalSearch -> {
                                navController.popBackStack()
                            }
                        }
                    },
                    onNavigateAfterSave = { openedFrom ->
                        when (openedFrom) {
                            EditPasswordOpenedFrom.Vault -> {
                                navigateAfterInternalFlowToTopLevel(AppBottomDestination.Passwords)
                            }

                            EditPasswordOpenedFrom.Passwords -> {
                                val returnedToPasswords = navController.popBackStack(
                                    AppBottomDestination.Passwords.route,
                                    inclusive = false
                                )
                                if (!returnedToPasswords) {
                                    navigateAfterInternalFlowToTopLevel(
                                        AppBottomDestination.Passwords
                                    )
                                }
                            }

                            EditPasswordOpenedFrom.EditCategory -> {
                                navController.popBackStack()
                            }

                            EditPasswordOpenedFrom.SecurityDetails -> {
                                navController.popBackStack(
                                    SecurityDetailsDestination.route,
                                    inclusive = false
                                )
                            }

                            EditPasswordOpenedFrom.GlobalSearch -> {
                                navController.popBackStack()
                            }
                        }
                    }
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
                    },
                    onSecuritySummaryClick = {
                        navController.navigate(SecurityDetailsDestination.route)
                    },
                    onOpenGlobalSearch = {
                        navController.navigate(GlobalSearchDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(SecurityDetailsDestination.route) {
                SecurityDetailsRoute(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onOpenPassword = { passwordId ->
                        navController.navigate(
                            EditPasswordDestination.createRoute(
                                passwordId = passwordId,
                                openedFrom = EditPasswordOpenedFrom.SecurityDetails
                            )
                        )
                    }
                )
            }
            composable(AllCategoriesRoute.route) {
                AllCategoriesEntry(
                    onBackClick = {
                        navController.popBackStack()
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
                    onNavigateBack = {
                        navController.popBackStack()
                    },
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

                            NewCategoryOpenedFrom.Vault -> {
                                navigateAfterInternalFlowToTopLevel(AppBottomDestination.Categories)
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
                            EditCategoryOpenedFrom.Vault -> {
                                navController.popBackStack(
                                    AppBottomDestination.Vault.route,
                                    inclusive = false
                                )
                            }

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

                            EditCategoryOpenedFrom.GlobalSearch -> {
                                navController.popBackStack()
                            }
                        }
                    },
                    onNavigateToCategories = {
                        val returnedToCategories = navController.popBackStack(
                            AppBottomDestination.Categories.route,
                            inclusive = false
                        )
                        if (!returnedToCategories) {
                            navigateAfterInternalFlowToTopLevel(AppBottomDestination.Categories)
                        }
                    },
                    onOpenPassword = { passwordId ->
                        navController.navigate(
                            EditPasswordDestination.createRoute(
                                passwordId = passwordId,
                                openedFrom = EditPasswordOpenedFrom.EditCategory
                            )
                        )
                    }
                )
            }
            composable(AppBottomDestination.Settings.route) {
                SettingsRoute(
                    onOpenGlobalSearch = {
                        navController.navigate(GlobalSearchDestination.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(GlobalSearchDestination.route) {
                GlobalSearchRoute(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onOpenCategory = { categoryId ->
                        navController.navigate(
                            EditCategoryRoute.createRoute(
                                categoryId = categoryId,
                                openedFrom = EditCategoryOpenedFrom.GlobalSearch
                            )
                        ) {
                            popUpTo(GlobalSearchDestination.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onOpenPassword = { passwordId ->
                        navController.navigate(
                            EditPasswordDestination.createRoute(
                                passwordId = passwordId,
                                openedFrom = EditPasswordOpenedFrom.GlobalSearch
                            )
                        ) {
                            popUpTo(GlobalSearchDestination.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
