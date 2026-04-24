package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityGuidance
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluatePasswordSecurityUseCaseTest {

    private val useCase = EvaluatePasswordSecurityUseCase()

    @Test
    fun givenBlankPassword_whenEvaluated_thenReturnsZeroAndHighRisk() {
        val result = useCase(password = "", isDuplicate = false)

        assertEquals(0, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertEquals(setOf(PasswordSecurityTag.Weak), result.tags.toSet())
        assertEquals(PasswordSecurityGuidance.HighRisk, result.guidance)
    }

    @Test
    fun givenExtremelyShortPassword_whenEvaluated_thenKeepsHighRisk() {
        val result = useCase(password = "12345", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenShortReasonablyMixedPassword_whenEvaluated_thenCapsResultInMediumRange() {
        val result = useCase(password = "Ab1!cd2@", isDuplicate = false)

        assertEquals(35, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenLongDiversePassword_whenEvaluated_thenReturnsSafeScoreAndSafeTag() {
        val result = useCase(password = "S7!mQ2#vN9@tL4\$z", isDuplicate = false)

        assertEquals(100, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.Low, result.riskLevel)
        assertEquals(setOf(PasswordSecurityTag.Safe), result.tags.toSet())
        assertEquals(PasswordSecurityGuidance.Safe, result.guidance)
    }

    @Test
    fun givenPasswordWithLowDiversity_whenEvaluated_thenPenalizesTheScore() {
        val result = useCase(password = "aaaaaaaaaaaa", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenPasswordWithGoodDiversityButWithoutGreatLength_whenEvaluated_thenKeepsModerateRisk() {
        val result = useCase(password = "R8!kLm2@Qp7#", isDuplicate = false)

        assertEquals(80, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.Medium, result.riskLevel)
        assertEquals(emptySet<PasswordSecurityTag>(), result.tags.toSet())
        assertEquals(PasswordSecurityGuidance.MediumRisk, result.guidance)
    }

    @Test
    fun givenPasswordWithObviousPattern_whenEvaluated_thenClassifiesAsWeak() {
        val result = useCase(password = "abcd1234", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenPasswordBasedOnCommonDictionaryTerm_whenEvaluated_thenClassifiesAsWeak() {
        val result = useCase(password = "P@ssw0rd123", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenPasswordWithRepeatedBlocks_whenEvaluated_thenClassifiesAsWeak() {
        val result = useCase(password = "abcabcabcabc", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenPasswordWithPredictableSequence_whenEvaluated_thenClassifiesAsWeak() {
        val result = useCase(password = "9876543210", isDuplicate = false)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertTrue(result.tags.contains(PasswordSecurityTag.Weak))
    }

    @Test
    fun givenDuplicatePassword_whenEvaluated_thenAddsDuplicateTagAndPreventsGreenStatus() {
        val result = useCase(password = "S7!mQ2#vN9@tL4\$z", isDuplicate = true)

        assertEquals(80, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.Medium, result.riskLevel)
        assertEquals(setOf(PasswordSecurityTag.Duplicate), result.tags.toSet())
        assertEquals(PasswordSecurityGuidance.MediumRisk, result.guidance)
    }

    @Test
    fun givenWeakAndDuplicatePassword_whenEvaluated_thenCombinesAllowedAlertTags() {
        val result = useCase(password = "123456", isDuplicate = true)

        assertEquals(15, result.scorePercent)
        assertEquals(PasswordSecurityRiskLevel.High, result.riskLevel)
        assertEquals(
            setOf(PasswordSecurityTag.Weak, PasswordSecurityTag.Duplicate),
            result.tags.toSet()
        )
    }
}
