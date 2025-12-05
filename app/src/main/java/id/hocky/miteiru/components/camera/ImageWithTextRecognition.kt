package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
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
    // Track the actual container dimensions
    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                // Get the actual container size after layout
                containerWidth = size.width
                containerHeight = size.height
            }
    ) {
        // Show the captured/imported image
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Analyzed image",
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.Fit
        )

        // Only show text boxes once we have container dimensions
        if (containerWidth > 0 && containerHeight > 0) {
            // Show text boxes on the image using actual container dimensions
            textBoxes.forEach { textBox ->
                TextRecognitionWithPopup(
                    textBox = textBox,
                    imageWidth = imageWidth,
                    imageHeight = imageHeight,
                    screenWidth = containerWidth,
                    screenHeight = containerHeight
                )
            }
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
