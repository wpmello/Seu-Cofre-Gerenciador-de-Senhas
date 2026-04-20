package com.inovalou.seucofregerenciadordesenhas.core.di

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.RoomPasswordsLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.repository.PasswordRepositoryImpl
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PasswordsModule {

    @Binds
    abstract fun bindPasswordsLocalDataSource(
        localDataSource: RoomPasswordsLocalDataSource
    ): PasswordsLocalDataSource

    @Binds
    @Singleton
    abstract fun bindPasswordRepository(
        repository: PasswordRepositoryImpl
    ): PasswordRepository
}
