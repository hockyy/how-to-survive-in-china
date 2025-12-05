package id.hocky.miteiru.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import id.hocky.miteiru.data.entities.PreviousCapture
import id.hocky.miteiru.data.entities.SmallCapture
import id.hocky.miteiru.data.entities.TextBoxInfo
import id.hocky.miteiru.data.entities.TextBoxConverter
import id.hocky.miteiru.utils.ChineseTextBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class CaptureRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val smallCaptureDao = database.smallCaptureDao()
    private val previousCaptureDao = database.previousCaptureDao()

    private val capturesDir: File
        get() = File(context.filesDir, "captures").also { it.mkdirs() }

    private val smallCapturesDir: File
        get() = File(capturesDir, "small").also { it.mkdirs() }

    private val fullCapturesDir: File
        get() = File(capturesDir, "full").also { it.mkdirs() }

    // SmallCapture operations
    fun getAllSmallCaptures() = smallCaptureDao.getAllSmallCaptures()
    fun getRecentSmallCaptures(limit: Int) = smallCaptureDao.getRecentSmallCaptures(limit)
    suspend fun getSmallCaptureById(id: Long) = smallCaptureDao.getSmallCaptureById(id)

    suspend fun saveSmallCapture(
        bitmap: Bitmap,
        text: String,
        language: String
    ): Long = withContext(Dispatchers.IO) {
        val filename = "small_${System.currentTimeMillis()}.jpg"
        val file = File(smallCapturesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        val smallCapture = SmallCapture(
            imagePath = file.absolutePath,
            text = text,
            language = language
        )
        smallCaptureDao.insert(smallCapture)
    }

    suspend fun updateSmallCaptureAnalysis(id: Long, analysis: String, sourceLanguage: String) {
        smallCaptureDao.updateAnalysis(id, analysis, sourceLanguage)
    }

    suspend fun deleteSmallCapture(id: Long) = withContext(Dispatchers.IO) {
        smallCaptureDao.getSmallCaptureById(id)?.let { capture ->
            File(capture.imagePath).delete()
            smallCaptureDao.deleteById(id)
        }
    }

    // PreviousCapture operations
    fun getAllPreviousCaptures() = previousCaptureDao.getAllPreviousCaptures()
    fun getRecentPreviousCaptures(limit: Int) = previousCaptureDao.getRecentPreviousCaptures(limit)
    suspend fun getPreviousCaptureById(id: Long) = previousCaptureDao.getPreviousCaptureById(id)

    suspend fun savePreviousCapture(
        bitmap: Bitmap,
        textBoxes: List<ChineseTextBox>
    ): Long = withContext(Dispatchers.IO) {
        val filename = "full_${System.currentTimeMillis()}.jpg"
        val file = File(fullCapturesDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        val textBoxInfoList = textBoxes.map { box ->
            TextBoxInfo(
                text = box.text,
                language = box.language,
                left = box.boundingBox.left,
                top = box.boundingBox.top,
                right = box.boundingBox.right,
                bottom = box.boundingBox.bottom
            )
        }

        val converter = TextBoxConverter()
        val previousCapture = PreviousCapture(
            imagePath = file.absolutePath,
            textBoxesJson = converter.fromTextBoxList(textBoxInfoList)
        )
        previousCaptureDao.insert(previousCapture)
    }

    suspend fun updatePreviousCaptureAnalysis(
        captureId: Long,
        boxIndex: Int,
        analysis: String,
        sourceLanguage: String
    ) = withContext(Dispatchers.IO) {
        val capture = previousCaptureDao.getPreviousCaptureById(captureId) ?: return@withContext
        val converter = TextBoxConverter()
        val textBoxes = converter.toTextBoxList(capture.textBoxesJson).toMutableList()
        
        if (boxIndex in textBoxes.indices) {
            textBoxes[boxIndex] = textBoxes[boxIndex].copy(
                analysis = analysis,
                sourceLanguage = sourceLanguage
            )
            previousCaptureDao.updateTextBoxes(captureId, converter.fromTextBoxList(textBoxes))
        }
    }

    suspend fun deletePreviousCapture(id: Long) = withContext(Dispatchers.IO) {
        previousCaptureDao.getPreviousCaptureById(id)?.let { capture ->
            File(capture.imagePath).delete()
            previousCaptureDao.deleteById(id)
        }
    }

    // Convert stored TextBoxInfo back to ChineseTextBox
    fun textBoxInfoToChineseTextBox(info: TextBoxInfo, imageUri: android.net.Uri? = null): ChineseTextBox {
        return ChineseTextBox(
            text = info.text,
            language = info.language,
            boundingBox = Rect(info.left, info.top, info.right, info.bottom),
            rotation = 0,
            imageUri = imageUri
        )
    }
}

