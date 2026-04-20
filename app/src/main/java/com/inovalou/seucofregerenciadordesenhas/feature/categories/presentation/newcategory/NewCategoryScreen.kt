package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryIconSelectionGrid
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryPrimaryActionButton
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySectionLabel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryValidationText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

@Composable
fun NewCategoryRoute(
    onNavigateBackToOrigin: (NewCategoryOpenedFrom) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewCategoryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is NewCategoryEffect.NavigateBackToOrigin -> {
                    onNavigateBackToOrigin(effect.openedFrom)
                }
            }
        }
    }

    NewCategoryScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun NewCategoryScreen(
    uiState: NewCategoryUiState,
    onAction: (NewCategoryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MidnightBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            VaultBackHeader(
                title = stringResource(R.string.new_category_title),
                navigationContentDescription = stringResource(R.string.new_category_back),
                onBackClick = { onAction(NewCategoryAction.OnBackClick) },
                testTag = "new_category_header"
            )

            Text(
                text = stringResource(R.string.new_category_description),
                color = MistText,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CategorySectionLabel(label = stringResource(R.string.new_category_name_label))
                TextField(
                    value = uiState.name,
                    onValueChange = { onAction(NewCategoryAction.OnNameChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_category_name_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.new_category_name_hint),
                            color = GhostOutline
                        )
                    },
                    isError = uiState.nameErrorResId != null,
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceBright,
                        unfocusedContainerColor = SurfaceBright,
                        disabledContainerColor = SurfaceBright,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = SoftWhite,
                        unfocusedTextColor = SoftWhite,
                        focusedPlaceholderColor = GhostOutline,
                        unfocusedPlaceholderColor = GhostOutline
                    )
                )
                uiState.nameErrorResId?.let { errorResId ->
                    CategoryValidationText(errorResId = errorResId)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategorySectionLabel(label = stringResource(R.string.new_category_icon_label))
                    Text(
                        text = stringResource(
                            R.string.new_category_icons_available,
                            uiState.availableIcons.size
                        ),
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                CategoryIconSelectionGrid(
                    icons = uiState.availableIcons,
                    onIconClick = { onAction(NewCategoryAction.OnIconSelected(it)) }
                )

                uiState.iconErrorResId?.let { errorResId ->
                    CategoryValidationText(errorResId = errorResId)
                }
            }

            uiState.submitErrorResId?.let { errorResId ->
                CategoryValidationText(errorResId = errorResId)
            }

            CategoryPrimaryActionButton(
                text = stringResource(R.string.new_category_create_button),
                isLoading = uiState.isSaving,
                onClick = { onAction(NewCategoryAction.OnCreateCategoryClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .testTag("new_category_create_button")
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NewCategoryScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        NewCategoryScreen(
            uiState = NewCategoryUiState(
                name = "",
                availableIcons = listOf(
                    CategorySelectableIconUiModel("ic_directory", R.drawable.ic_directory, true),
                    CategorySelectableIconUiModel("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category, false),
                    CategorySelectableIconUiModel("ic_global", R.drawable.ic_global, false),
                    CategorySelectableIconUiModel("ic_favorite", R.drawable.ic_favorite, false)
                ),
                selectedIconKey = "ic_directory"
            ),
            onAction = {}
        )
    }
}
