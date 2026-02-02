package com.muratcangzm.nerva.feature.library.components.search

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

@Immutable
data class HighlightStyle(
    val background: Color,
    val foreground: Color,
)

fun highlightText(
    text: String,
    query: String,
    style: HighlightStyle,
    minTokenLength: Int = 2
): AnnotatedString {
    val cleanedQuery = query.trim()
    if (cleanedQuery.isBlank()) return AnnotatedString(text)

    val tokens = cleanedQuery
        .split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.length >= minTokenLength }
        .distinctBy { it.lowercase() }
        .sortedByDescending { it.length }

    if (tokens.isEmpty()) return AnnotatedString(text)

    val lowerText = text.lowercase()
    val builder = AnnotatedString.Builder(text)

    val used = ArrayList<IntRange>(8)

    fun overlaps(range: IntRange): Boolean = used.any { it.first <= range.last && range.first <= it.last }

    tokens.forEach { token ->
        val lowerToken = token.lowercase()
        var start = 0
        while (start < lowerText.length) {
            val idx = lowerText.indexOf(lowerToken, startIndex = start)
            if (idx < 0) break

            val endExclusive = idx + lowerToken.length
            val range = idx until endExclusive

            if (!overlaps(range)) {
                builder.addStyle(
                    SpanStyle(
                        background = style.background,
                        color = style.foreground
                    ),
                    range.first,
                    range.last + 1
                )
                used.add(range)
            }

            start = idx + 1
        }
    }

    return builder.toAnnotatedString()
}
