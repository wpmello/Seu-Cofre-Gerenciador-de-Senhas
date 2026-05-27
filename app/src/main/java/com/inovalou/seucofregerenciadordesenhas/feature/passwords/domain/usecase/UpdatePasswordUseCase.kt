package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class UpdatePasswordUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val categoryRepository: CategoryRepository,
    private val generatePasswordTitleUseCase: GeneratePasswordTitleUseCase,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(
        passwordId: Long,
        title: String,
        login: String,
        categoryId: Long?,
        categoryName: String?,
        password: String,
        note: String? = null
    ): UpdatePasswordResult {
        val persistedCategory = when {
            categoryId == null -> null
            else -> categoryRepository.getCategoryById(categoryId)
        }
        val validation = UpdatePasswordValidation(
            categoryError = when {
                categoryId == null && categoryName.isNullOrBlank() -> CreatePasswordCategoryError.Missing
                categoryId == null && !categoryName.isNullOrBlank() -> CreatePasswordCategoryError.Invalid
                categoryId != null && persistedCategory == null -> CreatePasswordCategoryError.Invalid
                else -> null
            },
            passwordError = if (password.isBlank()) {
                UpdatePasswordPasswordError.Blank
            } else {
                null
            }
        )

        if (validation.hasError) {
            return UpdatePasswordResult.ValidationError(validation)
        }

        val currentPassword = passwordRepository.getPasswordDetails(passwordId)
            ?: return UpdatePasswordResult.NotFound

        return try {
            passwordRepository.updatePassword(
                currentPassword.copy(
                    title = generatePasswordTitleUseCase(title),
                    login = login.trim(),
                    categoryId = persistedCategory?.id,
                    categoryName = persistedCategory?.name,
                    password = password,
                    note = note?.trim()?.takeIf { it.isNotEmpty() },
                    updatedAt = timeProvider.currentTimeMillis()
                )
            )
            persistedCategory?.id?.let { categoryRepository.touchCategory(it) }
            UpdatePasswordResult.Success
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            UpdatePasswordResult.Failure
        }
    }
}

data class UpdatePasswordValidation(
    val categoryError: CreatePasswordCategoryError? = null,
    val passwordError: UpdatePasswordPasswordError? = null
) {
    val hasError: Boolean
        get() = categoryError != null || passwordError != null
}

enum class UpdatePasswordPasswordError {
    Blank
}

sealed interface UpdatePasswordResult {
    data object Success : UpdatePasswordResult
    data class ValidationError(
        val validation: UpdatePasswordValidation
    ) : UpdatePasswordResult
    data object NotFound : UpdatePasswordResult
    data object Failure : UpdatePasswordResult
}
