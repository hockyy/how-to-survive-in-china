package id.hocky.miteiru.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.ImageTextAnalyzer
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Composable
fun CameraView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    // Create a PreviewView with FIT_CENTER scale type
    val previewView = remember {
        PreviewView(context).apply {
            // This makes the preview fit within bounds like ContentScale.Fit
            scaleType = PreviewView.ScaleType.FIT_CENTER

            // Optional: Set the background color to be visible in letterboxed areas
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Create a properly configured ImageCapture
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    // States for capture mode
    var isCaptureMode by remember { mutableStateOf(false) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var capturedTextBoxes by remember { mutableStateOf<List<ChineseTextBox>>(emptyList()) }
    var capturedImageWidth by remember { mutableStateOf(0) }
    var capturedImageHeight by remember { mutableStateOf(0) }

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
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                capturedBitmap = bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(previewView) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Function to capture photo
    fun takePhoto() {
        // Create output file
        val photoFile = createImageFile(context)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    capturedImageUri = savedUri

                    // Process image for text recognition
                    textAnalyzer.analyzeImage(savedUri) { fullText, textBoxes, width, height ->
                        capturedTextBoxes = textBoxes
                        capturedImageWidth = width
                        capturedImageHeight = height
                        isCaptureMode = true
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        context, "Capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Always show camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

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
                TextRecognitionBox(
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
                onClick = { takePhoto() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text("Capture")
            }
        }
    }
}

// Helper function to create a unique file for storing the image
private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}