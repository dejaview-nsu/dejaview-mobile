package ru.dejaview.search.text.reference.domain

internal const val MIN_QUERY_LENGTH = 3
internal const val MAX_QUERY_LENGTH = 250
private const val MAX_DRAFT_LENGTH = MAX_QUERY_LENGTH + 1
// Unicode White_Space used by the backend. Kotlin trim also removes U+001C–U+001F.
private const val QUERY_WHITESPACE =
    "\t\n\u000B\u000C\r \u0085\u00A0\u1680\u2000\u2001\u2002\u2003\u2004\u2005" +
        "\u2006\u2007\u2008\u2009\u200A\u2028\u2029\u202F\u205F\u3000"

internal fun String.queryLength() = codePointCount(0, length)

internal fun String.trimQuery() = trim { it in QUERY_WHITESPACE }

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
