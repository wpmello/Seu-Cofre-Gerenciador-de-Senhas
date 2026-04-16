package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.repository

import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoriesLocalDataSource
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryRepositoryImplTest {

    @Test
    fun givenLocalEntities_whenObservingCategories_thenMapsEntitiesToDomainModels() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(
            listOf(
                CategoryEntity(id = 11L, name = "Viagens", itemCount = 21),
                CategoryEntity(id = 12L, name = "Saúde", itemCount = 12)
            )
        )
        val repository = CategoryRepositoryImpl(localDataSource)

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 11L, name = "Viagens", itemCount = 21),
                Category(id = 12L, name = "Saúde", itemCount = 12)
            ),
            observed
        )
    }

    @Test
    fun givenLocalUpdates_whenObservingCategories_thenEmitsMappedUpdatesReactively() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource)

        localDataSource.emit(
            listOf(
                CategoryEntity(id = 21L, name = "Entretenimento", itemCount = 28)
            )
        )

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 21L, name = "Entretenimento", itemCount = 28)
            ),
            observed
        )
    }

    @Test
    fun givenLocalDataSourceFailure_whenObservingCategories_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("local source failure")
        val repository = CategoryRepositoryImpl(
            object : CategoriesLocalDataSource {
                override fun observeCategories(): Flow<List<CategoryEntity>> = flow {
                    throw expected
                }
            }
        )

        val thrown = try {
            repository.observeCategories().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    private class FakeCategoriesLocalDataSource(
        initialCategories: List<CategoryEntity>
    ) : CategoriesLocalDataSource {

        private val categoriesFlow = MutableStateFlow(initialCategories)

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        fun emit(categories: List<CategoryEntity>) {
            categoriesFlow.value = categories
        }
    }
}
