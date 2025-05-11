package id.hocky.miteiru.components.language

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.hocky.miteiru.utils.*
import id.hocky.miteiru.utils.language.CantoneseProcessor
import id.hocky.miteiru.utils.language.MandarinProcessor
import id.hocky.miteiru.utils.language.PronunciationProcessor

/**
 * Ultra-compact shared implementation for Chinese displays
 */
@Composable
private fun ChineseDisplay(
    text: String,
    processor: PronunciationProcessor,
    modifier: Modifier = Modifier,
    formatPronunciation: (Granule) -> String
) {
    val processedInfo = processor.processSentence(text)
    val scrollState = rememberScrollState()

    CommonCJKDisplay(
        text = text,
        modifier = modifier
    ) { _ ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.Start
        ) {
            processedInfo.parts.forEach { part ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    // Character display
                    Text(
                        text = part.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    // Compact reading display
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.height(12.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.Center) {
                            part.granules.forEach { granule ->
                                Text(
                                    text = formatPronunciation(granule),
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 0.5.dp),
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Mandarin display
 */
@Composable
fun MandarinDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    ChineseDisplay(
        text = text,
        processor = MandarinProcessor(),
        modifier = modifier
    ) { granule ->
        if (granule.pinyin != null) {
            PronunciationFormatter.formatMandarinTone(
                granule.pinyin, granule.pinyinTone
            )
        } else {
            granule.char
        }
    }
}

/**
 * Compact Cantonese display
 */
@Composable
fun CantoneseDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    ChineseDisplay(
        text = text,
        processor = CantoneseProcessor(),
        modifier = modifier
    ) { granule ->
        if (granule.jyutping != null) {
            PronunciationFormatter.formatCantoneseTone(
                granule.jyutping, granule.jyutpingTone
            )
        } else {
            granule.char
        }
    }
}
