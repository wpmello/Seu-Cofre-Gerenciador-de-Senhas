package com.inovalou.seucofregerenciadordesenhas.feature.search.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSearchResult
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchVaultItemsUseCaseTest {

    @Test
    fun givenBlankQuery_whenSearching_thenReturnsEmptyResultsWithoutQueryingRepositories() = runTest {
        val categoryRepository = FakeCategoryRepository(emptyList())
        val passwordRepository = FakePasswordRepository(emptyList())
        val useCase = SearchVaultItemsUseCase(categoryRepository, passwordRepository)

        val results = useCase("   ").first()

        assertTrue(results.categories.isEmpty())
        assertTrue(results.passwords.isEmpty())
        assertEquals(null, categoryRepository.lastObservedQuery)
        assertEquals(null, passwordRepository.lastObservedQuery)
    }

    @Test
    fun givenSearchQuery_whenSearching_thenTrimsQueryAndCombinesCategoriesAndPasswords() = runTest {
        val categoryRepository = FakeCategoryRepository(
            listOf(
                Category(
                    id = 1L,
                    name = "Trabalho",
                    iconKey = "ic_work_bag",
                    itemCount = 4,
                    lastModifiedAt = 10L
                )
            )
        )
        val passwordRepository = FakePasswordRepository(
            listOf(
                PasswordSearchResult(
                    id = 7L,
                    title = "Work Email",
                    iconKey = ""
                )
            )
        )
        val useCase = SearchVaultItemsUseCase(categoryRepository, passwordRepository)

        val results = useCase("  work  ").first()

        assertEquals("work", categoryRepository.lastObservedQuery)
        assertEquals("work", passwordRepository.lastObservedQuery)
        assertEquals(listOf("Trabalho"), results.categories.map { category -> category.name })
        assertEquals(listOf("Work Email"), results.passwords.map { password -> password.title })
    }

    private class FakeCategoryRepository(
        private val categories: List<Category>
    ) : CategoryRepository {
        var lastObservedQuery: String? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 0L

        override suspend fun getCategoryById(categoryId: Long): Category? = null

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) = Unit

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = emptyFlow()

        override fun observeCategoriesMatchingQuery(query: String): Flow<List<Category>> {
            lastObservedQuery = query
            return MutableStateFlow(categories)
        }
    }

    private class FakePasswordRepository(
        private val passwords: List<PasswordSearchResult>
    ) : PasswordRepository {
        var lastObservedQuery: String? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override fun observePasswordSearchResults(query: String): Flow<List<PasswordSearchResult>> {
            lastObservedQuery = query
            return MutableStateFlow(passwords)
        }

        override suspend fun getPasswordCount(): Int = 0

        override suspend fun createPassword(password: NewPassword): Long = 0L

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }
}
