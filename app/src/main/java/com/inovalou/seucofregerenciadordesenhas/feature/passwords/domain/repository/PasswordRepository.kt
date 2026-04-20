package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import kotlinx.coroutines.flow.Flow

interface PasswordRepository {

    fun observePasswords(): Flow<List<PasswordSummary>>
}
