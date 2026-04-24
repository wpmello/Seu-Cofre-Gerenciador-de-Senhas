package com.inovalou.seucofregerenciadordesenhas.feature.passwords.presentation.editpassword

internal fun securityScoreSweepAngle(scorePercent: Int): Float {
    val normalizedScore = scorePercent.coerceIn(minimumValue = 0, maximumValue = 100)
    return normalizedScore / 100f * 360f
}
