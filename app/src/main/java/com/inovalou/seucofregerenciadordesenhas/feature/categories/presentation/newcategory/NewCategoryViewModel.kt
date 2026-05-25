package com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.newcategory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inovalou.seucofregerenciadordesenhas.R
import com.inovalou.seucofregerenciadordesenhas.core.text.TextInputLimits
import com.inovalou.seucofregerenciadordesenhas.core.text.limitToMaxCharacters
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.CreateCategoryIconError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.CreateCategoryNameError
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.CreateCategoryResult
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.usecase.CreateCategoryUseCase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.component.CategorySelectableIconUiModel
import com.inovalou.seucofregerenciadordesenhas.feature.categories.presentation.icon.CategoryIconCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NewCategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val categoryIconCatalog: CategoryIconCatalog
) : ViewModel() {

    private val iconOptions = categoryIconCatalog.all()
    private val defaultIconKey = iconOptions.firstOrNull()?.iconKey
    private val openedFrom = NewCategoryOpenedFrom.fromRouteValue(
        savedStateHandle[NewCategoryDestination.openedFromArg]
    )

    private val _uiState = MutableStateFlow(
        NewCategoryUiState(
            availableIcons = iconOptions.map { option ->
                CategorySelectableIconUiModel(
                    iconKey = option.iconKey,
                    iconResId = option.drawableResId,
                    isSelected = option.iconKey == defaultIconKey
                )
            },
            selectedIconKey = defaultIconKey
        )
    )
    val uiState: StateFlow<NewCategoryUiState> = _uiState.asStateFlow()

    private val _effects = Channel<NewCategoryEffect>(Channel.BUFFERED)
    val effects: Flow<NewCategoryEffect> = _effects.receiveAsFlow()

    fun onAction(action: NewCategoryAction) {
        when (action) {
            NewCategoryAction.OnBackClick -> navigateBackToOrigin()
            is NewCategoryAction.OnNameChanged -> onNameChanged(action.name)
            is NewCategoryAction.OnIconSelected -> onIconSelected(action.iconKey)
            NewCategoryAction.OnCreateCategoryClick -> createCategory()
        }
    }

    private fun navigateBackToOrigin() {
        viewModelScope.launch {
            _effects.send(NewCategoryEffect.NavigateBackToOrigin(openedFrom))
        }
    }

    private fun onNameChanged(name: String) {
        _uiState.update { state ->
            state.copy(
                name = name.limitToMaxCharacters(TextInputLimits.NAME_MAX_LENGTH),
                nameErrorResId = null,
                submitErrorResId = null
            )
        }
    }

    private fun onIconSelected(iconKey: String) {
        _uiState.update { state ->
            state.copy(
                selectedIconKey = iconKey,
                iconErrorResId = null,
                submitErrorResId = null,
                availableIcons = state.availableIcons.map { icon ->
                    icon.copy(isSelected = icon.iconKey == iconKey)
                }
            )
        }
    }

    private fun createCategory() {
        val currentState = _uiState.value
        if (currentState.isSaving) {
            return
        }
        _uiState.update { state ->
            state.copy(
                isSaving = true,
                nameErrorResId = null,
                iconErrorResId = null,
                submitErrorResId = null
            )
        }

        viewModelScope.launch {
            when (val result = createCategoryUseCase(
                name = currentState.name,
                iconKey = currentState.selectedIconKey
            )) {
                CreateCategoryResult.Success -> {
                    _effects.send(NewCategoryEffect.NavigateBackToOrigin(openedFrom))
                }

                CreateCategoryResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            submitErrorResId = R.string.new_category_create_error
                        )
                    }
                }

                is CreateCategoryResult.ValidationError -> {
                    _uiState.update { state ->
                        state.copy(
                            isSaving = false,
                            nameErrorResId = result.validation.nameError.toNameErrorResId(),
                            iconErrorResId = result.validation.iconError.toIconErrorResId()
                        )
                    }
                }
            }
        }
    }
}

private fun CreateCategoryNameError?.toNameErrorResId(): Int? = when (this) {
    CreateCategoryNameError.Blank -> R.string.new_category_name_error_blank
    null -> null
}

private fun CreateCategoryIconError?.toIconErrorResId(): Int? = when (this) {
    CreateCategoryIconError.Missing -> R.string.new_category_icon_error_missing
    null -> null
}
