package id.hocky.miteiru.components.camera

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CameraControls(
    onCapture: () -> Unit,
    onImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        // Import button
        Button(onClick = onImport) {
            Text("Import Image")
        }

        // Capture button
        Button(onClick = onCapture) {
            Text("Capture")
        }
    }
}