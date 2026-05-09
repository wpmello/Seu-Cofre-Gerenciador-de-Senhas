package com.inovalou.seucofregerenciadordesenhas.feature.settings.presentation

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppLanguage
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.model.AppThemePreference
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultEncryptedIndicator
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultTopBar
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.NeonPink
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SeuCofreGerenciadorDeSenhasTheme
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SurfaceBright

private val SettingsItemCardMinHeight = 112.dp

@Composable
fun SettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onAction: (SettingsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightBlue)
                .testTag("settings_screen")
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = 16.dp,
                    end = 24.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    VaultTopBar(
                        searchContentDescriptionResId = R.string.settings_search,
                        onSearchClick = { onAction(SettingsAction.OnSearchClick) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    SettingsTitle()
                }

                when (val contentState = uiState.contentState) {
                    SettingsContentState.Loading -> {
                        item {
                            SettingsLoading()
                        }
                    }

                    SettingsContentState.Empty -> {
                        item {
                            SettingsMessageCard(message = stringResource(R.string.settings_empty_message))
                        }
                    }

                    is SettingsContentState.Error -> {
                        item {
                            SettingsMessageCard(message = stringResource(contentState.messageResId))
                        }
                    }

                    SettingsContentState.Content -> {
                        item {
                            SettingsUserCard(
                                user = uiState.user,
                                onClick = { onAction(SettingsAction.OnUserCardClick) }
                            )
                        }

                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                uiState.items.forEach { item ->
                                    SettingsItemCard(
                                        item = item,
                                        onClick = { onAction(SettingsAction.OnItemClick(item.kind)) }
                                    )
                                }
                            }
                        }

                        item {
                            ActiveEncryptionCard()
                        }
                    }
                }
            }

            uiState.nameEditor?.let { nameEditor ->
                UserNameEditorBottomSheet(
                    state = nameEditor,
                    onNameChange = { onAction(SettingsAction.OnUserNameDraftChange(it)) },
                    onSaveClick = { onAction(SettingsAction.OnSaveUserNameClick) },
                    onDismiss = { onAction(SettingsAction.OnDismissUserNameEditor) }
                )
            }

            uiState.languageDialog?.let { languageDialog ->
                LanguageSelectionDialog(
                    state = languageDialog,
                    onLanguageSelected = { onAction(SettingsAction.OnLanguageDraftSelected(it)) },
                    onSaveClick = { onAction(SettingsAction.OnSaveLanguageClick) },
                    onDismiss = { onAction(SettingsAction.OnDismissLanguageDialog) }
                )
            }

            uiState.themeDialog?.let { themeDialog ->
                ThemeSelectionDialog(
                    state = themeDialog,
                    onThemeSelected = { onAction(SettingsAction.OnThemeDraftSelected(it)) },
                    onSaveClick = { onAction(SettingsAction.OnSaveThemeClick) },
                    onDismiss = { onAction(SettingsAction.OnDismissThemeDialog) }
                )
            }

            if (uiState.aboutDialogVisible) {
                AboutAppDialog(
                    onDismiss = { onAction(SettingsAction.OnDismissAboutDialog) }
                )
            }
        }
    }
}

@Composable
private fun SettingsTitle(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            color = SoftWhite,
            fontSize = 42.sp,
            lineHeight = 48.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = stringResource(R.string.settings_subtitle),
            color = MistText,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun SettingsLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 240.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = ElectricBlue,
            modifier = Modifier.testTag("settings_loading")
        )
    }
}

@Composable
private fun SettingsMessageCard(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DeepNavy)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MistText,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SettingsUserCard(
    user: SettingsUserUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .clickable(onClick = onClick)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SlateBlue.copy(alpha = 0.88f), DeepNavy, Color(0xFF2C2440))
                )
            )
            .testTag("settings_user_card")
            .padding(horizontal = 28.dp, vertical = 30.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text(
            text = user.name ?: stringResource(user.fallbackNameResId),
            color = SoftWhite,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.ExtraBold
        )
        VaultEncryptedIndicator(
            labelResId = user.encryptedStatusResId,
            container = false
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserNameEditorBottomSheet(
    state: SettingsNameEditorUiState,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        contentColor = SoftWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("settings_user_name_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.settings_user_name_sheet_title),
                    color = SoftWhite,
                    fontSize = 24.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_user_name_sheet_subtitle),
                    color = MistText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            OutlinedTextField(
                value = state.draftName,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_user_name_input"),
                label = { Text(text = stringResource(R.string.settings_user_name_input_label)) },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.settings_cancel))
                }
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("settings_user_name_save")
                ) {
                    Text(text = stringResource(R.string.settings_save))
                }
            }
        }
    }
}

