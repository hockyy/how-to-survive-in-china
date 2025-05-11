package id.hocky.miteiru.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cropBitmapByRect
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.loadBitmapFromUri


private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Text", text)
    clipboardManager.setPrimaryClip(clipData)
}

// In a suitable utils file, e.g., ImageUtils.kt
fun cropBitmapByRect(bitmap: Bitmap, rect: Rect): Bitmap {
    // Ensure we don't crop outside the image bounds
    val safeRect = Rect(
        kotlin.math.max(0, rect.left),
        kotlin.math.max(0, rect.top),
        kotlin.math.min(bitmap.width, rect.right),
        kotlin.math.min(bitmap.height, rect.bottom)
    )

    // Return a cropped bitmap
    return Bitmap.createBitmap(
        bitmap,
        safeRect.left,
        safeRect.top,
        safeRect.width(),
        safeRect.height()
    )
}
@Composable
fun TextBoxPopup(
    textBox: ChineseTextBox,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load and crop bitmap when component is created
    LaunchedEffect(textBox) {
        textBox.imageUri?.let { uri ->
            try {
                // Load the full bitmap
                val fullBitmap = loadBitmapFromUri(context, uri)

                // Crop it based on the bounding box
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
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // The rest of your popup content remains the same
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