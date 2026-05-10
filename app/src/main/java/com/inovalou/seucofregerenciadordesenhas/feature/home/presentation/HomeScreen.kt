package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import androidx.annotation.PluralsRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultFloatingTopBar
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultGradientFab
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListColumn
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListItem
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListItemModel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun VaultHomeRoute(
    onOpenCategory: (Long) -> Unit,
    onOpenAllCategories: () -> Unit,
    onOpenPasswords: () -> Unit,
    onOpenPassword: (Long) -> Unit,
    onAddPassword: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultHomeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is VaultHomeEffect.NavigateToCategoryDetails -> onOpenCategory(effect.categoryId)
                VaultHomeEffect.NavigateToAllCategories -> onOpenAllCategories()
                VaultHomeEffect.NavigateToPasswords -> onOpenPasswords()
                is VaultHomeEffect.NavigateToPasswordDetails -> onOpenPassword(effect.passwordId)
                VaultHomeEffect.NavigateToNewPassword -> onAddPassword()
            }
        }
    }

    VaultHomeScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onOpenGlobalSearch = onOpenGlobalSearch,
        modifier = modifier
    )
}

@Composable
fun VaultHomeScreen(
    uiState: VaultHomeUiState,
    onAction: (VaultHomeAction) -> Unit,
    onOpenGlobalSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val density = LocalDensity.current
    var summaryCardTopPx by remember { mutableFloatStateOf(Float.NaN) }
    var fabTopPx by remember { mutableFloatStateOf(Float.NaN) }
    val summaryExpandedMaxHeight = with(density) {
        if (summaryCardTopPx.isFinite() && fabTopPx.isFinite() && fabTopPx > summaryCardTopPx) {
            (fabTopPx - summaryCardTopPx - VaultHomeSummaryListFabClearance.toPx())
                .coerceAtLeast(VaultHomeSummaryExpandedMinimumHeight.toPx())
                .toDp()
        } else {
            null
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .testTag("vault_home_screen")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("vault_home_content"),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = VaultHomeContentTopPadding,
                    end = 24.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                when (val contentState = uiState.contentState) {
                    VaultHomeContentState.Loading -> {
                        item {
                            VaultHomeLoading()
                        }
                    }

                    is VaultHomeContentState.Error -> {
                        item {
                            VaultHomeError(contentState)
                        }
                    }

                    VaultHomeContentState.Content,
                    VaultHomeContentState.Empty -> {
                        item {
                            VaultHomeSummaryCard(
                                totalPasswords = uiState.totalPasswords,
                                weakPasswords = uiState.weakPasswords,
                                moderatePasswords = uiState.moderatePasswords,
                                strongPasswords = uiState.strongPasswords,
                                summaryCardState = uiState.summaryCardState,
                                expandedMaxHeight = summaryExpandedMaxHeight,
                                onAction = onAction,
                                modifier = Modifier.onGloballyPositioned { coordinates ->
                                    summaryCardTopPx = coordinates.positionInRoot().y
                                }
                            )
                        }

                        item {
                            VaultHomeCategoriesSection(
                                categories = uiState.categories,
                                showOtherCategories = uiState.showOtherCategories,
                                onAction = onAction
                            )
                        }

                        item {
                            VaultHomeRecentSection(
                                passwords = uiState.recentPasswords,
                                onAction = onAction
                            )
                        }
                    }
                }
            }

            VaultFloatingTopBar(
                searchContentDescriptionResId = R.string.categories_search,
                onSearchClick = onOpenGlobalSearch,
                testTag = "vault_home_top_bar",
                modifier = Modifier.align(Alignment.TopCenter)
            )

            VaultGradientFab(
                contentDescription = stringResource(R.string.passwords_create_fab),
                onClick = { onAction(VaultHomeAction.OnAddPasswordClick) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .onGloballyPositioned { coordinates ->
                        fabTopPx = coordinates.positionInRoot().y
                    }
                    .testTag("vault_home_create_password_fab")
            )
        }
    }
}

@Composable
private fun VaultHomeLoading() {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("vault_home_loading"),
            color = colors.primary
        )
    }
}

