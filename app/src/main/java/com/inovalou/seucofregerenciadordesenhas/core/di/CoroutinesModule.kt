package com.inovalou.seucofregerenciadordesenhas.core.di

import com.inovalou.seucofregerenciadordesenhas.core.coroutines.AppDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = AppDispatchers(
        default = Dispatchers.Default,
        io = Dispatchers.IO
    )
}
