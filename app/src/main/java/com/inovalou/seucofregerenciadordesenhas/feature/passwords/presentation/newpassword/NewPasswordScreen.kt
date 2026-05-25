package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.newpassword

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.CollectEffectWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.rememberEndCursorTextFieldState
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionDialog
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

@Composable
fun NewPasswordRoute(
    onNavigateBack: () -> Unit,
    onNavigateBackToOrigin: (NewPasswordOpenedFrom) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NewPasswordViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffectWithLifecycle(viewModel.effects) { effect ->
        when (effect) {
            NewPasswordEffect.NavigateBack -> onNavigateBack()
            is NewPasswordEffect.NavigateBackToOrigin -> {
                onNavigateBackToOrigin(effect.openedFrom)
            }
        }
    }

    NewPasswordScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun NewPasswordScreen(
    uiState: NewPasswordUiState,
    onAction: (NewPasswordAction) -> Unit,
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
                .testTag("new_password_screen"),
            verticalArrangement = Arrangement.spacedBy(40.dp)
        ) {
            VaultBackHeader(
                title = stringResource(R.string.new_password_title),
                navigationContentDescription = stringResource(R.string.new_password_back),
                onBackClick = { onAction(NewPasswordAction.OnBackClick) },
                testTag = "new_password_header"
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.new_password_description),
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth(0.88f)
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                PasswordTextField(
                    label = stringResource(R.string.new_password_name_label),
                    value = uiState.title,
                    placeholder = stringResource(R.string.new_password_name_hint),
                    leadingIcon = Icons.Outlined.Apps,
                    onValueChange = { onAction(NewPasswordAction.OnTitleChanged(it)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    testTag = "new_password_title_input",
                    highlightedLabel = true,
                    showBorder = false
                )

                PasswordTextField(
                    label = stringResource(R.string.new_password_email_label),
                    value = uiState.login,
                    placeholder = stringResource(R.string.new_password_email_hint),
                    leadingIcon = Icons.Outlined.Email,
                    onValueChange = { onAction(NewPasswordAction.OnLoginChanged(it)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    testTag = "new_password_email_input",
                    highlightedLabel = true
                )

                CategorySelectionField(
                    label = stringResource(R.string.new_password_category_label),
                    value = uiState.selectedCategoryName.orEmpty(),
                    placeholder = stringResource(R.string.new_password_category_hint),
                    errorResId = uiState.categoryErrorResId,
                    testTag = "new_password_category_field",
                    onClick = { onAction(NewPasswordAction.OnCategoryFieldClick) }
                )

                PasswordSection(
                    uiState = uiState,
                    onPasswordChanged = { onAction(NewPasswordAction.OnPasswordChanged(it)) },
                    onTogglePasswordVisibility = {
                        onAction(NewPasswordAction.OnTogglePasswordVisibility)
                    }
                )

                NoteSection(
                    note = uiState.note,
                    onNoteChanged = { onAction(NewPasswordAction.OnNoteChanged(it)) }
                )
            }

            uiState.submitErrorResId?.let { errorResId ->
                ValidationText(errorResId = errorResId)
            }

            Spacer(modifier = Modifier.height(4.dp))

            VaultPrimaryPersistenceButton(
                text = stringResource(R.string.new_password_save_button),
                isLoading = uiState.isSaving,
                onClick = { onAction(NewPasswordAction.OnSaveClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_password_save_button"),
            )
        }
    }

    if (uiState.isCategoryDialogVisible) {
        PasswordCategorySelectionDialog(
            state = uiState.categorySelectionState,
            onCategorySelected = { onAction(NewPasswordAction.OnCategorySelected(it)) },
            onDismiss = { onAction(NewPasswordAction.OnCategoryDialogDismissed) }
        )
    }
}

@Composable
private fun PasswordTextField(
    modifier: Modifier = Modifier,
    label: String?,
    value: String,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    testTag: String,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    containerColor: Color? = null,
    highlightedLabel: Boolean = false,
    showBorder: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    val colors = MaterialTheme.vaultColors
    val fieldContainerColor = containerColor ?: colors.surfaceHigh
    val textFieldState = rememberEndCursorTextFieldState(
        text = value,
        onTextChange = onValueChange,
        moveCursorToEndOnFocus = !readOnly
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        label?.let {
            PasswordSectionLabel(label = it, highlighted = highlightedLabel)
        }

        TextField(
            value = textFieldState.value,
            onValueChange = textFieldState.onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    textFieldState.onFocusChanged(focusState.isFocused)
                }
                .testTag(testTag),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = keyboardOptions,
            readOnly = readOnly,
            visualTransformation = if (isPasswordField && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = colors.outline.copy(alpha = 0.75f)
                )
            },
            leadingIcon = leadingIcon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.textPrimary.copy(alpha = 0.78f)
                    )
                }
            },
            trailingIcon = if (isPasswordField && onTogglePasswordVisibility != null) {
                {
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier.testTag("new_password_visibility_toggle")
                    ) {
                        AnimatedContent(
                            targetState = isPasswordVisible,
                            label = "password_visibility"
                        ) { isVisible ->
                            Icon(
                                imageVector = if (isVisible) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                                contentDescription = if (isVisible) {
                                    stringResource(R.string.new_password_hide_password)
                                } else {
                                    stringResource(R.string.new_password_show_password)
                                },
                                tint = colors.textPrimary.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            } else {
                null
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = fieldContainerColor,
                unfocusedContainerColor = fieldContainerColor,
                disabledContainerColor = fieldContainerColor,
                focusedIndicatorColor = if (showBorder) colors.outline.copy(alpha = 0.28f) else Color.Transparent,
                unfocusedIndicatorColor = if (showBorder) colors.outline.copy(alpha = 0.16f) else Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                focusedPlaceholderColor = colors.outline.copy(alpha = 0.75f),
                unfocusedPlaceholderColor = colors.outline.copy(alpha = 0.75f),
                cursorColor = colors.primary
            )
        )
    }
}

@Composable
private fun NoteSection(
    note: String,
    onNoteChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PasswordSectionLabel(
            label = stringResource(R.string.new_password_note_label),
            highlighted = true
        )

        PasswordTextField(
            label = null,
            value = note,
            placeholder = stringResource(R.string.new_password_note_hint),
            onValueChange = onNoteChanged,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            testTag = "new_password_note_input",
            modifier = Modifier.heightIn(min = 168.dp),
            singleLine = false,
            minLines = 6,
            maxLines = 6
        )

        if (note.isNotEmpty()) {
            PlainTextNoteWarning(
                warningText = stringResource(R.string.new_password_note_warning)
            )
        }
    }
}

@Composable
private fun PlainTextNoteWarning(
    warningText: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = colors.warning.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colors.warning.copy(alpha = 0.32f)
        )
    ) {
        Text(
            text = warningText,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = colors.warning,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun PasswordSection(
    uiState: NewPasswordUiState,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colors.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PasswordSectionLabel(
            label = stringResource(R.string.new_password_password_label),
            highlighted = true
        )

        PasswordTextField(
            label = null,
            value = uiState.password,
            placeholder = stringResource(R.string.new_password_password_hint),
            leadingIcon = Icons.Outlined.Lock,
            onValueChange = onPasswordChanged,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isPasswordField = true,
            isPasswordVisible = uiState.isPasswordVisible,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            testTag = "new_password_password_input",
        )

        uiState.passwordErrorResId?.let { errorResId ->
            ValidationText(errorResId = errorResId)
        }
    }
}

@Composable
private fun PasswordSectionLabel(
    label: String,
    highlighted: Boolean = false
) {
    val colors = MaterialTheme.vaultColors

    Text(
        text = label,
        color = if (highlighted) colors.primary else colors.textSecondary,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun CategorySelectionField(
    label: String,
    value: String,
    placeholder: String,
    errorResId: Int?,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PasswordSectionLabel(label = label, highlighted = true)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .clickable(
                    onClick = onClick,
                    role = Role.Button
                )
        ) {
            TextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(16.dp),
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
                    focusedContainerColor = colors.surfaceHigh,
                    unfocusedContainerColor = colors.surfaceHigh,
                    disabledContainerColor = colors.surfaceHigh,
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
            ValidationText(errorResId = validationErrorResId)
        }
    }
}

@Composable
private fun ValidationText(
    errorResId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(errorResId),
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}
