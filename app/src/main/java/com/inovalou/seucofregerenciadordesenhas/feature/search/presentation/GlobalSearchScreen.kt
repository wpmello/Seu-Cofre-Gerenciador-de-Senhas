package com.inovalou.seucofregerenciadordesenhas.feature.search.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultSearchField
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun GlobalSearchRoute(
    onBackClick: () -> Unit,
    onOpenCategory: (Long) -> Unit,
    onOpenPassword: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GlobalSearchViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GlobalSearchEffect.OpenCategoryDetails -> onOpenCategory(effect.categoryId)
                is GlobalSearchEffect.OpenPasswordDetails -> onOpenPassword(effect.passwordId)
            }
        }
    }

    GlobalSearchScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun GlobalSearchScreen(
    uiState: GlobalSearchUiState,
    onAction: (GlobalSearchAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .testTag("global_search_screen"),
            contentPadding = PaddingValues(
                start = 24.dp,
                top = 20.dp,
                end = 24.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                VaultBackHeader(
                    title = stringResource(R.string.global_search_title),
                    navigationContentDescription = stringResource(R.string.global_search_back),
                    onBackClick = onBackClick,
                    testTag = "global_search_header"
                )
            }

            item {
                VaultSearchField(
                    value = uiState.query,
                    onValueChange = { query ->
                        onAction(GlobalSearchAction.OnQueryChanged(query))
                    },
                    placeholderResId = R.string.global_search_placeholder,
                    testTag = "global_search_input"
                )
            }

            when (val contentState = uiState.contentState) {
                GlobalSearchContentState.AwaitingQuery -> {
                    item {
                        GlobalSearchMessage(
                            title = stringResource(R.string.global_search_initial_title),
                            message = stringResource(R.string.global_search_initial_message),
                            modifier = Modifier.testTag("global_search_awaiting_query")
                        )
                    }
                }

                GlobalSearchContentState.Loading -> {
                    item {
                        GlobalSearchLoading()
                    }
                }

                GlobalSearchContentState.Content -> {
                    items(
                        items = uiState.categories,
                        key = { category -> "category-${category.id}" }
                    ) { category ->
                        GlobalSearchCategoryItem(
                            category = category,
                            onClick = {
                                onAction(GlobalSearchAction.OnCategoryClick(category.id))
                            }
                        )
                    }

                    items(
                        items = uiState.passwords,
                        key = { password -> "password-${password.id}" }
                    ) { password ->
                        GlobalSearchPasswordItem(
                            password = password,
                            onClick = {
                                onAction(GlobalSearchAction.OnPasswordClick(password.id))
                            }
                        )
                    }
                }

                GlobalSearchContentState.Empty -> {
                    item {
                        GlobalSearchMessage(
                            title = stringResource(R.string.global_search_empty_title),
                            message = stringResource(R.string.global_search_empty_message),
                            modifier = Modifier.testTag("global_search_empty")
                        )
                    }
                }

                is GlobalSearchContentState.Error -> {
                    item {
                        GlobalSearchMessage(
                            title = stringResource(R.string.global_search_error_title),
                            message = stringResource(contentState.messageResId),
                            modifier = Modifier.testTag("global_search_error")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlobalSearchLoading(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.vaultColors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colors.primary,
            modifier = Modifier.testTag("global_search_loading")
        )
    }
}

@Composable
private fun GlobalSearchMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp)
            .background(
                color = colors.surfaceHigh.copy(alpha = 0.28f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            color = colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun GlobalSearchCategoryItem(
    category: GlobalSearchCategoryUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlobalSearchResultItem(
        title = category.name,
        testTag = "global_search_category_item_${category.id}",
        leadingContent = {
            Icon(
                imageVector = ImageVector.vectorResource(category.iconResId),
                contentDescription = null,
                tint = MaterialTheme.vaultColors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun GlobalSearchPasswordItem(
    password: GlobalSearchPasswordUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlobalSearchResultItem(
        title = password.title,
        testTag = "global_search_password_item_${password.id}",
        leadingContent = {
            Text(
                text = password.initials,
                color = MaterialTheme.vaultColors.textPrimary,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
        },
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun GlobalSearchResultItem(
    title: String,
    testTag: String,
    leadingContent: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surfaceHigh.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = colors.surface,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                leadingContent()
            }

            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = colors.outline,
            modifier = Modifier
                .padding(start = 12.dp)
                .size(16.dp)
        )
    }
}
