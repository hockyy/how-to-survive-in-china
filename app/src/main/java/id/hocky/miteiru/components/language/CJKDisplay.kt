package id.hocky.miteiru.components.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Compact base component for CJK display
 */
@Composable
fun CommonCJKDisplay(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content(text)
    }
}