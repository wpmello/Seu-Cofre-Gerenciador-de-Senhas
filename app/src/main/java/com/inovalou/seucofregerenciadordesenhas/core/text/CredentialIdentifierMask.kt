package com.inovalou.seucofregerenciadordesenhas.core.text

private const val Mask = "***"
private const val ShortSensitiveValueLength = 2
private const val MaxVisibleCharacters = 1

fun String.maskCredentialIdentifierForDisplay(): String {
    val identifier = trim()

    if (identifier.isBlank()) {
        return ""
    }

    val atIndex = identifier.indexOf('@')
    val hasSingleAtSign = atIndex == identifier.lastIndexOf('@')

    return if (atIndex > 0 && atIndex < identifier.lastIndex && hasSingleAtSign) {
        val localPart = identifier.substring(startIndex = 0, endIndex = atIndex)
        val domain = identifier.substring(startIndex = atIndex + 1)

        "${localPart.maskSensitiveSegment()}@${domain.maskSensitiveDomain()}"
    } else {
        identifier.maskSensitiveSegment()
    }
}

private fun String.maskSensitiveDomain(): String {
    val labels = split('.')

    if (labels.size == 1 || labels.any { it.isBlank() }) {
        return Mask
    }

    return labels
        .mapIndexed { index, label ->
            if (index == labels.lastIndex) {
                label.maskTopLevelDomain()
            } else {
                label.maskSensitiveSegment()
            }
        }
        .joinToString(separator = ".")
}

private fun String.maskTopLevelDomain(): String {
    return if (length <= ShortSensitiveValueLength) {
        Mask
    } else {
        take(MaxVisibleCharacters) + Mask
    }
}

private fun String.maskSensitiveSegment(): String {
    if (isBlank()) {
        return ""
    }

    return if (length <= ShortSensitiveValueLength) {
        Mask
    } else {
        take(MaxVisibleCharacters) + Mask
    }
}
