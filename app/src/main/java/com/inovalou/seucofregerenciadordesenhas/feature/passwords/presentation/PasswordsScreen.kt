package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultGradientFab
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultSearchField
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultTopBar
import com.inovalou.seucofregerenciadordesenhas.ui.theme.DeepNavy
import com.inovalou.seucofregerenciadordesenhas.ui.theme.ElectricBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.GhostOutline
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MidnightBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.MistText
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SlateBlue
import com.inovalou.seucofregerenciadordesenhas.ui.theme.SoftWhite

@Composable
fun PasswordsRoute(
    onOpenPassword: (Long) -> Unit,
    onAddPassword: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasswordsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PasswordsEffect.OpenPasswordDetails -> onOpenPassword(effect.passwordId)
                PasswordsEffect.NavigateToNewPassword -> onAddPassword()
            }
        }
    }

    PasswordsScreen(
        uiState = uiState.value,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun PasswordsScreen(
    uiState: PasswordsUiState,
    onAction: (PasswordsAction) -> Unit,
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
                .testTag("passwords_screen")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("passwords_list"),
                contentPadding = PaddingValues(
                    start = 24.dp,
                    top = 16.dp,
                    end = 24.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    VaultTopBar(
                        searchContentDescriptionResId = R.string.categories_search,
                        onSearchClick = {},
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    PasswordsHero(
                        totalPasswords = uiState.totalPasswords
                    )
                }

                item {
                    VaultSearchField(
                        value = uiState.query,
                        onValueChange = { onAction(PasswordsAction.OnSearchQueryChanged(it)) },
                        placeholderResId = R.string.passwords_search_placeholder,
                        testTag = "passwords_search_input"
                    )
                }

                when (val contentState = uiState.contentState) {
                    PasswordsContentState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.testTag("passwords_loading"),
                                    color = ElectricBlue
                                )
                            }
                        }
                    }

                    PasswordsContentState.Content -> {
                        items(
                            items = uiState.filteredPasswords,
                            key = { password -> password.id }
                        ) { password ->
                            PasswordListItem(
                                password = password,
                                onClick = {
                                    onAction(PasswordsAction.OnPasswordClick(password.id))
                                }
                            )
                        }
                    }

                    PasswordsContentState.EmptyPasswords -> {
                        item {
                            PasswordsEmptyState(
                                title = stringResource(R.string.passwords_empty_title),
                                message = stringResource(R.string.passwords_empty_message),
                                modifier = Modifier.testTag("passwords_empty")
                            )
                        }
                    }

                    PasswordsContentState.EmptySearchResult -> {
                        item {
                            PasswordsEmptyState(
                                title = stringResource(R.string.passwords_search_empty_title),
                                message = stringResource(R.string.passwords_search_empty_message),
                                modifier = Modifier.testTag("passwords_search_empty")
                            )
                        }
                    }

                    is PasswordsContentState.Error -> {
                        item {
                            Text(
                                text = stringResource(contentState.messageResId),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("passwords_error"),
                                color = MistText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            VaultGradientFab(
                contentDescription = stringResource(R.string.passwords_create_fab),
                onClick = { onAction(PasswordsAction.OnAddPasswordClick) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
                    .testTag("passwords_create_fab")
            )
        }
    }
}

@Composable
private fun PasswordsHero(
    totalPasswords: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.passwords_title),
            color = SoftWhite,
            fontSize = 36.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.9).sp
        )
        if (totalPasswords > 0) {
            Text(
                text = pluralStringResource(
                    R.plurals.passwords_subtitle,
                    totalPasswords,
                    totalPasswords
                ),
                color = MistText,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PasswordListItem(
    password: PasswordListItemUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SlateBlue.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("password_item_${password.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = DeepNavy,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = password.initials,
                    color = SoftWhite,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = password.title,
                    color = SoftWhite,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (password.supportingText.isNotBlank()) {
                    Text(
                        text = password.supportingText,
                        color = MistText,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
            contentDescription = null,
            tint = GhostOutline,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun PasswordsEmptyState(
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
