package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.repository

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
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

    private val timeProvider = object : TimeProvider {
        override fun currentTimeMillis(): Long = 123L
    }

    @Test
    fun givenLocalEntities_whenObservingCategories_thenMapsEntitiesToDomainModels() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(
            listOf(
                CategoryEntity(id = 11L, name = "Viagens", iconKey = "ic_global", itemCount = 21, lastModifiedAt = 11L),
                CategoryEntity(id = 12L, name = "Saúde", iconKey = "ic_padlock", itemCount = 12, lastModifiedAt = 12L)
            )
        )
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 11L, name = "Viagens", iconKey = "ic_global", itemCount = 21, lastModifiedAt = 11L),
                Category(id = 12L, name = "Saúde", iconKey = "ic_padlock", itemCount = 12, lastModifiedAt = 12L)
            ),
            observed
        )
    }

    @Test
    fun givenLocalUpdates_whenObservingCategories_thenEmitsMappedUpdatesReactively() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        localDataSource.emit(
            listOf(
                CategoryEntity(id = 21L, name = "Entretenimento", iconKey = "ic_favorite", itemCount = 28, lastModifiedAt = 21L)
            )
        )

        val observed = repository.observeCategories().first()

        assertEquals(
            listOf(
                Category(id = 21L, name = "Entretenimento", iconKey = "ic_favorite", itemCount = 28, lastModifiedAt = 21L)
            ),
            observed
        )
    }

    @Test
    fun givenSearchQuery_whenObservingMatchingCategories_thenDelegatesAndMapsResults() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(
            initialCategories = emptyList(),
            searchCategories = listOf(
                CategoryEntity(
                    id = 61L,
                    name = "Banco",
                    iconKey = "ic_padlock",
                    itemCount = 2,
                    lastModifiedAt = 61L
                )
            )
        )
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        val observed = repository.observeCategoriesMatchingQuery("ban").first()

        assertEquals("ban", localDataSource.lastSearchQuery)
        assertEquals(
            listOf(
                Category(
                    id = 61L,
                    name = "Banco",
                    iconKey = "ic_padlock",
                    itemCount = 2,
                    lastModifiedAt = 61L
                )
            ),
            observed
        )
    }

    @Test
    fun givenValidInput_whenCreatingCategory_thenPersistsNameAndIconKeyWithZeroItems() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        val insertedId = repository.createCategory(
            name = "Pessoal",
            iconKey = "ic_user_profile"
        )

        assertEquals(77L, insertedId)
        assertEquals(
            CategoryEntity(
                name = "Pessoal",
                iconKey = "ic_user_profile",
                itemCount = 0,
                lastModifiedAt = 123L
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
                    itemCount = 6,
                    lastModifiedAt = 31L
                )
            ),
            timeProvider
        )

        val category = repository.getCategoryById(31L)

        assertEquals(
            Category(
                id = 31L,
                name = "Financeiro",
                iconKey = "ic_star",
                itemCount = 6,
                lastModifiedAt = 31L
            ),
            category
        )
    }

    @Test
    fun givenUpdatedCategory_whenUpdating_thenPersistsMappedEntity() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        repository.updateCategory(
            Category(
                id = 41L,
                name = "Nova",
                iconKey = "ic_cloud",
                itemCount = 9,
                lastModifiedAt = 50L
            )
        )

        assertEquals(
            CategoryEntity(
                id = 41L,
                name = "Nova",
                iconKey = "ic_cloud",
                itemCount = 9,
                lastModifiedAt = 123L
            ),
            localDataSource.updatedCategory
        )
    }

    @Test
    fun givenCategoryId_whenTouchingCategory_thenUpdatesOnlyLastModifiedAt() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        repository.touchCategory(88L)

        assertEquals(88L, localDataSource.touchedCategoryId)
        assertEquals(123L, localDataSource.touchedLastModifiedAt)
    }

    @Test
    fun givenCategoryId_whenDeleting_thenDelegatesDeleteToLocalSource() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        repository.deleteCategoryById(52L)

        assertEquals(52L, localDataSource.deletedCategoryId)
    }

    @Test
    fun givenCategoryId_whenDeletingWithAssociatedPasswords_thenDelegatesTransactionalDeleteToLocalSource() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        repository.deleteCategoryWithAssociatedPasswords(52L)

        assertEquals(52L, localDataSource.deletedCategoryWithAssociatedPasswordsId)
    }

    @Test
    fun givenSourceAndTarget_whenTransferringPasswords_thenDelegatesBatchTransferWithTimestamp() = runTest {
        val localDataSource = FakeCategoriesLocalDataSource(emptyList())
        val repository = CategoryRepositoryImpl(localDataSource, timeProvider)

        repository.transferPasswordsToCategory(sourceCategoryId = 9L, targetCategoryId = 10L)

        assertEquals(9L, localDataSource.transferSourceCategoryId)
        assertEquals(10L, localDataSource.transferTargetCategoryId)
        assertEquals(123L, localDataSource.transferLastModifiedAt)
    }

    @Test
    fun givenLocalDataSourceFailure_whenObservingCategories_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("local source failure")
        val repository = CategoryRepositoryImpl(
            object : CategoriesLocalDataSource {
                override suspend fun insertCategory(category: CategoryEntity): Long = 1L

                override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = null

                override suspend fun updateCategory(category: CategoryEntity) = Unit

                override suspend fun updateCategoryLastModifiedAt(
                    categoryId: Long,
                    lastModifiedAt: Long
                ) = Unit

                override suspend fun deleteCategoryById(categoryId: Long) = Unit

                override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

                override suspend fun transferPasswordsToCategory(
                    sourceCategoryId: Long,
                    targetCategoryId: Long,
                    lastModifiedAt: Long
                ) = Unit

                override fun observeCategories(): Flow<List<CategoryEntity>> = flow {
                    throw expected
                }
            },
            timeProvider
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
        private val categoryById: CategoryEntity? = null,
        private val searchCategories: List<CategoryEntity> = emptyList()
    ) : CategoriesLocalDataSource {

        private val categoriesFlow = MutableStateFlow(initialCategories)
        private val searchCategoriesFlow = MutableStateFlow(searchCategories)
        var insertedCategory: CategoryEntity? = null
        var updatedCategory: CategoryEntity? = null
        var touchedCategoryId: Long? = null
        var touchedLastModifiedAt: Long? = null
        var deletedCategoryId: Long? = null
        var deletedCategoryWithAssociatedPasswordsId: Long? = null
        var transferSourceCategoryId: Long? = null
        var transferTargetCategoryId: Long? = null
        var transferLastModifiedAt: Long? = null
        var lastSearchQuery: String? = null

        override suspend fun insertCategory(category: CategoryEntity): Long {
            insertedCategory = category
            return 77L
        }

        override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = categoryById

        override suspend fun updateCategory(category: CategoryEntity) {
            updatedCategory = category
        }

        override suspend fun updateCategoryLastModifiedAt(categoryId: Long, lastModifiedAt: Long) {
            touchedCategoryId = categoryId
            touchedLastModifiedAt = lastModifiedAt
        }

        override suspend fun deleteCategoryById(categoryId: Long) {
            deletedCategoryId = categoryId
        }

        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) {
            deletedCategoryWithAssociatedPasswordsId = categoryId
        }

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long,
            lastModifiedAt: Long
        ) {
            transferSourceCategoryId = sourceCategoryId
            transferTargetCategoryId = targetCategoryId
            transferLastModifiedAt = lastModifiedAt
        }

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        override fun observeCategoriesMatchingQuery(query: String): Flow<List<CategoryEntity>> {
            lastSearchQuery = query
            return searchCategoriesFlow
        }

        fun emit(categories: List<CategoryEntity>) {
            categoriesFlow.value = categories
        }
    }
}
