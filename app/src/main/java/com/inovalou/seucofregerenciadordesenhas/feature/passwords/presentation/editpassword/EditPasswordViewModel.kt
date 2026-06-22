package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.text.TextInputLimits
import com.inovalou.seucofregerenciadordesenhas.core.text.limitToMaxCharacters
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.ObserveCategoriesUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.AnalyzePasswordSecurityUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.CreatePasswordCategoryError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.DeletePasswordByIdUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.DeletePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.GetPasswordDetailsUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordPasswordError
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase.UpdatePasswordUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.PasswordCategorySelectionUiState
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.toPasswordCategorySelectionState
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.shared.withSelection
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class EditPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPasswordDetailsUseCase: GetPasswordDetailsUseCase,
    private val observeCategoriesUseCase: ObserveCategoriesUseCase,
    private val analyzePasswordSecurityUseCase: AnalyzePasswordSecurityUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val deletePasswordByIdUseCase: DeletePasswordByIdUseCase
) : ViewModel() {

    private val passwordId = savedStateHandle.get<Long>(EditPasswordDestination.passwordIdArg)
    private val openedFrom = EditPasswordOpenedFrom.fromRouteValue(
        savedStateHandle.get<String>(EditPasswordDestination.openedFromArg)
    )

    private val _uiState = MutableStateFlow(EditPasswordUiState())
    val uiState: StateFlow<EditPasswordUiState> = _uiState.asStateFlow()

    private val _effects = Channel<EditPasswordEffect>(Channel.BUFFERED)
    val effects: Flow<EditPasswordEffect> = _effects.receiveAsFlow()
    private val _localAuthenticationEffects =
        Channel<EditPasswordLocalAuthenticationEffect>(Channel.BUFFERED)
    val localAuthenticationEffects: Flow<EditPasswordLocalAuthenticationEffect> =
        _localAuthenticationEffects.receiveAsFlow()
    private val passwordAnalysisRequests = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var hasStartedUnlockedContent = false

    init {
        requestLocalAuthentication()
    }

    fun onAction(action: EditPasswordAction) {
        when (action) {
            EditPasswordAction.OnBackClick -> navigateBack()
            EditPasswordAction.OnLocalAuthenticationSucceeded -> unlockAndLoadContent()
            EditPasswordAction.OnLocalAuthenticationCancelled,
            EditPasswordAction.OnLocalAuthenticationFailed -> markAuthenticationFailed()
            EditPasswordAction.OnLocalAuthenticationUnavailable -> markAuthenticationUnavailable()
            EditPasswordAction.OnLocalAuthenticationRetryClick -> requestLocalAuthentication()
            is EditPasswordAction.OnTitleChanged -> updateTitle(action.title)
            is EditPasswordAction.OnEmailChanged -> updateEmail(action.email)
            EditPasswordAction.OnCategoryFieldClick -> openCategoryDialog()
            EditPasswordAction.OnCategoryDialogDismissed -> closeCategoryDialog()
            is EditPasswordAction.OnCategorySelected -> selectCategory(action.categoryId)
            is EditPasswordAction.OnPasswordChanged -> updatePassword(action.password)
            is EditPasswordAction.OnNoteChanged -> updateNote(action.note)
            EditPasswordAction.OnTogglePasswordVisibility -> togglePasswordVisibility()
            EditPasswordAction.OnCopyEmailClick -> copyEmail()
            EditPasswordAction.OnCopyPasswordClick -> copyPassword()
            EditPasswordAction.OnSuggestStrongPasswordClick -> showComingSoonDialog(
                EditPasswordComingSoonDialog.SuggestPassword
            )
            EditPasswordAction.OnPasswordHistoryClick -> showComingSoonDialog(
                EditPasswordComingSoonDialog.History
            )
            EditPasswordAction.OnComingSoonDialogDismissed -> dismissComingSoonDialog()
            EditPasswordAction.OnSaveClick -> saveChanges()
            EditPasswordAction.OnDeleteClick -> showDeleteConfirmation()
            EditPasswordAction.OnDeleteDialogDismissed -> dismissDeleteConfirmation()
            EditPasswordAction.OnDeleteConfirmed -> deletePassword()
        }
    }

    private fun requestLocalAuthentication() {
        if (_uiState.value.localAuthenticationState == EditPasswordLocalAuthenticationState.Authenticated) {
            return
        }

        _uiState.update {
            it.copy(localAuthenticationState = EditPasswordLocalAuthenticationState.Authenticating)
        }
        viewModelScope.launch {
            _localAuthenticationEffects.send(
                EditPasswordLocalAuthenticationEffect.RequestLocalAuthentication
            )
        }
    }

    private fun unlockAndLoadContent() {
        if (hasStartedUnlockedContent) {
            return
        }
        hasStartedUnlockedContent = true
        _uiState.update {
            it.copy(localAuthenticationState = EditPasswordLocalAuthenticationState.Authenticated)
        }
        observeCategoriesUseCase()
            .collectIntoUiState()
        observePasswordSecurityAnalysisRequests()
        loadPassword()
    }

    private fun markAuthenticationFailed() {
        if (_uiState.value.localAuthenticationState == EditPasswordLocalAuthenticationState.Authenticated) {
            return
        }
        _uiState.update {
            it.copy(localAuthenticationState = EditPasswordLocalAuthenticationState.Failed)
        }
    }

    private fun markAuthenticationUnavailable() {
        if (_uiState.value.localAuthenticationState == EditPasswordLocalAuthenticationState.Authenticated) {
            return
        }
        _uiState.update {
            it.copy(localAuthenticationState = EditPasswordLocalAuthenticationState.Unavailable)
        }
    }

    private fun isUnlocked(): Boolean =
        _uiState.value.localAuthenticationState == EditPasswordLocalAuthenticationState.Authenticated

    private fun loadPassword() {
        if (!isUnlocked()) {
            return
        }
        val resolvedPasswordId = passwordId
        if (resolvedPasswordId == null) {
            _uiState.update {
                it.copy(
                    contentState = EditPasswordContentState.Error(
                        R.string.edit_password_load_error
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            val password = getPasswordDetailsUseCase(resolvedPasswordId)
            if (password == null) {
                _uiState.update {
                    it.copy(
                        contentState = EditPasswordContentState.Error(
                            R.string.edit_password_not_found_error
                        )
                    )
                }
                return@launch
            }

            _uiState.update {
                val updatedCategorySelectionState = when (val selectionState = it.categorySelectionState) {
                    is PasswordCategorySelectionUiState.Content -> {
                        selectionState.withSelection(password.categoryId)
                    }
                    PasswordCategorySelectionUiState.Empty,
                    PasswordCategorySelectionUiState.Loading -> selectionState
                }
                it.copy(
                    title = password.title,
                    email = password.login,
                    selectedCategoryId = password.categoryId,
                    selectedCategoryName = password.categoryName,
                    categorySelectionState = updatedCategorySelectionState,
                    password = password.password,
                    note = password.note.orEmpty(),
                    createdAt = password.createdAt,
                    updatedAt = password.updatedAt,
                    categoryErrorResId = if (
                        password.categoryId == null && !password.categoryName.isNullOrBlank()
                    ) {
                        R.string.edit_password_category_error_invalid
                    } else {
                        null
                    },
                    contentState = EditPasswordContentState.Content
                )
            }
            analyzePasswordSecurity(password.password)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _effects.send(EditPasswordEffect.NavigateBackToOrigin(openedFrom))
        }
    }

    private fun updateEmail(email: String) {
        if (!isUnlocked()) {
            return
        }
        _uiState.update {
            it.copy(
                email = email.limitToMaxCharacters(TextInputLimits.EMAIL_MAX_LENGTH),
                submitErrorResId = null
            )
        }
    }

    private fun openCategoryDialog() {
        if (!isUnlocked()) {
            return
        }
        _uiState.update { state ->
            state.copy(isCategoryDialogVisible = true)
        }
    }

    private fun closeCategoryDialog() {
        if (!isUnlocked()) {
            return
        }
        _uiState.update { state ->
            state.copy(isCategoryDialogVisible = false)
        }
    }

    private fun selectCategory(categoryId: Long) {
        if (!isUnlocked()) {
            return
        }
        val selectionState = _uiState.value.categorySelectionState
        if (selectionState !is PasswordCategorySelectionUiState.Content) {
            return
        }

        val selectedCategory = selectionState.categories.firstOrNull { it.id == categoryId } ?: return
        _uiState.update { state ->
            state.copy(
                selectedCategoryId = selectedCategory.id,
                selectedCategoryName = selectedCategory.name,
                isCategoryDialogVisible = false,
                categorySelectionState = selectionState.withSelection(selectedCategory.id),
                categoryErrorResId = null,
                submitErrorResId = null
            )
        }
    }

    private fun updateTitle(title: String) {
        if (!isUnlocked()) {
            return
        }
        _uiState.update {
            it.copy(
                title = title.limitToMaxCharacters(TextInputLimits.NAME_MAX_LENGTH),
                submitErrorResId = null
            )
        }
    }

    private fun updatePassword(password: String) {
        if (!isUnlocked()) {
            return
        }
        _uiState.update {
            it.copy(
                password = password,
                passwordErrorResId = null,
                submitErrorResId = null
            )
        }
        passwordAnalysisRequests.tryEmit(password)
    }

    private fun updateNote(note: String) {
        if (!isUnlocked()) {
            return
        }
        _uiState.update {
            it.copy(
                note = note.limitToMaxCharacters(TextInputLimits.NOTE_MAX_LENGTH),
                submitErrorResId = null
            )
        }
    }

    private fun togglePasswordVisibility() {
        if (!isUnlocked()) {
            return
        }
        _uiState.update { state ->
            state.copy(isPasswordVisible = !state.isPasswordVisible)
        }
    }

    private fun copyEmail() {
        if (!isUnlocked()) {
            return
        }
        val email = _uiState.value.email
        if (email.isBlank()) {
            return
        }

        _uiState.update { it.copy(lastCopiedField = EditPasswordCopiedField.Email) }
        viewModelScope.launch {
            _effects.send(EditPasswordEffect.CopyToClipboard(email, isSensitive = false))
        }
    }

    private fun copyPassword() {
        if (!isUnlocked()) {
            return
        }
        val password = _uiState.value.password
        if (password.isBlank()) {
            return
        }

        _uiState.update { it.copy(lastCopiedField = EditPasswordCopiedField.Password) }
        viewModelScope.launch {
            _effects.send(EditPasswordEffect.CopyToClipboard(password, isSensitive = true))
        }
    }

    private fun saveChanges() {
        if (!isUnlocked()) {
            return
        }
        val resolvedPasswordId = passwordId ?: return
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.isDeleting) {
            return
        }
        _uiState.update {
            it.copy(
                isSaving = true,
                categoryErrorResId = null,
                passwordErrorResId = null,
                submitErrorResId = null
            )
        }

        viewModelScope.launch {
            when (
                val result = updatePasswordUseCase(
                    passwordId = resolvedPasswordId,
                    title = currentState.title,
                    login = currentState.email,
                    categoryId = currentState.selectedCategoryId,
                    categoryName = currentState.selectedCategoryName,
                    password = currentState.password,
                    note = currentState.note
                )
            ) {
                UpdatePasswordResult.Success -> {
                    _effects.send(EditPasswordEffect.NavigateAfterSave(openedFrom))
                }

                UpdatePasswordResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            contentState = EditPasswordContentState.Error(
                                R.string.edit_password_not_found_error
                            )
                        )
                    }
                }

                UpdatePasswordResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            submitErrorResId = R.string.edit_password_save_error
                        )
                    }
                }

                is UpdatePasswordResult.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            categoryErrorResId = result.validation.categoryError.toCategoryErrorResId(),
                            passwordErrorResId = result.validation.passwordError.toPasswordErrorResId()
                        )
                    }
                }
            }
        }
    }

    private fun deletePassword() {
        if (!isUnlocked()) {
            return
        }
        val resolvedPasswordId = passwordId ?: return
        val currentState = _uiState.value
        if (
            currentState.isDeleting ||
            currentState.isSaving ||
            currentState.deleteFlowState !is EditPasswordDeleteFlowState.Confirmation
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isDeleting = true,
                deleteFlowState = EditPasswordDeleteFlowState.Idle,
                categoryErrorResId = null,
                passwordErrorResId = null,
                submitErrorResId = null
            )
        }

        viewModelScope.launch {
            when (deletePasswordByIdUseCase(resolvedPasswordId)) {
                DeletePasswordResult.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    _effects.send(EditPasswordEffect.NavigateBackToOrigin(openedFrom))
                }
                DeletePasswordResult.NotFound -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteFlowState = EditPasswordDeleteFlowState.Idle,
                            contentState = EditPasswordContentState.Error(
                                R.string.edit_password_not_found_error
                            )
                        )
                    }
                }
                DeletePasswordResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteFlowState = EditPasswordDeleteFlowState.Idle,
                            submitErrorResId = R.string.edit_password_delete_error
                        )
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmation() {
        if (!isUnlocked()) {
            return
        }
        val currentState = _uiState.value
        if (currentState.isDeleting || currentState.isSaving) {
            return
        }

        _uiState.update {
            it.copy(
                deleteFlowState = EditPasswordDeleteFlowState.Confirmation,
                submitErrorResId = null
            )
        }
    }

    private fun dismissDeleteConfirmation() {
        if (!isUnlocked()) {
            return
        }
        if (_uiState.value.isDeleting) {
            return
        }

        _uiState.update {
            it.copy(deleteFlowState = EditPasswordDeleteFlowState.Idle)
        }
    }

    private fun showComingSoonDialog(dialog: EditPasswordComingSoonDialog) {
        if (!isUnlocked()) {
            return
        }

        _uiState.update {
            it.copy(comingSoonDialog = dialog)
        }
    }

    private fun dismissComingSoonDialog() {
        if (!isUnlocked()) {
            return
        }

        _uiState.update {
            it.copy(comingSoonDialog = null)
        }
    }

    private fun analyzePasswordSecurity(password: String) {
        if (!isUnlocked()) {
            return
        }
        viewModelScope.launch {
            val analysis = analyzePasswordSecurityUseCase(
                password = password,
                currentPasswordId = passwordId
            )
            _uiState.update { state ->
                if (state.password != password) {
                    state
                } else {
                    state.copy(
                        securitySection = analysis.toEditPasswordSecuritySectionUiState()
                    )
                }
            }
        }
    }

    private fun observePasswordSecurityAnalysisRequests() {
        passwordAnalysisRequests
            .debounce(PASSWORD_SECURITY_ANALYSIS_DEBOUNCE_MILLIS)
            .distinctUntilChanged()
            .mapLatest { password ->
                password to analyzePasswordSecurityUseCase(
                    password = password,
                    currentPasswordId = passwordId
                )
            }
            .onEach { (password, analysis) ->
                _uiState.update { state ->
                    if (state.password != password) {
                        state
                    } else {
                        state.copy(
                            securitySection = analysis.toEditPasswordSecuritySectionUiState()
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun kotlinx.coroutines.flow.Flow<List<Category>>.collectIntoUiState() {
        viewModelScope.launch {
            collect { categories ->
                _uiState.update { state ->
                    val resolvedSelectedCategory = categories.firstOrNull {
                        it.id == state.selectedCategoryId
                    }
                    val resolvedCategoryName = resolvedSelectedCategory?.name ?: state.selectedCategoryName
                    state.copy(
                        selectedCategoryId = resolvedSelectedCategory?.id,
                        selectedCategoryName = resolvedCategoryName,
                        categorySelectionState = categories.toPasswordCategorySelectionState(
                            selectedCategoryId = resolvedSelectedCategory?.id
                        )
                    )
                }
            }
        }
    }
}

private fun CreatePasswordCategoryError?.toCategoryErrorResId(): Int? = when (this) {
    CreatePasswordCategoryError.Missing -> R.string.edit_password_category_error_missing
    CreatePasswordCategoryError.Invalid -> R.string.edit_password_category_error_invalid
    null -> null
}

private fun UpdatePasswordPasswordError?.toPasswordErrorResId(): Int? = when (this) {
    UpdatePasswordPasswordError.Blank -> R.string.edit_password_password_error_blank
    null -> null
}

private const val PASSWORD_SECURITY_ANALYSIS_DEBOUNCE_MILLIS = 300L