@Composable
private fun VaultHomeError(contentState: VaultHomeContentState.Error) {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
            .testTag("vault_home_error"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(contentState.messageResId),
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun VaultHomeSummaryCard(
    totalPasswords: Int,
    weakPasswords: Int,
    moderatePasswords: Int,
    strongPasswords: Int,
    summaryCardState: VaultHomeSummaryCardState,
    expandedMaxHeight: Dp?,
    onAction: (VaultHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    when (summaryCardState) {
        VaultHomeSummaryCardState.Overview -> VaultHomeSummaryOverviewCard(
            totalPasswords = totalPasswords,
            weakPasswords = weakPasswords,
            moderatePasswords = moderatePasswords,
            strongPasswords = strongPasswords,
            onFilterClick = { filter -> onAction(VaultHomeAction.OnSecuritySummaryTagClick(filter)) },
            modifier = modifier
        )

        is VaultHomeSummaryCardState.Loading,
        is VaultHomeSummaryCardState.Content,
        is VaultHomeSummaryCardState.Empty,
        is VaultHomeSummaryCardState.Error -> VaultHomeSummaryListCard(
            summaryCardState = summaryCardState,
            expandedMaxHeight = expandedMaxHeight,
            onBackClick = { onAction(VaultHomeAction.OnSecuritySummaryBackClick) },
            onPasswordClick = { passwordId ->
                onAction(VaultHomeAction.OnSecuritySummaryPasswordClick(passwordId))
            },
            modifier = modifier
        )
    }
}

@Composable
private fun VaultHomeSummaryOverviewCard(
    totalPasswords: Int,
    weakPasswords: Int,
    moderatePasswords: Int,
    strongPasswords: Int,
    onFilterClick: (VaultHomeSecurityFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.primary, colors.secondary)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("vault_home_summary_card")
            .padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(72.dp)
                .background(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(34.dp)
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.vault_home_total_passwords_label).uppercase(),
                    color = colors.onAccent.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = totalPasswords.toString(),
                    color = colors.onAccent,
                    fontSize = 60.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("vault_home_total_passwords")
                )
            }

            VaultHomeSecuritySummaryTags(
                weakPasswords = weakPasswords,
                moderatePasswords = moderatePasswords,
                strongPasswords = strongPasswords,
                onFilterClick = onFilterClick
            )
        }
    }
}

@Composable
private fun VaultHomeSummaryListCard(
    summaryCardState: VaultHomeSummaryCardState,
    expandedMaxHeight: Dp?,
    onBackClick: () -> Unit,
    onPasswordClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val cardMaxHeight = expandedMaxHeight ?: VaultHomeSummaryExpandedFallbackMaxHeight
    val filter = summaryCardState.filter
    val listMaxHeight = (cardMaxHeight - VaultHomeSummaryListChromeHeight)
        .coerceAtLeast(VaultHomeSummaryPasswordItemHeight)
        .coerceAtMost(VaultHomeSummaryVisiblePasswordListHeight)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = cardMaxHeight)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.primary, colors.secondary)
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .testTag("vault_home_summary_card")
            .padding(VaultHomeSummaryListPadding),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        VaultHomeSummaryListHeader(
            filter = filter,
            onBackClick = onBackClick
        )

        when (summaryCardState) {
            is VaultHomeSummaryCardState.Loading -> VaultHomeSummaryListLoading()
            is VaultHomeSummaryCardState.Content -> VaultHomeSummaryPasswordList(
                passwords = summaryCardState.passwords,
                maxListHeight = listMaxHeight,
                onPasswordClick = onPasswordClick
            )
            is VaultHomeSummaryCardState.Empty -> VaultHomeSummaryListMessage(
                message = stringResource(R.string.vault_home_summary_passwords_empty),
                testTag = "vault_home_summary_passwords_empty"
            )
            is VaultHomeSummaryCardState.Error -> VaultHomeSummaryListMessage(
                message = stringResource(summaryCardState.messageResId),
                testTag = "vault_home_summary_passwords_error"
            )
            VaultHomeSummaryCardState.Overview -> Unit
        }
    }
}

@Composable
private fun VaultHomeSummaryListHeader(
    filter: VaultHomeSecurityFilter,
    onBackClick: () -> Unit
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(colors.onAccent.copy(alpha = 0.14f))
                .clickable(onClick = onBackClick)
                .testTag("vault_home_summary_list_back"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.security_details_back),
                tint = colors.onAccent,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = stringResource(filter.titleResId),
            color = colors.onAccent,
            fontSize = 22.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun VaultHomeSummaryListLoading() {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(VaultHomeSummaryLoadingHeight)
            .testTag("vault_home_summary_passwords_loading"),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.onAccent
        )
    }
}

