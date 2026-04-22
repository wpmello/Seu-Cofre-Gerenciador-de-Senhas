package com.inovalou.seucofregerenciadordesenhas.core.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite

@Composable
fun VaultTopBar(
    @StringRes searchContentDescriptionResId: Int,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricBlue, NeonPink)
                        ),
                        shape = CircleShape
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DeepNavy)
                        .size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_user_profile),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Text(
                text = stringResource(R.string.app_brand_name),
                color = ElectricBlue,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = (-0.6).sp
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onSearchClick)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_search),
                contentDescription = stringResource(searchContentDescriptionResId),
                tint = Color.Unspecified
            )
        }
    }
}
