package id.hocky.miteiru.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.io.IOException

class ImageTextAnalyzer(private val context: Context) {
    private val textRecognizer =
        TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    fun analyzeImage(
        imageUri: Uri,
        onAnalysisComplete: (String, List<ChineseTextBox>, Int, Int) -> Unit
    ) {
        try {
            // Load bitmap from Uri to get dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(imageUri),
                null,
                options
            )

            val width = options.outWidth
            val height = options.outHeight

            Log.d("ImageTextAnalyzer", "Image dimensions: ${width}x${height}")

            // Create an InputImage for ML Kit - this handles orientation automatically
            val image = InputImage.fromFilePath(context, imageUri)

            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedText = visionText.text
                    Log.d("ImageTextAnalyzer", "Text detected: $detectedText")

                    val textBoxes = mutableListOf<ChineseTextBox>()

                    for (block in visionText.textBlocks) {
                        block.boundingBox?.let { boundingBox ->
                            val language = detectLanguage(block.text)

                            val textBox = ChineseTextBox(
                                boundingBox = boundingBox,
                                text = block.text,
                                rotation = 0, // File-based InputImage handles rotation automatically
                                language = language,
                                confidence = 0f
                            )

                            textBoxes.add(textBox)
                        }
                    }

                    if (textBoxes.isNotEmpty()) {
                        onAnalysisComplete(detectedText, textBoxes, width, height)
                    } else {
                        Log.d("ImageTextAnalyzer", "No text boxes detected")
                        onAnalysisComplete("", emptyList(), width, height)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ImageTextAnalyzer", "Text recognition failed", e)
                }

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("ImageTextAnalyzer", "Error processing image", e)
        }
    }

    private fun detectLanguage(text: String): String {
        return when {
            text.any { it.code in 0x4E00..0x9FFF } -> "zh"
            text.any { it.code in 0x3040..0x309F || it.code in 0x30A0..0x30FF } -> "ja"
            text.any { it.code in 0xAC00..0xD7A3 } -> "ko"
            else -> "en"
        }
    }
}