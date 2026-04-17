package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryIconSelectionGrid
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryPrimaryActionButton
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySectionLabel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryValidationText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

@Composable
fun EditCategoryEntry(
    onNavigateBackToOrigin: (EditCategoryOpenedFrom) -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditCategoryViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditCategoryEffect.NavigateToCategories -> onNavigateToCategories()
                is EditCategoryEffect.NavigateBackToOrigin -> onNavigateBackToOrigin(effect.openedFrom)
            }
        }
    }

    EditCategoryScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun EditCategoryScreen(
    uiState: EditCategoryUiState,
    onAction: (EditCategoryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIconResId = uiState.availableIcons.firstOrNull { it.isSelected }?.iconResId
        ?: uiState.availableIcons.firstOrNull()?.iconResId

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MidnightBlue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("edit_category_screen"),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                        .clickable { onAction(EditCategoryAction.OnBackClick) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.edit_category_back),
                        tint = ElectricBlue
                    )
                }

                Text(
                    text = stringResource(R.string.edit_category_title),
                    color = SoftWhite,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val contentState = uiState.contentState) {
                EditCategoryContentState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("edit_category_loading"),
                            color = ElectricBlue
                        )
                    }
                }
                is EditCategoryContentState.Error -> {
                    Text(
                        text = stringResource(contentState.messageResId),
                        color = MistText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                EditCategoryContentState.Content -> {
                    CategoryEditCard(
                        iconResId = selectedIconResId,
                        name = uiState.name,
                        nameErrorResId = uiState.nameErrorResId,
                        onNameChanged = { onAction(EditCategoryAction.OnNameChanged(it)) },
                        onEditIconClick = { onAction(EditCategoryAction.OnEditIconClick) }
                    )

                    CategoryPasswordsSection(state = uiState.passwordsSectionState)

                    uiState.submitErrorResId?.let { errorResId ->
                        CategoryValidationText(errorResId = errorResId)
                    }
                    uiState.deleteErrorResId?.let { errorResId ->
                        CategoryValidationText(errorResId = errorResId)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CategoryPrimaryActionButton(
                            text = stringResource(R.string.edit_category_save_button),
                            isLoading = uiState.isSaving,
                            onClick = { onAction(EditCategoryAction.OnSaveClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_category_save_button")
                        )

                        OutlinedButton(
                            onClick = { onAction(EditCategoryAction.OnDeleteClick) },
                            enabled = !uiState.isDeleting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("edit_category_delete_button"),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.24f)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.edit_category_delete_button),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isIconPickerVisible) {
        AlertDialog(
            onDismissRequest = { onAction(EditCategoryAction.OnIconPickerDismissed) },
            title = {
                Text(
                    text = stringResource(R.string.edit_category_icon_dialog_title),
                    color = SoftWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.edit_category_icon_dialog_message),
                        color = MistText
                    )
                    CategoryIconSelectionGrid(
                        icons = uiState.availableIcons,
                        onIconClick = { onAction(EditCategoryAction.OnIconSelected(it)) },
                        modifier = Modifier.testTag("edit_category_icon_dialog")
                    )
                    uiState.iconErrorResId?.let { errorResId ->
                        CategoryValidationText(errorResId = errorResId)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { onAction(EditCategoryAction.OnIconPickerDismissed) }) {
                    Text(text = stringResource(R.string.edit_category_cancel))
                }
            },
            containerColor = DeepNavy
        )
    }

    if (uiState.isDeleteConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { onAction(EditCategoryAction.OnDeleteDismissed) },
            title = {
                Text(
                    text = stringResource(R.string.edit_category_delete_dialog_title),
                    color = SoftWhite
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.edit_category_delete_dialog_message),
                    color = MistText
                )
            },
            confirmButton = {
                TextButton(onClick = { onAction(EditCategoryAction.OnDeleteConfirmed) }) {
                    Text(
                        text = stringResource(R.string.edit_category_confirm_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(EditCategoryAction.OnDeleteDismissed) }) {
                    Text(text = stringResource(R.string.edit_category_cancel))
                }
            },
            containerColor = DeepNavy
        )
    }
}

@Composable
private fun CategoryEditCard(
    iconResId: Int?,
    name: String,
    nameErrorResId: Int?,
    onNameChanged: (String) -> Unit,
    onEditIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = DeepNavy,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    color = SurfaceBright.copy(alpha = 0.48f),
                    shape = CircleShape
                )
                .clickable(onClick = onEditIconClick)
                .testTag("edit_category_icon_button"),
            contentAlignment = Alignment.Center
        ) {
            if (iconResId != null) {
                Icon(
                    imageVector = ImageVector.vectorResource(iconResId),
                    contentDescription = null,
                    tint = ElectricBlue,
                    modifier = Modifier.size(34.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(color = MidnightBlue, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = SoftWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategorySectionLabel(label = stringResource(R.string.edit_category_name_label))
            TextField(
                value = name,
                onValueChange = onNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_category_name_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = nameErrorResId != null,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SurfaceBright,
                    unfocusedContainerColor = SurfaceBright,
                    disabledContainerColor = SurfaceBright,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite
                )
            )
            nameErrorResId?.let { errorResId ->
                CategoryValidationText(errorResId = errorResId)
            }
        }
    }
}

@Composable
private fun CategoryPasswordsSection(
    state: CategoryPasswordsSectionUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_category_passwords_title),
            color = SoftWhite,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )

        when (state) {
            CategoryPasswordsSectionUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DeepNavy,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_category_passwords_empty_title),
                        color = SoftWhite,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.edit_category_passwords_empty_message),
                        color = MistText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
