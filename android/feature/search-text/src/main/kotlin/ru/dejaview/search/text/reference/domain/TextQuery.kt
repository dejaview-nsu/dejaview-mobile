package ru.dejaview.search.text.reference.domain

internal const val MIN_QUERY_LENGTH = 3
internal const val MAX_QUERY_LENGTH = 250
private const val MAX_DRAFT_LENGTH = MAX_QUERY_LENGTH + 1

internal fun String.queryLength() = codePointCount(0, length)

internal fun String.trimQuery() = trim { character ->
    // Unicode White_Space, as used by the backend. Kotlin's trim also removes U+001C–U+001F.
    character in '\u0009'..'\u000D' || character == ' ' || character == '\u0085' ||
        character == '\u00A0' || character == '\u1680' || character in '\u2000'..'\u200A' ||
        character == '\u2028' || character == '\u2029' || character == '\u202F' ||
        character == '\u205F' || character == '\u3000'
}

internal fun String.boundedDraft(): String {
    if (queryLength() <= MAX_DRAFT_LENGTH) return this
    val trimmed = trimQuery()
    if (trimmed.queryLength() <= MAX_DRAFT_LENGTH) return trimmed
    val end = trimmed.offsetByCodePoints(0, MAX_DRAFT_LENGTH)
    val prefix = trimmed.substring(0, end)
    if (prefix.trimQuery().queryLength() == MAX_DRAFT_LENGTH) return prefix
    // Keep the oversized draft invalid even if the bounded prefix ends in whitespace.
    return prefix.substring(0, prefix.offsetByCodePoints(0, MAX_QUERY_LENGTH)) +
        trimmed.substring(trimmed.offsetByCodePoints(trimmed.length, -1))
}
