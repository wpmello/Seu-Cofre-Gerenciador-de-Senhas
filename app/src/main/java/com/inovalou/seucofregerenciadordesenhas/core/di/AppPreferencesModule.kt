package com.inovalou.seucofregerenciadordesenhas.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.inovalou.seucofregerenciadordesenhas.core.preferences.data.repository.DataStoreAppPreferencesRepository
import com.inovalou.seucofregerenciadordesenhas.core.preferences.domain.repository.AppPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppPreferencesModule {

    @Binds
    @Singleton
    abstract fun bindAppPreferencesRepository(
        repository: DataStoreAppPreferencesRepository
    ): AppPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideAppPreferencesDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile("app_preferences")
            }
    }
}
