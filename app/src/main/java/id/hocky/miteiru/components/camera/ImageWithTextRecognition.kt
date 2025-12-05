package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.hocky.miteiru.data.CaptureRepository
import id.hocky.miteiru.utils.ChineseTextBox
import kotlinx.coroutines.launch

@Composable
fun ImageWithTextRecognition(
    bitmap: Bitmap,
    textBoxes: List<ChineseTextBox>,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    onResume: () -> Unit,
    onTextBoxClick: (ChineseTextBox) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { CaptureRepository(context) }
    var isSaved by remember { mutableStateOf(false) }

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
                    screenHeight = containerHeight,
                    onTextBoxClick = onTextBoxClick
                )
            }
        }

        // Buttons row
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(2f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Save button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        repository.savePreviousCapture(bitmap, textBoxes)
                        isSaved = true
                        Toast.makeText(context, "Saved to history!", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isSaved
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Text(if (isSaved) "Saved" else "Save")
            }
            
            // Resume button
            Button(onClick = onResume) {
                Text("Resume Camera")
            }
        }
    }
}
