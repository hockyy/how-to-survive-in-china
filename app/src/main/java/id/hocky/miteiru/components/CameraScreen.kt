package id.hocky.miteiru.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import id.hocky.miteiru.components.CameraView

@Composable
fun CameraScreen(modifier: Modifier = Modifier) {
    // You could add more screen-level UI elements here
    CameraView(modifier = modifier)
}