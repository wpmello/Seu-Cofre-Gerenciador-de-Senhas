package com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local

import com.inovalou.seucofregerenciadordesenhas.core.database.DatabaseTransactionRunner
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordDao
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordSearchResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomCategoriesLocalDataSourceTest {

    @Test
    fun givenDaoFlow_whenObservingCategories_thenDelegatesToDao() = runTest {
        val expected = listOf(
            CategoryEntity(id = 1L, name = "Educação", iconKey = "ic_home_2", itemCount = 15, lastModifiedAt = 10L),
            CategoryEntity(id = 2L, name = "Trabalho", iconKey = "ic_work_bag_add_category", itemCount = 42, lastModifiedAt = 20L)
        )
        val dataSource = dataSource(categoryDao = FakeCategoryDao(flowOf(expected)))

        val observed = dataSource.observeCategories().first()

        assertEquals(expected, observed)
    }

    @Test
    fun givenRawQuery_whenObservingMatchingCategories_thenEscapesLikeWildcardsAndDelegatesPatternToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = dataSource(categoryDao = dao)

        dataSource.observeCategoriesMatchingQuery("50%_safe").first()

        assertEquals("%50\\%\\_safe%", dao.lastSearchPattern)
    }

    @Test
    fun givenCategory_whenInserting_thenDelegatesToDao() = runTest {
        val category = CategoryEntity(
            name = "Pessoal",
            iconKey = "ic_user_profile",
            itemCount = 0,
            lastModifiedAt = 30L
        )
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = dataSource(categoryDao = dao)

        val insertedId = dataSource.insertCategory(category)

        assertEquals(99L, insertedId)
        assertEquals(category, dao.insertedCategory)
    }

    @Test
    fun givenPersistedCategoryId_whenGettingCategory_thenDelegatesToDao() = runTest {
        val expected = CategoryEntity(
            id = 7L,
            name = "Trabalho",
            iconKey = "ic_work_bag_add_category",
            itemCount = 4,
            lastModifiedAt = 40L
        )
        val dataSource = dataSource(
            categoryDao = FakeCategoryDao(
                categoriesFlow = flowOf(emptyList()),
                categoryById = expected
            )
        )

        val category = dataSource.getCategoryById(7L)

        assertEquals(expected, category)
    }

    @Test
    fun givenCategory_whenUpdating_thenDelegatesToDao() = runTest {
        val category = CategoryEntity(
            id = 9L,
            name = "Atualizada",
            iconKey = "ic_star",
            itemCount = 1,
            lastModifiedAt = 50L
        )
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = dataSource(categoryDao = dao)

        dataSource.updateCategory(category)

        assertEquals(category, dao.updatedCategory)
    }

    @Test
    fun givenCategoryId_whenDeleting_thenDelegatesToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = dataSource(categoryDao = dao)

        dataSource.deleteCategoryById(12L)

        assertEquals(12L, dao.deletedCategoryId)
    }

    @Test
    fun givenCategoryId_whenDeletingWithAssociatedPasswords_thenDeletesPasswordsAndCategoryInsideTransaction() = runTest {
        val categoryDao = FakeCategoryDao(flowOf(emptyList()))
        val passwordDao = FakePasswordDao()
        val transactionRunner = FakeTransactionRunner()
        val dataSource = dataSource(
            categoryDao = categoryDao,
            passwordDao = passwordDao,
            transactionRunner = transactionRunner
        )

        dataSource.deleteCategoryWithAssociatedPasswords(12L)

        assertEquals(1, transactionRunner.transactionCount)
        assertEquals(12L, passwordDao.deletedPasswordsCategoryId)
        assertEquals(12L, categoryDao.deletedCategoryId)
    }

    @Test
    fun givenSourceAndTarget_whenTransferringPasswords_thenUpdatesPasswordsAndTouchesBothCategoriesInsideTransaction() = runTest {
        val categoryDao = FakeCategoryDao(flowOf(emptyList()))
        val passwordDao = FakePasswordDao()
        val transactionRunner = FakeTransactionRunner()
        val dataSource = dataSource(
            categoryDao = categoryDao,
            passwordDao = passwordDao,
            transactionRunner = transactionRunner
        )

        dataSource.transferPasswordsToCategory(
            sourceCategoryId = 7L,
            targetCategoryId = 8L,
            lastModifiedAt = 500L
        )

        assertEquals(1, transactionRunner.transactionCount)
        assertEquals(7L to 8L, passwordDao.updatedPasswordsCategory)
        assertEquals(listOf(7L to 500L, 8L to 500L), categoryDao.touchedCategories)
    }

    @Test
    fun givenCategoryIdAndTimestamp_whenTouchingCategory_thenDelegatesToDao() = runTest {
        val dao = FakeCategoryDao(flowOf(emptyList()))
        val dataSource = dataSource(categoryDao = dao)

        dataSource.updateCategoryLastModifiedAt(categoryId = 13L, lastModifiedAt = 77L)

        assertEquals(13L, dao.touchedCategoryId)
        assertEquals(77L, dao.touchedLastModifiedAt)
    }

    @Test
    fun givenDaoFailure_whenObservingCategories_thenPropagatesTheError() = runTest {
        val expected = IllegalStateException("database unavailable")
        val dataSource = dataSource(
            categoryDao = FakeCategoryDao(
                flow {
                    throw expected
                }
            )
        )

        val thrown = try {
            dataSource.observeCategories().first()
            null
        } catch (error: IllegalStateException) {
            error
        }

        assertTrue(thrown is IllegalStateException)
        assertEquals(expected.message, thrown?.message)
    }

    private fun dataSource(
        categoryDao: CategoryDao = FakeCategoryDao(flowOf(emptyList())),
        passwordDao: PasswordDao = FakePasswordDao(),
        transactionRunner: DatabaseTransactionRunner = FakeTransactionRunner()
    ) = RoomCategoriesLocalDataSource(
        categoryDao = categoryDao,
        passwordsDao = passwordDao,
        transactionRunner = transactionRunner
    )

    private class FakeTransactionRunner : DatabaseTransactionRunner {
        var transactionCount: Int = 0

        override suspend fun <T> runInTransaction(block: suspend () -> T): T {
            transactionCount += 1
            return block()
        }
    }

    private class FakeCategoryDao(
        private val categoriesFlow: Flow<List<CategoryEntity>>,
        private val categoryById: CategoryEntity? = null
    ) : CategoryDao {

        var insertedCategory: CategoryEntity? = null
        var updatedCategory: CategoryEntity? = null
        var touchedCategoryId: Long? = null
        var touchedLastModifiedAt: Long? = null
        val touchedCategories = mutableListOf<Pair<Long, Long>>()
        var deletedCategoryId: Long? = null
        var lastSearchPattern: String? = null

        override suspend fun insertCategory(category: CategoryEntity): Long {
            insertedCategory = category
            return 99L
        }

        override suspend fun getCategoryById(categoryId: Long): CategoryEntity? = categoryById

        override suspend fun updateCategory(category: CategoryEntity) {
            updatedCategory = category
        }

        override suspend fun updateCategoryLastModifiedAt(categoryId: Long, lastModifiedAt: Long) {
            touchedCategoryId = categoryId
            touchedLastModifiedAt = lastModifiedAt
            touchedCategories += categoryId to lastModifiedAt
        }

        override suspend fun deleteCategoryById(categoryId: Long) {
            deletedCategoryId = categoryId
        }

        override fun observeCategories(): Flow<List<CategoryEntity>> = categoriesFlow

        override fun observeCategoriesMatchingQuery(searchPattern: String): Flow<List<CategoryEntity>> {
            lastSearchPattern = searchPattern
            return flowOf(emptyList())
        }
    }

    private class FakePasswordDao : PasswordDao {
        var deletedPasswordsCategoryId: Long? = null
        var updatedPasswordsCategory: Pair<Long, Long>? = null

        override fun observePasswords(): Flow<List<PasswordEntity>> = flowOf(emptyList())

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordEntity>> =
            flowOf(emptyList())

        override fun observePasswordCount(): Flow<Int> = flowOf(0)

        override fun observeRecentPasswords(limit: Int): Flow<List<PasswordEntity>> = flowOf(emptyList())

        override fun observePasswordSearchResults(searchPattern: String): Flow<List<PasswordSearchResultEntity>> =
            flowOf(emptyList())

        override suspend fun getPasswordById(passwordId: Long): PasswordEntity? = null

        override suspend fun insert(password: PasswordEntity): Long = 1L

        override suspend fun update(password: PasswordEntity) = Unit

        override suspend fun countPasswords(): Int = 0

        override suspend fun countPasswordsWithFingerprint(
            passwordFingerprint: String,
            excludePasswordId: Long?
        ): Int = 0

        override suspend fun getPasswordsMissingFingerprint(): List<PasswordEntity> = emptyList()

        override suspend fun updatePasswordFingerprint(passwordId: Long, passwordFingerprint: String) = Unit

        override suspend fun deletePasswordById(passwordId: Long): Int = 0

        override suspend fun deletePasswordsByCategoryId(categoryId: Long) {
            deletedPasswordsCategoryId = categoryId
        }

        override suspend fun updatePasswordsCategory(sourceCategoryId: Long, targetCategoryId: Long) {
            updatedPasswordsCategory = sourceCategoryId to targetCategoryId
        }
    }
}
