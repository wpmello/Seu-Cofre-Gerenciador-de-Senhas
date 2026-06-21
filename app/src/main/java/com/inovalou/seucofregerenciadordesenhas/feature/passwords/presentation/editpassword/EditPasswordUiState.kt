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
    val note: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPasswordVisible: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    @StringRes val categoryErrorResId: Int? = null,
    @StringRes val passwordErrorResId: Int? = null,
    @StringRes val submitErrorResId: Int? = null,
    val lastCopiedField: EditPasswordCopiedField? = null,
    val contentState: EditPasswordContentState = EditPasswordContentState.Loading,
    val localAuthenticationState: EditPasswordLocalAuthenticationState =
        EditPasswordLocalAuthenticationState.Locked,
    val deleteFlowState: EditPasswordDeleteFlowState = EditPasswordDeleteFlowState.Idle,
    val securitySection: EditPasswordSecuritySectionUiState = EditPasswordSecuritySectionUiState()
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

enum class EditPasswordLocalAuthenticationState {
    Locked,
    Authenticating,
    Authenticated,
    Failed,
    Unavailable
}

sealed interface EditPasswordDeleteFlowState {
    data object Idle : EditPasswordDeleteFlowState
    data object Confirmation : EditPasswordDeleteFlowState
}

data class EditPasswordSecuritySectionUiState(
    val scorePercent: Int = 0,
    val visualState: EditPasswordSecurityVisualState = EditPasswordSecurityVisualState.HighRisk,
    @StringRes val riskTitleResId: Int = R.string.edit_password_security_title,
    val tagResIds: List<Int> = emptyList(),
    @StringRes val alertResId: Int = R.string.edit_password_security_alert_high
)

enum class EditPasswordSecurityVisualState {
    HighRisk,
    MediumRisk,
    Safe
}

sealed interface EditPasswordEffect {
    data class NavigateBackToOrigin(val openedFrom: EditPasswordOpenedFrom) : EditPasswordEffect
    data class NavigateAfterSave(val openedFrom: EditPasswordOpenedFrom) : EditPasswordEffect
    data class CopyToClipboard(
        val value: String,
        val isSensitive: Boolean
    ) : EditPasswordEffect
}

sealed interface EditPasswordLocalAuthenticationEffect {
    data object RequestLocalAuthentication : EditPasswordLocalAuthenticationEffect
}
