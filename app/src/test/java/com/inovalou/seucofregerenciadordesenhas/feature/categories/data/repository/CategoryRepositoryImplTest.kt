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
                CategoryEntity(id = 11L, name = "Viagens", iconKey = "ic_global", itemCount = 21),
                CategoryEntity(id = 12L, name = "Saúde", iconKey = "ic_padlock", itemCount = 12)
            )
        )
        val repository = CategoryRepositoryImpl(localDataSource)

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 11L, name = "Viagens", iconKey = "ic_global", itemCount = 21),
                Category(id = 12L, name = "Saúde", iconKey = "ic_padlock", itemCount = 12)
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
                CategoryEntity(id = 21L, name = "Entretenimento", iconKey = "ic_favorite", itemCount = 28)
            )
        )

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 21L, name = "Entretenimento", iconKey = "ic_favorite", itemCount = 28)
            ),
            observed
        )
    }

    @Test
    fun givenValidInput_whenCreatingCategory_thenPersistsNameAndIconKeyWithZeroItems() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource)

        val insertedId = repository.createCategory(
            name = "Pessoal",
            iconKey = "ic_user_profile"
        )

        assertEquals(77L, insertedId)
        assertEquals(
            CategoryEntity(
                name = "Pessoal",
                iconKey = "ic_user_profile",
                itemCount = 0
            ),
            localDataSource.insertedCategory
        )
    }

    @Test
    fun givenPersistedCategoryId_whenGettingCategory_thenMapsEntityToDomainModel() = runTest {
        val repository = CategoryRepositoryImpl(
            FakeCategoriesLocalDataSource(
                initialCategories = emptyList(),
                categoryById = CategoryEntity(
                    id = 31L,
                    name = "Financeiro",
                    iconKey = "ic_star",
                    itemCount = 6
                )
            )
        )

        val category = repository.getCategoryById(31L)

        assertEquals(
            Category(
                id = 31L,
                name = "Financeiro",
                iconKey = "ic_star",
                itemCount = 6
            ),
            category
        )
    }

    @Test
    fun givenUpdatedCategory_whenUpdating_thenPersistsMappedEntity() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource)

        repository.updateCategory(
            Category(
                id = 41L,
                name = "Nova",
                iconKey = "ic_cloud",
                itemCount = 9
            )
        )

        assertEquals(
            CategoryEntity(
                id = 41L,
                name = "Nova",
                iconKey = "ic_cloud",
                itemCount = 9
            ),
            localDataSource.updatedCategory
        )
    }

    @Test
    fun givenCategoryId_whenDeleting_thenDelegatesDeleteToLocalSource() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource)

        repository.deleteCategoryById(52L)

        assertEquals(52L, localDataSource.deletedCategoryId)
    }

    @Test
    fun givenLocalDataSourceFailure_whenObservingCategories_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("local source failure")
        val repository = CategoryRepositoryImpl(
            object : CategoriesLocalDataSource {
                override suspend fun insertCategory(category: CategoryEntity): Long = 1L

                override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = null

                override suspend fun updateCategory(category: CategoryEntity) = Unit

                override suspend fun deleteCategoryById(categoryId: Long) = Unit

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
        initialCategories: List<CategoryEntity>,
        private val categoryById: CategoryEntity? = null
    ) : CategoriesLocalDataSource {

        private val categoriesFlow = MutableStateFlow(initialCategories)
        var insertedCategory: CategoryEntity? = null
        var updatedCategory: CategoryEntity? = null
        var deletedCategoryId: Long? = null

        override suspend fun insertCategory(category: CategoryEntity): Long {
            insertedCategory = category
            return 77L
        }

        override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = categoryById

        override suspend fun updateCategory(category: CategoryEntity) {
            updatedCategory = category
        }

        override suspend fun deleteCategoryById(categoryId: Long) {
            deletedCategoryId = categoryId
        }

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        fun emit(categories: List<CategoryEntity>) {
            categoriesFlow.value = categories
        }
    }
}
