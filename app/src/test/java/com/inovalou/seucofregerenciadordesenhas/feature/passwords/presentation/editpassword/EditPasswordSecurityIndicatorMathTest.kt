package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

import org.junit.Assert.assertEquals
import org.junit.Test

class EditPasswordSecurityIndicatorMathTest {

    @Test
    fun givenZeroPercent_whenCalculatingSweepAngle_thenReturnsZero() {
        assertEquals(0f, securityScoreSweepAngle(scorePercent = 0))
    }

    @Test
    fun givenHalfPercent_whenCalculatingSweepAngle_thenReturnsHalfCircle() {
        assertEquals(180f, securityScoreSweepAngle(scorePercent = 50))
    }

    @Test
    fun givenFullPercent_whenCalculatingSweepAngle_thenReturnsFullCircle() {
        assertEquals(360f, securityScoreSweepAngle(scorePercent = 100))
    }

    @Test
    fun givenOutOfRangePercent_whenCalculatingSweepAngle_thenClampsValue() {
        assertEquals(0f, securityScoreSweepAngle(scorePercent = -20))
        assertEquals(360f, securityScoreSweepAngle(scorePercent = 140))
    }
}
