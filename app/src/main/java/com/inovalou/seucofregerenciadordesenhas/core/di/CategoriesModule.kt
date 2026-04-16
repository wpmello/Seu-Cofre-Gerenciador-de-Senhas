package com.inovalou.seucofregerenciadordesenhas.core.di

import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoriesLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.RoomCategoriesLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.repository.CategoryRepositoryImpl
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoriesModule {

    @Binds
    abstract fun bindCategoriesLocalDataSource(
        localDataSource: RoomCategoriesLocalDataSource
    ): CategoriesLocalDataSource

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        repository: CategoryRepositoryImpl
    ): CategoryRepository
}
