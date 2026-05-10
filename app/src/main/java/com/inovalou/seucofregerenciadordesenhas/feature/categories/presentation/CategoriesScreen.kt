package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultEncryptedIndicator
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultFloatingTopBar
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryCreateFab
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryGridCard
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

private val CategoriesContentTopPadding = 72.dp

@Composable
fun CategoriesRoute(
    onCategoryClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onSecuritySummaryClick: () -> Unit,
    onOpenGlobalSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    CategoriesScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onCategoryClick = onCategoryClick,
        onViewAllClick = onViewAllClick,
        onAddCategoryClick = onAddCategoryClick,
        onSecuritySummaryClick = onSecuritySummaryClick,
        onOpenGlobalSearch = onOpenGlobalSearch,
        modifier = modifier
    )
}

@Composable
fun CategoriesScreen(
    uiState: CategoriesUiState,
    onAction: (CategoriesAction) -> Unit,
    onCategoryClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onSecuritySummaryClick: () -> Unit,
    onOpenGlobalSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .testTag("categories_screen")
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("categories_grid"),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = CategoriesContentTopPadding,
                    end = 24.dp,
                    bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SecuritySummaryCard(
                        summary = uiState.securitySummary,
                        encryptedIndicator = uiState.encryptedIndicator,
                        onClick = onSecuritySummaryClick
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    CategoriesSectionHeader(
                        onViewAllClick = onViewAllClick
                    )
                }

                uiState.currentCategory?.let { currentCategory ->
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HighlightedCategoryCard(
                            currentCategory = currentCategory,
                            onClick = { onCategoryClick(currentCategory.id) }
                        )
                    }
                }

                when (val categoriesState = uiState.categoriesState) {
                    CategoriesContentUiState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = colors.primary,
                                    modifier = Modifier.testTag("categories_loading")
                                )
                            }
                        }
                    }

                    CategoriesContentUiState.Empty -> Unit

                    is CategoriesContentUiState.Content -> {
                        items(
                            items = categoriesState.categories,
                            key = { category -> category.id }
                        ) { category ->
                            CategoryGridCard(
                                category = category,
                                onClick = { onCategoryClick(category.id) }
                            )
                        }

                        if (uiState.shouldShowBottomViewAllButton) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CategoriesBottomViewAllButton(
                                    onClick = onViewAllClick
                                )
                            }
                        }
                    }

                    is CategoriesContentUiState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(categoriesState.messageResId),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("categories_error"),
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            VaultFloatingTopBar(
                searchContentDescriptionResId = R.string.categories_search,
                onSearchClick = onOpenGlobalSearch,
                testTag = "categories_top_bar",
                modifier = Modifier.align(Alignment.TopCenter)
            )

            CategoryCreateFab(
                contentDescription = stringResource(R.string.categories_create_fab),
                onClick = onAddCategoryClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .testTag("categories_create_fab")
            )
        }
    }
}

@Composable
private fun SecuritySummaryCard(
    summary: SecuritySummaryUiModel,
    encryptedIndicator: EncryptedIndicatorUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val cardBrush = when (summary.visualState) {
        SecuritySummaryVisualState.Poor -> Brush.linearGradient(
            colors = listOf(
                colors.securityPoorGradientStart,
                colors.securityPoorGradientEnd
            )
        )
        SecuritySummaryVisualState.Moderate -> Brush.linearGradient(
            colors = listOf(
                colors.securityModerateGradientStart,
                colors.securityModerateGradientEnd
            )
        )
        SecuritySummaryVisualState.Excellent -> Brush.linearGradient(
            colors = listOf(
                colors.securityExcellentGradientStart,
                colors.securityExcellentGradientEnd
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .background(
                    brush = cardBrush,
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("categories_security_summary_card")
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(120.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(summary.titleResId),
                        color = colors.onAccent.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.35.sp
                    )

                    Text(
                        text = stringResource(summary.statusResId),
                        color = colors.onAccent,
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.testTag("categories_security_summary_status")
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = summary.totalItems.toString(),
                        color = colors.onAccent,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.categories_total_items_label),
                        color = colors.onAccent,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        VaultEncryptedIndicator(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp),
            labelResId = encryptedIndicator.labelResId
        )
    }
}

@Composable
private fun CategoriesSectionHeader(
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.categories_title),
            color = colors.textPrimary,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )

        CategoriesViewAllButton(
            onClick = onViewAllClick
        )
    }
}

@Composable
private fun CategoriesBottomViewAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        CategoriesViewAllButton(
            onClick = onClick,
            modifier = Modifier
                .testTag("categories_bottom_view_all")
                .padding(horizontal = 18.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun CategoriesViewAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.categories_view_all),
            color = colors.primaryDim,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = colors.primaryDim,
            modifier = Modifier.size(10.dp)
        )
    }
}

@Composable
private fun HighlightedCategoryCard(
    currentCategory: HighlightedCategoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.linearGradient(listOf(colors.primary, colors.primaryDim)),
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("highlighted_category_card")
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_current_category),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(width = 49.dp, height = 48.dp)
            )

            Text(
                text = stringResource(currentCategory.badgeResId),
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = currentCategory.name,
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = pluralStringResource(
                R.plurals.categories_password_count,
                currentCategory.itemCount,
                currentCategory.itemCount
            ),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CategoriesScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        CategoriesScreen(
            uiState = CategoriesUiState(
                currentCategory = HighlightedCategoryUiModel(
                    id = 4L,
                    name = "Viagens",
                    itemCount = 21
                ),
                categoriesState = CategoriesContentUiState.Content(
                    categories = listOf(
                        CategoryCardUiModel(id = 1, name = "Entretenimento", iconKey = "ic_favorite", iconResId = R.drawable.ic_favorite, itemCount = 28),
                        CategoryCardUiModel(id = 2, name = "Educação", iconKey = "ic_home_2", iconResId = R.drawable.ic_home_2, itemCount = 15),
                        CategoryCardUiModel(id = 3, name = "Saúde", iconKey = "ic_padlock", iconResId = R.drawable.ic_padlock, itemCount = 12),
                        CategoryCardUiModel(id = 4, name = "Viagens", iconKey = "ic_global", iconResId = R.drawable.ic_global, itemCount = 21)
                    )
                )
            ),
            onAction = {},
            onCategoryClick = {},
            onViewAllClick = {},
            onAddCategoryClick = {},
            onSecuritySummaryClick = {}
        )
    }
}
