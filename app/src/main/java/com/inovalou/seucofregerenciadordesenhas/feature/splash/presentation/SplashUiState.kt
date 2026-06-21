package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

data class SplashUiState(
    val progress: Float = 0f
)

enum class SplashLaunchDestination {
    Onboarding,
    App
}

data class SplashLaunchUiState(
    val destination: SplashLaunchDestination? = null
)
