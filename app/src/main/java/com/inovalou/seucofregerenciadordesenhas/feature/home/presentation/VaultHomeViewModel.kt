package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.text.maskCredentialIdentifierForDisplay
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHome
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomePassword
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomeSecurityPassword
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase.ObserveVaultHomeUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase.ObserveVaultHomeSecurityPasswordsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VaultHomeViewModel @Inject constructor(
    observeVaultHomeUseCase: ObserveVaultHomeUseCase,
    observeVaultHomeSecurityPasswordsUseCase: ObserveVaultHomeSecurityPasswordsUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    private val selectedSecurityFilter = MutableStateFlow<VaultHomeSecurityFilter?>(null)

    private val summaryCardState: StateFlow<VaultHomeSummaryCardState> = selectedSecurityFilter
        .flatMapLatest { filter ->
            if (filter == null) {
                flowOf(VaultHomeSummaryCardState.Overview)
            } else {
                observeVaultHomeSecurityPasswordsUseCase(filter.bucket)
                    .map<List<VaultHomeSecurityPassword>, VaultHomeSummaryCardState> { passwords ->
                        if (passwords.isEmpty()) {
                            VaultHomeSummaryCardState.Empty(filter)
                        } else {
                            VaultHomeSummaryCardState.Content(
                                filter = filter,
                                passwords = passwords.map { password -> password.toUiModel() }
                            )
                        }
                    }
                    .onStart {
                        emit(VaultHomeSummaryCardState.Loading(filter))
                    }
                    .catch {
                        emit(
                            VaultHomeSummaryCardState.Error(
                                filter = filter,
                                messageResId = R.string.vault_home_summary_passwords_error
                            )
                        )
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultHomeSummaryCardState.Overview
        )

    val uiState: StateFlow<VaultHomeUiState> = combine(
        observeVaultHomeUseCase()
            .map<VaultHome, VaultHomeLoadState> { home -> VaultHomeLoadState.Loaded(home) }
            .catch {
                emit(
                    VaultHomeLoadState.Error(
                        messageResId = R.string.vault_home_load_error
                    )
                )
            },
        summaryCardState
    ) { homeState, summaryCardState ->
        when (homeState) {
            is VaultHomeLoadState.Loaded -> homeState.home.toUiState(summaryCardState)
            is VaultHomeLoadState.Error -> VaultHomeUiState(
                contentState = VaultHomeContentState.Error(
                    messageResId = homeState.messageResId
                )
            )
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultHomeUiState()
    )

    private val _effects = Channel<VaultHomeEffect>(Channel.BUFFERED)
    val effects: Flow<VaultHomeEffect> = _effects.receiveAsFlow()

    fun onAction(action: VaultHomeAction) {
        when (action) {
            VaultHomeAction.OnSearchClick -> Unit
            is VaultHomeAction.OnSecuritySummaryTagClick -> {
                selectedSecurityFilter.value = action.filter
            }
            VaultHomeAction.OnSecuritySummaryBackClick -> {
                selectedSecurityFilter.value = null
            }
            is VaultHomeAction.OnSecuritySummaryPasswordClick -> emitEffect(
                VaultHomeEffect.NavigateToPasswordDetails(action.passwordId)
            )
            is VaultHomeAction.OnCategoryClick -> emitEffect(
                VaultHomeEffect.NavigateToCategoryDetails(action.categoryId)
            )
            VaultHomeAction.OnOtherCategoriesClick -> emitEffect(VaultHomeEffect.NavigateToAllCategories)
            VaultHomeAction.OnViewAllPasswordsClick -> emitEffect(VaultHomeEffect.NavigateToPasswords)
            is VaultHomeAction.OnRecentPasswordClick -> emitEffect(
                VaultHomeEffect.NavigateToPasswordDetails(action.passwordId)
            )
            VaultHomeAction.OnAddPasswordClick -> emitEffect(VaultHomeEffect.NavigateToNewPassword)
        }
    }

    private fun emitEffect(effect: VaultHomeEffect) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }

    private fun VaultHome.toUiState(summaryCardState: VaultHomeSummaryCardState): VaultHomeUiState {
        val contentState = if (
            totalPasswords == 0 &&
            categories.isEmpty() &&
            recentPasswords.isEmpty()
        ) {
            VaultHomeContentState.Empty
        } else {
            VaultHomeContentState.Content
        }

        return VaultHomeUiState(
            totalPasswords = totalPasswords,
            weakPasswords = weakPasswords,
            moderatePasswords = moderatePasswords,
            strongPasswords = strongPasswords,
            categories = categories.map { category ->
                VaultHomeCategoryUiModel(
                    id = category.id,
                    name = category.name,
                    iconKey = category.iconKey,
                    iconResId = categoryIconCatalog.resolve(category.iconKey).drawableResId,
                    itemCount = category.itemCount
                )
            },
            showOtherCategories = showOtherCategories,
            recentPasswords = recentPasswords.map { password -> password.toUiModel() },
            summaryCardState = summaryCardState,
            contentState = contentState
        )
    }
}

private sealed interface VaultHomeLoadState {
    data class Loaded(val home: VaultHome) : VaultHomeLoadState
    data class Error(@androidx.annotation.StringRes val messageResId: Int) : VaultHomeLoadState
}

private fun VaultHomePassword.toUiModel(): VaultHomeRecentPasswordUiModel =
    VaultHomeRecentPasswordUiModel(
        id = id,
        title = title,
        supportingText = login.maskCredentialIdentifierForDisplay(),
        initials = title.toInitials(),
        securityLevel = securityRiskLevel.toVaultPasswordListSecurityLevel()
    )

private fun VaultHomeSecurityPassword.toUiModel(): VaultHomeSummaryPasswordUiModel =
    VaultHomeSummaryPasswordUiModel(
        id = id,
        title = title,
        supportingText = login.maskCredentialIdentifierForDisplay(),
        initials = title.toInitials(),
        bucket = bucket,
        securityLevel = bucket.toVaultPasswordListSecurityLevel(),
        scorePercent = scorePercent
    )

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

private fun PasswordSecurityRiskLevel.toVaultPasswordListSecurityLevel(): VaultPasswordListSecurityLevel =
    when (this) {
        PasswordSecurityRiskLevel.High -> VaultPasswordListSecurityLevel.Weak
        PasswordSecurityRiskLevel.Medium -> VaultPasswordListSecurityLevel.Moderate
        PasswordSecurityRiskLevel.Low -> VaultPasswordListSecurityLevel.Safe
    }

private fun PasswordSecurityBucket.toVaultPasswordListSecurityLevel(): VaultPasswordListSecurityLevel =
    when (this) {
        PasswordSecurityBucket.Weak -> VaultPasswordListSecurityLevel.Weak
        PasswordSecurityBucket.Moderate -> VaultPasswordListSecurityLevel.Moderate
        PasswordSecurityBucket.Safe -> VaultPasswordListSecurityLevel.Safe
    }
