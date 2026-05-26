package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecuritySnapshot
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface PasswordRepository {

    fun observePasswords(): Flow<List<PasswordSummary>>

    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>>

    fun observePasswordCount(): Flow<Int> = observePasswords().map { passwords ->
        passwords.size
    }

    fun observeRecentPasswords(limit: Int): Flow<List<PasswordSummary>> =
        observePasswords().map { passwords ->
            passwords.sortedByMostRecentChange().take(limit)
        }

    fun observePasswordSecuritySnapshots(): Flow<List<PasswordSecuritySnapshot>> = flowOf(emptyList())

    fun observePasswordSearchResults(query: String): Flow<List<PasswordSearchResult>> =
        observePasswords().map { passwords ->
            passwords
                .filter { password -> password.title.contains(query.trim(), ignoreCase = true) }
                .map { password ->
                    PasswordSearchResult(
                        id = password.id,
                        title = password.title,
                        iconKey = ""
                    )
                }
        }

    suspend fun getPasswordCount(): Int

    suspend fun createPassword(password: NewPassword): Long

    suspend fun getPasswordDetails(passwordId: Long): PasswordDetails?

    suspend fun updatePassword(password: PasswordDetails)

    suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long? = null): Boolean

    suspend fun deletePasswordById(passwordId: Long): Boolean
}

private fun List<PasswordSummary>.sortedByMostRecentChange(): List<PasswordSummary> =
    sortedWith(
        compareByDescending<PasswordSummary> { password ->
            maxOf(password.createdAt, password.updatedAt)
        }.thenByDescending { password -> password.id }
    )
