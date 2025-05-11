package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.ImageTextAnalyzer
import id.hocky.miteiru.utils.createImageCapture
import id.hocky.miteiru.utils.createPreviewView
import id.hocky.miteiru.utils.handleTapToFocus
import id.hocky.miteiru.utils.initializeCamera
import id.hocky.miteiru.utils.loadBitmapFromUri
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

    // Image picker
    val imagePickerLauncher = setupImagePicker(
        onImageSelected = { uri ->
            capturedImageUri = uri
            processImage(uri, textAnalyzer) { textBoxes, width, height ->
                capturedTextBoxes = textBoxes
                capturedImageWidth = width
                capturedImageHeight = height
                isCaptureMode = true
            }
        }
    )

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
            // Display the captured/imported image with text recognition
            ImageWithTextRecognition(
                bitmap = capturedBitmap!!,
                textBoxes = capturedTextBoxes,
                imageWidth = capturedImageWidth,
                imageHeight = capturedImageHeight,
                screenWidth = screenWidth.toInt(),
                screenHeight = screenHeight.toInt(),
                onResume = {
                    isCaptureMode = false
                    capturedTextBoxes = emptyList()
                    capturedImageUri = null
                    capturedBitmap = null
                }
            )
        } else {
            // Show camera controls
            CameraControls(
                onCapture = {
                    captureImage(
                        context = context,
                        imageCapture = imageCapture,
                        cameraExecutor = cameraExecutor,
                        onImageCaptured = { uri ->
                            capturedImageUri = uri
                            processImage(uri, textAnalyzer) { textBoxes, width, height ->
                                capturedTextBoxes = textBoxes
                                capturedImageWidth = width
                                capturedImageHeight = height
                                isCaptureMode = true
                            }
                        }
                    )
                },
                onImport = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.align(Alignment.BottomCenter) // This is the key fix
            )
        }
    }
}