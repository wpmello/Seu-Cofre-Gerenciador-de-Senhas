package com.inovalou.seucofregerenciadordesenhas.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun SeuCofreBottomBar(
    currentDestination: AppBottomDestination?,
    onDestinationSelected: (AppBottomDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surfaceBright.copy(alpha = 0.68f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AppBottomDestination.entries.forEach { destination ->
            val selected = destination == currentDestination

            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        text = stringResource(destination.labelResId),
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.secondary,
                    selectedTextColor = colors.secondary,
                    indicatorColor = colors.secondary.copy(alpha = 0.16f),
                    unselectedIconColor = colors.textPrimary.copy(alpha = 0.55f),
                    unselectedTextColor = colors.textSecondary.copy(alpha = 0.7f)
                )
            )
        }
    }
}
