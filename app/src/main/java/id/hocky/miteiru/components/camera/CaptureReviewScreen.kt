package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import copyToClipboard
import id.hocky.miteiru.components.textBoxPopUp.CopyButton
import id.hocky.miteiru.components.textBoxPopUp.LanguageIndicator
import id.hocky.miteiru.components.textBoxPopUp.LanguageSelector
import id.hocky.miteiru.components.textBoxPopUp.OpenRouterSidebar
import id.hocky.miteiru.components.textBoxPopUp.PronunciationDisplay
import id.hocky.miteiru.components.textBoxPopUp.TextContent
import id.hocky.miteiru.components.textBoxPopUp.modelOptions
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.OpenRouterClient
import id.hocky.miteiru.utils.saveBitmapToGallery
import id.hocky.miteiru.utils.language.MiteiruProcess
import kotlinx.coroutines.launch

@Composable
fun CaptureReviewScreen(
    bitmap: Bitmap,
    textBoxes: List<ChineseTextBox>,
    imageWidth: Int,
    imageHeight: Int,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var containerWidth by remember { mutableIntStateOf(0) }
    var containerHeight by remember { mutableIntStateOf(0) }
    var selectedBox by remember { mutableStateOf(textBoxes.firstOrNull()) }

    // OpenRouter settings
    var apiKey by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(modelOptions.first()) }
    var sourceLanguage by remember { mutableStateOf("Cantonese") }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    // Pronunciation state
    var pronunciationType by remember {
        mutableStateOf(
            when (selectedBox?.language) {
                "zh" -> MiteiruProcess.MANDARIN
                "ja" -> MiteiruProcess.JAPANESE
                else -> MiteiruProcess.MANDARIN
            }
        )
    }

    LaunchedEffect(selectedBox?.language) {
        pronunciationType = when (selectedBox?.language) {
            "zh" -> MiteiruProcess.MANDARIN
            "ja" -> MiteiruProcess.JAPANESE
            else -> MiteiruProcess.MANDARIN
        }
    }

    val sentences = remember(selectedBox?.text) {
        selectedBox?.text
            ?.split("\n")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.ifEmpty { selectedBox?.text?.let { listOf(it) } }
            ?: emptyList()
    }
    var selectedSentenceIndex by remember { mutableIntStateOf(0) }
    val selectedSentence = sentences.getOrNull(selectedSentenceIndex) ?: selectedBox?.text.orEmpty()

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Image and boxes
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .onSizeChanged { size ->
                        containerWidth = size.width
                        containerHeight = size.height
                    }
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured image",
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f),
                    contentScale = ContentScale.Fit
                )

                if (containerWidth > 0 && containerHeight > 0) {
                    textBoxes.forEach { box ->
                        TextRecognitionBox(
                            textBox = box,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight,
                            screenWidth = containerWidth,
                            screenHeight = containerHeight,
                            onClick = { selectedBox = box }
                        )
                    }
                }
            }

            // Sidebar panel
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onResume) {
                        Text("Back to Camera")
                    }
                    Button(onClick = {
                        val result = saveBitmapToGallery(context, bitmap)
                        val msg = result.fold(
                            onSuccess = { "Saved to gallery" },
                            onFailure = { "Save failed: ${it.message}" }
                        )
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }) {
                        Text("Save Image")
                    }
                }

                selectedBox?.let { box ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Detected Text",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextContent(box.text)

                            HorizontalDivider()

                            SentenceSelector(
                                sentences = sentences,
                                selectedIndex = selectedSentenceIndex,
                                onSelect = { selectedSentenceIndex = it }
                            )

                            if (box.language == "zh") {
                                LanguageSelector(
                                    selectedLanguage = pronunciationType,
                                    onLanguageSelected = { pronunciationType = it },
                                    text = selectedSentence,
                                    context = context
                                )
                            }

                            PronunciationDisplay(
                                text = selectedSentence,
                                language = box.language,
                                pronunciationType = pronunciationType
                            )

                            LanguageIndicator(language = box.language)

                            CopyButton(
                                onCopy = {
                                    copyToClipboard(context, selectedSentence)
                                    Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            )
                        }
                    }
                }

                OpenRouterSidebar(
                    apiKey = apiKey,
                    onApiKeyChange = { apiKey = it },
                    selectedModel = selectedModel,
                    onModelChange = { selectedModel = it },
                    sourceLanguage = sourceLanguage,
                    onSourceLanguageChange = { sourceLanguage = it },
                    isTranslating = isTranslating,
                    translationResult = translationResult,
                    onTranslate = {
                        val text = selectedSentence.ifBlank { selectedBox?.text.orEmpty() }
                        if (apiKey.isBlank()) {
                            Toast.makeText(context, "API key is required", Toast.LENGTH_SHORT).show()
                            return@OpenRouterSidebar
                        }
                        if (text.isBlank()) {
                            Toast.makeText(context, "Select a sentence first", Toast.LENGTH_SHORT).show()
                            return@OpenRouterSidebar
                        }
                        isTranslating = true
                        translationResult = null
                        scope.launch {
                            val result = OpenRouterClient.requestExplanation(
                                apiKey = apiKey.trim(),
                                model = selectedModel,
                                sourceLanguage = sourceLanguage,
                                sentence = text
                            )
                            translationResult = result.getOrElse { it.message ?: "Unknown error" }
                            isTranslating = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SentenceSelector(
    sentences: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    if (sentences.size <= 1) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Sentences",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.primary
        )

        sentences.forEachIndexed { index, sentence ->
            val isSelected = index == selectedIndex
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(index) },
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "${index + 1}. ",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = sentence,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

