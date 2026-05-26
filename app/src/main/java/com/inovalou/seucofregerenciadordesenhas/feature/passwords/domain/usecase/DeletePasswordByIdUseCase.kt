package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class DeletePasswordByIdUseCase @Inject constructor(
    private val passwordRepository: PasswordRepository
) {
    suspend operator fun invoke(passwordId: Long): DeletePasswordResult {
        if (passwordId <= 0L) {
            return DeletePasswordResult.NotFound
        }

        return try {
            if (passwordRepository.deletePasswordById(passwordId)) {
                DeletePasswordResult.Success
            } else {
                DeletePasswordResult.NotFound
            }
        } catch (error: Exception) {
            if (error is CancellationException) {
                throw error
            }
            DeletePasswordResult.Failure
        }
    }
}

sealed interface DeletePasswordResult {
    data object Success : DeletePasswordResult
    data object NotFound : DeletePasswordResult
    data object Failure : DeletePasswordResult
}
