package id.hocky.miteiru.components.camera

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.ImageCapture
import id.hocky.miteiru.utils.ImageTextAnalyzer
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.takePhoto
import java.util.concurrent.Executor

fun captureImage(
    context: Context,
    imageCapture: ImageCapture,
    cameraExecutor: Executor,
    onImageCaptured: (Uri) -> Unit
) {
    takePhoto(
        context = context,
        imageCapture = imageCapture,
        executor = cameraExecutor,
        onImageCaptured = onImageCaptured,
        onError = { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    )
}

fun processImage(
    uri: Uri,
    textAnalyzer: ImageTextAnalyzer,
    onProcessed: (textBoxes: List<ChineseTextBox>, width: Int, height: Int) -> Unit
) {
    textAnalyzer.analyzeImage(uri) { fullText, textBoxes, width, height ->
        onProcessed(textBoxes, width, height)
    }
}