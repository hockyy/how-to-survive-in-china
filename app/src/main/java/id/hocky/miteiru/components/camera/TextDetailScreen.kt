package id.hocky.miteiru.components.camera

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.halilibo.richtext.commonmark.Markdown
import com.halilibo.richtext.ui.material3.RichText
import copyToClipboard
import cropBitmapByRect
import id.hocky.miteiru.components.textBoxPopUp.CopyButton
import id.hocky.miteiru.components.textBoxPopUp.LanguageIndicator
import id.hocky.miteiru.components.textBoxPopUp.LanguageSelector
import id.hocky.miteiru.components.textBoxPopUp.PronunciationDisplay
import id.hocky.miteiru.components.textBoxPopUp.modelOptions
import id.hocky.miteiru.components.textBoxPopUp.sourceLanguageOptions
import id.hocky.miteiru.data.CaptureRepository
import id.hocky.miteiru.data.SettingsDataStore
import id.hocky.miteiru.utils.ChineseTextBox
import id.hocky.miteiru.utils.OpenRouterClient
import id.hocky.miteiru.utils.language.MiteiruProcess
import id.hocky.miteiru.utils.loadBitmapFromUri
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextDetailScreen(
    textBox: ChineseTextBox,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    // Settings persistence
    val settingsDataStore = remember { SettingsDataStore(context) }
    val repository = remember { CaptureRepository(context) }

    // Cropped image from bounding box
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    // Load and crop bitmap when component is created
    LaunchedEffect(textBox) {
        textBox.imageUri?.let { uri ->
            try {
                val fullBitmap = loadBitmapFromUri(context, uri)
                croppedBitmap = cropBitmapByRect(fullBitmap, textBox.boundingBox, 20)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Sentences
    val sentences = remember(textBox.text) {
        textBox.text
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(textBox.text) }
    }
    var selectedSentenceIndex by remember { mutableIntStateOf(0) }
    val selectedSentence = sentences.getOrElse(selectedSentenceIndex) { textBox.text }

    // Pronunciation type
    var pronunciationType by remember {
        mutableStateOf(
            when (textBox.language) {
                "zh" -> MiteiruProcess.MANDARIN
                "ja" -> MiteiruProcess.JAPANESE
                else -> MiteiruProcess.MANDARIN
            }
        )
    }

    // OpenRouter settings - load from DataStore
    val savedApiKey by settingsDataStore.apiKey.collectAsState(initial = "")
    val savedModel by settingsDataStore.selectedModel.collectAsState(initial = modelOptions.first())
    val savedSourceLanguage by settingsDataStore.sourceLanguage.collectAsState(initial = sourceLanguageOptions.first())

    var apiKey by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf(modelOptions.first()) }
    var sourceLanguage by remember { mutableStateOf(sourceLanguageOptions.first()) }
    var translationResult by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }

    // Initialize with saved values
    LaunchedEffect(savedApiKey) { if (apiKey.isEmpty()) apiKey = savedApiKey }
    LaunchedEffect(savedModel) { selectedModel = savedModel }
    LaunchedEffect(savedSourceLanguage) { sourceLanguage = savedSourceLanguage }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AI Translation",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { newKey ->
                            apiKey = newKey
                            scope.launch { settingsDataStore.saveApiKey(newKey) }
                        },
                        label = { Text("OpenRouter API Key") },
                        placeholder = { Text("sk-or-...") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Model dropdown
                    DropdownSelector(
                        label = "Model",
                        options = modelOptions,
                        selected = selectedModel,
                        onSelect = { model ->
                            selectedModel = model
                            scope.launch { settingsDataStore.saveSelectedModel(model) }
                        }
                    )

                    // Source language dropdown
                    DropdownSelector(
                        label = "Source Language",
                        options = sourceLanguageOptions,
                        selected = sourceLanguage,
                        onSelect = { lang ->
                            sourceLanguage = lang
                            scope.launch { settingsDataStore.saveSourceLanguage(lang) }
                        }
                    )

                    Button(
                        onClick = {
                            if (apiKey.isBlank()) {
                                Toast.makeText(context, "API key required", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isTranslating = true
                            translationResult = null
                            scope.launch {
                                val result = OpenRouterClient.requestExplanation(
                                    apiKey = apiKey.trim(),
                                    model = selectedModel,
                                    sourceLanguage = sourceLanguage,
                                    sentence = selectedSentence
                                )
                                translationResult = result.getOrElse { it.message ?: "Error" }
                                isTranslating = false
                            }
                        },
                        enabled = !isTranslating && apiKey.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(18.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text(if (isTranslating) "Analyzing..." else "Explain in English")
                    }

                    translationResult?.let { result ->
                        HorizontalDivider()
                        Text(
                            text = "Analysis Result",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                RichText {
                                    Markdown(result)
                                }
                            }
                        }
                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Text Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Save button
                        IconButton(
                            onClick = {
                                croppedBitmap?.let { bitmap ->
                                    scope.launch {
                                        repository.saveSmallCapture(
                                            bitmap = bitmap,
                                            text = textBox.text,
                                            language = textBox.language
                                        )
                                        isSaved = true
                                        Toast.makeText(context, "Saved to history!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = croppedBitmap != null && !isSaved
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Save",
                                tint = if (isSaved) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        // Menu button
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Translate")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cropped image from bounding box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Detected Region",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        croppedBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Cropped text region",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp, max = 200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        // Also show the text below the image
                        Text(
                            text = textBox.text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Sentence selector
                if (sentences.size > 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Select Sentence",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            sentences.forEachIndexed { index, sentence ->
                                val isSelected = index == selectedSentenceIndex
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedSentenceIndex = index },
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}. ",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = sentence,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Pronunciation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pronunciation",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (textBox.language == "zh") {
                            LanguageSelector(
                                selectedLanguage = pronunciationType,
                                onLanguageSelected = { pronunciationType = it },
                                text = selectedSentence,
                                context = context
                            )
                        }

                        PronunciationDisplay(
                            text = selectedSentence,
                            language = textBox.language,
                            pronunciationType = pronunciationType
                        )

                        LanguageIndicator(language = textBox.language)
                    }
                }

                // Copy button
                CopyButton(
                    onCopy = {
                        copyToClipboard(context, selectedSentence)
                        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    }
                )

                // Hint
                Text(
                    text = "Swipe from right edge or tap menu icon to open translation panel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = selected,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

