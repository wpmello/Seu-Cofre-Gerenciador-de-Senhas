package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.core.time.TimeProvider
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.model.Category
import com.inovalou.seucofregerenciadordesenhas.feature.categories.domain.repository.CategoryRepository
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.NewPassword
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.repository.PasswordRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreatePasswordUseCaseTest {

    @Test
    fun givenBlankPassword_whenInvoked_thenReturnsValidationError() = runTest {
        val repository = FakePasswordRepository()
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(8L, "Streaming", "ic_tv", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "Netflix",
            login = "joao@email.com",
            categoryId = 8L,
            categoryName = "Streaming",
            password = "   "
        )

        assertTrue(result is CreatePasswordResult.ValidationError)
        assertEquals(
            CreatePasswordPasswordError.Blank,
            (result as CreatePasswordResult.ValidationError).validation.passwordError
        )
        assertNull(repository.createdPassword)
    }

    @Test
    fun givenBlankTitle_whenInvoked_thenCreatesPasswordWithAutomaticAppName() = runTest {
        val repository = FakePasswordRepository(passwordCount = 1)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(4L, "Trabalho", "ic_work", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "   ",
            login = "  joao@email.com  ",
            categoryId = 4L,
            categoryName = "  Trabalho  ",
            password = " senha super secreta "
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals(
            NewPassword(
                title = "App 2",
                login = "joao@email.com",
                categoryId = 4L,
                categoryName = "Trabalho",
                password = " senha super secreta ",
                note = null,
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_700_000_000_000L
            ),
            repository.createdPassword
        )
    }

    @Test
    fun givenPasswordCreatedWithPersistedCategory_whenInvoked_thenTouchesThatCategory() = runTest {
        val repository = FakePasswordRepository(passwordCount = 1)
        val categoryRepository = FakeCategoryRepository(
            listOf(Category(4L, "Trabalho", "ic_work", 0, 50L))
        )
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = categoryRepository,
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "Email",
            login = "joao@email.com",
            categoryId = 4L,
            categoryName = "Trabalho",
            password = "senha"
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals(4L, categoryRepository.touchedCategoryId)
    }

    @Test
    fun givenExplicitTitle_whenInvoked_thenPreservesTrimmedUserValue() = runTest {
        val repository = FakePasswordRepository(passwordCount = 8)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(2L, "Geral", "ic_app", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "  GitHub  ",
            login = "",
            categoryId = 2L,
            categoryName = "Geral",
            password = "abc123"
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals("GitHub", repository.createdPassword?.title)
    }

    @Test
    fun givenOptionalNote_whenInvoked_thenPersistsTrimmedPlainTextNote() = runTest {
        val repository = FakePasswordRepository(passwordCount = 1)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(4L, "Trabalho", "ic_work", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "GitHub",
            login = "dev@empresa.com",
            categoryId = 4L,
            categoryName = "Trabalho",
            password = "abc123",
            note = "  Conta usada apenas para deploy.  "
        )

        assertEquals(CreatePasswordResult.Success, result)
        assertEquals("Conta usada apenas para deploy.", repository.createdPassword?.note)
    }

    @Test
    fun givenNoSelectedCategory_whenInvoked_thenReturnsValidationError() = runTest {
        val repository = FakePasswordRepository(passwordCount = 3)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(
                listOf(Category(3L, "Trabalho", "ic_work", 0, 50L))
            ),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "GitHub",
            login = "dev@empresa.com",
            categoryId = null,
            categoryName = null,
            password = "abc123",
            note = null
        )

        assertTrue(result is CreatePasswordResult.ValidationError)
        assertEquals(
            CreatePasswordCategoryError.Missing,
            (result as CreatePasswordResult.ValidationError).validation.categoryError
        )
        assertNull(repository.createdPassword)
    }

    @Test
    fun givenSelectedCategoryDoesNotExist_whenInvoked_thenReturnsValidationError() = runTest {
        val repository = FakePasswordRepository(passwordCount = 3)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(emptyList()),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "GitHub",
            login = "dev@empresa.com",
            categoryId = 99L,
            categoryName = "Inexistente",
            password = "abc123",
            note = null
        )

        assertTrue(result is CreatePasswordResult.ValidationError)
        assertEquals(
            CreatePasswordCategoryError.Invalid,
            (result as CreatePasswordResult.ValidationError).validation.categoryError
        )
        assertNull(repository.createdPassword)
    }

    @Test
    fun givenRepositoryFailure_whenInvoked_thenReturnsFailure() = runTest {
        val repository = FakePasswordRepository(shouldFailOnCreate = true)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(1L, "Pessoal", "ic_home", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val result = useCase(
            title = "",
            login = "",
            categoryId = 1L,
            categoryName = "Pessoal",
            password = "abc123",
            note = "  "
        )

        assertEquals(CreatePasswordResult.Failure, result)
    }

    @Test
    fun givenRepositoryCancellation_whenInvoked_thenRethrowsCancellation() = runTest {
        val cancellation = CancellationException("create cancelled")
        val repository = FakePasswordRepository(createFailure = cancellation)
        val useCase = CreatePasswordUseCase(
            passwordRepository = repository,
            categoryRepository = FakeCategoryRepository(listOf(Category(1L, "Pessoal", "ic_home", 0, 50L))),
            generatePasswordTitleUseCase = GeneratePasswordTitleUseCase(repository),
            timeProvider = FixedTimeProvider(1_700_000_000_000L)
        )

        val thrown = try {
            useCase(
                title = "",
                login = "",
                categoryId = 1L,
                categoryName = "Pessoal",
                password = "abc123",
                note = null
            )
            null
        } catch (error: CancellationException) {
            error
        }

        assertEquals(cancellation, thrown)
    }

    private class FakePasswordRepository(
        private val passwordCount: Int = 0,
        private val shouldFailOnCreate: Boolean = false,
        private val createFailure: Throwable? = null
    ) : PasswordRepository {


        var createdPassword: NewPassword? = null

        override fun observePasswords(): Flow<List<PasswordSummary>> = emptyFlow()

        override fun observePasswordsByCategoryId(categoryId: Long): Flow<List<PasswordSummary>> =
            emptyFlow()

        override suspend fun getPasswordCount(): Int = passwordCount

        override suspend fun createPassword(password: NewPassword): Long {
            createFailure?.let { throw it }
            if (shouldFailOnCreate) {
                error("repository failure")
            }
            createdPassword = password
            return 1L
        }

        override suspend fun getPasswordDetails(passwordId: Long): PasswordDetails? = null

        override suspend fun updatePassword(password: PasswordDetails) = Unit

        override suspend fun hasPasswordDuplicate(
            password: String,
            excludePasswordId: Long?
        ): Boolean = false
    }

    private class FakeCategoryRepository(
        categories: List<Category>
    ) : CategoryRepository {
        override suspend fun deleteCategoryWithAssociatedPasswords(categoryId: Long) = Unit

        override suspend fun transferPasswordsToCategory(
            sourceCategoryId: Long,
            targetCategoryId: Long
        ) = Unit


        private val categoriesFlow = MutableStateFlow(categories)
        var touchedCategoryId: Long? = null

        override suspend fun createCategory(name: String, iconKey: String): Long = 1L

        override suspend fun getCategoryById(categoryId: Long): Category? =
            categoriesFlow.value.firstOrNull { it.id == categoryId }

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun touchCategory(categoryId: Long) {
            touchedCategoryId = categoryId
        }

        override suspend fun deleteCategoryById(categoryId: Long) = Unit

        override fun observeCategories(): Flow<List<Category>> = categoriesFlow
    }

    private class FixedTimeProvider(
        private val currentTimeMillis: Long
    ) : TimeProvider {
        override fun currentTimeMillis(): Long = currentTimeMillis
    }
}
