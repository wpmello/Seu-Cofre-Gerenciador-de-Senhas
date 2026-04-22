package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite

data class VaultPasswordListItemModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val initials: String? = null
)

@Composable
fun VaultPasswordListColumn(
    passwords: List<VaultPasswordListItemModel>,
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 16.dp,
    showTrailingIndicator: Boolean = false,
    onItemClick: ((Long) -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(itemSpacing)
    ) {
        passwords.forEach { password ->
            VaultPasswordListItem(
                password = password,
                onClick = onItemClick?.let { click -> { click(password.id) } },
                showTrailingIndicator = showTrailingIndicator
            )
        }
    }
}

@Composable
fun VaultPasswordListItem(
    password: VaultPasswordListItemModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showTrailingIndicator: Boolean = onClick != null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SlateBlue.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp)
            .testTag("password_item_${password.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = DeepNavy,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = password.initials?.takeIf { it.isNotBlank() } ?: password.title.toInitials(),
                    color = SoftWhite,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = password.title,
                    color = SoftWhite,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (password.supportingText.isNotBlank()) {
                    Text(
                        text = password.supportingText,
                        color = MistText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (showTrailingIndicator) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = GhostOutline,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun String.toInitials(): String {
    val parts = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) {
        return "A"
    }

    if (parts.size == 1) {
        return parts.first().take(1).uppercase()
    }

    return buildString {
        append(parts.first().take(1))
        append(parts.last().take(1))
    }.uppercase()
}
