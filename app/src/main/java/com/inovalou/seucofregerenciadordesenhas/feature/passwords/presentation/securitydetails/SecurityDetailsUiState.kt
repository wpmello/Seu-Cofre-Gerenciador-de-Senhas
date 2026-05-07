package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

import androidx.annotation.StringRes
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket

data class SecurityDetailsUiState(
    val contentState: SecurityDetailsContentState = SecurityDetailsContentState.Loading,
    val selectedTab: SecurityDetailsTab = SecurityDetailsTab.Weak,
    val overallScorePercent: Int = 0,
    @StringRes val statusResId: Int = R.string.categories_security_excellent,
    val visualState: SecurityDetailsVisualState = SecurityDetailsVisualState.Excellent,
    val totalPasswords: Int = 0,
    val criteria: List<SecurityDetailsCriteriaUiModel> = defaultSecurityDetailsCriteria(),
    val tabs: List<SecurityDetailsTabUiModel> = SecurityDetailsTab.entries.map { tab ->
        SecurityDetailsTabUiModel(tab = tab, count = 0)
    },
    val visiblePasswords: List<SecurityDetailsPasswordUiModel> = emptyList()
)

sealed interface SecurityDetailsContentState {
    data object Loading : SecurityDetailsContentState
    data object Empty : SecurityDetailsContentState
    data object Content : SecurityDetailsContentState
    data class Error(@StringRes val messageResId: Int) : SecurityDetailsContentState
}

enum class SecurityDetailsTab(
    val bucket: PasswordSecurityBucket,
    @StringRes val labelResId: Int
) {
    Weak(
        bucket = PasswordSecurityBucket.Weak,
        labelResId = R.string.security_details_tab_weak
    ),
    Moderate(
        bucket = PasswordSecurityBucket.Moderate,
        labelResId = R.string.security_details_tab_moderate
    ),
    Safe(
        bucket = PasswordSecurityBucket.Safe,
        labelResId = R.string.security_details_tab_strong
    )
}

enum class SecurityDetailsVisualState {
    Poor,
    Moderate,
    Excellent
}

data class SecurityDetailsCriteriaUiModel(
    @StringRes val labelResId: Int,
    @StringRes val rangeResId: Int,
    val visualState: SecurityDetailsVisualState
)

data class SecurityDetailsTabUiModel(
    val tab: SecurityDetailsTab,
    val count: Int
)

data class SecurityDetailsPasswordUiModel(
    val id: Long,
    val title: String,
    val supportingText: String,
    val initials: String,
    val scorePercent: Int,
    val visualState: SecurityDetailsVisualState,
    val tagResIds: List<Int>
)

sealed interface SecurityDetailsEffect {
    data object NavigateBack : SecurityDetailsEffect
    data class OpenPassword(val passwordId: Long) : SecurityDetailsEffect
}

private fun defaultSecurityDetailsCriteria(): List<SecurityDetailsCriteriaUiModel> = listOf(
    SecurityDetailsCriteriaUiModel(
        labelResId = R.string.categories_security_poor,
        rangeResId = R.string.security_details_range_poor,
        visualState = SecurityDetailsVisualState.Poor
    ),
    SecurityDetailsCriteriaUiModel(
        labelResId = R.string.categories_security_moderate,
        rangeResId = R.string.security_details_range_moderate,
        visualState = SecurityDetailsVisualState.Moderate
    ),
    SecurityDetailsCriteriaUiModel(
        labelResId = R.string.categories_security_excellent,
        rangeResId = R.string.security_details_range_excellent,
        visualState = SecurityDetailsVisualState.Excellent
    )
)