@Composable
private fun VaultHomeSummaryListMessage(
    message: String,
    testTag: String
) {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.onAccent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(18.dp)
            .testTag(testTag)
    ) {
        Text(
            text = message,
            color = colors.onAccent,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VaultHomeSummaryPasswordList(
    passwords: List<VaultHomeSummaryPasswordUiModel>,
    maxListHeight: Dp,
    onPasswordClick: (Long) -> Unit
) {
    val listItems = passwords.map { password -> password.toVaultPasswordListItemModel() }
    val fullListHeight = summaryPasswordListHeight(listItems.size)
    val naturalVisibleHeight = summaryPasswordListHeight(
        listItems.size.coerceAtMost(VaultHomeSummaryVisiblePasswordCount)
    )
    val visibleListHeight = minOf(
        fullListHeight,
        naturalVisibleHeight,
        maxListHeight
    )
    val needsScroll = fullListHeight > visibleListHeight

    if (needsScroll) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(visibleListHeight)
                .testTag("vault_home_summary_password_list"),
            verticalArrangement = Arrangement.spacedBy(VaultHomeSummaryPasswordItemSpacing)
        ) {
            items(
                items = listItems,
                key = { password -> password.id }
            ) { password ->
                VaultPasswordListItem(
                    password = password,
                    onClick = { onPasswordClick(password.id) },
                    showTrailingIndicator = true
                )
            }
        }
    } else {
        VaultPasswordListColumn(
            passwords = listItems,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vault_home_summary_password_list"),
            itemSpacing = VaultHomeSummaryPasswordItemSpacing,
            showTrailingIndicator = true,
            onItemClick = onPasswordClick
        )
    }
}

@Composable
private fun VaultHomeSecuritySummaryTags(
    weakPasswords: Int,
    moderatePasswords: Int,
    strongPasswords: Int,
    onFilterClick: (VaultHomeSecurityFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val tags = listOf(
        VaultHomeSecuritySummaryTagUiModel(
            count = weakPasswords,
            filter = VaultHomeSecurityFilter.Weak,
            labelResId = R.plurals.vault_home_weak_passwords,
            accentColor = colors.danger,
            testTag = "vault_home_weak_passwords"
        ),
        VaultHomeSecuritySummaryTagUiModel(
            count = moderatePasswords,
            filter = VaultHomeSecurityFilter.Moderate,
            labelResId = R.plurals.vault_home_moderate_passwords,
            accentColor = colors.warning,
            testTag = "vault_home_moderate_passwords"
        ),
        VaultHomeSecuritySummaryTagUiModel(
            count = strongPasswords,
            filter = VaultHomeSecurityFilter.Safe,
            labelResId = R.plurals.vault_home_strong_passwords,
            accentColor = colors.success,
            testTag = "vault_home_strong_passwords"
        )
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vault_home_security_summary_tags"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = tags,
            key = { tag -> tag.testTag }
        ) { tag ->
            VaultHomeSecuritySummaryTag(
                tag = tag,
                onClick = { onFilterClick(tag.filter) }
            )
        }
    }
}

