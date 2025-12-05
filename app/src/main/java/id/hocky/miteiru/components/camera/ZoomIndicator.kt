package id.hocky.miteiru.components.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Displays the current zoom level temporarily when user zooms.
 */
@Composable
fun ZoomIndicator(
    zoomRatio: Float,
    modifier: Modifier = Modifier
) {
    var showIndicator by remember { mutableStateOf(false) }
    var currentZoom by remember { mutableFloatStateOf(zoomRatio) }

    // Show indicator when zoom changes
    LaunchedEffect(zoomRatio) {
        if (zoomRatio != currentZoom) {
            currentZoom = zoomRatio
            showIndicator = true
            delay(1500) // Show for 1.5 seconds
            showIndicator = false
        }
    }

    AnimatedVisibility(
        visible = showIndicator,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = String.format("%.1fx", zoomRatio),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

