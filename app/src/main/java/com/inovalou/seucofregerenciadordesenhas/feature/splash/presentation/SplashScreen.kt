package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme

internal object SplashScreenSpec {
    const val animationDurationMillis = 2200
}

@Composable
fun SplashRoute(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashScreenSpec.animationDurationMillis,
                easing = LinearEasing
            )
        )
        onSplashFinished()
    }

    SplashScreen(
        uiState = SplashUiState(progress = progress.value),
        modifier = modifier
    )
}

@Composable
fun SplashScreen(
    uiState: SplashUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1C2639),
                            Color(0xFF121A2A),
                            MidnightBlue
                        )
                    )
                )
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.02f),
                                Color.Transparent
                            )
                        )
                    )
                }
        ) {
            DecorativeBackground()

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 35.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrandSection()

                Spacer(modifier = Modifier.height(72.dp))

                FooterSection(
                    progress = uiState.progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DecorativeBackground() {
    Box(
        modifier = Modifier
            .size(384.dp)
            .offset(x = (-96).dp, y = (-96).dp)
            .align(Alignment.TopStart)
            .blur(60.dp)
            .background(
                color = ElectricBlue.copy(alpha = 0.03f),
                shape = CircleShape
            )
    )
}

@Composable
private fun BrandSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShieldLogo(
            modifier = Modifier.testTag("splash_logo")
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.splash_title),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2.4).sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.splash_subtitle),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.4.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ShieldLogo(modifier: Modifier = Modifier) {
    val logoShape = RoundedCornerShape(40.dp)
    val logoContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.98f)
    val logoInnerGlowColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.10f)
    val logoBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)

    Box(
        modifier = modifier.size(128.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(141.dp)
                .blur(32.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            ElectricBlue.copy(alpha = 0.20f),
                            NeonPink.copy(alpha = 0.20f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(128.dp)
                .background(
                    color = logoContainerColor,
                    shape = logoShape
                )
                .drawBehind {
                    drawRoundRect(
                        color = logoInnerGlowColor,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            x = 40.dp.toPx(),
                            y = 40.dp.toPx()
                        )
                    )
                }
                .drawBehind {
                    drawRoundRect(
                        color = logoBorderColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                            x = 40.dp.toPx(),
                            y = 40.dp.toPx()
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
private fun FooterSection(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProgressBar(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("splash_progress_indicator")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.splash_loading),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 1.sp
            ),
            color = if (progress >= 1f) NeonPink else ElectricBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = ElectricBlue.copy(alpha = 0.40f),
                modifier = Modifier.size(10.dp)
            )

            Text(
                text = stringResource(R.string.splash_encryption_hint),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = ElectricBlue.copy(alpha = 0.40f)
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Canvas(
        modifier = modifier.height(2.dp)
    ) {
        val centerY = size.height / 2f
        val clampedProgress = progress.coerceIn(0f, 1f)

        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(ElectricBlue, NeonPink)
            ),
            start = Offset(0f, centerY),
            end = Offset(size.width * clampedProgress, centerY),
            strokeWidth = size.height,
            cap = StrokeCap.Round
        )
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        SplashScreen(uiState = SplashUiState())
    }
}
