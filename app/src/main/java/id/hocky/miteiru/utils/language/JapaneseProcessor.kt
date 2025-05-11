package id.hocky.miteiru.utils.language

import id.hocky.miteiru.utils.Granule
import id.hocky.miteiru.utils.Part
import id.hocky.miteiru.utils.PronunciationInfo

// Basic implementation for Japanese (to be expanded later)
class JapaneseProcessor : PronunciationProcessor {
    override fun processSentence(text: String): PronunciationInfo {
        // Simple placeholder implementation
        val parts = text.map { char ->
            Part(
                word = char.toString(),
                reading = null, // We'll implement this later
                granules = listOf(
                    Granule(
                        char = char.toString(),
                        reading = null
                    )
                )
            )
        }

        return PronunciationInfo(text, parts)
    }
}
