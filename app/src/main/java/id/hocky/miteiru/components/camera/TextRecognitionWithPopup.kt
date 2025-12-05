package id.hocky.miteiru.components.camera

import androidx.compose.runtime.*
import id.hocky.miteiru.utils.ChineseTextBox

@Composable
fun TextRecognitionWithPopup(
    textBox: ChineseTextBox,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    onTextBoxClick: (ChineseTextBox) -> Unit = {}
) {
    // Draw the recognition box
    TextRecognitionBox(
        textBox = textBox,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        onClick = { onTextBoxClick(textBox) }
    )
}
