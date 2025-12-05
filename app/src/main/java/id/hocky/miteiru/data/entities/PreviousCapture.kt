package id.hocky.miteiru.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject

/**
 * Represents a full page capture with all detected text boxes.
 * Stores the full image, all text boxes, and cached analysis for each.
 */
@Entity(tableName = "previous_captures")
@TypeConverters(TextBoxConverter::class)
data class PreviousCapture(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,          // Path to saved full image
    val textBoxesJson: String,      // JSON array of text boxes with their analysis
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Data class for serializing text box info with analysis
 */
data class TextBoxInfo(
    val text: String,
    val language: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val analysis: String? = null,
    val sourceLanguage: String? = null
)

class TextBoxConverter {
    @TypeConverter
    fun fromTextBoxList(textBoxes: List<TextBoxInfo>): String {
        val jsonArray = JSONArray()
        textBoxes.forEach { box ->
            val obj = JSONObject().apply {
                put("text", box.text)
                put("language", box.language)
                put("left", box.left)
                put("top", box.top)
                put("right", box.right)
                put("bottom", box.bottom)
                put("analysis", box.analysis ?: JSONObject.NULL)
                put("sourceLanguage", box.sourceLanguage ?: JSONObject.NULL)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toTextBoxList(json: String): List<TextBoxInfo> {
        if (json.isEmpty()) return emptyList()
        val jsonArray = JSONArray(json)
        val list = mutableListOf<TextBoxInfo>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                TextBoxInfo(
                    text = obj.getString("text"),
                    language = obj.getString("language"),
                    left = obj.getInt("left"),
                    top = obj.getInt("top"),
                    right = obj.getInt("right"),
                    bottom = obj.getInt("bottom"),
                    analysis = if (obj.isNull("analysis")) null else obj.getString("analysis"),
                    sourceLanguage = if (obj.isNull("sourceLanguage")) null else obj.getString("sourceLanguage")
                )
            )
        }
        return list
    }
}

