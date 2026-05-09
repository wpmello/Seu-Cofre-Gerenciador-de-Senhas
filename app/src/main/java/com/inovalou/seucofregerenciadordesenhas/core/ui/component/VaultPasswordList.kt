package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

data class VaultPasswordListItemModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val initials: String? = null,
    val securityLevel: VaultPasswordListSecurityLevel? = null,
    val scorePercent: Int? = null,
    val tagResIds: List<Int> = emptyList()
)

enum class VaultPasswordListSecurityLevel {
    Weak,
    Moderate,
    Safe
}

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

fun LazyListScope.vaultPasswordListItems(
    passwords: List<VaultPasswordListItemModel>,
    onItemClick: ((Long) -> Unit)? = null,
    showTrailingIndicator: Boolean = onItemClick != null
) {
    items(
        items = passwords,
        key = { password -> password.id }
    ) { password ->
        VaultPasswordListItem(
            password = password,
            onClick = onItemClick?.let { click -> { click(password.id) } },
            showTrailingIndicator = showTrailingIndicator
        )
    }
}

@Composable
fun VaultPasswordListItem(
    password: VaultPasswordListItemModel,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showTrailingIndicator: Boolean = onClick != null
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surfaceHigh.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp)
            .testTag("password_item_${password.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = colors.surface,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = password.initials?.takeIf { it.isNotBlank() } ?: password.title.toInitials(),
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                password.securityLevel?.let { securityLevel ->
                    PasswordStrengthDot(
                        securityLevel = securityLevel,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                            .testTag("password_strength_dot_${password.id}")
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = password.title,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (password.supportingText.isNotBlank()) {
                    Text(
                        text = password.supportingText,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (password.tagResIds.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        password.tagResIds.take(2).forEach { tagResId ->
                            PasswordSecurityTag(
                                labelResId = tagResId,
                                accentColor = tagResId.securityTagAccentColor()
                            )
                        }
                    }
                }
            }
        }

        if (showTrailingIndicator || password.scorePercent != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (password.scorePercent != null) {
                    Text(
                        text = "${password.scorePercent}%",
                        color = password.securityLevel?.accentColor() ?: colors.textSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("password_score_${password.id}")
                    )
                } else {
                    password.securityLevel?.let { securityLevel ->
                        val flagResId = securityLevel.flagResId()
                        if (flagResId != null) {
                            Text(
                                text = stringResource(flagResId),
                                color = securityLevel.accentColor(),
                                fontSize = 10.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.testTag("password_strength_flag_${password.id}")
                            )
                        }
                    }
                }
                if (showTrailingIndicator) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        tint = colors.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PasswordSecurityTag(
    labelResId: Int,
    accentColor: Color
) {
    Text(
        text = stringResource(labelResId).uppercase(),
        color = accentColor,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.45.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .background(
                color = accentColor.copy(alpha = 0.14f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun PasswordStrengthDot(
    securityLevel: VaultPasswordListSecurityLevel,
    modifier: Modifier = Modifier
) {
    val accentColor = securityLevel.accentColor()

    Box(
        modifier = modifier
            .size(20.dp)
            .drawBehind {
                drawCircle(
                    color = accentColor.copy(alpha = 0.5f),
                    radius = size.maxDimension / 2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = accentColor,
                    shape = CircleShape
                )
        )
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

private fun VaultPasswordListSecurityLevel.flagResId(): Int? = when (this) {
    VaultPasswordListSecurityLevel.Weak -> R.string.passwords_strength_weak_flag
    VaultPasswordListSecurityLevel.Moderate -> R.string.passwords_strength_moderate_flag
    VaultPasswordListSecurityLevel.Safe -> null
}

@Composable
private fun VaultPasswordListSecurityLevel.accentColor(): Color = when (this) {
    VaultPasswordListSecurityLevel.Weak -> MaterialTheme.vaultColors.danger
    VaultPasswordListSecurityLevel.Moderate -> MaterialTheme.vaultColors.warning
    VaultPasswordListSecurityLevel.Safe -> MaterialTheme.vaultColors.success
}

@Composable
private fun Int.securityTagAccentColor(): Color = when (this) {
    R.string.edit_password_security_tag_weak -> MaterialTheme.vaultColors.danger
    R.string.edit_password_security_tag_safe -> MaterialTheme.vaultColors.success
    else -> MaterialTheme.vaultColors.textSecondary
}
