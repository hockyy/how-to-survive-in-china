package id.hocky.miteiru.components.camera

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun FocusIndicator(point: Offset) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .offset(
                x = with(LocalDensity.current) { (point.x - 40.dp.toPx()).toDp() },
                y = with(LocalDensity.current) { (point.y - 40.dp.toPx()).toDp() }
            )
            .border(2.dp, Color.Yellow, CircleShape)
    )
}