package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.ImageTextAnalyzer
import id.hocky.miteiru.utils.createImageCapture
import id.hocky.miteiru.utils.createPreviewView
import id.hocky.miteiru.utils.handleTapToFocus
import id.hocky.miteiru.utils.initializeCamera
import id.hocky.miteiru.utils.loadBitmapFromUri
import id.hocky.miteiru.utils.takePhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun CameraView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val coroutineScope = rememberCoroutineScope()

    // Store a reference to the camera
    var camera by remember { mutableStateOf<Camera?>(null) }

    // Focus indicator state
    var focusPoint by remember { mutableStateOf<Offset?>(null) }

    // Create a PreviewView with FIT_CENTER scale type
    val previewView = remember { createPreviewView(context) }

    // Create a properly configured ImageCapture
    val imageCapture = remember { createImageCapture() }

    // States for capture mode
    var isCaptureMode by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedTextBoxes by remember { mutableStateOf<List<ChineseTextBox>>(emptyList()) }
    var capturedImageWidth by remember { mutableIntStateOf(0) }
    var capturedImageHeight by remember { mutableIntStateOf(0) }

    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp * LocalDensity.current.density
    val screenHeight = configuration.screenHeightDp * LocalDensity.current.density

    // Helper for processing the captured image
    val textAnalyzer = remember { ImageTextAnalyzer(context) }

    // Load bitmap when URI changes
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            try {
                capturedBitmap = loadBitmapFromUri(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Initialize camera when view is created
    LaunchedEffect(previewView) {
        initializeCamera(
            cameraProviderFuture,
            lifecycleOwner,
            previewView,
            imageCapture,
            context
        ) { boundCamera ->
            camera = boundCamera
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Always show camera preview with tap-to-focus
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        if (!isCaptureMode) {
                            handleTapToFocus(
                                camera,
                                previewView,
                                offset.x,
                                offset.y
                            ) { newFocusPoint ->
                                focusPoint = newFocusPoint
                                // Hide focus indicator after delay
                                coroutineScope.launch {
                                    delay(1000)
                                    focusPoint = null
                                }
                            }
                        }
                    }
                }
        )

        // Show focus indicator when tapped
        focusPoint?.let { point ->
            FocusIndicator(point)
        }

        if (isCaptureMode && capturedBitmap != null) {
            // Show the captured image
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Captured image",
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                contentScale = ContentScale.Fit
            )

            // Show text boxes on frozen screen
            capturedTextBoxes.forEach { textBox ->
                TextRecognitionWithPopup(
                    textBox = textBox,
                    imageWidth = capturedImageWidth,
                    imageHeight = capturedImageHeight,
                    screenWidth = screenWidth.toInt(),
                    screenHeight = screenHeight.toInt()
                )
            }

            // Button to resume camera
            Button(
                onClick = {
                    isCaptureMode = false
                    capturedTextBoxes = emptyList()
                    capturedImageUri = null
                    capturedBitmap = null
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(2f)
            ) {
                Text("Resume Camera")
            }
        } else {
            // Capture button
            Button(
                onClick = {
                    takePhoto(
                        context,
                        imageCapture,
                        cameraExecutor,
                        onImageCaptured = { uri ->
                            capturedImageUri = uri
                            // Process image for text recognition
                            textAnalyzer.analyzeImage(uri) { fullText, textBoxes, width, height ->
                                capturedTextBoxes = textBoxes
                                capturedImageWidth = width
                                capturedImageHeight = height
                                isCaptureMode = true
                            }
                        },
                        onError = { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Capture")
            }
        }
    }
}