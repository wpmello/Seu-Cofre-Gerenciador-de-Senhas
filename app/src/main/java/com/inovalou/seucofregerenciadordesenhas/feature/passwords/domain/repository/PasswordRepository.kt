package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {

    fun observePasswords(): Flow<List<PasswordSummary>>

    fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>>

    suspend fun getPasswordCount(): Int

    suspend fun createPassword(password: NewPassword): Long

    suspend fun getPasswordDetails(passwordId: Long): PasswordDetails?

    suspend fun updatePassword(password: PasswordDetails)

    suspend fun hasPasswordDuplicate(password: String, excludePasswordId: Long? = null): Boolean
}
