package com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    suspend fun createCategory(
        name: String,
        iconKey: String
    ): Long

    fun observeCategories(): Flow<List<Category>>
}
