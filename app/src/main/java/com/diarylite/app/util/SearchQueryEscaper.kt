package com.diarylite.app.util

fun String.toEscapedLikeQueryOrNull(): String? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null

    val escaped = buildString {
        trimmed.forEach { char ->
            when (char) {
                '\\', '%', '_' -> {
                    append('\\')
                    append(char)
                }
                else -> append(char)
            }
        }
    }
    return "%$escaped%"
}
