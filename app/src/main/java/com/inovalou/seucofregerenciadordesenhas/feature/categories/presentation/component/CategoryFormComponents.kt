package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

private val CategoryPrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, NeonPink)
)
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
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CategoryPrimaryButtonGradient),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MidnightBlue,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    color = MidnightBlue,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CategoryCreateFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 14.dp,
            focusedElevation = 12.dp,
            hoveredElevation = 12.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricBlue, NeonPink)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
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
