package id.hocky.miteiru.components.textBoxPopUp

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.hocky.miteiru.components.language.CantoneseDisplay
import id.hocky.miteiru.components.language.JapaneseDisplay
import id.hocky.miteiru.components.language.MandarinDisplay
import id.hocky.miteiru.utils.language.MiteiruProcess

@Composable
fun PronunciationDisplay(
    text: String,
    language: String,
    pronunciationType: MiteiruProcess
) {
    when (language) {
        "zh" -> {
            when (pronunciationType) {
                MiteiruProcess.MANDARIN -> {
                    MandarinDisplay(
                        text = text,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                MiteiruProcess.CANTONESE -> {
                    CantoneseDisplay(
                        text = text,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                else -> {} // No other options for Chinese
            }
        }
        "ja" -> {
            JapaneseDisplay(
                text = text,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        else -> {} // No pronunciation display for other languages
    }
}