package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun VaultSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes placeholderResId: Int,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val textFieldState = rememberEndCursorTextFieldState(
        text = value,
        onTextChange = onValueChange
    )

    TextField(
        value = textFieldState.value,
        onValueChange = textFieldState.onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                textFieldState.onFocusChanged(focusState.isFocused)
            }
            .testTag(testTag),
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                contentDescription = stringResource(placeholderResId),
                tint = colors.textSecondary
            )
        },
        placeholder = {
            Text(
                text = stringResource(placeholderResId),
                color = colors.textSecondary
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceHigh,
            unfocusedContainerColor = colors.surfaceHigh,
            disabledContainerColor = colors.surfaceHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.primary
        )
    )
}
