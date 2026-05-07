package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityBucket
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.VaultSecurityStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PasswordSecurityClassificationTest {

    @Test
    fun givenScoreInPoorRange_whenClassified_thenReturnsWeakBucketAndPoorVaultStatus() {
        assertEquals(PasswordSecurityBucket.Weak, 49.toPasswordSecurityBucket())
        assertEquals(VaultSecurityStatus.Poor, 49.toVaultSecurityStatus())
    }

    @Test
    fun givenScoreInModerateRange_whenClassified_thenReturnsModerateBucketAndVaultStatus() {
        assertEquals(PasswordSecurityBucket.Moderate, 50.toPasswordSecurityBucket())
        assertEquals(PasswordSecurityBucket.Moderate, 90.toPasswordSecurityBucket())
        assertEquals(VaultSecurityStatus.Moderate, 90.toVaultSecurityStatus())
    }

    @Test
    fun givenScoreInExcellentRange_whenClassified_thenReturnsSafeBucketAndExcellentVaultStatus() {
        assertEquals(PasswordSecurityBucket.Safe, 91.toPasswordSecurityBucket())
        assertEquals(VaultSecurityStatus.Excellent, 100.toVaultSecurityStatus())
    }
}