@Composable
private fun VaultHomeSecuritySummaryTag(
    tag: VaultHomeSecuritySummaryTagUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                color = colors.onAccent.copy(alpha = 0.1f),
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .testTag(tag.testTag)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = tag.accentColor,
                    shape = CircleShape
                )
        )
        Text(
            text = pluralStringResource(
                tag.labelResId,
                tag.count,
                tag.count
            ),
            color = colors.onAccent,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class VaultHomeSecuritySummaryTagUiModel(
    val count: Int,
    val filter: VaultHomeSecurityFilter,
    @PluralsRes val labelResId: Int,
    val accentColor: Color,
    val testTag: String
)

private val VaultHomeSummaryListPadding = 24.dp
private val VaultHomeContentTopPadding = 88.dp
private val VaultHomeSummaryListChromeHeight = 100.dp
private val VaultHomeSummaryListFabClearance = 16.dp
private val VaultHomeSummaryLoadingHeight = 160.dp
private val VaultHomeSummaryPasswordItemHeight = 80.dp
private val VaultHomeSummaryPasswordItemSpacing = 12.dp
private const val VaultHomeSummaryVisiblePasswordCount = 5
private val VaultHomeSummaryVisiblePasswordListHeight = summaryPasswordListHeight(
    VaultHomeSummaryVisiblePasswordCount
)
private val VaultHomeSummaryExpandedFallbackMaxHeight =
    VaultHomeSummaryListChromeHeight + VaultHomeSummaryVisiblePasswordListHeight
private val VaultHomeSummaryExpandedMinimumHeight =
    VaultHomeSummaryListChromeHeight + VaultHomeSummaryLoadingHeight

@Composable
private fun VaultHomeCategoriesSection(
    categories: List<VaultHomeCategoryUiModel>,
    showOtherCategories: Boolean,
    onAction: (VaultHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            color = colors.textPrimary,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )

        val cards = buildList {
            categories.forEach { category -> add(VaultHomeCategoryCardItem.Category(category)) }
            if (showOtherCategories) {
                add(VaultHomeCategoryCardItem.Other)
            }
        }

        if (cards.isEmpty()) {
            VaultHomeEmptyCard(
                message = stringResource(R.string.vault_home_categories_empty),
                testTag = "vault_home_categories_empty"
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                cards.chunked(2).forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowCards.forEach { card ->
                            when (card) {
                                is VaultHomeCategoryCardItem.Category -> {
                                    VaultHomeCategoryCard(
                                        category = card.category,
                                        onClick = {
                                            onAction(VaultHomeAction.OnCategoryClick(card.category.id))
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                VaultHomeCategoryCardItem.Other -> {
                                    VaultHomeOtherCategoriesCard(
                                        onClick = { onAction(VaultHomeAction.OnOtherCategoriesClick) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        if (rowCards.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultHomeCategoryCard(
    category: VaultHomeCategoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("vault_home_category_${category.id}")
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = colors.surfaceHigh.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(category.iconResId),
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = category.name,
            color = colors.textPrimary,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
        )
    }
}

@Composable
private fun VaultHomeOtherCategoriesCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("vault_home_other_categories_card")
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = colors.surfaceBright.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = stringResource(R.string.vault_home_other_categories),
            color = colors.textPrimary,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VaultHomeRecentSection(
    passwords: List<VaultHomeRecentPasswordUiModel>,
    onAction: (VaultHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.vault_home_recent_title),
                color = colors.textPrimary,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.vault_home_view_all),
                color = colors.primary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onAction(VaultHomeAction.OnViewAllPasswordsClick) }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("vault_home_recent_view_all")
            )
        }

        if (passwords.isEmpty()) {
            VaultHomeEmptyCard(
                message = stringResource(R.string.vault_home_recent_empty),
                testTag = "vault_home_recent_empty"
            )
        } else {
            VaultPasswordListColumn(
                passwords = passwords.map { password -> password.toVaultPasswordListItemModel() },
                itemSpacing = 12.dp,
                showTrailingIndicator = true,
                onItemClick = { passwordId ->
                    onAction(VaultHomeAction.OnRecentPasswordClick(passwordId))
                }
            )
        }
    }
}

@Composable
private fun VaultHomeEmptyCard(
    message: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
            .testTag(testTag)
    ) {
        Text(
            text = message,
            color = colors.textSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

private sealed interface VaultHomeCategoryCardItem {
    data class Category(val category: VaultHomeCategoryUiModel) : VaultHomeCategoryCardItem
    data object Other : VaultHomeCategoryCardItem
}

private fun VaultHomeRecentPasswordUiModel.toVaultPasswordListItemModel(): VaultPasswordListItemModel =
    VaultPasswordListItemModel(
        id = id,
        title = title,
        supportingText = supportingText,
        initials = initials,
        securityLevel = securityLevel
    )

private fun VaultHomeSummaryPasswordUiModel.toVaultPasswordListItemModel(): VaultPasswordListItemModel =
    VaultPasswordListItemModel(
        id = id,
        title = title,
        supportingText = supportingText,
        initials = initials,
        securityLevel = securityLevel,
        scorePercent = scorePercent
    )

private val VaultHomeSummaryCardState.filter: VaultHomeSecurityFilter
    get() = when (this) {
        is VaultHomeSummaryCardState.Loading -> filter
        is VaultHomeSummaryCardState.Content -> filter
        is VaultHomeSummaryCardState.Empty -> filter
        is VaultHomeSummaryCardState.Error -> filter
        VaultHomeSummaryCardState.Overview -> VaultHomeSecurityFilter.Weak
    }

private fun summaryPasswordListHeight(itemCount: Int): Dp {
    if (itemCount <= 0) {
        return 0.dp
    }

    val spacingCount = itemCount - 1
    return (
        VaultHomeSummaryPasswordItemHeight.value * itemCount +
            VaultHomeSummaryPasswordItemSpacing.value * spacingCount
        ).dp
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun VaultHomeScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        VaultHomeScreen(
            uiState = VaultHomeUiState(
                totalPasswords = 28,
                weakPasswords = 3,
                moderatePasswords = 8,
                strongPasswords = 17,
                categories = listOf(
                    VaultHomeCategoryUiModel(1L, "Redes Sociais", "ic_global", R.drawable.ic_global, 8),
                    VaultHomeCategoryUiModel(2L, "Compras", "ic_work_bag", R.drawable.ic_work_bag, 5),
                    VaultHomeCategoryUiModel(3L, "Bancos", "ic_padlock", R.drawable.ic_padlock, 4)
                ),
                showOtherCategories = true,
                recentPasswords = listOf(
                    VaultHomeRecentPasswordUiModel(
                        id = 1L,
                        title = "Facebook",
                        supportingText = "usuario@email.com",
                        initials = "F",
                        securityLevel = VaultPasswordListSecurityLevel.Weak
                    )
                ),
                contentState = VaultHomeContentState.Content
            ),
            onAction = {}
        )
    }
}
