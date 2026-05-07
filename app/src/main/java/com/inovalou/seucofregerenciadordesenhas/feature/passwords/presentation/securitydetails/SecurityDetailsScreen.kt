package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListColumn
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListItemModel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultAmber
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultGreen

private val HeroGradient = Brush.linearGradient(
    colors = listOf(ElectricBlue, NeonPink)
)

@Composable
fun SecurityDetailsRoute(
    onNavigateBack: () -> Unit,
    onOpenPassword: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SecurityDetailsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                SecurityDetailsEffect.NavigateBack -> onNavigateBack()
                is SecurityDetailsEffect.OpenPassword -> onOpenPassword(effect.passwordId)
            }
        }
    }

    SecurityDetailsScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun SecurityDetailsScreen(
    uiState: SecurityDetailsUiState,
    onAction: (SecurityDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MidnightBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("security_details_screen"),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            VaultBackHeader(
                title = stringResource(R.string.security_details_title),
                titleFontSize = 22,
                navigationContentDescription = stringResource(R.string.security_details_back),
                onBackClick = { onAction(SecurityDetailsAction.OnBackClick) },
                testTag = "security_details_header"
            )

            when (val contentState = uiState.contentState) {
                SecurityDetailsContentState.Loading -> SecurityDetailsLoading()
                is SecurityDetailsContentState.Error -> SecurityDetailsError(contentState)
                SecurityDetailsContentState.Empty,
                SecurityDetailsContentState.Content -> SecurityDetailsContent(
                    uiState = uiState,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
private fun SecurityDetailsContent(
    uiState: SecurityDetailsUiState,
    onAction: (SecurityDetailsAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        OverallStatusCard(uiState = uiState)
        CalculationInfoCard(criteria = uiState.criteria)
        SecurityDetailsTabs(
            tabs = uiState.tabs,
            selectedTab = uiState.selectedTab,
            onTabSelected = { tab -> onAction(SecurityDetailsAction.OnTabSelected(tab)) }
        )
        PasswordBucketList(
            passwords = uiState.visiblePasswords,
            contentState = uiState.contentState,
            onPasswordClick = { passwordId ->
                onAction(SecurityDetailsAction.OnPasswordClick(passwordId))
            }
        )
    }
}

@Composable
private fun SecurityDetailsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = ElectricBlue,
            modifier = Modifier.testTag("security_details_loading")
        )
    }
}

@Composable
private fun SecurityDetailsError(contentState: SecurityDetailsContentState.Error) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
            .testTag("security_details_error"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(contentState.messageResId),
            color = SoftWhite,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun OverallStatusCard(uiState: SecurityDetailsUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DeepNavy, SlateBlue.copy(alpha = 0.92f))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("security_details_overall_card")
            .padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(164.dp)
                .drawBehind {
                    drawCircle(
                        brush = HeroGradient,
                        alpha = 0.18f,
                        center = Offset(size.width * 0.72f, size.height * 0.2f),
                        radius = size.maxDimension * 0.72f
                    )
                }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CircularScoreGauge(
                scorePercent = uiState.overallScorePercent,
                visualState = uiState.visualState
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.security_details_status_label),
                    color = MistText,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = stringResource(uiState.statusResId),
                    color = SoftWhite,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("security_details_status")
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.security_details_passwords_analyzed,
                        uiState.totalPasswords,
                        uiState.totalPasswords
                    ),
                    color = MistText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = stringResource(R.string.security_details_index_calculated),
                    color = MistText,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun CircularScoreGauge(
    scorePercent: Int,
    visualState: SecurityDetailsVisualState
) {
    val normalizedScore = scorePercent.coerceIn(0, 100)
    val accentColor = visualState.accentColor()

    Box(
        modifier = Modifier
            .size(112.dp)
            .testTag("security_details_score_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 10.dp.toPx()
            drawCircle(
                color = SlateBlue.copy(alpha = 0.62f),
                radius = (size.minDimension - strokeWidth) / 2f,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * normalizedScore / 100f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Text(
            text = "$normalizedScore%",
            color = SoftWhite,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("security_details_score")
        )
    }
}

@Composable
private fun CalculationInfoCard(criteria: List<SecurityDetailsCriteriaUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("security_details_calculation_card")
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = stringResource(R.string.security_details_how_calculation_works),
            color = SoftWhite,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.security_details_calculation_description),
            color = MistText,
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            criteria.forEach { criterion ->
                SecurityCriterionRow(criterion = criterion)
            }
        }
        Text(
            text = stringResource(R.string.security_details_absolute_note),
            color = MistText.copy(alpha = 0.72f),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontStyle = FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = SlateBlue.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        )
    }
}

