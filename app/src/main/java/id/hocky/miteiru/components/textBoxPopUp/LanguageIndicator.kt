package id.hocky.miteiru.components.textBoxPopUp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguageIndicator(language: String) {
    val languageText = when(language) {
        "zh" -> "Chinese"
        "ja" -> "Japanese"
        "ko" -> "Korean"
        else -> "Unknown"
    }

    Text(
        text = "Language: $languageText",
        fontSize = 14.sp,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}