@Composable
private fun SettingsItemCard(
    item: SettingsItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SettingsItemCardMinHeight)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .background(DeepNavy, RoundedCornerShape(24.dp))
            .testTag("settings_item")
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsItemIconBox(icon = item.icon)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(item.titleResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_item_title_${item.kind.name}"),
                color = SoftWhite,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(item.subtitleResId),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("settings_item_subtitle_${item.kind.name}"),
                    color = MistText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                item.trailingLabelResId?.let { trailingLabelResId ->
                    Text(
                        text = stringResource(trailingLabelResId),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("settings_item_trailing_${item.kind.name}"),
                        textAlign = TextAlign.End,
                        color = MistText.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MistText.copy(alpha = 0.45f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun SettingsItemIconBox(
    icon: SettingsItemIcon,
    modifier: Modifier = Modifier
) {
    val (imageVector, iconTint, containerColor) = when (icon) {
        SettingsItemIcon.Password -> Triple(Icons.Rounded.Password, ElectricBlue, SlateBlue.copy(alpha = 0.74f))
        SettingsItemIcon.Palette -> Triple(Icons.Rounded.Palette, Color(0xFFA7FFC0), Color(0xFF1A3031))
        SettingsItemIcon.Info -> Triple(Icons.Rounded.Info, MistText, SlateBlue.copy(alpha = 0.54f))
    }

    Box(
        modifier = modifier
            .size(54.dp)
            .background(containerColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
private fun LanguageSelectionDialog(
    state: SettingsLanguageDialogUiState,
    onLanguageSelected: (AppLanguage) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        titleContentColor = SoftWhite,
        textContentColor = MistText,
        title = { Text(text = stringResource(R.string.settings_language_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.testTag("settings_language_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { language ->
                    SettingsRadioOption(
                        label = stringResource(language.labelResId()),
                        selected = state.draftLanguage == language,
                        onClick = { onLanguageSelected(language) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSaveClick,
                modifier = Modifier.testTag("settings_language_save")
            ) {
                Text(text = stringResource(R.string.settings_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Composable
private fun ThemeSelectionDialog(
    state: SettingsThemeDialogUiState,
    onThemeSelected: (AppThemePreference) -> Unit,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        titleContentColor = SoftWhite,
        textContentColor = MistText,
        title = { Text(text = stringResource(R.string.settings_theme_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.testTag("settings_theme_dialog"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppThemePreference.entries.forEach { themePreference ->
                    SettingsRadioOption(
                        label = stringResource(themePreference.labelResId()),
                        selected = state.draftTheme == themePreference,
                        onClick = { onThemeSelected(themePreference) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSaveClick,
                modifier = Modifier.testTag("settings_theme_save")
            ) {
                Text(text = stringResource(R.string.settings_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_cancel))
            }
        }
    )
}

@Composable
private fun SettingsRadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(SurfaceBright.copy(alpha = if (selected) 0.52f else 0.22f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = label,
            color = SoftWhite,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AboutAppDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepNavy,
        titleContentColor = SoftWhite,
        textContentColor = MistText,
        title = { Text(text = stringResource(R.string.settings_about_dialog_title)) },
        text = {
            Text(
                text = stringResource(R.string.settings_about_dialog_message),
                modifier = Modifier.testTag("settings_about_dialog"),
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.settings_ok))
            }
        }
    )
}

@Composable
private fun ActiveEncryptionCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceBright.copy(alpha = 0.34f), RoundedCornerShape(24.dp))
            .testTag("settings_crypto_card")
            .padding(horizontal = 24.dp, vertical = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_crypto_title),
                color = SoftWhite,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.settings_crypto_description),
                color = MistText,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(listOf(ElectricBlue, NeonPink)),
                    shape = RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = MidnightBlue,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    SeuCofreGerenciadorDeSenhasTheme {
        SettingsScreen(
            uiState = SettingsUiState.content(),
            onAction = {}
        )
    }
}
