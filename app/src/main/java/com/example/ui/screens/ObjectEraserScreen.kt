package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeSecondaryButton
import com.example.ui.components.CreativeSlider
import com.example.ui.components.CreativeTopBar
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
import com.example.ui.theme.AuraPrimarySurface
import com.example.ui.theme.AuraSecondaryAccent
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraToolbarSurface
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ObjectEraserScreen(
    inputBitmap: Bitmap? = null,
    onNavigateBack: () -> Unit,
    onOpenInEditor: (Bitmap) -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sourceBitmap = remember {
        inputBitmap ?: ImageProcessingUtils.createSampleCreativeBitmap("landscape")
    }

    var currentBitmap by remember { mutableStateOf(sourceBitmap) }
    var isProcessing by remember { mutableStateOf(false) }
    var brushSize by remember { mutableFloatStateOf(40f) }
    var isComparingOriginal by remember { mutableStateOf(false) }

    // Brush stroke points (relative 0..1)
    val strokePoints = remember { mutableStateListOf<Pair<Offset, Float>>() }

    fun processInpaint() {
        if (strokePoints.isEmpty()) return
        isProcessing = true

        scope.launch(Dispatchers.Default) {
            // Generate mask bitmap from drawn stroke points
            val mask = Bitmap.createBitmap(currentBitmap.width, currentBitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(mask)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.MAGENTA
                style = Paint.Style.FILL
            }

            for ((point, radius) in strokePoints) {
                val cx = point.x * currentBitmap.width
                val cy = point.y * currentBitmap.height
                val r = (radius / 100f) * currentBitmap.width * 0.15f
                canvas.drawCircle(cx, cy, r, paint)
            }

            val inpainted = ImageProcessingUtils.inpaintMask(currentBitmap, mask)
            withContext(Dispatchers.Main) {
                currentBitmap = inpainted
                strokePoints.clear()
                isProcessing = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "AI Object Eraser",
            subtitle = if (isComparingOriginal) "Viewing Before" else "Paint over object to remove",
            onBack = onNavigateBack,
            actions = {
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

                IconButton(
                    onClick = { onOpenInEditor(currentBitmap) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Open in Editor",
                        tint = AuraCreativeAccent
                    )
                }
            }
        )

        // Interactive Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val displayBitmap = if (isComparingOriginal) sourceBitmap else currentBitmap

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(12.dp))
                    .background(androidx.compose.ui.graphics.Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = displayBitmap.asImageBitmap(),
                    contentDescription = "Inpaint Canvas",
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(displayBitmap.width.toFloat() / displayBitmap.height.toFloat())
                        .pointerInput(brushSize) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                                val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                                strokePoints.add(Pair(Offset(normX, normY), brushSize))
                            }
                        }
                )

                // Visual stroke feedback
                if (strokePoints.isNotEmpty()) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .matchParentSize()
                    ) {
                        for ((pt, rad) in strokePoints) {
                            val px = pt.x * size.width
                            val py = pt.y * size.height
                            drawCircle(
                                color = androidx.compose.ui.graphics.Color(0x99C084FC),
                                radius = (rad / 100f) * size.width * 0.08f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                if (isProcessing) {
                    CircularProgressIndicator(
                        color = AuraCyanHighlight,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        // Bottom Controls
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AuraToolbarSurface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                CreativeSlider(
                    name = "Brush Radius",
                    value = brushSize,
                    onValueChange = { brushSize = it },
                    valueRange = 15f..90f,
                    unit = "px"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CreativeSecondaryButton(
                        text = "Undo Mask",
                        icon = Icons.AutoMirrored.Filled.Undo,
                        onClick = {
                            if (strokePoints.isNotEmpty()) {
                                val toRemove = strokePoints.takeLast(10)
                                strokePoints.removeAll(toRemove)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CreativeSecondaryButton(
                        text = "Clear All",
                        icon = Icons.Default.Clear,
                        onClick = { strokePoints.clear() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))

                CreativePrimaryButton(
                    text = if (isProcessing) "Synthesizing Clean Fill..." else "Erase Selected Object (${strokePoints.size} pts)",
                    icon = Icons.Default.AutoAwesome,
                    useAuraGradient = true,
                    enabled = strokePoints.isNotEmpty() && !isProcessing,
                    isLoading = isProcessing,
                    onClick = { processInpaint() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
