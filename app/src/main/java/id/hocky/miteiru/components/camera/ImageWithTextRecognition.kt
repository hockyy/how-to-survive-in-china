package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.hocky.miteiru.utils.ChineseTextBox

@Composable
fun ImageWithTextRecognition(
    bitmap: Bitmap,
    textBoxes: List<ChineseTextBox>,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Show the captured/imported image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Analyzed image",
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.Fit
        )

        // Show text boxes on the image
        textBoxes.forEach { textBox ->
            TextRecognitionWithPopup(
                textBox = textBox,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )
        }

        // Button to resume camera
        Button(
            onClick = onResume,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(2f)
        ) {
            Text("Resume Camera")
        }
    }
}