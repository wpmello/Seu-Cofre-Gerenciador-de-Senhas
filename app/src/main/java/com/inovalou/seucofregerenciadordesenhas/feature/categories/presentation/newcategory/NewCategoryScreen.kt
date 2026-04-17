package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

private val CreateCategoryButtonGradient = Brush.horizontalGradient(
    colors = listOf(ElectricBlue, NeonPink)
)

@Composable
fun NewCategoryRoute(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewCategoryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                NewCategoryEffect.NavigateBack -> onBackClick()
            }
        }
    }

    NewCategoryScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun NewCategoryScreen(
    uiState: NewCategoryUiState,
    onAction: (NewCategoryAction) -> Unit,
    onBackClick: () -> Unit,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.new_category_back),
                        tint = ElectricBlue
                    )
                }

                Text(
                    text = stringResource(R.string.new_category_title),
                    color = SoftWhite,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = stringResource(R.string.new_category_description),
                color = MistText,
                fontSize = 16.sp,
                lineHeight = 26.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel(label = stringResource(R.string.new_category_name_label))
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
                    ValidationText(errorResId = errorResId)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionLabel(label = stringResource(R.string.new_category_icon_label))
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

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    uiState.availableIcons.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { icon ->
                                CategoryIconCell(
                                    icon = icon,
                                    onClick = { onAction(NewCategoryAction.OnIconSelected(icon.iconKey)) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - row.size) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                uiState.iconErrorResId?.let { errorResId ->
                    ValidationText(errorResId = errorResId)
                }
            }

            uiState.submitErrorResId?.let { errorResId ->
                ValidationText(errorResId = errorResId)
            }

            GradientActionButton(
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

@Composable
private fun SectionLabel(
    label: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = label,
        modifier = modifier,
        color = GhostOutline,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
}

@Composable
private fun CategoryIconCell(
    icon: NewCategoryIconUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (icon.isSelected) {
        SlateSelection
    } else {
        DeepNavy
    }
    val borderColor = if (icon.isSelected) {
        ElectricBlue.copy(alpha = 0.7f)
    } else {
        Color.Transparent
    }
    val iconTint = if (icon.isSelected) {
        ElectricBlue
    } else {
        GhostOutline
    }

    Box(
        modifier = modifier
            .size(64.dp)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .testTag("new_category_icon_${icon.iconKey}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon.iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

private val SlateSelection = SurfaceBright.copy(alpha = 0.62f)

@Composable
private fun ValidationText(
    @StringRes errorResId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(errorResId),
        modifier = modifier,
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

@Composable
private fun GradientActionButton(
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CreateCategoryButtonGradient),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MidnightBlue,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = text,
                    color = MidnightBlue,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
                    NewCategoryIconUiModel("ic_directory", R.drawable.ic_directory, true),
                    NewCategoryIconUiModel("ic_work_bag_add_category", R.drawable.ic_work_bag_add_category, false),
                    NewCategoryIconUiModel("ic_global", R.drawable.ic_global, false),
                    NewCategoryIconUiModel("ic_favorite", R.drawable.ic_favorite, false)
                ),
                selectedIconKey = "ic_directory"
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}
