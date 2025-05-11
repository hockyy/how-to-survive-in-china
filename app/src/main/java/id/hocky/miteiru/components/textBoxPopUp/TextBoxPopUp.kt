package id.hocky.miteiru.components.textBoxPopUp

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cropBitmapByRect
import copyToClipboard
import id.hocky.miteiru.utils.*
import id.hocky.miteiru.utils.language.MiteiruProcess

@Composable
fun TextBoxPopUp(
    textBox: ChineseTextBox,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // State for selected pronunciation type
    var pronunciationType by remember { mutableStateOf(
        when (textBox.language) {
            "zh" -> MiteiruProcess.MANDARIN
            "ja" -> MiteiruProcess.JAPANESE
            else -> MiteiruProcess.MANDARIN
        }
    )}

    // Load and crop bitmap when component is created
    LaunchedEffect(textBox) {
        textBox.imageUri?.let { uri ->
            try {
                val fullBitmap = loadBitmapFromUri(context, uri)
                croppedBitmap = cropBitmapByRect(fullBitmap, textBox.boundingBox, 20)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display the cropped image
                croppedBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Text region",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Original text
                TextContent(textBox.text)

                // Language selection for Chinese
                if (textBox.language == "zh") {
                    LanguageSelector(
                        selectedLanguage = pronunciationType,
                        onLanguageSelected = { pronunciationType = it }
                    )
                }

                // Pronunciation display based on language
                PronunciationDisplay(
                    text = textBox.text,
                    language = textBox.language,
                    pronunciationType = pronunciationType
                )

                // Language indicator
                LanguageIndicator(language = textBox.language)

                // Copy button
                CopyButton(
                    onCopy = {
                        copyToClipboard(context, textBox.text)
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}