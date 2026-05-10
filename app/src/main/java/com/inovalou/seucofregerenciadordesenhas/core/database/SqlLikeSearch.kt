package com.inovalou.seucofregerenciadordesenhas.core.database

fun String.toSqlLikeContainsPattern(): String {
    val normalized = trim()
    val escaped = buildString {
        normalized.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '%' -> append("\\%")
                '_' -> append("\\_")
                else -> append(character)
            }
        }
    }
    return "%$escaped%"
}
