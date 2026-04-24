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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
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
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultBackHeader
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPrimaryPersistenceButton
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionDialog
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultAmber
import com.inovalou.seucofregerenciadordesenhas.ui.theme.VaultGreen
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditPasswordRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPasswordViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditPasswordEffect.NavigateBack -> onNavigateBack()
                is EditPasswordEffect.CopyToClipboard -> {
                    context.copyToClipboard(
                        value = effect.value,
                        isSensitive = effect.isSensitive
                    )
                }
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
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MidnightBlue
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

            when (val contentState = uiState.contentState) {
                EditPasswordContentState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.testTag("edit_password_loading"),
                            color = ElectricBlue
                        )
                    }
                }

                is EditPasswordContentState.Error -> {
                    Text(
                        text = stringResource(contentState.messageResId),
                        color = MistText,
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
                        isEditing = uiState.isIdentityCardEditing,
                        isPasswordVisible = uiState.isPasswordVisible,
                        passwordErrorResId = uiState.passwordErrorResId,
                        onEditClick = { onAction(EditPasswordAction.OnIdentityCardEditClick) },
                        onCardSaveClick = { onAction(EditPasswordAction.OnIdentityCardSaveClick) },
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
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lightbulb,
                                    contentDescription = null,
                                    tint = VaultGreen
                                )
                            },
                            accent = VaultGreen.copy(alpha = 0.2f),
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = stringResource(R.string.edit_password_history_action),
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null,
                                    tint = ElectricBlue
                                )
                            },
                            accent = ElectricBlue.copy(alpha = 0.18f),
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
                                .testTag("edit_password_save_button")
                        )

                        OutlinedButton(
                            onClick = { onAction(EditPasswordAction.OnDeleteClick) },
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

    if (uiState.isCategoryDialogVisible) {
        PasswordCategorySelectionDialog(
            state = uiState.categorySelectionState,
            onCategorySelected = { onAction(EditPasswordAction.OnCategorySelected(it)) },
            onDismiss = { onAction(EditPasswordAction.OnCategoryDialogDismissed) }
        )
    }
}

@Composable
private fun PasswordIdentityCard(
    title: String,
    email: String,
    categoryName: String,
    categoryErrorResId: Int?,
    password: String,
    isEditing: Boolean,
    isPasswordVisible: Boolean,
    passwordErrorResId: Int?,
    onEditClick: () -> Unit,
    onCardSaveClick: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    onCopyEmail: () -> Unit,
    onCopyPassword: () -> Unit,
    onTogglePasswordVisibility: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = SlateBlue.copy(alpha = 0.82f),
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IdentityCardModeButton(
                isEditing = isEditing,
                onEditClick = onEditClick,
                onSaveClick = onCardSaveClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 20.dp)
                    .testTag("edit_password_identity_card_mode_button")
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 28.dp, top = 56.dp, end = 28.dp, bottom = 28.dp),
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
                            .background(SurfaceBright),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.toInitials(),
                            color = SoftWhite,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (isEditing) {
                        CredentialField(
                            label = stringResource(R.string.edit_password_app_name_label),
                            value = title,
                            onValueChange = onTitleChanged,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            trailingContent = {},
                            testTag = "edit_password_title_input"
                        )
                    } else {
                        Text(
                            text = title.ifBlank {
                                stringResource(R.string.edit_password_app_name_placeholder)
                            },
                            modifier = Modifier.testTag("edit_password_title_text"),
                            color = SoftWhite,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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
                                tint = ElectricBlue
                            )
                        }
                    },
                    testTag = "edit_password_email_input",
                    readOnly = !isEditing
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
                                    tint = MistText
                                )
                            }
                            IconButton(
                                onClick = onCopyPassword,
                                modifier = Modifier.testTag("edit_password_copy_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = stringResource(R.string.edit_password_copy_password),
                                    tint = ElectricBlue
                                )
                            }
                        }
                    },
                    testTag = "edit_password_password_input",
                    readOnly = !isEditing
                )

                CategoryField(
                    label = stringResource(R.string.edit_password_category_label),
                    value = categoryName,
                    placeholder = stringResource(R.string.edit_password_category_placeholder),
                    errorResId = categoryErrorResId,
                    onClick = onCategoryClick,
                    testTag = "edit_password_category_field",
                    enabled = isEditing
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
private fun IdentityCardModeButton(
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isEditing) {
        Surface(
            modifier = modifier
                .widthIn(min = 88.dp)
                .height(34.dp)
                .clickable(onClick = onSaveClick),
            shape = RoundedCornerShape(999.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ElectricBlue, NeonPink)
                        ),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.edit_password_identity_card_save),
                    color = SoftWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    } else {
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onEditClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.edit_password_identity_card_edit),
                color = ElectricBlue,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(14.dp)
            )
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
    testTag: String,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = ElectricBlue.copy(alpha = 0.72f),
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
                    enabled = enabled,
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
                        color = GhostOutline.copy(alpha = 0.75f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = null,
                        tint = SoftWhite.copy(alpha = 0.78f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DeepNavy.copy(alpha = 0.55f),
                    unfocusedContainerColor = DeepNavy.copy(alpha = 0.55f),
                    disabledContainerColor = DeepNavy.copy(alpha = 0.55f),
                    focusedIndicatorColor = GhostOutline.copy(alpha = 0.28f),
                    unfocusedIndicatorColor = GhostOutline.copy(alpha = 0.16f),
                    disabledIndicatorColor = GhostOutline.copy(alpha = 0.16f),
                    focusedTextColor = SoftWhite,
                    unfocusedTextColor = SoftWhite,
                    disabledTextColor = SoftWhite,
                    focusedPlaceholderColor = GhostOutline.copy(alpha = 0.75f),
                    unfocusedPlaceholderColor = GhostOutline.copy(alpha = 0.75f),
                    disabledPlaceholderColor = GhostOutline.copy(alpha = 0.75f),
                    disabledLeadingIconColor = SoftWhite.copy(alpha = 0.78f),
                    cursorColor = ElectricBlue
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            color = ElectricBlue.copy(alpha = 0.72f),
            fontSize = 10.sp,
            lineHeight = 15.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.SemiBold
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(20.dp),
            keyboardOptions = keyboardOptions,
            singleLine = true,
            readOnly = readOnly,
            visualTransformation = visualTransformation,
            trailingIcon = trailingContent,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DeepNavy.copy(alpha = 0.55f),
                unfocusedContainerColor = DeepNavy.copy(alpha = 0.55f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = SoftWhite,
                unfocusedTextColor = SoftWhite,
                cursorColor = ElectricBlue
            )
        )
    }
}

