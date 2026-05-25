package com.inovalou.seucofregerenciadordesenhas.core.text

object TextInputLimits {
    const val NAME_MAX_LENGTH = 100
    const val EMAIL_MAX_LENGTH = 254
    const val NOTE_MAX_LENGTH = 1_500
}

fun String.limitToMaxCharacters(maxLength: Int): String {
    require(maxLength >= 0) { "maxLength must be non-negative" }
    if (codePointCount(0, length) <= maxLength) {
        return this
    }

    return substring(0, offsetByCodePoints(0, maxLength))
}
