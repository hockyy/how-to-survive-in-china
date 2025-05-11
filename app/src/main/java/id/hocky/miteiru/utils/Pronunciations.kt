package id.hocky.miteiru.utils

import android.graphics.Rect
import android.net.Uri

// Represents a single character with multiple possible readings
data class Granule(
    val char: String,
    val reading: String? = null,  // General reading
    val pinyin: String? = null,   // Mandarin reading
    val pinyinTone: Int = 0,      // Mandarin tone
    val jyutping: String? = null, // Cantonese reading
    val jyutpingTone: Int = 0     // Cantonese tone
)

// Represents a word or phrase made up of one or more characters
data class Part(
    val word: String,
    val reading: String? = null,  // For Japanese/general use
    val pinyin: String? = null,   // Mandarin reading of the whole part
    val jyutping: String? = null, // Cantonese reading of the whole part
    val granules: List<Granule>
)

// Represents the full pronunciation information for a text
data class PronunciationInfo(
    val text: String,
    val parts: List<Part>
)

object PronunciationFormatter {
    private val mandarinToneMarks = mapOf(
        1 to "ˉ¹",
        2 to "⸍²",
        3 to "ᵛ₃",
        4 to "⸌⁴"
    )

    private val cantoneseToneMarks = mapOf(
        1 to "ˉ¹",
        2 to "⸍²",
        3 to "-₃",
        4 to "⸜₄",
        5 to "⸝₅",
        6 to "ˍ₆"
    )

    fun formatMandarinTone(pinyin: String?, tone: Int): String {
        return if (pinyin != null) "$pinyin${mandarinToneMarks[tone] ?: ""}" else ""
    }

    fun formatCantoneseTone(jyutping: String?, tone: Int): String {
        return if (jyutping != null) "$jyutping${cantoneseToneMarks[tone] ?: ""}" else ""
    }
}