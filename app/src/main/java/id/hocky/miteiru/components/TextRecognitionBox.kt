package id.hocky.miteiru.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import id.hocky.miteiru.utils.ChineseTextBox
import android.graphics.Rect
import androidx.compose.ui.unit.Dp

@Composable
fun TextRecognitionBox(
    textBox: ChineseTextBox,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    onClick: (ChineseTextBox) -> Unit = {}
) {
    val density = LocalDensity.current.density
    val boxPadding = 8f // Padding in pixels to make the box larger

    // Calculate scaling and transformed coordinates
    val (scale, offsetX, offsetY) = calculateScalingFactors(
        imageWidth, imageHeight, screenWidth, screenHeight
    )

    val (leftDp, topDp, widthDp, heightDp) = calculateBoxDimensions(
        textBox.boundingBox, scale, offsetX, offsetY, density, boxPadding
    )

    // Color based on language
    val boxColor = remember(textBox.language) {
        when (textBox.language) {
            "zh" -> Color.Green
            "ja" -> Color.Blue
            "ko" -> Color.Yellow
            else -> Color.White
        }
    }

    // Draw box with clickable
    Box(
        modifier = Modifier
            .size(width = widthDp, height = heightDp)
            .offset(x = leftDp, y = topDp)
            .border(2.dp, boxColor, RoundedCornerShape(4.dp))
            .clickable { onClick(textBox) }
            .zIndex(3f)
    )
}

private fun calculateScalingFactors(
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int
): Triple<Float, Float, Float> {
    // Handle orientation
    val (finalImageWidth, finalImageHeight) = if (imageWidth > imageHeight) {
        imageHeight to imageWidth
    } else {
        imageWidth to imageHeight
    }

    // Calculate scale for ContentScale.Fit
    val scale = minOf(
        screenWidth.toFloat() / finalImageWidth.toFloat(),
        screenHeight.toFloat() / finalImageHeight.toFloat()
    )

    // Calculate centering offsets
    val offsetX = (screenWidth - finalImageWidth * scale) / 2f
    val offsetY = (screenHeight - finalImageHeight * scale) / 2f

    return Triple(scale, offsetX, offsetY)
}

private fun calculateBoxDimensions(
    rect: Rect,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    density: Float,
    padding: Float = 0f
): Quadruple<Dp, Dp, Dp, Dp> {
    // Scale coordinates with added padding
    val paddingScaled = padding * scale

    // Adjust the left and top to account for the padding
    val scaledLeft = (rect.left * scale) - paddingScaled + offsetX
    val scaledTop = (rect.top * scale) - paddingScaled + offsetY

    // Add padding to both width and height
    val scaledWidth = (rect.width() * scale) + (paddingScaled * 2)
    val scaledHeight = (rect.height() * scale) + (paddingScaled * 2)

    // Convert to dp for Compose
    return Quadruple(
        (scaledLeft / density).dp,
        (scaledTop / density).dp,
        (scaledWidth / density).dp,
        (scaledHeight / density).dp
    )
}

// Helper class for returning 4 values together
private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)