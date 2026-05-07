package com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.usecase

import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityAnalysis
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityGuidance
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityRiskLevel
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.domain.model.PasswordSecurityTag
import java.text.Normalizer
import javax.inject.Inject
import kotlin.math.min

class EvaluatePasswordSecurityUseCase @Inject constructor() {

    operator fun invoke(password: String, isDuplicate: Boolean): PasswordSecurityAnalysis {
        if (password.isBlank()) {
            return PasswordSecurityAnalysis(
                scorePercent = 0,
                riskLevel = PasswordSecurityRiskLevel.High,
                tags = listOf(PasswordSecurityTag.Weak),
                guidance = PasswordSecurityGuidance.HighRisk
            )
        }

        val normalizedPassword = normalizeForComparison(password)
        val loweredPassword = normalizeForLookup(password)
        val findings = evaluateFindings(password, loweredPassword)
        val baseScore = calculateBaseScore(password, findings)
        var scorePercent = mapBaseScore(baseScore)

        if (findings.isBlocked) {
            scorePercent = min(scorePercent, 25)
        }

        scorePercent = when {
            password.length < 8 -> min(scorePercent, 20)
            password.length in 8..11 -> min(scorePercent, 45)
            else -> scorePercent
        }

        if (isDuplicate) {
            scorePercent = min(scorePercent, 80)
        }

        if (
            baseScore == 4 &&
            password.length >= 16 &&
            !isDuplicate &&
            !findings.hasPenalty
        ) {
            scorePercent = 100
        }

        val riskLevel = when (scorePercent) {
            in 0..49 -> PasswordSecurityRiskLevel.High
            in 50..90 -> PasswordSecurityRiskLevel.Medium
            else -> PasswordSecurityRiskLevel.Low
        }

        val tags = buildList {
            if (
                scorePercent <= 49 ||
                findings.isBlocked ||
                baseScore <= 1 ||
                password.length < 8
            ) {
                add(PasswordSecurityTag.Weak)
            }
            if (isDuplicate) {
                add(PasswordSecurityTag.Duplicate)
            }
            if (isEmpty() && scorePercent >= 91) {
                add(PasswordSecurityTag.Safe)
            }
        }

        val guidance = when (riskLevel) {
            PasswordSecurityRiskLevel.High -> PasswordSecurityGuidance.HighRisk
            PasswordSecurityRiskLevel.Medium -> PasswordSecurityGuidance.MediumRisk
            PasswordSecurityRiskLevel.Low -> PasswordSecurityGuidance.Safe
        }

        return PasswordSecurityAnalysis(
            scorePercent = scorePercent,
            riskLevel = riskLevel,
            tags = tags,
            guidance = guidance
        )
    }

    private fun calculateBaseScore(
        password: String,
        findings: PasswordSecurityFindings
    ): Int {
        var score = when {
            password.length >= 20 -> 4
            password.length >= 16 -> 3
            password.length >= 12 -> 2
            password.length >= 8 -> 1
            else -> 0
        }

        val characterGroups = countCharacterGroups(password)
        val uniqueRatio = password.toSet().size.toDouble() / password.length.toDouble()

        if (characterGroups >= 3 && password.length >= 12 && uniqueRatio >= 0.55) {
            score += 1
        }
        if (characterGroups == 4 && password.length >= 16 && uniqueRatio >= 0.65) {
            score += 1
        }
        if (characterGroups <= 1 && password.length < 16) {
            score -= 1
        }
        if (findings.isLowDiversity) {
            score -= 1
        }
        if (findings.hasRepeatingPattern || findings.hasSequencePattern || findings.hasDatePattern) {
            score -= 1
        }
        if (findings.isBlocked) {
            score -= 2
        }

        return score.coerceIn(0, 4)
    }

    private fun countCharacterGroups(password: String): Int = listOf(
        password.any(Char::isLowerCase),
        password.any(Char::isUpperCase),
        password.any(Char::isDigit),
        password.any { !it.isLetterOrDigit() }
    ).count { it }

