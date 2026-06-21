package com.inovalou.seucofregerenciadordesenhas.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppPreferences
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.usecase.CompleteOnboardingUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation.OnboardingRoute
import com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation.OnboardingViewModel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class RootNavigationOnboardingTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun givenOnboardingCompletion_whenNavigatingToApp_thenClearsOnboardingFromBackStack() {
        val repository = FakeAppPreferencesRepository()
        val viewModel = OnboardingViewModel(CompleteOnboardingUseCase(repository))
        composeRule.setContent {
            SeuCofreGerenciadorDeSenhasTheme {
                TestRootNavigation(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("Próximo").performClick()
        composeRule.onNodeWithText("Próximo").performClick()
        composeRule.onNodeWithText("Começar").performClick()

        composeRule.onNodeWithText("App").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(SeuCofreRoutes.APP, capturedRoute)
            assertNull(previousRoute)
        }
    }

    @Composable
    private fun TestRootNavigation(viewModel: OnboardingViewModel) {
        val navController = rememberNavController()
        DisposableEffect(navController) {
            val listener = NavController.OnDestinationChangedListener { controller, destination, _ ->
                capturedRoute = destination.route
                previousRoute = controller.previousBackStackEntry?.destination?.route
            }
            navController.addOnDestinationChangedListener(listener)
            onDispose {
                navController.removeOnDestinationChangedListener(listener)
            }
        }
        NavHost(navController = navController, startDestination = SeuCofreRoutes.ONBOARDING) {
            composable(SeuCofreRoutes.ONBOARDING) {
                OnboardingRoute(
                    onOnboardingCompleted = {
                        navController.navigate(SeuCofreRoutes.APP) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    viewModel = viewModel
                )
            }
            composable(SeuCofreRoutes.APP) {
                Text("App")
            }
        }
    }

    private class FakeAppPreferencesRepository(
        initialPreferences: AppPreferences = AppPreferences()
    ) : AppPreferencesRepository {
        private val preferencesFlow = MutableStateFlow(initialPreferences)

        override fun observePreferences(): Flow<AppPreferences> = preferencesFlow

        override suspend fun updateUserName(userName: String) {
            preferencesFlow.value = preferencesFlow.value.copy(userName = userName)
        }

        override suspend fun updateLanguage(language: AppLanguage) {
            preferencesFlow.value = preferencesFlow.value.copy(language = language)
        }

        override suspend fun updateThemePreference(themePreference: AppThemePreference) {
            preferencesFlow.value = preferencesFlow.value.copy(themePreference = themePreference)
        }

        override suspend fun completeOnboarding() {
            preferencesFlow.value = preferencesFlow.value.copy(hasCompletedOnboarding = true)
        }
    }

    private companion object {
        var capturedRoute: String? = null
        var previousRoute: String? = null
    }
}
