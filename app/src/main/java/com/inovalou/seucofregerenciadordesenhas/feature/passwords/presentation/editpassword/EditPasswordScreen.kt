package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.security.LocalAuthenticationPromptText
import com.inovalou.seucofregerenciadordesenhas.core.security.LocalAuthenticationResult
import com.inovalou.seucofregerenciadordesenhas.core.security.findFragmentActivity
import com.inovalou.seucofregerenciadordesenhas.core.security.requestLocalAuthentication
import com.inovalou.seucofregerenciadordesenhas.core.ui.CollectEffectWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultLocalAuthenticationGate
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.rememberEndCursorTextFieldState
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionDialog
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditPasswordRoute(
    onNavigateBackToOrigin: (EditPasswordOpenedFrom) -> Unit,
    onNavigateAfterSave: (EditPasswordOpenedFrom) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPasswordViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val localAuthPromptText = LocalAuthenticationPromptText(
        title = stringResource(R.string.local_auth_prompt_title),
        subtitle = stringResource(R.string.local_auth_prompt_subtitle)
    )

    CollectEffectWithLifecycle(viewModel.localAuthenticationEffects) { effect ->
        when (effect) {
            EditPasswordLocalAuthenticationEffect.RequestLocalAuthentication -> {
                val activity = context.findFragmentActivity()
                if (activity == null) {
                    viewModel.onAction(EditPasswordAction.OnLocalAuthenticationUnavailable)
                } else {
                    activity.requestLocalAuthentication(localAuthPromptText) { result ->
                        viewModel.onAction(result.toEditPasswordAction())
                    }
                }
            }
        }
    }

    CollectEffectWithLifecycle(viewModel.effects) { effect ->
        when (effect) {
            is EditPasswordEffect.NavigateBackToOrigin -> {
                onNavigateBackToOrigin(effect.openedFrom)
            }
            is EditPasswordEffect.NavigateAfterSave -> {
                onNavigateAfterSave(effect.openedFrom)
            }
            is EditPasswordEffect.CopyToClipboard -> {
                context.copyToClipboard(
                    value = effect.value,
                    isSensitive = effect.isSensitive
                )
            }
        }
    }

    EditPasswordScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun EditPasswordScreen(
    uiState: EditPasswordUiState,
    onAction: (EditPasswordAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding()
                .testTag("edit_password_screen"),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VaultBackHeader(
                title = stringResource(R.string.edit_password_title),
                navigationContentDescription = stringResource(R.string.edit_password_back),
                onBackClick = { onAction(EditPasswordAction.OnBackClick) },
                modifier = Modifier.testTag("edit_password_header")
            )

            if (uiState.localAuthenticationState != EditPasswordLocalAuthenticationState.Authenticated) {
                VaultLocalAuthenticationGate(
                    isAuthenticating = uiState.localAuthenticationState ==
                        EditPasswordLocalAuthenticationState.Authenticating,
                    messageResId = uiState.localAuthenticationState.toMessageResId(),
                    onRetryClick = { onAction(EditPasswordAction.OnLocalAuthenticationRetryClick) },
                    testTag = "edit_password_local_auth_gate"
                )
            } else when (val contentState = uiState.contentState) {
                EditPasswordContentState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("edit_password_loading"),
                            color = colors.primary
                        )
                    }
                }

                is EditPasswordContentState.Error -> {
                    Text(
                        text = stringResource(contentState.messageResId),
                        color = colors.textSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                EditPasswordContentState.Content -> {
                    PasswordIdentityCard(
                        title = uiState.title,
                        email = uiState.email,
                        categoryName = uiState.selectedCategoryName.orEmpty(),
                        categoryErrorResId = uiState.categoryErrorResId,
                        password = uiState.password,
                        isPasswordVisible = uiState.isPasswordVisible,
                        passwordErrorResId = uiState.passwordErrorResId,
                        onTitleChanged = { onAction(EditPasswordAction.OnTitleChanged(it)) },
                        onEmailChanged = { onAction(EditPasswordAction.OnEmailChanged(it)) },
                        onCategoryClick = { onAction(EditPasswordAction.OnCategoryFieldClick) },
                        onPasswordChanged = { onAction(EditPasswordAction.OnPasswordChanged(it)) },
                        onCopyEmail = { onAction(EditPasswordAction.OnCopyEmailClick) },
                        onCopyPassword = { onAction(EditPasswordAction.OnCopyPasswordClick) },
                        onTogglePasswordVisibility = {
                            onAction(EditPasswordAction.OnTogglePasswordVisibility)
                        }
                    )

                    SecuritySection(state = uiState.securitySection)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DateInfoCard(
                            label = stringResource(R.string.edit_password_created_at_label),
                            value = uiState.createdAt.toDateLabel(),
                            modifier = Modifier.weight(1f)
                        )
                        DateInfoCard(
                            label = stringResource(R.string.edit_password_updated_at_label),
                            value = uiState.updatedAt.toDateLabel(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    NotesCard(
                        note = uiState.note,
                        onNoteChanged = { onAction(EditPasswordAction.OnNoteChanged(it)) }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ActionCard(
                            title = stringResource(R.string.edit_password_suggest_action),
                            onClick = { onAction(EditPasswordAction.OnSuggestStrongPasswordClick) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = colors.success
                                )
                            },
                            accent = colors.success.copy(alpha = 0.2f),
                            testTag = "edit_password_suggest_action_card",
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = stringResource(R.string.edit_password_history_action),
                            onClick = { onAction(EditPasswordAction.OnPasswordHistoryClick) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = colors.primary
                                )
                            },
                            accent = colors.primary.copy(alpha = 0.18f),
                            testTag = "edit_password_history_action_card",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    uiState.submitErrorResId?.let { errorResId ->
                        Text(
                            text = stringResource(errorResId),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VaultPrimaryPersistenceButton(
                            text = stringResource(R.string.edit_password_save_button),
                            isLoading = uiState.isSaving,
                            onClick = { onAction(EditPasswordAction.OnSaveClick) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_password_save_button"),
                            enabled = !uiState.isDeleting
                        )

                        OutlinedButton(
                            onClick = { onAction(EditPasswordAction.OnDeleteClick) },
                            enabled = !uiState.isDeleting && !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("edit_password_delete_button"),
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.24f)
                            )
                        ) {
                            if (uiState.isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.edit_password_delete_button),
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
    }

    if (uiState.isCategoryDialogVisible) {
        PasswordCategorySelectionDialog(
            state = uiState.categorySelectionState,
            onCategorySelected = { onAction(EditPasswordAction.OnCategorySelected(it)) },
            onDismiss = { onAction(EditPasswordAction.OnCategoryDialogDismissed) }
        )
    }

    EditPasswordDeleteFlow(
        state = uiState.deleteFlowState,
        onAction = onAction
    )

    uiState.comingSoonDialog?.let { dialog ->
        ComingSoonDialog(
            title = stringResource(dialog.titleResId()),
            message = stringResource(R.string.edit_password_coming_soon_message),
            confirmText = stringResource(R.string.settings_ok),
            onDismiss = { onAction(EditPasswordAction.OnComingSoonDialogDismissed) }
        )
    }
}

private fun LocalAuthenticationResult.toEditPasswordAction(): EditPasswordAction = when (this) {
    LocalAuthenticationResult.Succeeded -> EditPasswordAction.OnLocalAuthenticationSucceeded
    LocalAuthenticationResult.Cancelled -> EditPasswordAction.OnLocalAuthenticationCancelled
    LocalAuthenticationResult.Failed -> EditPasswordAction.OnLocalAuthenticationFailed
    LocalAuthenticationResult.Unavailable -> EditPasswordAction.OnLocalAuthenticationUnavailable
}

private fun EditPasswordLocalAuthenticationState.toMessageResId(): Int = when (this) {
    EditPasswordLocalAuthenticationState.Locked,
    EditPasswordLocalAuthenticationState.Authenticating -> R.string.local_auth_authenticating_message
    EditPasswordLocalAuthenticationState.Failed -> R.string.local_auth_failed_message
    EditPasswordLocalAuthenticationState.Unavailable -> R.string.local_auth_unavailable_message
    EditPasswordLocalAuthenticationState.Authenticated -> R.string.local_auth_authenticating_message
}

@Composable
private fun EditPasswordDeleteFlow(
    state: EditPasswordDeleteFlowState,
    onAction: (EditPasswordAction) -> Unit
) {
    when (state) {
        EditPasswordDeleteFlowState.Idle -> Unit
        EditPasswordDeleteFlowState.Confirmation -> {
            DeletePasswordConfirmationDialog(onAction = onAction)
        }
    }
}

@Composable
private fun DeletePasswordConfirmationDialog(
    onAction: (EditPasswordAction) -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = { onAction(EditPasswordAction.OnDeleteDialogDismissed) },
        modifier = Modifier.testTag("edit_password_delete_confirmation_dialog"),
        title = {
            Text(
                text = stringResource(R.string.edit_password_delete_dialog_title),
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.edit_password_delete_dialog_message),
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAction(EditPasswordAction.OnDeleteConfirmed) },
                modifier = Modifier.testTag("edit_password_confirm_delete_button")
            ) {
                Text(
                    text = stringResource(R.string.edit_password_confirm_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onAction(EditPasswordAction.OnDeleteDialogDismissed) },
                modifier = Modifier.testTag("edit_password_cancel_delete_button")
            ) {
                Text(text = stringResource(R.string.edit_password_cancel))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun ComingSoonDialog(
    title: String,
    message: String,
    confirmText: String,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("edit_password_coming_soon_dialog"),
        title = {
            Text(
                text = title,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                text = message,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("edit_password_coming_soon_ok_button")
            ) {
                Text(text = confirmText)
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun PasswordIdentityCard(
    title: String,
    email: String,
    categoryName: String,
    categoryErrorResId: Int?,
    password: String,
    isPasswordVisible: Boolean,
    passwordErrorResId: Int?,
    onTitleChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onCopyEmail: () -> Unit,
    onCopyPassword: () -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = colors.surfaceHigh.copy(alpha = 0.92f),
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(colors.surfaceBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.toInitials(),
                            color = colors.textPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    CredentialField(
                        label = stringResource(R.string.edit_password_app_name_label),
                        value = title,
                        onValueChange = onTitleChanged,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        trailingContent = {},
                        testTag = "edit_password_title_input"
                    )
                }

                CredentialField(
                    label = stringResource(R.string.edit_password_email_label),
                    value = email,
                    onValueChange = onEmailChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    trailingContent = {
                        IconButton(
                            onClick = onCopyEmail,
                            modifier = Modifier.testTag("edit_password_copy_email_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = stringResource(R.string.edit_password_copy_email),
                                tint = colors.primary
                            )
                        }
                    },
                    testTag = "edit_password_email_input"
                )

                CredentialField(
                    label = stringResource(R.string.edit_password_password_label),
                    value = password,
                    onValueChange = onPasswordChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = onTogglePasswordVisibility,
                                modifier = Modifier.testTag("edit_password_visibility_toggle")
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) {
                                        Icons.Outlined.VisibilityOff
                                    } else {
                                        Icons.Outlined.Visibility
                                    },
                                    contentDescription = stringResource(
                                        if (isPasswordVisible) {
                                            R.string.edit_password_hide_password
                                        } else {
                                            R.string.edit_password_show_password
                                        }
                                    ),
                                    tint = colors.textSecondary
                                )
                            }
                            IconButton(
                                onClick = onCopyPassword,
                                modifier = Modifier.testTag("edit_password_copy_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.edit_password_copy_password),
                                    tint = colors.primary
                                )
                            }
                        }
                    },
                    testTag = "edit_password_password_input"
                )

                CategoryField(
                    label = stringResource(R.string.edit_password_category_label),
                    value = categoryName,
                    placeholder = stringResource(R.string.edit_password_category_placeholder),
                    errorResId = categoryErrorResId,
                    onClick = onCategoryClick,
                    testTag = "edit_password_category_field"
                )

                passwordErrorResId?.let { errorResId ->
                    Text(
                        text = stringResource(errorResId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryField(
    label: String,
    value: String,
    placeholder: String,
    errorResId: Int?,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = MaterialTheme.vaultColors

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = colors.primary.copy(alpha = 0.72f),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clickable(
                    onClick = onClick,
                    role = Role.Button
                )
        )
        {
            TextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                readOnly = true,
                enabled = false,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = colors.outline.copy(alpha = 0.75f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = colors.textPrimary.copy(alpha = 0.78f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface.copy(alpha = 0.68f),
                    unfocusedContainerColor = colors.surface.copy(alpha = 0.68f),
                    disabledContainerColor = colors.surface.copy(alpha = 0.68f),
                    focusedIndicatorColor = colors.outline.copy(alpha = 0.28f),
                    unfocusedIndicatorColor = colors.outline.copy(alpha = 0.16f),
                    disabledIndicatorColor = colors.outline.copy(alpha = 0.16f),
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    disabledTextColor = colors.textPrimary,
                    focusedPlaceholderColor = colors.outline.copy(alpha = 0.75f),
                    unfocusedPlaceholderColor = colors.outline.copy(alpha = 0.75f),
                    disabledPlaceholderColor = colors.outline.copy(alpha = 0.75f),
                    disabledLeadingIconColor = colors.textPrimary.copy(alpha = 0.78f),
                    cursorColor = colors.primary
                )
            )
        }

        errorResId?.let { validationErrorResId ->
            Text(
                text = stringResource(validationErrorResId),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CredentialField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    testTag: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    readOnly: Boolean = false,
    trailingContent: @Composable () -> Unit
) {
    val colors = MaterialTheme.vaultColors
    val textFieldState = rememberEndCursorTextFieldState(
        text = value,
        onTextChange = onValueChange,
        moveCursorToEndOnFocus = !readOnly
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = colors.primary.copy(alpha = 0.72f),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        )
        TextField(
            value = textFieldState.value,
            onValueChange = textFieldState.onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    textFieldState.onFocusChanged(focusState.isFocused)
                }
                .testTag(testTag),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = keyboardOptions,
            singleLine = true,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            trailingIcon = trailingContent,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surface.copy(alpha = 0.68f),
                unfocusedContainerColor = colors.surface.copy(alpha = 0.68f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primary
            )
        )
    }
}

@Composable
private fun SecuritySection(
    state: EditPasswordSecuritySectionUiState
) {
    val colors = MaterialTheme.vaultColors
    val accentColor = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk -> colors.danger
        EditPasswordSecurityVisualState.MediumRisk -> colors.warning
        EditPasswordSecurityVisualState.Safe -> colors.success
    }
    val statusIcon = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk,
        EditPasswordSecurityVisualState.MediumRisk -> Icons.Rounded.WarningAmber
        EditPasswordSecurityVisualState.Safe -> Icons.Rounded.CheckCircle
    }
    val alertLabel = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk,
        EditPasswordSecurityVisualState.MediumRisk -> stringResource(R.string.edit_password_security_alert_label)
        EditPasswordSecurityVisualState.Safe -> stringResource(R.string.edit_password_security_strong_label)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("edit_password_security_section"),
        shape = RoundedCornerShape(40.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 33.dp, vertical = 33.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(state.riskTitleResId),
                            color = accentColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.7.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.tagResIds.forEach { tagResId ->
                            SecurityTag(
                                text = stringResource(tagResId),
                                style = securityTagStyle(tagResId)
                            )
                        }
                    }
                }

                SecurityScoreIndicator(
                    scorePercent = state.scorePercent,
                    accentColor = accentColor
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = colors.surfaceHigh.copy(alpha = 0.62f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .widthIn(min = 4.dp)
                            .background(accentColor)
                    )
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                SpanStyle(
                                    color = accentColor,
                                    fontWeight = FontWeight.SemiBold
                                )
                            ) {
                                append(alertLabel)
                                append(" ")
                            }
                            withStyle(
                                SpanStyle(
                                    color = colors.textSecondary,
                                    fontWeight = FontWeight.Normal
                                )
                            ) {
                                append(stringResource(state.alertResId))
                            }
                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 15.dp, bottom = 15.dp),
                        fontSize = 14.sp,
                        lineHeight = 22.75.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SecurityTag(
    text: String,
    style: SecurityTagStyle
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = style.backgroundColor,
        border = BorderStroke(1.dp, style.borderColor)
    ) {
        Row(
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 13.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = style.icon,
                contentDescription = null,
                tint = style.contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = style.contentColor,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.6).sp
            )
        }
    }
}

@Composable
private fun SecurityScoreIndicator(
    scorePercent: Int,
    accentColor: Color
) {
    val colors = MaterialTheme.vaultColors

    val sweepAngle = securityScoreSweepAngle(scorePercent = scorePercent)

    Column(
        modifier = Modifier.widthIn(min = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val strokeWidth = 4.dp.toPx()
                        drawCircle(
                            color = accentColor.copy(alpha = 0.2f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = accentColor,
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        )
                    }
            )
            Text(
                text = "${scorePercent}%",
                color = accentColor,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            text = stringResource(R.string.edit_password_security_index_label),
            color = colors.textSecondary,
            fontSize = 9.sp,
            lineHeight = 13.5.sp,
            letterSpacing = 0.9.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun securityTagStyle(tagResId: Int): SecurityTagStyle {
    val colors = MaterialTheme.vaultColors

    return when (tagResId) {
        R.string.edit_password_security_tag_weak -> SecurityTagStyle(
            icon = Icons.Rounded.WarningAmber,
            contentColor = colors.danger,
            backgroundColor = colors.danger.copy(alpha = 0.14f),
            borderColor = colors.danger.copy(alpha = 0.22f)
        )
        R.string.edit_password_security_tag_duplicate -> SecurityTagStyle(
            icon = Icons.Outlined.ContentCopy,
            contentColor = colors.secondary,
            backgroundColor = colors.secondary.copy(alpha = 0.14f),
            borderColor = colors.secondary.copy(alpha = 0.22f)
        )
        else -> SecurityTagStyle(
            icon = Icons.Rounded.CheckCircle,
            contentColor = colors.success,
            backgroundColor = colors.success.copy(alpha = 0.14f),
            borderColor = colors.success.copy(alpha = 0.22f)
        )
    }
}

private data class SecurityTagStyle(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentColor: Color,
    val backgroundColor: Color,
    val borderColor: Color
)

@Composable
private fun DateInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = colors.textSecondary.copy(alpha = 0.72f),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = colors.textPrimary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun NotesCard(
    note: String,
    onNoteChanged: (String) -> Unit
) {
    val colors = MaterialTheme.vaultColors
    val noteTextFieldState = rememberEndCursorTextFieldState(
        text = note,
        onTextChange = onNoteChanged
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = colors.surfaceHigh.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.edit_password_notes_label),
                color = colors.textSecondary.copy(alpha = 0.72f),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextField(
                value = noteTextFieldState.value,
                onValueChange = noteTextFieldState.onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .onFocusChanged { focusState ->
                        noteTextFieldState.onFocusChanged(focusState.isFocused)
                    }
                    .testTag("edit_password_note_input"),
                shape = RoundedCornerShape(20.dp),
                singleLine = false,
                minLines = 5,
                maxLines = 5,
                placeholder = {
                    Text(
                        text = stringResource(R.string.edit_password_notes_empty_hint),
                        color = colors.outline.copy(alpha = 0.75f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.surface.copy(alpha = 0.68f),
                    unfocusedContainerColor = colors.surface.copy(alpha = 0.68f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                )
            )

            if (note.isNotEmpty()) {
                PlainTextNoteNotice(text = stringResource(R.string.new_password_note_warning))
            }
        }
    }
}

@Composable
private fun PlainTextNoteNotice(text: String) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.warning.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = colors.warning,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    accent: Color,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(
                onClick = onClick,
                role = Role.Button
            )
            .testTag(testTag),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun String.toInitials(): String {
    val parts = trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) {
        return "A"
    }

    if (parts.size == 1) {
        return parts.first().take(1).uppercase()
    }

    return buildString {
        append(parts.first().take(1))
        append(parts.last().take(1))
    }.uppercase()
}

private fun Long.toDateLabel(): String {
    if (this <= 0L) {
        return "--/--/----"
    }

    return SimpleDateFormat(DATE_LABEL_PATTERN, Locale.getDefault())
        .format(Date(this))
}

private const val DATE_LABEL_PATTERN = "dd/MM/yyyy"

private fun EditPasswordComingSoonDialog.titleResId(): Int = when (this) {
    EditPasswordComingSoonDialog.SuggestPassword -> R.string.edit_password_suggest_action
    EditPasswordComingSoonDialog.History -> R.string.edit_password_history_action
}

private fun Context.copyToClipboard(
    value: String,
    isSensitive: Boolean
) {
    val clipboardManager = getSystemService(ClipboardManager::class.java) ?: return
    val clipData = ClipData.newPlainText(getString(R.string.app_name), value)
    if (isSensitive) {
        clipData.description.extras = PersistableBundle().apply {
            putBoolean("android.content.extra.IS_SENSITIVE", true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                putBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
    }
    clipboardManager.setPrimaryClip(clipData)
}
