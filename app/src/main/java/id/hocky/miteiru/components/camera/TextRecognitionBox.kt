package id.hocky.miteiru.components.camera

import android.graphics.Rect
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import id.hocky.miteiru.utils.ChineseTextBox

/**
 * Displays a bounding box overlay for detected text with proper coordinate transformation.
 * Handles ContentScale.Fit transformation from image coordinates to screen coordinates.
 */
@Composable
fun TextRecognitionBox(
    textBox: ChineseTextBox,
    imageWidth: Int,
    imageHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    onClick: (ChineseTextBox) -> Unit = {}
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    // Calculate transformation and box dimensions
    val boxDimensions = remember(textBox.boundingBox, imageWidth, imageHeight, screenWidth, screenHeight) {
        calculateBoxDimensions(
            rect = textBox.boundingBox,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            containerWidth = screenWidth,
            containerHeight = screenHeight,
            density = density.density
        )
    }

    // Language-based color scheme
    val baseColor = remember(textBox.language) {
        when (textBox.language) {
            "zh" -> Color(0xFF4CAF50)  // Green for Chinese
            "ja" -> Color(0xFF2196F3)  // Blue for Japanese
            "ko" -> Color(0xFFFFEB3B)  // Yellow for Korean
            else -> Color(0xFF9E9E9E)  // Gray for others
        }
    }

    // Animated colors based on interaction
    val borderColor by animateColorAsState(
        targetValue = when {
            isPressed -> baseColor.copy(alpha = 1f)
            isHovered -> baseColor.copy(alpha = 0.9f)
            else -> baseColor.copy(alpha = 0.7f)
        },
        label = "borderColor"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isHovered) 6.dp else 4.dp,
        label = "cornerRadius"
    )

    val backgroundAlpha = when {
        isPressed -> 0.2f
        isHovered -> 0.12f
        else -> 0.06f
    }

    val borderWidth by animateDpAsState(
        targetValue = if (isHovered) 2.dp else 1.5.dp,
        label = "borderWidth"
    )

    // Draw the bounding box using offset and size modifiers
    Box(
        modifier = Modifier
            .offset(x = boxDimensions.leftDp, y = boxDimensions.topDp)
            .size(width = boxDimensions.widthDp, height = boxDimensions.heightDp)
            .clip(RoundedCornerShape(cornerRadius))
            .background(borderColor.copy(alpha = backgroundAlpha))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(cornerRadius)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick(textBox)
            }
            .zIndex(3f)
    ) {
        // Language badge on hover
        if (isHovered) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        color = borderColor.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = textBox.language.uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Represents box dimensions in Dp for Compose
 */
private data class BoxDimensionsDp(
    val leftDp: Dp,
    val topDp: Dp,
    val widthDp: Dp,
    val heightDp: Dp
)

/**
 * Calculate the bounding box dimensions in Dp.
 * 
 * This function handles the ContentScale.Fit transformation:
 * 1. Calculate how the image is scaled to fit the container
 * 2. Calculate the centering offset
 * 3. Transform the bounding box coordinates
 * 4. Convert to Dp for Compose
 */
private fun calculateBoxDimensions(
    rect: Rect,
    imageWidth: Int,
    imageHeight: Int,
    containerWidth: Int,
    containerHeight: Int,
    density: Float,
    paddingPx: Float = 4f
): BoxDimensionsDp {
    // Avoid division by zero
    if (imageWidth == 0 || imageHeight == 0 || containerWidth == 0 || containerHeight == 0) {
        return BoxDimensionsDp(0.dp, 0.dp, 0.dp, 0.dp)
    }

    // Calculate scale factor - ContentScale.Fit uses the minimum scale
    val scaleX = containerWidth.toFloat() / imageWidth.toFloat()
    val scaleY = containerHeight.toFloat() / imageHeight.toFloat()
    val scale = minOf(scaleX, scaleY)

    // Calculate the actual displayed image size after scaling
    val displayedWidth = imageWidth * scale
    val displayedHeight = imageHeight * scale

    // Calculate centering offsets (where the scaled image starts in the container)
    val offsetX = (containerWidth - displayedWidth) / 2f
    val offsetY = (containerHeight - displayedHeight) / 2f

    // Transform bounding box coordinates from image space to container space
    // Formula: containerCoord = (imageCoord * scale) + offset
    val left = (rect.left * scale) + offsetX - paddingPx
    val top = (rect.top * scale) + offsetY - paddingPx
    val right = (rect.right * scale) + offsetX + paddingPx
    val bottom = (rect.bottom * scale) + offsetY + paddingPx

    // Convert from pixels to Dp
    return BoxDimensionsDp(
        leftDp = (left / density).dp,
        topDp = (top / density).dp,
        widthDp = ((right - left) / density).dp,
        heightDp = ((bottom - top) / density).dp
    )
}
