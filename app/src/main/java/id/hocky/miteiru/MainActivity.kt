package id.hocky.miteiru

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import id.hocky.miteiru.ui.theme.MiteiruTheme
import id.hocky.miteiru.screens.CameraScreen

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - camera can now be used
        } else {
            // Permission denied - in a full app, you would show a message
            // explaining why the camera is needed or disable camera features
        }
    }

    private fun checkCameraPermission() {
        when {
            // Check if camera permission is already granted
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission already granted - can proceed with camera usage
            }

            else -> {
                // Request camera permission using the permission launcher
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display (full screen with insets handling)
        enableEdgeToEdge()
        // Set the content view using Jetpack Compose
        setContent {
            // Apply the application theme
            MiteiruTheme {
                // Scaffold provides the basic material design visual layout structure
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Display the camera view, passing padding from the Scaffold
                    CameraScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        // Check for camera permission when the activity starts
        checkCameraPermission()
    }
}