package id.hocky.miteiru.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a cropped region from a text bounding box.
 * Stores the cropped image, detected text, and any AI analysis.
 */
@Entity(tableName = "small_captures")
data class SmallCapture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,          // Path to saved cropped image
    val text: String,               // Detected text
    val language: String,           // Detected language (zh, ja, ko, etc.)
    val analysis: String? = null,   // AI analysis/translation result (markdown)
    val sourceLanguage: String? = null, // Source language used for analysis
    val createdAt: Long = System.currentTimeMillis()
)

