package id.hocky.miteiru.components

import androidx.compose.runtime.*
import id.hocky.miteiru.utils.ChineseTextBox



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