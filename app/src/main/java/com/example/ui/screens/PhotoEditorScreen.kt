package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.AuraDatabase
import com.example.data.model.ImageAdjustments
import com.example.data.model.LayerType
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.StudioLayer
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeSecondaryButton
import com.example.ui.components.CreativeSlider
import com.example.ui.components.CreativeTopBar
import com.example.ui.components.EditorToolButton
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
import com.example.ui.theme.AuraInputSurface
import com.example.ui.theme.AuraPrimarySurface
import com.example.ui.theme.AuraSecondaryAccent
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraToolbarSurface
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

@Composable
fun PhotoEditorScreen(
    initialBitmap: Bitmap? = null,
    projectId: String? = null,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AuraDatabase.getInstance(context) }

    // Source Bitmap (original)
    var originalBitmap by remember {
        mutableStateOf(initialBitmap ?: ImageProcessingUtils.createSampleCreativeBitmap("portrait"))
    }

    // Processed Bitmap (preview)
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Undo / Redo history
    val historyStack = remember { mutableStateListOf<ImageAdjustments>() }
    val redoStack = remember { mutableStateListOf<ImageAdjustments>() }

    // Current adjustments
    var currentAdjustments by remember { mutableStateOf(ImageAdjustments()) }

    // Compare with original state
    var isComparingOriginal by remember { mutableStateOf(false) }

    // Active tool panel
    var activeTool by remember { mutableStateOf<String?>(null) }

    // Studio layers (Text, Stickers, Shapes)
    val layers = remember { mutableStateListOf<StudioLayer>() }
    var selectedLayerId by remember { mutableStateOf<String?>(null) }

    // Dialogs
    var showExportDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var showSaveToast by remember { mutableStateOf(false) }
    var exportSuccessPath by remember { mutableStateOf<String?>(null) }

    // Render bitmap whenever adjustments change
    fun updatePreview(adjustments: ImageAdjustments) {
        scope.launch(Dispatchers.Default) {
            val processed = ImageProcessingUtils.applyAdjustments(originalBitmap, adjustments)
            withContext(Dispatchers.Main) {
                renderedBitmap = processed
            }
        }
    }

    LaunchedEffect(currentAdjustments) {
        updatePreview(currentAdjustments)
    }

    fun pushHistory(newAdjustments: ImageAdjustments) {
        historyStack.add(currentAdjustments)
        redoStack.clear()
        currentAdjustments = newAdjustments
    }

    fun undo() {
        if (historyStack.isNotEmpty()) {
            val previous = historyStack.removeAt(historyStack.lastIndex)
            redoStack.add(currentAdjustments)
            currentAdjustments = previous
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            historyStack.add(currentAdjustments)
            currentAdjustments = next
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        // 1. Top Bar
        CreativeTopBar(
            title = "Photo Studio",
            subtitle = if (isComparingOriginal) "Viewing Original" else "Editable Workspace",
            onBack = onNavigateBack,
            actions = {
                // Undo
                IconButton(
                    onClick = { undo() },
                    enabled = historyStack.isNotEmpty(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (historyStack.isNotEmpty()) AuraTextPrimary else AuraTextMuted
                    )
                }

                // Redo
                IconButton(
                    onClick = { redo() },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) AuraTextPrimary else AuraTextMuted
                    )
                }

                // Compare button (Hold/Tap)
                IconButton(
                    onClick = { isComparingOriginal = !isComparingOriginal },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Compare",
                        tint = if (isComparingOriginal) AuraCyanHighlight else AuraTextSecondary
                    )
                }

                // Save Project
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val activeBmp = renderedBitmap ?: originalBitmap
                            val thumbPath = ImageProcessingUtils.saveBitmap(
                                context, activeBmp, "thumb_${System.currentTimeMillis()}.png"
                            )
                            val project = ProjectEntity(
                                id = projectId ?: UUID.randomUUID().toString(),
                                name = "Studio Photo ${System.currentTimeMillis() % 1000}",
                                type = ProjectType.PHOTO.name,
                                lastModified = System.currentTimeMillis(),
                                width = activeBmp.width,
                                height = activeBmp.height,
                                thumbnailUri = thumbPath
                            )
                            db.projectDao().insertProject(project)
                            withContext(Dispatchers.Main) {
                                showSaveToast = true
                            }
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save Project",
                        tint = AuraTextPrimary
                    )
                }

                // Export
                IconButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export",
                        tint = AuraCreativeAccent
                    )
                }
            }
        )

        // Save confirmation toast
        if (showSaveToast) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = AuraElevatedSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AuraSuccess)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = AuraSuccess, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Project saved to library", style = MaterialTheme.typography.labelMedium, color = AuraTextPrimary)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { showSaveToast = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = AuraTextMuted, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // 2. Central Canvas with layers
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(AuraDarkBackground)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayBitmap = if (isComparingOriginal) originalBitmap else (renderedBitmap ?: originalBitmap)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Canvas Image",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(displayBitmap.width.toFloat() / displayBitmap.height.toFloat())
                )

                // Overlaid Studio Layers (Text, Stickers, Shapes)
                for (layer in layers.filter { it.isVisible }) {
                    StudioLayerOverlay(
                        layer = layer,
                        isSelected = selectedLayerId == layer.id,
                        onSelect = { selectedLayerId = layer.id },
                        onMove = { dx, dy ->
                            val idx = layers.indexOfFirst { it.id == layer.id }
                            if (idx != -1) {
                                layers[idx] = layers[idx].copy(
                                    x = (layers[idx].x + dx).coerceIn(0.05f, 0.95f),
                                    y = (layers[idx].y + dy).coerceIn(0.05f, 0.95f)
                                )
                            }
                        }
                    )
                }
            }
        }

        // 3. Contextual Properties Panel (Adjustments sliders, filters, crop)
        AnimatedVisibility(
            visible = activeTool != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AuraBorderSubtle),
                color = AuraToolbarSurface
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeTool ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = AuraTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { activeTool = null },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = AuraTextSecondary)
                        }
                    }

                    when (activeTool) {
                        "Adjust" -> {
                            val adjustScroll = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(vertical = 4.dp)
                                    .verticalScroll(adjustScroll)
                            ) {
                                CreativeSlider(
                                    name = "Brightness",
                                    value = currentAdjustments.brightness,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(brightness = it) },
                                    valueRange = -100f..100f,
                                    onReset = { currentAdjustments = currentAdjustments.copy(brightness = 0f) }
                                )
                                CreativeSlider(
                                    name = "Contrast",
                                    value = currentAdjustments.contrast,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(contrast = it) },
                                    valueRange = -100f..100f,
                                    onReset = { currentAdjustments = currentAdjustments.copy(contrast = 0f) }
                                )
                                CreativeSlider(
                                    name = "Saturation",
                                    value = currentAdjustments.saturation,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(saturation = it) },
                                    valueRange = -100f..100f,
                                    onReset = { currentAdjustments = currentAdjustments.copy(saturation = 0f) }
                                )
                                CreativeSlider(
                                    name = "Temperature",
                                    value = currentAdjustments.temperature,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(temperature = it) },
                                    valueRange = -100f..100f,
                                    onReset = { currentAdjustments = currentAdjustments.copy(temperature = 0f) }
                                )
                                CreativeSlider(
                                    name = "Vignette",
                                    value = currentAdjustments.vignette,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(vignette = it) },
                                    valueRange = 0f..100f,
                                    onReset = { currentAdjustments = currentAdjustments.copy(vignette = 0f) }
                                )
                            }
                        }

                        "Filters" -> {
                            val filters = listOf(
                                "Normal", "Cinematic", "Cyberpunk", "Film Noir",
                                "Vintage 70s", "Golden Hour", "Pastel", "Emerald"
                            )
                            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                                CreativeSlider(
                                    name = "Filter Intensity",
                                    value = currentAdjustments.filterIntensity * 100f,
                                    onValueChange = { currentAdjustments = currentAdjustments.copy(filterIntensity = it / 100f) },
                                    valueRange = 0f..100f,
                                    unit = "%"
                                )
                                LazyRow(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filters) { filterName ->
                                        CreativeChip(
                                            text = filterName,
                                            isSelected = currentAdjustments.filterPreset == filterName,
                                            onClick = {
                                                pushHistory(currentAdjustments)
                                                currentAdjustments = currentAdjustments.copy(filterPreset = filterName)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        "Crop & Rotate" -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CreativeSecondaryButton(
                                    text = "Rotate 90°",
                                    icon = Icons.Default.Rotate90DegreesCcw,
                                    onClick = {
                                        pushHistory(currentAdjustments)
                                        currentAdjustments = currentAdjustments.copy(
                                            rotation = (currentAdjustments.rotation + 90) % 360
                                        )
                                    }
                                )
                                CreativeSecondaryButton(
                                    text = "Flip Horiz",
                                    icon = Icons.Default.Flip,
                                    onClick = {
                                        pushHistory(currentAdjustments)
                                        currentAdjustments = currentAdjustments.copy(
                                            flipH = !currentAdjustments.flipH
                                        )
                                    }
                                )
                                CreativeSecondaryButton(
                                    text = "Flip Vert",
                                    icon = Icons.Default.Flip,
                                    onClick = {
                                        pushHistory(currentAdjustments)
                                        currentAdjustments = currentAdjustments.copy(
                                            flipV = !currentAdjustments.flipV
                                        )
                                    }
                                )
                            }
                        }

                        "Stickers & Shapes" -> {
                            val stickers = listOf("✨ Sparkle", "❤️ Heart", "🔥 Fire", "👑 Crown", "⚡ Bold", "💎 Jewel", "⭐ Star")
                            LazyRow(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(stickers) { sticker ->
                                    CreativeChip(
                                        text = sticker,
                                        isSelected = false,
                                        onClick = {
                                            layers.add(
                                                StudioLayer(
                                                    id = UUID.randomUUID().toString(),
                                                    name = sticker,
                                                    type = LayerType.STICKER,
                                                    text = sticker.split(" ").firstOrNull() ?: "⭐",
                                                    fontSize = 44f
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Bottom Photoshop-inspired Tool Selector
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AuraPrimarySurface,
            tonalElevation = 6.dp
        ) {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                EditorToolButton(
                    name = "Adjust",
                    icon = Icons.Default.Tune,
                    isSelected = activeTool == "Adjust",
                    onClick = { activeTool = if (activeTool == "Adjust") null else "Adjust" }
                )
                EditorToolButton(
                    name = "Filters",
                    icon = Icons.Default.ColorLens,
                    isSelected = activeTool == "Filters",
                    onClick = { activeTool = if (activeTool == "Filters") null else "Filters" }
                )
                EditorToolButton(
                    name = "Transform",
                    icon = Icons.Default.Crop,
                    isSelected = activeTool == "Crop & Rotate",
                    onClick = { activeTool = if (activeTool == "Crop & Rotate") null else "Crop & Rotate" }
                )
                EditorToolButton(
                    name = "Text",
                    icon = Icons.Default.TextFields,
                    isSelected = false,
                    onClick = { showAddTextDialog = true }
                )
                EditorToolButton(
                    name = "Stickers",
                    icon = Icons.Default.AutoAwesome,
                    isSelected = activeTool == "Stickers & Shapes",
                    onClick = { activeTool = if (activeTool == "Stickers & Shapes") null else "Stickers & Shapes" }
                )
                EditorToolButton(
                    name = "Layers (${layers.size})",
                    icon = Icons.Default.Layers,
                    isSelected = showLayersSheet,
                    onClick = { showLayersSheet = !showLayersSheet }
                )
            }
        }
    }

    // Add Text Layer Dialog
    if (showAddTextDialog) {
        var textInput by remember { mutableStateOf("AURA STUDIO") }
        var selectedColor by remember { mutableStateOf(AuraCyanHighlight) }

        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text("Add Text Layer", color = AuraTextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = { Text("Text content") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary,
                            focusedBorderColor = AuraCreativeAccent,
                            unfocusedBorderColor = AuraBorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Palette", style = MaterialTheme.typography.labelMedium, color = AuraTextSecondary)
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(Color.White, AuraCyanHighlight, AuraSecondaryAccent, AuraSuccess, Color(0xFFF5C16C)).forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(
                                        2.dp,
                                        if (selectedColor == col) AuraTextPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = col }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            layers.add(
                                StudioLayer(
                                    id = UUID.randomUUID().toString(),
                                    name = textInput,
                                    type = LayerType.TEXT,
                                    text = textInput,
                                    colorHex = String.format("#%06X", 0xFFFFFF and selectedColor.toArgb()),
                                    fontSize = 28f
                                )
                            )
                        }
                        showAddTextDialog = false
                    }
                ) {
                    Text("Add Layer", color = AuraCreativeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) {
                    Text("Cancel", color = AuraTextSecondary)
                }
            },
            containerColor = AuraElevatedSurface
        )
    }

    // Layers Management Sheet / Dialog
    if (showLayersSheet) {
        AlertDialog(
            onDismissRequest = { showLayersSheet = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Layers Manager", color = AuraTextPrimary)
                    Text("${layers.size} layers", style = MaterialTheme.typography.labelSmall, color = AuraTextMuted)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (layers.isEmpty()) {
                        Text(
                            "No graphic layers added yet. Tap Text or Stickers to add creative layers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextSecondary
                        )
                    } else {
                        layers.forEachIndexed { idx, layer ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = if (selectedLayerId == layer.id) AuraToolbarSurface else AuraPrimarySurface,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            layers[idx] = layer.copy(isVisible = !layer.isVisible)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle visibility",
                                            tint = if (layer.isVisible) AuraCyanHighlight else AuraTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = layer.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AuraTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { layers.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = AuraTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLayersSheet = false }) {
                    Text("Done", color = AuraCreativeAccent)
                }
            },
            containerColor = AuraElevatedSurface
        )
    }

    // Export Dialog
    if (showExportDialog) {
        var exportFormat by remember { mutableStateOf("PNG") }
        var exportQuality by remember { mutableFloatStateOf(95f) }
        var isExporting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = { Text("Export Artwork", color = AuraTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select output format:", style = MaterialTheme.typography.labelMedium, color = AuraTextSecondary)
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CreativeChip(
                            text = "PNG (Lossless)",
                            isSelected = exportFormat == "PNG",
                            onClick = { exportFormat = "PNG" }
                        )
                        CreativeChip(
                            text = "JPEG (Compact)",
                            isSelected = exportFormat == "JPEG",
                            onClick = { exportFormat = "JPEG" }
                        )
                    }

                    if (exportFormat == "JPEG") {
                        CreativeSlider(
                            name = "Quality",
                            value = exportQuality,
                            onValueChange = { exportQuality = it },
                            valueRange = 50f..100f,
                            unit = "%"
                        )
                    }

                    if (exportSuccessPath != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Exported successfully!",
                            style = MaterialTheme.typography.labelMedium,
                            color = AuraSuccess
                        )
                    }
                }
            },
            confirmButton = {
                CreativePrimaryButton(
                    text = if (isExporting) "Exporting..." else "Save & Share",
                    isLoading = isExporting,
                    onClick = {
                        isExporting = true
                        scope.launch(Dispatchers.IO) {
                            val activeBmp = renderedBitmap ?: originalBitmap
                            val ext = if (exportFormat == "PNG") "png" else "jpg"
                            val format = if (exportFormat == "PNG") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                            val path = ImageProcessingUtils.saveBitmap(
                                context,
                                activeBmp,
                                filename = "aura_art_${System.currentTimeMillis()}.$ext",
                                format = format,
                                quality = exportQuality.toInt()
                            )
                            exportSuccessPath = path
                            isExporting = false

                            // Trigger system share sheet
                            val file = File(path)
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (exportFormat == "PNG") "image/png" else "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Artwork"))
                        }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = AuraTextSecondary)
                }
            },
            containerColor = AuraElevatedSurface
        )
    }
}

@Composable
fun StudioLayerOverlay(
    layer: StudioLayer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                    onMove(dragAmount.x / 500f, dragAmount.y / 500f)
                }
            }
            .clickable(onClick = onSelect)
            .border(
                1.dp,
                if (isSelected) AuraCyanHighlight else Color.Transparent,
                RoundedCornerShape(4.dp)
            )
            .padding(4.dp)
    ) {
        when (layer.type) {
            LayerType.TEXT -> {
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(layer.colorHex))
                } catch (e: Exception) {
                    Color.White
                }
                Text(
                    text = layer.text,
                    fontSize = layer.fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    color = parsedColor
                )
            }
            LayerType.STICKER -> {
                Text(
                    text = layer.text,
                    fontSize = layer.fontSize.sp
                )
            }
            else -> {}
        }
    }
}
