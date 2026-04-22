package com.inovalou.seucofregerenciadordesenhas.core.di

import com.inovalou.seucofregerenciadordesenhas.core.time.SystemTimeProvider
import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {

    @Binds
    @Singleton
    abstract fun bindTimeProvider(
        timeProvider: SystemTimeProvider
    ): TimeProvider
}
