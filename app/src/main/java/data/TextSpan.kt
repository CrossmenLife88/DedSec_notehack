package com.example.dedsec.data

data class TextSpan(val start: Int, val end: Int, val fontSize: Int)

fun encodeSpans(spans: List<TextSpan>): String =
    spans.joinToString(";") { "${it.start}:${it.end}:${it.fontSize}" }

fun decodeSpans(raw: String): List<TextSpan> {
    if (raw.isBlank()) return emptyList()
    return raw.split(";").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size == 3) {
            val start = parts[0].toIntOrNull()
            val end = parts[1].toIntOrNull()
            val size = parts[2].toIntOrNull()
            if (start != null && end != null && size != null) TextSpan(start, end, size) else null
        } else null
    }
}