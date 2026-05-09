package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.ui.theme.vaultColors

sealed interface PasswordCategorySelectionUiState {
    data object Loading : PasswordCategorySelectionUiState
    data object Empty : PasswordCategorySelectionUiState
    data class Content(
        val categories: List<PasswordCategoryOptionUiModel>
    ) : PasswordCategorySelectionUiState
}

data class PasswordCategoryOptionUiModel(
    val id: Long,
    val name: String,
    val isSelected: Boolean
)

@Composable
fun PasswordCategorySelectionDialog(
    state: PasswordCategorySelectionUiState,
    onCategorySelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.new_password_category_dialog_title),
                color = colors.textPrimary
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier.testTag("password_category_dialog")
            ) {
                when (state) {
                    PasswordCategorySelectionUiState.Loading -> {
                        Text(
                            text = stringResource(R.string.new_password_category_dialog_loading),
                            color = colors.textSecondary
                        )
                    }

                    PasswordCategorySelectionUiState.Empty -> {
                        Text(
                            text = stringResource(R.string.new_password_category_dialog_empty),
                            color = colors.textSecondary
                        )
                    }

                    is PasswordCategorySelectionUiState.Content -> {
                        state.categories.forEach { category ->
                            PasswordCategorySelectionRow(
                                category = category,
                                onClick = { onCategorySelected(category.id) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.edit_category_cancel))
            }
        },
        containerColor = colors.surface
    )
}

@Composable
private fun PasswordCategorySelectionRow(
    category: PasswordCategoryOptionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.vaultColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = category.isSelected,
            onClick = onClick
        )
        Text(
            text = category.name,
            color = colors.textPrimary,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

fun List<Category>.toPasswordCategorySelectionState(
    selectedCategoryId: Long?
): PasswordCategorySelectionUiState {
    if (isEmpty()) {
        return PasswordCategorySelectionUiState.Empty
    }

    return PasswordCategorySelectionUiState.Content(
        categories = map { category ->
            PasswordCategoryOptionUiModel(
                id = category.id,
                name = category.name,
                isSelected = category.id == selectedCategoryId
            )
        }
    )
}

fun PasswordCategorySelectionUiState.Content.withSelection(
    selectedCategoryId: Long?
): PasswordCategorySelectionUiState.Content = copy(
    categories = categories.map { category ->
        category.copy(isSelected = category.id == selectedCategoryId)
    }
)
