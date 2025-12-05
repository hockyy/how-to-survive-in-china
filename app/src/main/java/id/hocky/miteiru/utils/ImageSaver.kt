package id.hocky.miteiru.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Saves a bitmap to the user's Pictures directory using MediaStore.
 */
fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Result<Uri> {
    return try {
        val filename = "miteiru_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Miteiru")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Result.failure(IllegalStateException("Failed to insert MediaStore row"))

        resolver.openOutputStream(uri).use { stream: OutputStream? ->
            if (stream == null) {
                return Result.failure(IllegalStateException("Failed to open output stream"))
            }
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                return Result.failure(IllegalStateException("Failed to compress bitmap"))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        Result.success(uri)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

