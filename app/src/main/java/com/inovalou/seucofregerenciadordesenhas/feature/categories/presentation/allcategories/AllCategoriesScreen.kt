package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.allcategories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultSearchField
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryCreateFab
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryGridCard
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory.EditCategoryOpenedFrom
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite

@Composable
fun AllCategoriesEntry(
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Long, EditCategoryOpenedFrom) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AllCategoriesViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AllCategoriesEffect.NavigateToEditCategory -> {
                    onEditCategoryClick(effect.categoryId, effect.openedFrom)
                }
            }
        }
    }

    AllCategoriesScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        onAddCategoryClick = onAddCategoryClick,
        modifier = modifier
    )
}

@Composable
fun AllCategoriesScreen(
    uiState: AllCategoriesUiState,
    onAction: (AllCategoriesAction) -> Unit,
    onBackClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
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
                .testTag("all_categories_screen")
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = 20.dp,
                    end = 24.dp,
                    bottom = 120.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    AllCategoriesTopBar(onBackClick = onBackClick)
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    AllCategoriesSearchField(
                        query = uiState.query,
                        onQueryChanged = { onAction(AllCategoriesAction.OnSearchQueryChanged(it)) }
                    )
                }

                when (val contentState = uiState.contentState) {
                    AllCategoriesContentState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.testTag("all_categories_loading"),
                                    color = ElectricBlue
                                )
                            }
                        }
                    }

                    AllCategoriesContentState.Content -> {
                        items(
                            items = uiState.filteredCategories,
                            key = { category -> category.id }
                        ) { category ->
                            CategoryGridCard(
                                category = category,
                                onClick = {
                                    onAction(AllCategoriesAction.OnCategoryClick(category.id))
                                }
                            )
                        }
                    }

                    AllCategoriesContentState.EmptyCategories -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AllCategoriesEmptyState(
                                title = stringResource(R.string.all_categories_empty_title),
                                message = stringResource(R.string.all_categories_empty_message),
                                modifier = Modifier.testTag("all_categories_empty")
                            )
                        }
                    }

                    AllCategoriesContentState.EmptySearchResult -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AllCategoriesEmptyState(
                                title = stringResource(R.string.all_categories_search_empty_title),
                                message = stringResource(R.string.all_categories_search_empty_message),
                                modifier = Modifier.testTag("all_categories_search_empty")
                            )
                        }
                    }

                    is AllCategoriesContentState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(contentState.messageResId),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("all_categories_error"),
                                color = MistText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            CategoryCreateFab(
                contentDescription = stringResource(R.string.categories_create_fab),
                onClick = onAddCategoryClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .testTag("all_categories_create_fab")
            )
        }
    }
}

@Composable
private fun AllCategoriesTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VaultBackHeader(
        title = stringResource(R.string.all_categories_title),
        navigationContentDescription = stringResource(R.string.all_categories_back),
        onBackClick = onBackClick,
        modifier = modifier,
        testTag = "all_categories_header"
    )
}

@Composable
private fun AllCategoriesSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    VaultSearchField(
        value = query,
        onValueChange = onQueryChanged,
        placeholderResId = R.string.all_categories_search_placeholder,
        testTag = "all_categories_search_input",
        modifier = modifier
    )
}

@Composable
private fun AllCategoriesEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = SoftWhite,
            fontSize = 18.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            color = MistText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
