package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeSlider
import com.example.ui.components.CreativeTopBar
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCanvasCheckerDark
import com.example.ui.theme.AuraCanvasCheckerLight
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
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

@Composable
fun BackgroundRemoverScreen(
    inputBitmap: Bitmap? = null,
    onNavigateBack: () -> Unit,
    onOpenInEditor: (Bitmap) -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sourceBitmap = remember {
        inputBitmap ?: ImageProcessingUtils.createSampleCreativeBitmap("portrait")
    }

    var transparentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(true) }
    var tolerance by remember { mutableFloatStateOf(0.28f) }
    var refineEdges by remember { mutableStateOf(true) }

    // Background mode: "TRANSPARENT", "COLOR", "GRADIENT"
    var bgMode by remember { mutableStateOf("TRANSPARENT") }
    var selectedColor by remember { mutableStateOf(Color.Transparent) }
    var selectedGradientIndex by remember { mutableStateOf(0) }

    fun processRemoval() {
        isProcessing = true
        scope.launch(Dispatchers.Default) {
            val result = ImageProcessingUtils.removeBackground(
                sourceBitmap,
                tolerance = tolerance,
                refineEdges = refineEdges
            )
            withContext(Dispatchers.Main) {
                transparentBitmap = result
                isProcessing = false
            }
        }
    }

    LaunchedEffect(tolerance, refineEdges) {
        processRemoval()
    }

    val gradients = listOf(
        Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF67D8F0))),
        Brush.linearGradient(listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
        Brush.linearGradient(listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
        Brush.linearGradient(listOf(Color(0xFF20232A), Color(0xFF101114)))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "AI Background Remover",
            subtitle = "Smart Foreground Segmentation",
            onBack = onNavigateBack,
            actions = {
                if (transparentBitmap != null) {
                    IconButton(
                        onClick = { onOpenInEditor(transparentBitmap!!) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Open in Editor",
                            tint = AuraCyanHighlight
                        )
                    }
                }
            }
        )

        // Canvas Viewport with Checkerboard Background for True Transparency
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(12.dp))
                    .background(
                        when (bgMode) {
                            "COLOR" -> Brush.linearGradient(listOf(selectedColor, selectedColor))
                            "GRADIENT" -> gradients[selectedGradientIndex]
                            else -> Brush.linearGradient(listOf(AuraCanvasCheckerDark, AuraCanvasCheckerLight))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = AuraCreativeAccent,
                        modifier = Modifier.padding(32.dp)
                    )
                } else if (transparentBitmap != null) {
                    Image(
                        bitmap = transparentBitmap!!.asImageBitmap(),
                        contentDescription = "Transparent Output",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(sourceBitmap.width.toFloat() / sourceBitmap.height.toFloat())
                    )
                }
            }
        }

        // Control Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AuraToolbarSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Background Replacement Selection
                Text(
                    text = "Background Replacement",
                    style = MaterialTheme.typography.titleSmall,
                    color = AuraTextPrimary
                )
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CreativeChip(
                        text = "Transparent (PNG)",
                        isSelected = bgMode == "TRANSPARENT",
                        onClick = { bgMode = "TRANSPARENT" }
                    )
                    CreativeChip(
                        text = "Studio Colors",
                        isSelected = bgMode == "COLOR",
                        onClick = {
                            bgMode = "COLOR"
                            selectedColor = Color(0xFF17191E)
                        }
                    )
                    CreativeChip(
                        text = "Aura Gradients",
                        isSelected = bgMode == "GRADIENT",
                        onClick = { bgMode = "GRADIENT" }
                    )
                }

                if (bgMode == "COLOR") {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf(Color.White, Color(0xFF17191E), Color(0xFF8B5CF6), Color(0xFF67D8F0), Color(0xFF55D6A2)).forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, if (selectedColor == color) AuraCyanHighlight else Color.Transparent, CircleShape)
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                } else if (bgMode == "GRADIENT") {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        gradients.forEachIndexed { index, brush ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(brush)
                                    .border(2.dp, if (selectedGradientIndex == index) AuraCyanHighlight else Color.Transparent, CircleShape)
                                    .clickable { selectedGradientIndex = index }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tolerance Slider
                CreativeSlider(
                    name = "Sensitivity",
                    value = tolerance * 100f,
                    onValueChange = { tolerance = it / 100f },
                    valueRange = 10f..60f,
                    unit = "%"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Refine Edge Feathering", style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
                    Switch(
                        checked = refineEdges,
                        onCheckedChange = { refineEdges = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = AuraCyanHighlight, checkedTrackColor = AuraCreativeAccent)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Export Button
                CreativePrimaryButton(
                    text = "Export Transparent PNG",
                    icon = Icons.Default.FileDownload,
                    useAuraGradient = true,
                    onClick = {
                        if (transparentBitmap != null) {
                            scope.launch(Dispatchers.IO) {
                                val path = ImageProcessingUtils.saveBitmap(
                                    context,
                                    transparentBitmap!!,
                                    filename = "aura_cutout_${System.currentTimeMillis()}.png",
                                    format = Bitmap.CompressFormat.PNG
                                )
                                val file = File(path)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Transparent Cutout"))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
