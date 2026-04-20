package com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.mapper

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity
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
            encryptedPassword = "encrypted",
            passwordIv = "iv-value",
            passwordCipherVersion = 1,
            iconKey = ""
        )

        assertEquals(
            PasswordSummary(
                id = 8L,
                title = "GitHub",
                login = "joao@empresa.com",
                category = "Work"
            ),
            entity.toDomain()
        )
    }
}
