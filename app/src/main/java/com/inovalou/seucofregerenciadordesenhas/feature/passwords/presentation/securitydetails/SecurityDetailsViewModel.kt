package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.securitydetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityDetailsItem
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObserveVaultSecurityDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SecurityDetailsViewModel @Inject constructor(
    observeVaultSecurityDetailsUseCase: ObserveVaultSecurityDetailsUseCase
) : ViewModel() {

    private val selectedTab = MutableStateFlow(SecurityDetailsTab.Weak)

    val uiState: StateFlow<SecurityDetailsUiState> = observeVaultSecurityDetailsUseCase()
        .combine(selectedTab) { details, tab ->
            details.toUiState(selectedTab = tab)
        }
        .catch {
            emit(
                SecurityDetailsUiState(
                    contentState = SecurityDetailsContentState.Error(
                        messageResId = R.string.security_details_load_error
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SecurityDetailsUiState()
        )

    private val _effects = MutableSharedFlow<SecurityDetailsEffect>()
    val effects: SharedFlow<SecurityDetailsEffect> = _effects.asSharedFlow()

    fun onAction(action: SecurityDetailsAction) {
        when (action) {
            SecurityDetailsAction.OnBackClick -> {
                viewModelScope.launch {
                    _effects.emit(SecurityDetailsEffect.NavigateBack)
                }
            }

            is SecurityDetailsAction.OnPasswordClick -> {
                viewModelScope.launch {
                    _effects.emit(SecurityDetailsEffect.OpenPassword(action.passwordId))
                }
            }

            is SecurityDetailsAction.OnTabSelected -> {
                selectedTab.value = action.tab
            }
        }
    }
}

private fun VaultSecurityDetails.toUiState(selectedTab: SecurityDetailsTab): SecurityDetailsUiState {
    val visiblePasswords = passwordsFor(selectedTab.bucket).map { password ->
        password.toUiModel()
    }

    return SecurityDetailsUiState(
        contentState = if (totalPasswords == 0) {
            SecurityDetailsContentState.Empty
        } else {
            SecurityDetailsContentState.Content
        },
        selectedTab = selectedTab,
        overallScorePercent = averageScorePercent,
        statusResId = status.toStatusResId(),
        visualState = status.toVisualState(),
        totalPasswords = totalPasswords,
        tabs = SecurityDetailsTab.entries.map { tab ->
            SecurityDetailsTabUiModel(
                tab = tab,
                count = countFor(tab.bucket)
            )
        },
        visiblePasswords = visiblePasswords
    )
}

private fun PasswordSecurityDetailsItem.toUiModel(): SecurityDetailsPasswordUiModel =
    SecurityDetailsPasswordUiModel(
        id = id,
        title = title,
        supportingText = login,
        initials = title.toInitials(),
        scorePercent = scorePercent,
        visualState = bucket.toVisualState(),
        tagResIds = tags.map { tag -> tag.toTagResId() }
    )

private fun VaultSecurityStatus.toStatusResId(): Int = when (this) {
    VaultSecurityStatus.Poor -> R.string.categories_security_poor
    VaultSecurityStatus.Moderate -> R.string.categories_security_moderate
    VaultSecurityStatus.Excellent -> R.string.categories_security_excellent
}

private fun VaultSecurityStatus.toVisualState(): SecurityDetailsVisualState = when (this) {
    VaultSecurityStatus.Poor -> SecurityDetailsVisualState.Poor
    VaultSecurityStatus.Moderate -> SecurityDetailsVisualState.Moderate
    VaultSecurityStatus.Excellent -> SecurityDetailsVisualState.Excellent
}

private fun PasswordSecurityBucket.toVisualState(): SecurityDetailsVisualState = when (this) {
    PasswordSecurityBucket.Weak -> SecurityDetailsVisualState.Poor
    PasswordSecurityBucket.Moderate -> SecurityDetailsVisualState.Moderate
    PasswordSecurityBucket.Safe -> SecurityDetailsVisualState.Excellent
}

private fun PasswordSecurityTag.toTagResId(): Int = when (this) {
    PasswordSecurityTag.Weak -> R.string.edit_password_security_tag_weak
    PasswordSecurityTag.Duplicate -> R.string.edit_password_security_tag_duplicate
    PasswordSecurityTag.Safe -> R.string.edit_password_security_tag_safe
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
