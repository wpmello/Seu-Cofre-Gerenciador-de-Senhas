package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel

data class VaultHomeUiState(
    val totalPasswords: Int = 0,
    val weakPasswords: Int = 0,
    val moderatePasswords: Int = 0,
    val strongPasswords: Int = 0,
    val categories: List<VaultHomeCategoryUiModel> = emptyList(),
    val showOtherCategories: Boolean = false,
    val recentPasswords: List<VaultHomeRecentPasswordUiModel> = emptyList(),
    val contentState: VaultHomeContentState = VaultHomeContentState.Loading
)

data class VaultHomeCategoryUiModel(
    val id: Long,
    val name: String,
    val iconKey: String,
    @DrawableRes val iconResId: Int,
    val itemCount: Int
)

data class VaultHomeRecentPasswordUiModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val initials: String,
    val securityLevel: VaultPasswordListSecurityLevel
)

sealed interface VaultHomeContentState {
    data object Loading : VaultHomeContentState
    data object Content : VaultHomeContentState
    data object Empty : VaultHomeContentState
    data class Error(@StringRes val messageResId: Int = R.string.vault_home_load_error) : VaultHomeContentState
}

sealed interface VaultHomeEffect {
    data class NavigateToCategoryDetails(val categoryId: Long) : VaultHomeEffect
    data object NavigateToAllCategories : VaultHomeEffect
    data object NavigateToPasswords : VaultHomeEffect
    data class NavigateToPasswordDetails(val passwordId: Long) : VaultHomeEffect
    data object NavigateToNewPassword : VaultHomeEffect
}
