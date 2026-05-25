package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.editcategory

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListItemModel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.rememberEndCursorTextFieldState
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.vaultPasswordListItems
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryIconSelectionGrid
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySectionLabel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategoryValidationText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun EditCategoryEntry(
    onNavigateBackToOrigin: (EditCategoryOpenedFrom) -> Unit,
    onNavigateToCategories: () -> Unit,
    onOpenPassword: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditCategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditCategoryEffect.NavigateToCategories -> onNavigateToCategories()
                is EditCategoryEffect.NavigateBackToOrigin -> onNavigateBackToOrigin(effect.openedFrom)
                is EditCategoryEffect.OpenPassword -> onOpenPassword(effect.passwordId)
            }
        }
    }

    EditCategoryScreen(
        uiState = uiState,
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
    val colors = MaterialTheme.vaultColors
    val selectedIconResId = uiState.availableIcons.firstOrNull { it.isSelected }?.iconResId
        ?: uiState.availableIcons.firstOrNull()?.iconResId
    val isCriticalOperation = uiState.deleteFlowState is EditCategoryDeleteFlowState.CriticalOperation

    BackHandler(enabled = isCriticalOperation) {}

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .testTag("edit_category_screen"),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VaultBackHeader(
                title = stringResource(R.string.edit_category_title),
                navigationContentDescription = stringResource(R.string.edit_category_back),
                onBackClick = { onAction(EditCategoryAction.OnBackClick) },
                testTag = "edit_category_header"
            )

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
                            color = colors.primary
                        )
                    }
                }
                is EditCategoryContentState.Error -> {
                    Text(
                        text = stringResource(contentState.messageResId),
                        color = colors.textSecondary,
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

                    CategoryPasswordsSection(
                        state = uiState.passwordsSectionState,
                        onAction = onAction
                    )

                    uiState.submitErrorResId?.let { errorResId ->
                        CategoryValidationText(errorResId = errorResId)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        VaultPrimaryPersistenceButton(
                            text = stringResource(R.string.edit_category_save_button),
                            isLoading = uiState.isSaving,
                            onClick = { onAction(EditCategoryAction.OnSaveClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_category_save_button")
                        )

                        OutlinedButton(
                            onClick = { onAction(EditCategoryAction.OnDeleteButtonClick) },
                            enabled = !isCriticalOperation,
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
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.edit_category_icon_dialog_message),
                        color = colors.textSecondary
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
            containerColor = colors.surface
        )
    }

    EditCategoryDeleteFlow(
        state = uiState.deleteFlowState,
        onAction = onAction
    )
}

@Composable
private fun EditCategoryDeleteFlow(
    state: EditCategoryDeleteFlowState,
    onAction: (EditCategoryAction) -> Unit
) {
    when (state) {
        EditCategoryDeleteFlowState.Idle -> Unit
        EditCategoryDeleteFlowState.SimpleDeleteConfirmation -> {
            SimpleDeleteConfirmationDialog(onAction = onAction)
        }
        is EditCategoryDeleteFlowState.AssociatedPasswordsChoice -> {
            AssociatedPasswordsChoiceDialog(
                passwordCount = state.passwordCount,
                onAction = onAction
            )
        }
        is EditCategoryDeleteFlowState.DeleteAllConfirmation -> {
            DeleteAllAssociatedPasswordsConfirmationDialog(
                passwordCount = state.passwordCount,
                onAction = onAction
            )
        }
        is EditCategoryDeleteFlowState.TransferSelection -> {
            TransferPasswordsDialog(
                passwordCount = state.passwordCount,
                categories = state.categories,
                selectedCategoryId = state.selectedCategoryId,
                onAction = onAction
            )
        }
        EditCategoryDeleteFlowState.PostTransferDeleteConfirmation -> {
            PostTransferDeleteConfirmationDialog(onAction = onAction)
        }
        is EditCategoryDeleteFlowState.CriticalOperation -> {
            CriticalCategoryOperationOverlay(state = state)
        }
        is EditCategoryDeleteFlowState.Error -> {
            CategoryOperationErrorDialog(
                messageResId = state.messageResId,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun SimpleDeleteConfirmationDialog(
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
        modifier = Modifier.testTag("edit_category_simple_delete_confirmation_dialog"),
        title = {
            Text(
                text = stringResource(R.string.edit_category_delete_dialog_title),
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.edit_category_delete_dialog_message),
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnSimpleDeleteConfirmed) },
                modifier = Modifier.testTag("edit_category_confirm_simple_delete_button")
            ) {
                Text(
                    text = stringResource(R.string.edit_category_confirm_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
                modifier = Modifier.testTag("edit_category_cancel_simple_delete_button")
            ) {
                Text(text = stringResource(R.string.edit_category_cancel))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun AssociatedPasswordsChoiceDialog(
    passwordCount: Int,
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    Dialog(onDismissRequest = { onAction(EditCategoryAction.OnDeleteDialogDismissed) }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_category_associated_passwords_choice_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_category_associated_passwords_choice_title),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
                        modifier = Modifier.testTag("edit_category_associated_passwords_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.edit_category_close_dialog),
                            tint = colors.textSecondary
                        )
                    }
                }

                AssociatedPasswordsCountText(passwordCount = passwordCount)

                Text(
                    text = stringResource(R.string.edit_category_associated_passwords_choice_message),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAction(EditCategoryAction.OnDeleteAllSelected) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_category_delete_all_button"),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.34f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.edit_category_delete_all_button),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = { onAction(EditCategoryAction.OnTransferSelected) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("edit_category_transfer_passwords_button")
                    ) {
                        Text(text = stringResource(R.string.edit_category_transfer_passwords_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteAllAssociatedPasswordsConfirmationDialog(
    passwordCount: Int,
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = { onAction(EditCategoryAction.OnDeleteAllCancelled) },
        modifier = Modifier.testTag("edit_category_delete_all_confirmation_dialog"),
        title = {
            Text(
                text = stringResource(R.string.edit_category_delete_all_confirmation_title),
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AssociatedPasswordsCountText(passwordCount = passwordCount)
                Text(
                    text = stringResource(R.string.edit_category_delete_all_confirmation_message),
                    color = colors.textSecondary
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnDeleteAllConfirmed) },
                modifier = Modifier.testTag("edit_category_confirm_delete_all_button")
            ) {
                Text(
                    text = stringResource(R.string.edit_category_confirm_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnDeleteAllCancelled) },
                modifier = Modifier.testTag("edit_category_cancel_delete_all_button")
            ) {
                Text(text = stringResource(R.string.edit_category_cancel))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun TransferPasswordsDialog(
    passwordCount: Int,
    categories: List<CategoryTransferOptionUiModel>,
    selectedCategoryId: Long?,
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors
    val canConfirm = selectedCategoryId != null &&
        categories.any { category -> category.id == selectedCategoryId }

    Dialog(
        onDismissRequest = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("edit_category_transfer_passwords_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onAction(EditCategoryAction.OnTransferBackClick) },
                        modifier = Modifier.testTag("edit_category_transfer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.edit_category_transfer_back),
                            tint = colors.textPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.edit_category_transfer_dialog_title),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
                        modifier = Modifier.testTag("edit_category_transfer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.edit_category_close_dialog),
                            tint = colors.textSecondary
                        )
                    }
                }

                AssociatedPasswordsCountText(passwordCount = passwordCount)

                Text(
                    text = stringResource(R.string.edit_category_transfer_dialog_message),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (categories.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = colors.surfaceBright.copy(alpha = 0.44f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(18.dp)
                            .testTag("edit_category_transfer_empty_state"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.edit_category_transfer_empty_title),
                            color = colors.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.edit_category_transfer_empty_message),
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                            .testTag("edit_category_transfer_categories_grid"),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories, key = { it.id }) { category ->
                            TransferCategoryOptionCard(
                                category = category,
                                isSelected = category.id == selectedCategoryId,
                                onClick = {
                                    onAction(EditCategoryAction.OnTransferCategorySelected(category.id))
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { onAction(EditCategoryAction.OnTransferConfirmed) },
                    enabled = canConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("edit_category_confirm_transfer_button")
                ) {
                    Text(text = stringResource(R.string.edit_category_confirm_transfer_button))
                }
            }
        }
    }
}

@Composable
private fun PostTransferDeleteConfirmationDialog(
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = { onAction(EditCategoryAction.OnPostTransferDeleteCancelled) },
        modifier = Modifier.testTag("edit_category_post_transfer_delete_confirmation_dialog"),
        title = {
            Text(
                text = stringResource(R.string.edit_category_post_transfer_delete_title),
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.edit_category_post_transfer_delete_message),
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnPostTransferDeleteConfirmed) },
                modifier = Modifier.testTag("edit_category_confirm_post_transfer_delete_button")
            ) {
                Text(
                    text = stringResource(R.string.edit_category_confirm_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnPostTransferDeleteCancelled) },
                modifier = Modifier.testTag("edit_category_cancel_post_transfer_delete_button")
            ) {
                Text(text = stringResource(R.string.edit_category_cancel))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun CategoryOperationErrorDialog(
    messageResId: Int,
    onAction: (EditCategoryAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
        modifier = Modifier.testTag("edit_category_operation_error_dialog"),
        title = {
            Text(
                text = stringResource(R.string.edit_category_operation_error_title),
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(messageResId),
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(EditCategoryAction.OnDeleteDialogDismissed) },
                modifier = Modifier.testTag("edit_category_operation_error_confirm_button")
            ) {
                Text(text = stringResource(R.string.edit_category_operation_error_confirm))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun CriticalCategoryOperationOverlay(
    state: EditCategoryDeleteFlowState.CriticalOperation
) {
    val colors = MaterialTheme.vaultColors

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("edit_category_critical_operation_overlay"),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                CircularProgressIndicator(
                    color = colors.primary,
                    modifier = Modifier.testTag("edit_category_critical_operation_progress")
                )
                Text(
                    text = stringResource(state.titleResId),
                    color = colors.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(state.messageResId),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.stepResIds.forEachIndexed { index, stepResId ->
                        CriticalOperationStep(
                            text = stringResource(stepResId),
                            isActive = index == state.activeStepIndex,
                            isCompleted = index < state.activeStepIndex
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CriticalOperationStep(
    text: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    val colors = MaterialTheme.vaultColors
    val tint = when {
        isCompleted -> colors.primary
        isActive -> colors.textPrimary
        else -> colors.textSecondary
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isCompleted) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(
                        width = 1.dp,
                        color = tint.copy(alpha = 0.64f),
                        shape = CircleShape
                    )
            )
        }
        Text(
            text = text,
            color = tint,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AssociatedPasswordsCountText(
    passwordCount: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(
                    pluralStringResource(
                        R.plurals.edit_category_associated_passwords_count,
                        passwordCount,
                        passwordCount
                    )
                )
            }
        },
        modifier = modifier.testTag("edit_category_associated_passwords_count"),
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun TransferCategoryOptionCard(
    category: CategoryTransferOptionUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors
    val borderColor = if (isSelected) {
        colors.primary
    } else {
        Color.Transparent
    }
    val backgroundColor = if (isSelected) {
        colors.surfaceBright.copy(alpha = 0.62f)
    } else {
        colors.surfaceBright.copy(alpha = 0.36f)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, borderColor, RoundedCornerShape(22.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("edit_category_transfer_option_${category.id}"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    color = colors.surfaceHigh.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(category.iconResId),
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = category.name,
                color = colors.textPrimary,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = pluralStringResource(
                    R.plurals.categories_password_count,
                    category.itemCount,
                    category.itemCount
                ),
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
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
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
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
                    color = colors.surfaceBright.copy(alpha = 0.48f),
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
                    tint = colors.primary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .background(color = colors.background, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategorySectionLabel(label = stringResource(R.string.edit_category_name_label))
            val nameTextFieldState = rememberEndCursorTextFieldState(
                text = name,
                onTextChange = onNameChanged
            )
            TextField(
                value = nameTextFieldState.value,
                onValueChange = nameTextFieldState.onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        nameTextFieldState.onFocusChanged(focusState.isFocused)
                    }
                    .testTag("edit_category_name_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = nameErrorResId != null,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceBright,
                    unfocusedContainerColor = colors.surfaceBright,
                    disabledContainerColor = colors.surfaceBright,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
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
    onAction: (EditCategoryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.edit_category_passwords_title),
            color = colors.textPrimary,
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
                            color = colors.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_category_passwords_empty_title),
                        color = colors.textPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.edit_category_passwords_empty_message),
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is CategoryPasswordsSectionUiState.Content -> {
                val passwords = state.passwords.map { password ->
                    password.toVaultPasswordListItemModel()
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(categoryPasswordsListHeight(passwords.size))
                        .background(
                            color = colors.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("edit_category_passwords_list"),
                    verticalArrangement = Arrangement.spacedBy(CategoryPasswordsListItemSpacing)
                ) {
                    vaultPasswordListItems(
                        passwords = passwords,
                        onItemClick = { passwordId ->
                            onAction(EditCategoryAction.OnPasswordClick(passwordId))
                        }
                    )
                }
            }
        }
    }
}

internal fun categoryPasswordsVisibleItemCount(passwordCount: Int): Int =
    passwordCount.coerceIn(
        minimumValue = 0,
        maximumValue = CategoryPasswordsMaxVisibleItems
    )

private fun categoryPasswordsListHeight(passwordCount: Int): Dp {
    val visibleItemCount = categoryPasswordsVisibleItemCount(passwordCount)
    val visibleSpacingCount = (visibleItemCount - 1).coerceAtLeast(0)

    return (CategoryPasswordsListItemHeight * visibleItemCount) +
        (CategoryPasswordsListItemSpacing * visibleSpacingCount) +
        CategoryPasswordsListVerticalPadding
}

private fun CategoryPasswordItemUiModel.toVaultPasswordListItemModel(): VaultPasswordListItemModel =
    VaultPasswordListItemModel(
        id = id,
        title = title,
        supportingText = supportingText,
        securityLevel = securityLevel.toVaultPasswordListSecurityLevel()
    )

private fun CategoryPasswordItemSecurityLevel.toVaultPasswordListSecurityLevel(): VaultPasswordListSecurityLevel =
    when (this) {
        CategoryPasswordItemSecurityLevel.Weak -> VaultPasswordListSecurityLevel.Weak
        CategoryPasswordItemSecurityLevel.Moderate -> VaultPasswordListSecurityLevel.Moderate
        CategoryPasswordItemSecurityLevel.Safe -> VaultPasswordListSecurityLevel.Safe
    }

private const val CategoryPasswordsMaxVisibleItems = 7
private val CategoryPasswordsListItemHeight = 80.dp
private val CategoryPasswordsListItemSpacing = 16.dp
private val CategoryPasswordsListVerticalPadding = 40.dp
