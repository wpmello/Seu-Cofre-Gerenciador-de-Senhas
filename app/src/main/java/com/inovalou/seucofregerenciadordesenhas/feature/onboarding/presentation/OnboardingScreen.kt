package com.inovalou.seucofregerenciadordesenhas.feature.onboarding.presentation

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.inovalou.seucofregerenciadordesenhas.core.ui.CollectEffectWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun OnboardingRoute(
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffectWithLifecycle(viewModel.effects) { effect ->
        when (effect) {
            OnboardingEffect.NavigateToApp -> onOnboardingCompleted()
        }
    }

    OnboardingScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val step = onboardingSteps()[uiState.currentStep]

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            colors.background,
                            colors.surface.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.onboarding_brand),
                    color = colors.textPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("onboarding_brand")
                )

                Spacer(modifier = Modifier.height(48.dp))

                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(colors.primary, colors.secondary)),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Security,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                StepIndicators(
                    currentStep = uiState.currentStep,
                    modifier = Modifier.testTag("onboarding_step_indicator")
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(step.titleResId),
                    color = colors.textPrimary,
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_title")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(step.bodyResId),
                    color = colors.textSecondary,
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_body")
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.onboarding_vault_encrypted),
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                VaultPrimaryPersistenceButton(
                    text = stringResource(
                        if (uiState.isLastStep) {
                            R.string.onboarding_finish
                        } else {
                            R.string.onboarding_next
                        }
                    ),
                    isLoading = uiState.isCompleting,
                    onClick = { onAction(OnboardingAction.OnNextClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_next_button")
                )
            }
        }
    }
}

@Composable
private fun StepIndicators(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(OnboardingUiState.STEP_COUNT) { index ->
            val indicatorModifier = if (index == currentStep) {
                Modifier
                    .size(width = 26.dp, height = 10.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(colors.primary, colors.secondary)),
                        shape = CircleShape
                    )
            } else {
                Modifier
                    .size(10.dp)
                    .background(colors.outline.copy(alpha = 0.65f), CircleShape)
            }
            Box(modifier = indicatorModifier)
        }
    }
}

@Composable
private fun onboardingSteps(): List<OnboardingStepUiModel> = listOf(
    OnboardingStepUiModel(
        titleResId = R.string.onboarding_step_one_title,
        bodyResId = R.string.onboarding_step_one_body
    ),
    OnboardingStepUiModel(
        titleResId = R.string.onboarding_step_two_title,
        bodyResId = R.string.onboarding_step_two_body
    ),
    OnboardingStepUiModel(
        titleResId = R.string.onboarding_step_three_title,
        bodyResId = R.string.onboarding_step_three_body
    )
)

private data class OnboardingStepUiModel(
    val titleResId: Int,
    val bodyResId: Int
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        OnboardingScreen(
            uiState = OnboardingUiState(),
            onAction = {}
        )
    }
}
