import android.graphics.Bitmap
import android.graphics.Rect
import kotlin.math.max
import kotlin.math.min

fun cropBitmapByRect(bitmap: Bitmap, rect: Rect, paddingPx: Int = 10): Bitmap? {
    // Ensure the rect is within the bitmap bounds with padding
    val safeRect = Rect(
        max(0, rect.left - paddingPx),
        max(0, rect.top - paddingPx),
        min(bitmap.width, rect.right + paddingPx),
        min(bitmap.height, rect.bottom + paddingPx)
    )

    // If the rect is invalid or too small, return null
    if (safeRect.width() <= 0 || safeRect.height() <= 0) {
        return null
    }

    // Return a cropped bitmap
    return Bitmap.createBitmap(
        bitmap,
        safeRect.left,
        safeRect.top,
        safeRect.width(),
        safeRect.height()
    )
}