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
import id.hocky.miteiru.utils.language.JapaneseProcessor

/**
 * Compact Japanese display
 */
@Composable
fun JapaneseDisplay(
    text: String,
    modifier: Modifier = Modifier
) {
    val processor = JapaneseProcessor()
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
                        Text(
                            text = part.reading ?: "",
                            fontSize = 9.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            lineHeight = 10.sp
                        )
                    }
                }
            }
        }
    }
}