    private fun evaluateFindings(
        password: String,
        loweredPassword: String
    ): PasswordSecurityFindings {
        val uniqueRatio = password.toSet().size.toDouble() / password.length.toDouble()
        val characterGroups = countCharacterGroups(password)
        val canonicalCommonPassword = normalizeLeet(loweredPassword)

        return PasswordSecurityFindings(
            isBlocked = loweredPassword in blockedPasswords ||
                canonicalCommonPassword in blockedPasswords ||
                containsBlockedBaseWord(canonicalCommonPassword),
            isLowDiversity = uniqueRatio < 0.5 || (characterGroups <= 2 && password.length >= 12),
            hasRepeatingPattern = hasRepeatedCharacters(password) || hasRepeatedBlocks(loweredPassword),
            hasSequencePattern = hasSequentialPattern(loweredPassword),
            hasDatePattern = obviousDateRegex.matches(loweredPassword)
        )
    }

    private fun containsBlockedBaseWord(candidate: String): Boolean {
        return blockedWords.any { blockedWord ->
            candidate.contains(blockedWord) &&
                candidate.replace(blockedWord, "").all { it.isDigit() || !it.isLetterOrDigit() }
        }
    }

    private fun hasSequentialPattern(password: String): Boolean {
        if (password.length < 4) {
            return false
        }
        val lower = password.lowercase()
        val orderedCandidates = keyboardRows + keyboardRows.map { it.reversed() }
        if (orderedCandidates.any { row -> containsWindowOf(row, lower, 4) }) {
            return true
        }

        return lower.windowed(size = 4, step = 1).any { window ->
            isAscending(window) || isDescending(window)
        }
    }

    private fun containsWindowOf(candidate: String, password: String, minSize: Int): Boolean {
        return (minSize..candidate.length).any { size ->
            candidate.windowed(size, 1).any(password::contains)
        }
    }

    private fun isAscending(window: String): Boolean =
        window.zipWithNext().all { (previous, next) -> next.code - previous.code == 1 }

    private fun isDescending(window: String): Boolean =
        window.zipWithNext().all { (previous, next) -> previous.code - next.code == 1 }

    private fun hasRepeatedCharacters(password: String): Boolean =
        password.windowed(size = 3, step = 1).any { it.toSet().size == 1 }

    private fun hasRepeatedBlocks(password: String): Boolean {
        val candidateLength = password.length
        if (candidateLength < 6) {
            return false
        }

        for (blockSize in 1..candidateLength / 2) {
            if (candidateLength % blockSize != 0) {
                continue
            }
            val block = password.substring(0, blockSize)
            if (block.repeat(candidateLength / blockSize) == password) {
                return true
            }
        }

        return false
    }

    private fun mapBaseScore(baseScore: Int): Int = when (baseScore) {
        0 -> 15
        1 -> 35
        2 -> 60
        3 -> 80
        else -> 95
    }

    private fun normalizeForComparison(password: String): String =
        Normalizer.normalize(password, Normalizer.Form.NFKC)

    private fun normalizeForLookup(password: String): String =
        normalizeForComparison(password).lowercase()

    private fun normalizeLeet(password: String): String = buildString(password.length) {
        password.forEach { character ->
            append(
                when (character) {
                    '0' -> 'o'
                    '4' -> 'a'
                    '5' -> 's'
                    '7' -> 't'
                    '@' -> 'a'
                    '$' -> 's'
                    '!' -> 'i'
                    else -> character
                }
            )
        }
    }

    private data class PasswordSecurityFindings(
        val isBlocked: Boolean,
        val isLowDiversity: Boolean,
        val hasRepeatingPattern: Boolean,
        val hasSequencePattern: Boolean,
        val hasDatePattern: Boolean
    ) {
        val hasPenalty: Boolean
            get() = isBlocked || isLowDiversity || hasRepeatingPattern || hasSequencePattern || hasDatePattern
    }

    private companion object {
        val keyboardRows = listOf(
            "0123456789",
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm",
            "abcdefghijklmnopqrstuvwxyz"
        )

        val blockedWords = setOf(
            "password",
            "senha",
            "admin",
            "welcome",
            "letmein",
            "qwerty",
            "asdf",
            "abc",
            "secret"
        )

        val blockedPasswords = setOf(
            "123456",
            "1234567",
            "12345678",
            "123456789",
            "1234567890",
            "qwerty",
            "qwerty123",
            "asdf1234",
            "password",
            "password1",
            "password123",
            "senha123",
            "admin123",
            "letmein",
            "welcome",
            "abcd1234",
            "987654321",
            "111111",
            "000000"
        )

        val obviousDateRegex = Regex(
            pattern = "^(19|20)\\d{2}([01]\\d)([0-3]\\d)$|^([0-3]\\d)([01]\\d)(19|20)\\d{2}$|^\\d{2}[/-]\\d{2}[/-](19|20)\\d{2}$"
        )
    }
}
