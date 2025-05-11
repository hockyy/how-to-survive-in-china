package id.hocky.miteiru.utils.language

import id.hocky.miteiru.utils.PronunciationInfo

// Models
enum class MiteiruProcess {
    MANDARIN,
    CANTONESE,
    JAPANESE
}
// Interface for pronunciation processors
interface PronunciationProcessor {
    fun processSentence(text: String): PronunciationInfo
}
