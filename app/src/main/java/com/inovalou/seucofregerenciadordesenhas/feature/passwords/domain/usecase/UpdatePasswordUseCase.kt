package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository,
    private val timeProvider: TimeProvider
) {

    suspend operator fun invoke(
        passwordId: Long,
        login: String,
        password: String
    ): UpdatePasswordResult {
        val validation = UpdatePasswordValidation(
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
                    login = login.trim(),
                    password = password,
                    updatedAt = timeProvider.currentTimeMillis()
                )
            )
            UpdatePasswordResult.Success
        } catch (_: Exception) {
            UpdatePasswordResult.Failure
        }
    }
}

data class UpdatePasswordValidation(
    val passwordError: UpdatePasswordPasswordError? = null
) {
    val hasError: Boolean
        get() = passwordError != null
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
