package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState

data class EditPasswordUiState(
    val title: String = "",
    val email: String = "",
    val selectedCategoryId: Long? = null,
    val selectedCategoryName: String? = null,
    val isCategoryDialogVisible: Boolean = false,
    val categorySelectionState: PasswordCategorySelectionUiState =
        PasswordCategorySelectionUiState.Loading,
    val password: String = "",
    val isIdentityCardEditing: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPasswordVisible: Boolean = false,
    val isSaving: Boolean = false,
    @StringRes val categoryErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    val lastCopiedField: EditPasswordCopiedField? = null,
    val contentState: EditPasswordContentState = EditPasswordContentState.Loading,
    val securitySection: EditPasswordSecuritySectionUiState = EditPasswordSecuritySectionUiState(),
    @StringRes val notesResId: Int = R.string.edit_password_notes_mock
)

enum class EditPasswordCopiedField {
    Email,
    Password
}

sealed interface EditPasswordContentState {
    data object Loading : EditPasswordContentState
    data object Content : EditPasswordContentState
    data class Error(@StringRes val messageResId: Int) : EditPasswordContentState
}

data class EditPasswordSecuritySectionUiState(
    val scorePercent: Int = 32,
    @StringRes val riskTitleResId: Int = R.string.edit_password_security_title,
    val tagResIds: List<Int> = listOf(
        R.string.edit_password_security_tag_weak,
        R.string.edit_password_security_tag_duplicate
    ),
    @StringRes val alertResId: Int = R.string.edit_password_security_alert
)

sealed interface EditPasswordEffect {
    data object NavigateBack : EditPasswordEffect
    data class CopyToClipboard(
        val value: String,
        val isSensitive: Boolean
    ) : EditPasswordEffect
}
