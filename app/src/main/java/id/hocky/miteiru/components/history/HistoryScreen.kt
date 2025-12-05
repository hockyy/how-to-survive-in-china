package id.hocky.miteiru.components.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.hocky.miteiru.data.CaptureRepository
import id.hocky.miteiru.data.entities.PreviousCapture
import id.hocky.miteiru.data.entities.SmallCapture
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onSmallCaptureSelected: (SmallCapture) -> Unit,
    onPreviousCaptureSelected: (PreviousCapture) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { CaptureRepository(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text Regions", "Full Captures")

    val smallCaptures by repository.getAllSmallCaptures().collectAsState(initial = emptyList())
    val previousCaptures by repository.getAllPreviousCaptures().collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> SmallCapturesList(
                    captures = smallCaptures,
                    onSelect = onSmallCaptureSelected,
                    onDelete = { capture ->
                        scope.launch {
                            repository.deleteSmallCapture(capture.id)
                        }
                    }
                )
                1 -> PreviousCapturesList(
                    captures = previousCaptures,
                    onSelect = onPreviousCaptureSelected,
                    onDelete = { capture ->
                        scope.launch {
                            repository.deletePreviousCapture(capture.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SmallCapturesList(
    captures: List<SmallCapture>,
    onSelect: (SmallCapture) -> Unit,
    onDelete: (SmallCapture) -> Unit
) {
    if (captures.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved text regions yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(captures, key = { it.id }) { capture ->
                SmallCaptureCard(
                    capture = capture,
                    onClick = { onSelect(capture) },
                    onDelete = { onDelete(capture) }
                )
            }
        }
    }
}

@Composable
private fun SmallCaptureCard(
    capture: SmallCapture,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bitmap = remember(capture.imagePath) {
        try {
            BitmapFactory.decodeFile(capture.imagePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Captured text region",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = capture.text,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${capture.language.uppercase()} • ${dateFormat.format(Date(capture.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (capture.analysis != null) {
                    Text(
                        text = "✓ Has analysis",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun PreviousCapturesList(
    captures: List<PreviousCapture>,
    onSelect: (PreviousCapture) -> Unit,
    onDelete: (PreviousCapture) -> Unit
) {
    if (captures.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No saved captures yet",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(captures, key = { it.id }) { capture ->
                PreviousCaptureCard(
                    capture = capture,
                    onClick = { onSelect(capture) },
                    onDelete = { onDelete(capture) }
                )
            }
        }
    }
}

@Composable
private fun PreviousCaptureCard(
    capture: PreviousCapture,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val bitmap = remember(capture.imagePath) {
        try {
            BitmapFactory.decodeFile(capture.imagePath)?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Full capture",
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Full Capture",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = dateFormat.format(Date(capture.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

