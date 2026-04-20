package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper.toDomain
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepositoryImpl @Inject constructor(
    private val localDataSource: PasswordsLocalDataSource
) : PasswordRepository {

    override fun observePasswords(): Flow<List<PasswordSummary>> =
        localDataSource.observePasswords().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
}
