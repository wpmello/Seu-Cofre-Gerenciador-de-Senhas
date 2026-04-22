package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordDetails
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordMapperTest {

    @Test
    fun givenPasswordEntity_whenMappingToDomain_thenReturnsSummary() {
        val entity = PasswordEntity(
            id = 8L,
            title = "GitHub",
            login = "joao@empresa.com",
            category = "Work",
            categoryId = 7L,
            encryptedPassword = "encrypted",
            passwordIv = "iv-value",
            passwordCipherVersion = 1,
            iconKey = "",
            createdAt = 1_710_000_000_000L,
            updatedAt = 1_720_000_000_000L
        )

        assertEquals(
            PasswordSummary(
                id = 8L,
                title = "GitHub",
                login = "joao@empresa.com",
                categoryId = 7L,
                categoryName = "Work"
            ),
            entity.toDomain()
        )
    }

    @Test
    fun givenPasswordEntityAndPlainPassword_whenMappingToDetails_thenReturnsFullEditableDomainModel() {
        val entity = PasswordEntity(
            id = 12L,
            title = "Spotify",
            login = "premium@vault.com",
            category = "Music",
            categoryId = 4L,
            encryptedPassword = "encrypted",
            passwordIv = "iv-value",
            passwordCipherVersion = 1,
            iconKey = "sp",
            createdAt = 1_700_000_000_000L,
            updatedAt = 1_730_000_000_000L
        )

        assertEquals(
            PasswordDetails(
                id = 12L,
                title = "Spotify",
                login = "premium@vault.com",
                password = "plain-secret",
                categoryId = 4L,
                categoryName = "Music",
                iconKey = "sp",
                createdAt = 1_700_000_000_000L,
                updatedAt = 1_730_000_000_000L
            ),
            entity.toDetailsDomain(password = "plain-secret")
        )
    }
}
