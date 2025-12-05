package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import id.hocky.miteiru.components.history.HistoryScreen
import id.hocky.miteiru.data.CaptureRepository
import id.hocky.miteiru.data.entities.PreviousCapture
import id.hocky.miteiru.data.entities.SmallCapture
import id.hocky.miteiru.data.entities.TextBoxConverter
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.ImageTextAnalyzer
import id.hocky.miteiru.utils.createImageCapture
import id.hocky.miteiru.utils.createPreviewView
import id.hocky.miteiru.utils.handleTapToFocus
import id.hocky.miteiru.utils.initializeCamera
import id.hocky.miteiru.utils.loadBitmapFromUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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

    // Zoom state
    var zoomRatio by remember { mutableFloatStateOf(1f) }

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

    // Selected text box for detail screen
    var selectedTextBox by remember { mutableStateOf<ChineseTextBox?>(null) }

    // Navigation states
    var showHistoryScreen by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var historyTab by remember { mutableIntStateOf(0) } // 0 = SmallCapture, 1 = PreviousCapture

    // Track actual preview view dimensions (not screen dimensions!)
    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }

    // Helper for processing the captured image
    val textAnalyzer = remember { ImageTextAnalyzer(context) }
    val repository = remember { CaptureRepository(context) }

    // Image picker for gallery
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

    // Function to load a SmallCapture
    fun loadSmallCapture(capture: SmallCapture) {
        coroutineScope.launch {
            try {
                val file = File(capture.imagePath)
                if (file.exists()) {
                    capturedBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    capturedImageUri = file.toUri()
                    // For SmallCapture, we show just the cropped image with its text
                    val rect = android.graphics.Rect(0, 0, capturedBitmap!!.width, capturedBitmap!!.height)
                    capturedTextBoxes = listOf(
                        ChineseTextBox(
                            text = capture.text,
                            language = capture.language,
                            boundingBox = rect,
                            rotation = 0,
                            imageUri = capturedImageUri
                        )
                    )
                    capturedImageWidth = capturedBitmap!!.width
                    capturedImageHeight = capturedBitmap!!.height
                    isCaptureMode = true
                    showHistoryScreen = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load capture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to load a PreviousCapture
    fun loadPreviousCapture(capture: PreviousCapture) {
        coroutineScope.launch {
            try {
                val file = File(capture.imagePath)
                if (file.exists()) {
                    capturedBitmap = BitmapFactory.decodeFile(file.absolutePath)
                    capturedImageUri = file.toUri()

                    // Convert stored text boxes back to ChineseTextBox
                    val converter = TextBoxConverter()
                    val storedBoxes = converter.toTextBoxList(capture.textBoxesJson)
                    capturedTextBoxes = storedBoxes.map { info ->
                        repository.textBoxInfoToChineseTextBox(info, capturedImageUri)
                    }
                    capturedImageWidth = capturedBitmap!!.width
                    capturedImageHeight = capturedBitmap!!.height
                    isCaptureMode = true
                    showHistoryScreen = false
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load capture", Toast.LENGTH_SHORT).show()
            }
        }
    }

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

    // Show history screen if requested
    if (showHistoryScreen) {
        HistoryScreen(
            onBack = { showHistoryScreen = false },
            onSmallCaptureSelected = { capture -> loadSmallCapture(capture) },
            onPreviousCaptureSelected = { capture -> loadPreviousCapture(capture) }
        )
        return
    }

    // If a text box is selected, show the detail screen
    if (selectedTextBox != null) {
        TextDetailScreen(
            textBox = selectedTextBox!!,
            onBack = { selectedTextBox = null }
        )
        return
    }

    // Import dialog
    if (showImportDialog) {
        ImportOptionsDialog(
            onDismiss = { showImportDialog = false },
            onGallerySelected = {
                imagePickerLauncher.launch("image/*")
            },
            onSmallCaptureSelected = {
                historyTab = 0
                showHistoryScreen = true
            },
            onPreviousCaptureSelected = {
                historyTab = 1
                showHistoryScreen = true
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Always show camera preview with tap-to-focus and pinch-to-zoom
        AndroidView(
            factory = { previewView },
            update = { view ->
                // Get actual preview dimensions after layout
                view.post {
                    previewWidth = view.width
                    previewHeight = view.height
                }
            },
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
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        if (!isCaptureMode) {
                            camera?.let { cam ->
                                val currentZoom = cam.cameraInfo.zoomState.value?.zoomRatio ?: 1f
                                val minZoom = cam.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
                                val maxZoom = cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 10f
                                
                                // Calculate new zoom ratio
                                val newZoom = (currentZoom * zoom).coerceIn(minZoom, maxZoom)
                                zoomRatio = newZoom
                                
                                // Apply zoom
                                cam.cameraControl.setZoomRatio(newZoom)
                            }
                        }
                    }
                }
        )

        // Show focus indicator when tapped
        focusPoint?.let { point ->
            FocusIndicator(point)
        }

        // Show zoom indicator
        if (!isCaptureMode) {
            ZoomIndicator(
                zoomRatio = zoomRatio,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )
        }

        if (isCaptureMode && capturedBitmap != null) {
            // Show image with text recognition boxes
            // Clicking a box opens the detail screen
            ImageWithTextRecognition(
                bitmap = capturedBitmap!!,
                textBoxes = capturedTextBoxes,
                imageWidth = capturedBitmap!!.width,
                imageHeight = capturedBitmap!!.height,
                screenWidth = if (previewWidth > 0) previewWidth else 1080,
                screenHeight = if (previewHeight > 0) previewHeight else 1920,
                onResume = {
                    isCaptureMode = false
                    capturedTextBoxes = emptyList()
                    capturedImageUri = null
                    capturedBitmap = null
                },
                onTextBoxClick = { textBox ->
                    selectedTextBox = textBox
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
                    showImportDialog = true
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
