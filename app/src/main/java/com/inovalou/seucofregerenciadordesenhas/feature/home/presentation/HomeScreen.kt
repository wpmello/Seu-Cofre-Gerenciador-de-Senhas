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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultGradientFab
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListColumn
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListItemModel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultTopBar
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultAmber
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultGreen

@Composable
fun VaultHomeRoute(
    onOpenCategory: (Long) -> Unit,
    onOpenAllCategories: () -> Unit,
    onOpenPasswords: () -> Unit,
    onOpenPassword: (Long) -> Unit,
    onAddPassword: () -> Unit,
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
        modifier = modifier
    )
}

@Composable
fun VaultHomeScreen(
    uiState: VaultHomeUiState,
    onAction: (VaultHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MidnightBlue
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .testTag("vault_home_screen")
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = 16.dp,
                    end = 24.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    VaultTopBar(
                        searchContentDescriptionResId = R.string.categories_search,
                        onSearchClick = { onAction(VaultHomeAction.OnSearchClick) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

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
                                strongPasswords = uiState.strongPasswords
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

            VaultGradientFab(
                contentDescription = stringResource(R.string.passwords_create_fab),
                onClick = { onAction(VaultHomeAction.OnAddPasswordClick) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .testTag("vault_home_create_password_fab")
            )
        }
    }
}

@Composable
private fun VaultHomeLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.testTag("vault_home_loading"),
            color = ElectricBlue
        )
    }
}

@Composable
private fun VaultHomeError(contentState: VaultHomeContentState.Error) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp)
            .testTag("vault_home_error"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(contentState.messageResId),
            color = SoftWhite,
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(ElectricBlue, NeonPink)
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
                tint = MidnightBlue,
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
                    color = MidnightBlue.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = totalPasswords.toString(),
                    color = MidnightBlue,
                    fontSize = 60.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("vault_home_total_passwords")
                )
            }

            VaultHomeSecuritySummaryTags(
                weakPasswords = weakPasswords,
                moderatePasswords = moderatePasswords,
                strongPasswords = strongPasswords
            )
        }
    }
}

@Composable
private fun VaultHomeSecuritySummaryTags(
    weakPasswords: Int,
    moderatePasswords: Int,
    strongPasswords: Int,
    modifier: Modifier = Modifier
) {
    val tags = listOf(
        VaultHomeSecuritySummaryTagUiModel(
            count = weakPasswords,
            labelResId = R.plurals.vault_home_weak_passwords,
            accentColor = Color(0xFFFF716C),
            testTag = "vault_home_weak_passwords"
        ),
        VaultHomeSecuritySummaryTagUiModel(
            count = moderatePasswords,
            labelResId = R.plurals.vault_home_moderate_passwords,
            accentColor = VaultAmber,
            testTag = "vault_home_moderate_passwords"
        ),
        VaultHomeSecuritySummaryTagUiModel(
            count = strongPasswords,
            labelResId = R.plurals.vault_home_strong_passwords,
            accentColor = VaultGreen,
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
            VaultHomeSecuritySummaryTag(tag = tag)
        }
    }
}

@Composable
private fun VaultHomeSecuritySummaryTag(
    tag: VaultHomeSecuritySummaryTagUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MidnightBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(999.dp)
            )
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
            color = MidnightBlue,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.testTag(tag.testTag)
        )
    }
}

private data class VaultHomeSecuritySummaryTagUiModel(
    val count: Int,
    @PluralsRes val labelResId: Int,
    val accentColor: Color,
    val testTag: String
)

@Composable
private fun VaultHomeCategoriesSection(
    categories: List<VaultHomeCategoryUiModel>,
    showOtherCategories: Boolean,
    onAction: (VaultHomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            color = SoftWhite,
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
    Column(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                color = DeepNavy,
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
                    color = SlateBlue.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(category.iconResId),
                contentDescription = null,
                tint = SoftWhite,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = category.name,
            color = SoftWhite,
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
    Column(
        modifier = modifier
            .height(144.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                color = DeepNavy,
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
                    color = SurfaceBright.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = null,
                tint = MistText,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = stringResource(R.string.vault_home_other_categories),
            color = SoftWhite,
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
                color = SoftWhite,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.vault_home_view_all),
                color = ElectricBlue,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
            .testTag(testTag)
    ) {
        Text(
            text = message,
            color = MistText,
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
