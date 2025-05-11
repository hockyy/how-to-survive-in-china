package id.hocky.miteiru.components.camera

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun FocusIndicator(
    point: Offset,
    indicatorSize: Float = 80f,
    color: Color = Color(0xFFFFD700), // gold/yellow color
    visibleDurationMillis: Int = 600,
    enterAnimationDuration: Int = 300
) {
    var isVisible by remember { mutableStateOf(true) }

    // Manage indicator visibility lifecycle
    LaunchedEffect(point) {
        isVisible = true
        delay(visibleDurationMillis.toLong())
        isVisible = false
    }

    // Scale animation with bounce effect
    val scaleAnim = remember { Animatable(0.5f) }
    LaunchedEffect(point) {
        scaleAnim.snapTo(0.5f)
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Fade-out animation after visible duration
    val alphaAnim by animateFloatAsState(
        targetValue = if (isVisible) 0.9f else 0f,
        animationSpec = tween(durationMillis = enterAnimationDuration)
    )

    val density = LocalDensity.current
    val offsetX = with(density) { (point.x - indicatorSize / 2).toDp() }
    val offsetY = with(density) { (point.y - indicatorSize / 2).toDp() }

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(with(density) { indicatorSize.toDp() })
    ) {
        Canvas(modifier = Modifier.size(with(density) { indicatorSize.toDp() })) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) * scaleAnim.value

            // Shadow gradient effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                    center = center,
                    radius = radius * 1.4f
                ),
                radius = radius * 1.4f,
                center = center
            )

            // Main indicator circle
            drawCircle(
                color = color,
                radius = radius,
                style = Stroke(width = 3.dp.toPx()),
                alpha = alphaAnim
            )

            // Crosshair lines
            val lineLength = radius / 3
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = color,
                start = Offset(center.x - lineLength, center.y),
                end = Offset(center.x + lineLength, center.y),
                strokeWidth = strokeWidth,
                alpha = alphaAnim
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y - lineLength),
                end = Offset(center.x, center.y + lineLength),
                strokeWidth = strokeWidth,
                alpha = alphaAnim
            )
        }
    }
}