@Composable
private fun SecuritySection(
    state: EditPasswordSecuritySectionUiState
) {
    val accentColor = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk -> MaterialTheme.colorScheme.error
        EditPasswordSecurityVisualState.MediumRisk -> VaultAmber
        EditPasswordSecurityVisualState.Safe -> VaultGreen
    }
    val accentGlow = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk -> Color(0x14FF716C)
        EditPasswordSecurityVisualState.MediumRisk -> Color(0x1AFFC857)
        EditPasswordSecurityVisualState.Safe -> Color(0x143FFF8B)
    }
    val accentRadial = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk -> Color(0x22FF716C)
        EditPasswordSecurityVisualState.MediumRisk -> Color(0x22FFC857)
        EditPasswordSecurityVisualState.Safe -> Color(0x223FFF8B)
    }
    val statusIcon = when (state.visualState) {
        EditPasswordSecurityVisualState.HighRisk,
        EditPasswordSecurityVisualState.MediumRisk -> Icons.Rounded.WarningAmber
        EditPasswordSecurityVisualState.Safe -> Icons.Rounded.CheckCircle
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(40.dp),
        color = DeepNavy
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(accentGlow)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(state.riskTitleResId),
                                color = accentColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                letterSpacing = 0.7.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        state.tagResIds.forEach { tagResId ->
                            SecurityTag(
                                text = stringResource(tagResId),
                                visualState = state.visualState
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                accentRadial,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MidnightBlue)
                                    .background(Color.Transparent)
                            )
                            Text(
                                text = "${state.scorePercent}%",
                                color = accentColor,
                                fontSize = 24.sp,
                                lineHeight = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = stringResource(R.string.edit_password_security_index_label),
                            color = MistText,
                            fontSize = 9.sp,
                            lineHeight = 14.sp,
                            letterSpacing = 0.9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0x99000000)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(width = 3.dp, height = 72.dp)
                                .background(accentColor)
                        )
                        Text(
                            text = stringResource(state.alertResId),
                            color = MistText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityTag(
    text: String,
    visualState: EditPasswordSecurityVisualState
) {
    val contentColor = when (visualState) {
        EditPasswordSecurityVisualState.HighRisk -> MaterialTheme.colorScheme.error
        EditPasswordSecurityVisualState.MediumRisk -> VaultAmber
        EditPasswordSecurityVisualState.Safe -> VaultGreen
    }
    val background = contentColor.copy(alpha = 0.16f)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = contentColor,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DateInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = DeepNavy
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = MistText.copy(alpha = 0.62f),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = SoftWhite,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = DeepNavy.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, GhostOutline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.edit_password_notes_label),
                color = MistText.copy(alpha = 0.62f),
                fontSize = 10.sp,
                lineHeight = 15.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.SemiBold
            )
            TextField(
                value = note,
                onValueChange = onNoteChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp)
                    .testTag("edit_password_note_input"),
                singleLine = false,
                minLines = 5,
                maxLines = 5,
                placeholder = {
                    Text(
                        text = stringResource(R.string.edit_password_notes_empty_hint),
                        color = GhostOutline.copy(alpha = 0.75f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DeepNavy.copy(alpha = 0.3f),
                    unfocusedContainerColor = DeepNavy.copy(alpha = 0.3f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MistText,
                    unfocusedTextColor = MistText,
                    cursorColor = ElectricBlue
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0x1AF4C95D),
        border = BorderStroke(1.dp, Color(0x66F4C95D))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color(0xFFF4C95D),
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: @Composable () -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = DeepNavy
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
                color = SoftWhite,
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

    return DateTimeFormatter.ofPattern("dd/MM/yyyy")
        .format(Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()))
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
