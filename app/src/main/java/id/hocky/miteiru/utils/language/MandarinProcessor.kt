package id.hocky.miteiru.utils.language

import id.hocky.miteiru.utils.Granule
import id.hocky.miteiru.utils.Part
import id.hocky.miteiru.utils.PronunciationInfo
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

// Mandarin implementation with pinyin4j
class MandarinProcessor : PronunciationProcessor {
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.LOWERCASE
        toneType = HanyuPinyinToneType.WITH_TONE_NUMBER  // e.g., "ni3"
        vCharType = HanyuPinyinVCharType.WITH_V
    }

    // Common multi-character words for word segmentation
    private val wordDictionary = mapOf(
        "你好" to "ni3 hao3",
        "中国" to "zhong1 guo2",
        "日本" to "ri4 ben3",
        "谢谢" to "xie4 xie4",
        "早上好" to "zao3 shang4 hao3",
        "晚上好" to "wan3 shang4 hao3",
        "学生" to "xue2 sheng1",
        "老师" to "lao3 shi1",
        "电脑" to "dian4 nao3",
        "手机" to "shou3 ji1",
        "朋友" to "peng2 you3",
        "家人" to "jia1 ren2"
        // Add more common words as needed
    )

    override fun processSentence(text: String): PronunciationInfo {
        val parts = mutableListOf<Part>()
        var remainingText = text

        // Try to match known words first
        while (remainingText.isNotEmpty()) {
            var matched = false

            // Try to find word matches, starting with longer words
            for (word in wordDictionary.keys.sortedByDescending { it.length }) {
                if (remainingText.startsWith(word)) {
                    val pinyinWithTones = wordDictionary[word] ?: ""
                    val granules = processWordToPinyinGranules(word, pinyinWithTones)

                    parts.add(
                        Part(
                        word = word,
                        pinyin = pinyinWithTones,
                        granules = granules
                    )
                    )

                    remainingText = remainingText.substring(word.length)
                    matched = true
                    break
                }
            }

            // If no word match found, process a single character
            if (!matched) {
                val char = remainingText.first().toString()
                val granule = processCharToPinyinGranule(char.first())

                parts.add(
                    Part(
                    word = char,
                    pinyin = granule.pinyin?.let { "$it${granule.pinyinTone}" },
                    granules = listOf(granule)
                )
                )

                remainingText = remainingText.substring(1)
            }
        }

        return PronunciationInfo(text, parts)
    }

    private fun processWordToPinyinGranules(word: String, pinyinWithTones: String): List<Granule> {
        val pinyinParts = pinyinWithTones.split(" ")

        // If the pinyin parts don't match the word length, process character by character
        if (pinyinParts.size != word.length) {
            return word.map { processCharToPinyinGranule(it) }
        }

        return word.mapIndexed { index, char ->
            val pinyinWithTone = pinyinParts.getOrNull(index) ?: ""
            val tone = pinyinWithTone.lastOrNull()?.toString()?.toIntOrNull() ?: 0
            val pinyin = if (tone > 0) pinyinWithTone.dropLast(1) else pinyinWithTone

            Granule(
                char = char.toString(),
                pinyin = pinyin,
                pinyinTone = tone
            )
        }
    }

    private fun processCharToPinyinGranule(char: Char): Granule {
        try {
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)

            if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                val pinyinWithTone = pinyinArray[0] // Take first reading if multiple exist
                val tone = pinyinWithTone.lastOrNull()?.toString()?.toIntOrNull() ?: 0
                val pinyin = if (tone > 0) pinyinWithTone.dropLast(1) else pinyinWithTone

                return Granule(
                    char = char.toString(),
                    pinyin = pinyin,
                    pinyinTone = tone
                )
            }
        } catch (e: Exception) {
            // Character might not be Chinese or other error
        }

        // Default if no pinyin found
        return Granule(char = char.toString())
    }
}
