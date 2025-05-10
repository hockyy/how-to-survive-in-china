package id.hocky.miteiru.utils

import android.graphics.Rect

/**
 * A data class that contains information about a detected text box including:
 * - The bounding box coordinates
 * - The text content
 * - The image rotation when detected
 * - The language identification
 * - Additional metadata
 */
data class ChineseTextBox(
    val boundingBox: Rect,        // The bounding rectangle
    val text: String,             // The actual text content
    val rotation: Int,            // The rotation of the image (0, 90, 180, 270)
    val language: String = "",    // Language code (e.g. "zh", "en", "ja")
    val confidence: Float = 0f,   // Confidence score of recognition
    val metadata: Map<String, Any> = emptyMap() // For future extensions
) {
    // Additional helper methods
    fun width(): Int = boundingBox.width()
    fun height(): Int = boundingBox.height()

    // You could add utility methods here, for example:
    fun meetsMinimumConfidence(threshold: Float): Boolean = confidence >= threshold

    // Method to create a copy with updated metadata
    fun withMetadata(key: String, value: Any): ChineseTextBox {
        val updatedMetadata = metadata.toMutableMap().apply {
            this[key] = value
        }
        return this.copy(metadata = updatedMetadata)
    }
}