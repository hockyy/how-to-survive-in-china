package id.hocky.miteiru.components.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val boxPadding = 20f // Padding in pixels to make the box larger
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Calculate scaling and transformed coordinates
    val (scale, offsetX, offsetY) = calculateScalingFactors(
        imageWidth, imageHeight, screenWidth, screenHeight
    )

    val (leftDp, topDp, widthDp, heightDp) = calculateBoxDimensions(
        textBox.boundingBox, scale, offsetX, offsetY, density, boxPadding
    )

    // Modern color scheme based on language
    val baseColor = remember(textBox.language) {
        when (textBox.language) {
            "zh" -> Color(0xFF4CAF50)  // More subdued green
            "ja" -> Color(0xFF2196F3)  // Softer blue
            "ko" -> Color(0xFFFFEB3B)  // Softer yellow
            else -> Color(0xFFE0E0E0)  // Light gray
        }
    }

    // Animate colors and properties based on interaction state
    val borderColor by animateColorAsState(
        targetValue = when {
            isPressed -> baseColor.copy(alpha = 0.9f)
            isHovered -> baseColor.copy(alpha = 1f)
            else -> baseColor.copy(alpha = 0.7f)
        },
        label = "borderColor"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when {
            isHovered -> 6.dp
            else -> 4.dp
        },
        label = "cornerRadius"
    )

    val backgroundAlpha = when {
        isPressed -> 0.15f
        isHovered -> 0.1f
        else -> 0.05f
    }

    // Draw modern box with subtle effects
    Box(
        modifier = Modifier
            .size(width = widthDp, height = heightDp)
            .offset(x = leftDp, y = topDp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(borderColor.copy(alpha = backgroundAlpha))
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick(textBox) }
            .zIndex(3f)
    ) {
        // Language indicator badge
        if (isHovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = textBox.language,
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
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