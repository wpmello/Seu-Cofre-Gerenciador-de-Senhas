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
            iconKey = "ic_cloud"
        )

        assertEquals(
            PasswordSummary(
                id = 8L,
                title = "GitHub",
                login = "joao@empresa.com",
                iconKey = "ic_cloud"
            ),
            entity.toDomain()
        )
    }

    @Test
    fun givenPasswordSummary_whenMappingToEntity_thenReturnsEntity() {
        val summary = PasswordSummary(
            id = 12L,
            title = "Workspace",
            login = "work@empresa.com",
            iconKey = "ic_directory"
        )

        assertEquals(
            PasswordEntity(
                id = 12L,
                title = "Workspace",
                login = "work@empresa.com",
                iconKey = "ic_directory"
            ),
            summary.toEntity()
        )
    }
}