@Composable
private fun SecurityCriterionRow(criterion: SecurityDetailsCriteriaUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MidnightBlue.copy(alpha = 0.46f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SecurityDot(visualState = criterion.visualState)
        Text(
            text = stringResource(criterion.labelResId),
            color = SoftWhite,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(criterion.rangeResId),
            color = MistText,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SecurityDetailsTabs(
    tabs: List<SecurityDetailsTabUiModel>,
    selectedTab: SecurityDetailsTab,
    onTabSelected: (SecurityDetailsTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .testTag("security_details_tabs"),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        tabs.forEach { tab ->
            SecurityDetailsTabButton(
                tab = tab,
                selected = tab.tab == selectedTab,
                onClick = { onTabSelected(tab.tab) }
            )
        }
    }
}

@Composable
private fun SecurityDetailsTabButton(
    tab: SecurityDetailsTabUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accentColor = tab.tab.visualState().accentColor()

    Column(
        modifier = Modifier
            .background(
                color = if (selected) SlateBlue else DeepNavy,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("security_details_tab_${tab.tab.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = tab.count.toString(),
            color = if (selected) accentColor else MistText,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(tab.tab.labelResId).uppercase(),
            color = if (selected) SoftWhite else MistText,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun PasswordBucketList(
    passwords: List<SecurityDetailsPasswordUiModel>,
    contentState: SecurityDetailsContentState,
    onPasswordClick: (Long) -> Unit
) {
    when {
        contentState is SecurityDetailsContentState.Empty -> SecurityDetailsEmptyVault()
        passwords.isEmpty() -> SecurityDetailsEmptyTab()
        else -> VaultPasswordListColumn(
            passwords = passwords.map { password -> password.toVaultPasswordListItemModel() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("security_details_password_list"),
            itemSpacing = 12.dp,
            showTrailingIndicator = true,
            onItemClick = onPasswordClick
        )
    }
}

@Composable
private fun SecurityDetailsEmptyVault() {
    EmptySecurityCard(
        title = stringResource(R.string.security_details_empty_title),
        message = stringResource(R.string.security_details_empty_message),
        testTag = "security_details_empty"
    )
}

@Composable
private fun SecurityDetailsEmptyTab() {
    EmptySecurityCard(
        title = stringResource(R.string.security_details_empty_tab),
        message = "",
        testTag = "security_details_empty_tab"
    )
}

@Composable
private fun EmptySecurityCard(
    title: String,
    message: String,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = SoftWhite,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold
        )
        if (message.isNotBlank()) {
            Text(
                text = message,
                color = MistText,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SecurityDot(
    visualState: SecurityDetailsVisualState,
    modifier: Modifier = Modifier
) {
    val accentColor = visualState.accentColor()

    Box(
        modifier = modifier
            .size(18.dp)
            .drawBehind {
                drawCircle(
                    color = accentColor.copy(alpha = 0.36f),
                    radius = size.maxDimension / 2f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = accentColor,
                    shape = CircleShape
                )
        )
    }
}

private fun SecurityDetailsVisualState.accentColor(): Color = when (this) {
    SecurityDetailsVisualState.Poor -> Color(0xFFFF716C)
    SecurityDetailsVisualState.Moderate -> VaultAmber
    SecurityDetailsVisualState.Excellent -> VaultGreen
}

private fun SecurityDetailsTab.visualState(): SecurityDetailsVisualState = when (this) {
    SecurityDetailsTab.Weak -> SecurityDetailsVisualState.Poor
    SecurityDetailsTab.Moderate -> SecurityDetailsVisualState.Moderate
    SecurityDetailsTab.Safe -> SecurityDetailsVisualState.Excellent
}

private fun SecurityDetailsPasswordUiModel.toVaultPasswordListItemModel(): VaultPasswordListItemModel =
    VaultPasswordListItemModel(
        id = id,
        title = title,
        supportingText = supportingText,
        initials = initials,
        securityLevel = visualState.toVaultPasswordListSecurityLevel(),
        scorePercent = scorePercent,
        tagResIds = tagResIds
    )

private fun SecurityDetailsVisualState.toVaultPasswordListSecurityLevel(): VaultPasswordListSecurityLevel =
    when (this) {
        SecurityDetailsVisualState.Poor -> VaultPasswordListSecurityLevel.Weak
        SecurityDetailsVisualState.Moderate -> VaultPasswordListSecurityLevel.Moderate
        SecurityDetailsVisualState.Excellent -> VaultPasswordListSecurityLevel.Safe
    }

@Preview
@Composable
private fun SecurityDetailsScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        SecurityDetailsScreen(
            uiState = SecurityDetailsUiState(
                contentState = SecurityDetailsContentState.Content,
                overallScorePercent = 58,
                statusResId = R.string.categories_security_moderate,
                visualState = SecurityDetailsVisualState.Moderate,
                totalPasswords = 28,
                tabs = listOf(
                    SecurityDetailsTabUiModel(SecurityDetailsTab.Weak, 4),
                    SecurityDetailsTabUiModel(SecurityDetailsTab.Moderate, 7),
                    SecurityDetailsTabUiModel(SecurityDetailsTab.Safe, 12)
                ),
                visiblePasswords = listOf(
                    SecurityDetailsPasswordUiModel(
                        id = 1L,
                        title = "Banco Aurora",
                        supportingText = "usuario@email.com",
                        initials = "BA",
                        scorePercent = 35,
                        visualState = SecurityDetailsVisualState.Moderate,
                        tagResIds = listOf(R.string.edit_password_security_tag_weak)
                    )
                )
            ),
            onAction = {}
        )
    }
}
