package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.ObservePasswordsUseCase
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
class PasswordsViewModel @Inject constructor(
    observePasswordsUseCase: ObservePasswordsUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<PasswordsUiState> = combine(
        observePasswordsUseCase(),
        searchQuery
    ) { passwords, query ->
        passwords.toUiState(query = query)
    }
        .catch {
            emit(
                PasswordsUiState(
                    contentState = PasswordsContentState.Error(
                        messageResId = R.string.passwords_load_error
                    )
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PasswordsUiState()
        )

    private val _effects = MutableSharedFlow<PasswordsEffect>()
    val effects: SharedFlow<PasswordsEffect> = _effects.asSharedFlow()

    fun onAction(action: PasswordsAction) {
        when (action) {
            is PasswordsAction.OnSearchQueryChanged -> {
                searchQuery.value = action.query
            }

            is PasswordsAction.OnPasswordClick -> {
                viewModelScope.launch {
                    _effects.emit(
                        PasswordsEffect.OpenPasswordDetails(passwordId = action.passwordId)
                    )
                }
            }

            PasswordsAction.OnAddPasswordClick -> {
                viewModelScope.launch {
                    _effects.emit(PasswordsEffect.NavigateToNewPassword)
                }
            }
        }
    }
}

private fun List<PasswordSummary>.toUiState(
    query: String
): PasswordsUiState {
    val allPasswords = map { password ->
        PasswordListItemUiModel(
            id = password.id,
            title = password.title,
            supportingText = password.login.ifBlank { password.category },
            initials = password.title.toInitials()
        )
    }
    val normalizedQuery = query.trim()
    val filteredPasswords = if (normalizedQuery.isBlank()) {
        allPasswords
    } else {
        allPasswords.filter { password ->
            password.title.contains(normalizedQuery, ignoreCase = true) ||
                password.supportingText.contains(normalizedQuery, ignoreCase = true)
        }
    }

    val contentState = when {
        allPasswords.isEmpty() -> PasswordsContentState.EmptyPasswords
        filteredPasswords.isEmpty() -> PasswordsContentState.EmptySearchResult
        else -> PasswordsContentState.Content
    }

    return PasswordsUiState(
        query = query,
        allPasswords = allPasswords,
        filteredPasswords = filteredPasswords,
        totalPasswords = allPasswords.size,
        contentState = contentState
    )
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
