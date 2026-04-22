package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.repository

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoriesLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.mapper.toDomain
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.mapper.toEntity
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val localDataSource: CategoriesLocalDataSource,
    private val timeProvider: TimeProvider
) : CategoryRepository {

    override suspend fun createCategory(
        name: String,
        iconKey: String
    ): Long = localDataSource.insertCategory(
        CategoryEntity(
            name = name,
            iconKey = iconKey,
            itemCount = 0,
            lastModifiedAt = timeProvider.currentTimeMillis()
        )
    )

    override suspend fun getCategoryById(categoryId: Long): Category? =
        localDataSource.getCategoryById(categoryId)?.toDomain()

    override suspend fun updateCategory(category: Category) {
        localDataSource.updateCategory(
            category.copy(lastModifiedAt = timeProvider.currentTimeMillis()).toEntity()
        )
    }

    override suspend fun touchCategory(categoryId: Long) {
        localDataSource.updateCategoryLastModifiedAt(
            categoryId = categoryId,
            lastModifiedAt = timeProvider.currentTimeMillis()
        )
    }

    override suspend fun deleteCategoryById(categoryId: Long) {
        localDataSource.deleteCategoryById(categoryId)
    }

    override fun observeCategories(): Flow<List<Category>> =
        localDataSource.observeCategories().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
}
