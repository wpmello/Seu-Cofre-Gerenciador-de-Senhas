package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultGradientFab
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

private val SlateSelection = SurfaceBright.copy(alpha = 0.62f)

data class CategorySelectableIconUiModel(
    val iconKey: String,
    @DrawableRes val iconResId: Int,
    val isSelected: Boolean
)

@Composable
fun CategorySectionLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        modifier = modifier,
        color = GhostOutline,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
fun CategoryValidationText(
    @StringRes errorResId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(errorResId),
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

@Composable
fun CategoryPrimaryActionButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VaultPrimaryPersistenceButton(
        text = text,
        isLoading = isLoading,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun CategoryCreateFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VaultGradientFab(
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun CategoryIconSelectionGrid(
    icons: List<CategorySelectableIconUiModel>,
    onIconClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        icons.chunked(columns).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { icon ->
                    CategoryIconCell(
                        icon = icon,
                        onClick = { onIconClick(icon.iconKey) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - row.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryIconCell(
    icon: CategorySelectableIconUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (icon.isSelected) SlateSelection else DeepNavy
    val borderColor = if (icon.isSelected) {
        ElectricBlue.copy(alpha = 0.7f)
    } else {
        Color.Transparent
    }
    val iconTint = if (icon.isSelected) ElectricBlue else GhostOutline

    Box(
        modifier = modifier
            .size(64.dp)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon.iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}
