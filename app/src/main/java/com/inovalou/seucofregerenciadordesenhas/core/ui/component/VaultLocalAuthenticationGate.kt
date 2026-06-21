package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun VaultLocalAuthenticationGate(
    isAuthenticating: Boolean,
    @StringRes messageResId: Int,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "local_auth_gate"
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        color = colors.surfaceHigh,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isAuthenticating) {
                CircularProgressIndicator(
                    modifier = Modifier.testTag("${testTag}_loading"),
                    color = colors.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = colors.primary
                )
            }
            Text(
                text = stringResource(R.string.local_auth_locked_title),
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(messageResId),
                color = colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            OutlinedButton(
                onClick = onRetryClick,
                enabled = !isAuthenticating,
                modifier = Modifier.testTag("${testTag}_retry_button")
            ) {
                Text(text = stringResource(R.string.local_auth_retry_button))
            }
        }
    }
}
