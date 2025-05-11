package id.hocky.miteiru.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import id.hocky.miteiru.utils.ChineseTextBox

@Composable
fun TextBoxPopup(
    textBox: ChineseTextBox,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                // Display the text
                Text(
                    text = textBox.text,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Language indicator
                val languageText = when(textBox.language) {
                    "zh" -> "Chinese"
                    "ja" -> "Japanese"
                    "ko" -> "Korean"
                    else -> "Unknown"
                }

                Text(
                    text = "Language: $languageText",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Copy button
                Button(
                    onClick = {
                        copyToClipboard(context, textBox.text)
                        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Copy Text")
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Text", text)
    clipboardManager.setPrimaryClip(clipData)
}

@Composable
fun TextRecognitionWithPopup(
    textBox: ChineseTextBox,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int
) {
    var showPopup by remember { mutableStateOf(false) }

    // Draw the recognition box
    TextRecognitionBox(
        textBox = textBox,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        onClick = { showPopup = true }
    )

    // Show popup when clicked
    if (showPopup) {
        TextBoxPopup(
            textBox = textBox,
            onDismiss = { showPopup = false }
        )
    }
}