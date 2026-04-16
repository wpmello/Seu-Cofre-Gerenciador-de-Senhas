package com.inovalou.seucofregerenciadordesenhas.core.di

import android.content.Context
import androidx.room.Room
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabase
import com.inovalou.seucofregerenciadordesenhas.core.database.SeuCofreDatabaseMigrations
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSeuCofreDatabase(
        @ApplicationContext context: Context
    ): SeuCofreDatabase = Room.databaseBuilder(
        context,
        SeuCofreDatabase::class.java,
        "seu_cofre.db"
    )
        .addMigrations(SeuCofreDatabaseMigrations.MIGRATION_1_2)
        .build()

    @Provides
    fun provideCategoryDao(
        database: SeuCofreDatabase
    ): CategoryDao = database.categoryDao()
}
