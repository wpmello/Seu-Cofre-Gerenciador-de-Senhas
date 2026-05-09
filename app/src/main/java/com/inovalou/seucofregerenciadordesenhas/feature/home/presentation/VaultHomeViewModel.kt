package com.inovalou.seucofregerenciadordesenhas.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.ui.component.VaultPasswordListSecurityLevel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHome
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.model.VaultHomePassword
import com.inovalou.seucofregerenciadordesenhas.feature.home.domain.usecase.ObserveVaultHomeUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class VaultHomeViewModel @Inject constructor(
    observeVaultHomeUseCase: ObserveVaultHomeUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    val uiState: StateFlow<VaultHomeUiState> = observeVaultHomeUseCase()
        .map { home -> home.toUiState() }
        .catch {
            emit(
                VaultHomeUiState(
                    contentState = VaultHomeContentState.Error(
                        messageResId = R.string.vault_home_load_error
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = VaultHomeUiState()
        )

    private val _effects = MutableSharedFlow<VaultHomeEffect>()
    val effects: SharedFlow<VaultHomeEffect> = _effects.asSharedFlow()

    fun onAction(action: VaultHomeAction) {
        when (action) {
            VaultHomeAction.OnSearchClick -> Unit
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
            _effects.emit(effect)
        }
    }

    private fun VaultHome.toUiState(): VaultHomeUiState {
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
            contentState = contentState
        )
    }
}

private fun VaultHomePassword.toUiModel(): VaultHomeRecentPasswordUiModel =
    VaultHomeRecentPasswordUiModel(
        id = id,
        title = title,
        supportingText = login,
        initials = title.toInitials(),
        securityLevel = securityRiskLevel.toVaultPasswordListSecurityLevel()
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
