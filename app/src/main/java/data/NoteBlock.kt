package com.example.dedsec.data

sealed class NoteBlock {
    data class TextBlock(val text: String, val fontSize: Int, val spans: List<TextSpan>) : NoteBlock()
    data class ImageBlock(val fileName: String) : NoteBlock()
}

private const val BLOCK_SEP = "\u0001"
private const val FIELD_SEP = "\u0002"

fun encodeBlocks(blocks: List<NoteBlock>): String =
    blocks.joinToString(BLOCK_SEP) { block ->
        when (block) {
            is NoteBlock.TextBlock ->
                "T$FIELD_SEP${block.fontSize}$FIELD_SEP${encodeSpans(block.spans)}$FIELD_SEP${block.text}"
            is NoteBlock.ImageBlock ->
                "I$FIELD_SEP${block.fileName}"
        }
    }

fun decodeBlocks(raw: String): List<NoteBlock> {
    if (raw.isBlank()) return emptyList()
    return raw.split(BLOCK_SEP).mapNotNull { entry ->
        val parts = entry.split(FIELD_SEP, limit = 4)
        when (parts.getOrNull(0)) {
            "T" -> {
                val fontSize = parts.getOrNull(1)?.toIntOrNull() ?: 16
                val spans = decodeSpans(parts.getOrNull(2) ?: "")
                val text = parts.getOrNull(3) ?: ""
                NoteBlock.TextBlock(text, fontSize, spans)
            }
            "I" -> {
                val fileName = parts.getOrNull(1) ?: return@mapNotNull null
                NoteBlock.ImageBlock(fileName)
            }
            else -> null
        }
    }
}

fun previewText(raw: String): String {
    val blocks = decodeBlocks(raw)
    val firstText = blocks.filterIsInstance<NoteBlock.TextBlock>().firstOrNull { it.text.isNotBlank() }
    if (firstText != null) return firstText.text.take(60)
    return if (blocks.any { it is NoteBlock.ImageBlock }) "[IMG]" else ""
}