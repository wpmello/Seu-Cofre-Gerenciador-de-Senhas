package com.inovalou.seucofregerenciadordesenhas.feature.splash.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Size
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

internal object SplashScreenSpec {
    const val animationDurationMillis = 2200
}

@Composable
fun SplashRoute(
    onLaunchResolved: (SplashLaunchDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val launchUiState = viewModel.uiState.collectAsStateWithLifecycle()
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
    }

    LaunchedEffect(launchUiState.value.destination) {
        launchUiState.value.destination?.let(onLaunchResolved)
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
        color = Color.Black
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            StarFieldBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("splash_star_field_background")
            )

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
private fun StarFieldBackground(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.vaultColors
    val dimStar = colors.textSecondary
    val brightStar = Color.White
    val blueStar = colors.primary
    val purpleStar = colors.secondary
    val starSpecs = remember { buildStarFieldSpecs() }

    Canvas(modifier = modifier) {
        drawRect(Color.Black)

        val baseStarSize = 1.dp.toPx()

        starSpecs.forEach { star ->
            val starSize = baseStarSize * star.sizeMultiplier
            val starColor = when (star.color) {
                StarColor.Purple -> purpleStar
                StarColor.Blue -> blueStar
                StarColor.Bright -> brightStar
                StarColor.Dim -> dimStar
            }

            drawRect(
                color = starColor.copy(alpha = star.alpha),
                topLeft = Offset(
                    x = star.xSeed * size.width,
                    y = star.ySeed * size.height
                ),
                size = Size(starSize, starSize)
            )

            if (star.hasCluster) {
                val clusterSize = baseStarSize * 0.82f
                val clusterOrigin = Offset(
                    x = (star.xSeed * size.width + baseStarSize * 4f).coerceAtMost(size.width),
                    y = (star.ySeed * size.height + baseStarSize * 3f).coerceAtMost(size.height)
                )

                drawRect(
                    color = starColor.copy(alpha = 0.42f),
                    topLeft = clusterOrigin,
                    size = Size(clusterSize, clusterSize)
                )
            }
        }
    }
}

private fun buildStarFieldSpecs(): List<StarSpec> = List(STAR_FIELD_COUNT) { index ->
    val starSizeSeed = (index * 37) % 100
    val alphaSeed = (index * 53) % 100
    StarSpec(
        xSeed = ((index * 73 + index * index * 17) % 997) / 997f,
        ySeed = ((index * 151 + index * index * 29) % 1543) / 1543f,
        sizeMultiplier = when {
            starSizeSeed > 96 -> 2.2f
            starSizeSeed > 84 -> 1.45f
            else -> 1f
        },
        alpha = when {
            alphaSeed > 92 -> 0.92f
            alphaSeed > 70 -> 0.62f
            else -> 0.36f
        },
        color = when {
            index % 41 == 0 -> StarColor.Purple
            index % 29 == 0 -> StarColor.Blue
            index % 7 == 0 -> StarColor.Bright
            else -> StarColor.Dim
        },
        hasCluster = index % 67 == 0
    )
}

private data class StarSpec(
    val xSeed: Float,
    val ySeed: Float,
    val sizeMultiplier: Float,
    val alpha: Float,
    val color: StarColor,
    val hasCluster: Boolean
)

private enum class StarColor {
    Dim,
    Bright,
    Blue,
    Purple
}

private const val STAR_FIELD_COUNT = 720

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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val colors = MaterialTheme.vaultColors
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
                            colors.primary.copy(alpha = 0.20f),
                            colors.secondary.copy(alpha = 0.20f)
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
                tint = colors.primary,
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
    val colors = MaterialTheme.vaultColors

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
            color = if (progress >= 1f) colors.secondary else colors.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = colors.primary.copy(alpha = 0.56f),
                modifier = Modifier.size(10.dp)
            )

            Text(
                text = stringResource(R.string.splash_encryption_hint),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    lineHeight = 13.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colors.primary.copy(alpha = 0.56f)
            )
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
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
                colors = listOf(colors.primary, colors.secondary)
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
