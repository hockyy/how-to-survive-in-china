package id.hocky.miteiru.components.textBoxPopUp

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CopyButton(
    onCopy: () -> Unit
) {
    Button(
        onClick = onCopy,
        modifier = Modifier.fillMaxWidth(0.8f)
    ) {
        Text("Copy Text")
    }
}