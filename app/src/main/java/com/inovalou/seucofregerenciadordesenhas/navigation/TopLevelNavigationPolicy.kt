package com.inovalou.seucofregerenciadordesenhas.navigation

import com.inovalou.seucofregerenciadordesenhas.core.navigation.AppBottomDestination

internal enum class TopLevelNavigationIntent {
    BottomDestinationSelection,
    InternalFlowCompletion
}

internal data class TopLevelNavigationOptions(
    val saveState: Boolean,
    val restoreState: Boolean
)

internal fun topLevelNavigationOptionsFor(
    intent: TopLevelNavigationIntent,
    destination: AppBottomDestination
): TopLevelNavigationOptions = when (intent) {
    TopLevelNavigationIntent.BottomDestinationSelection ->
        if (destination == AppBottomDestination.Vault) {
            TopLevelNavigationOptions(
                saveState = false,
                restoreState = false
            )
        } else {
            TopLevelNavigationOptions(
                saveState = true,
                restoreState = true
            )
        }

    TopLevelNavigationIntent.InternalFlowCompletion -> TopLevelNavigationOptions(
        saveState = false,
        restoreState = false
    )
}
