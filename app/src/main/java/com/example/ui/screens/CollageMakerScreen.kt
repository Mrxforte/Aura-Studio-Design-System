package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeSlider
import com.example.ui.components.CreativeTopBar
import com.example.ui.theme.AuraBorderSubtle
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
fun CollageMakerScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedGridType by remember { mutableIntStateOf(4) } // 2, 3, 4, 6
    var spacing by remember { mutableFloatStateOf(12f) }
    var cornerRadius by remember { mutableFloatStateOf(16f) }
    var bgColor by remember { mutableStateOf(Color(0xFF17191E)) }

    var collageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun renderCollage() {
        isGenerating = true
        scope.launch(Dispatchers.Default) {
            val size = 900
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            // Fill background
            val bgPaint = Paint().apply { color = bgColor.toArgb() }
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

            // Sample photos for slots
            val p1 = ImageProcessingUtils.createSampleCreativeBitmap("portrait")
            val p2 = ImageProcessingUtils.createSampleCreativeBitmap("landscape")
            val p3 = ImageProcessingUtils.createSampleCreativeBitmap("product")

            val sp = spacing
            val cr = cornerRadius

            val slotPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

            when (selectedGridType) {
                2 -> {
                    // Vertical split 2
                    val w = (size - sp * 3) / 2f
                    val h = size - sp * 2
                    canvas.drawBitmap(p1, null, RectF(sp, sp, sp + w, sp + h), slotPaint)
                    canvas.drawBitmap(p2, null, RectF(sp * 2 + w, sp, size - sp, sp + h), slotPaint)
                }
                3 -> {
                    // 1 Top, 2 Bottom
                    val topH = (size - sp * 3) * 0.55f
                    val botH = size - sp * 3 - topH
                    val halfW = (size - sp * 3) / 2f
                    canvas.drawBitmap(p1, null, RectF(sp, sp, size - sp, sp + topH), slotPaint)
                    canvas.drawBitmap(p2, null, RectF(sp, sp * 2 + topH, sp + halfW, size - sp), slotPaint)
                    canvas.drawBitmap(p3, null, RectF(sp * 2 + halfW, sp * 2 + topH, size - sp, size - sp), slotPaint)
                }
                4 -> {
                    // 2x2 Quad
                    val cellW = (size - sp * 3) / 2f
                    val cellH = (size - sp * 3) / 2f
                    canvas.drawBitmap(p1, null, RectF(sp, sp, sp + cellW, sp + cellH), slotPaint)
                    canvas.drawBitmap(p2, null, RectF(sp * 2 + cellW, sp, size - sp, sp + cellH), slotPaint)
                    canvas.drawBitmap(p3, null, RectF(sp, sp * 2 + cellH, sp + cellW, size - sp), slotPaint)
                    canvas.drawBitmap(p1, null, RectF(sp * 2 + cellW, sp * 2 + cellH, size - sp, size - sp), slotPaint)
                }
                else -> {
                    // 6 grid (3x2)
                    val cellW = (size - sp * 4) / 3f
                    val cellH = (size - sp * 3) / 2f
                    for (row in 0..1) {
                        for (col in 0..2) {
                            val left = sp + col * (cellW + sp)
                            val top = sp + row * (cellH + sp)
                            val img = if ((row + col) % 2 == 0) p1 else p2
                            canvas.drawBitmap(img, null, RectF(left, top, left + cellW, top + cellH), slotPaint)
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                collageBitmap = bmp
                isGenerating = false
            }
        }
    }

    LaunchedEffect(selectedGridType, spacing, cornerRadius, bgColor) {
        renderCollage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Collage Studio",
            subtitle = "Smart Photo Grid & Layouts",
            onBack = onNavigateBack
        )

        // Collage Canvas Preview
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
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = AuraCyanHighlight)
                } else if (collageBitmap != null) {
                    Image(
                        bitmap = collageBitmap!!.asImageBitmap(),
                        contentDescription = "Collage Output",
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(1f)
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
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Layout Selector
                Text("Select Grid Template", style = MaterialTheme.typography.titleSmall, color = AuraTextPrimary)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2, 3, 4, 6).forEach { count ->
                        CreativeChip(
                            text = "$count Photos",
                            isSelected = selectedGridType == count,
                            onClick = { selectedGridType = count }
                        )
                    }
                }

                // Spacing and Corner Sliders
                CreativeSlider(
                    name = "Grid Spacing",
                    value = spacing,
                    onValueChange = { spacing = it },
                    valueRange = 0f..40f,
                    unit = "dp"
                )

                CreativeSlider(
                    name = "Corner Radius",
                    value = cornerRadius,
                    onValueChange = { cornerRadius = it },
                    valueRange = 0f..48f,
                    unit = "dp"
                )

                // Background Colors
                Text("Canvas Border Color", style = MaterialTheme.typography.titleSmall, color = AuraTextPrimary)
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Color(0xFF17191E),
                        Color.White,
                        AuraCreativeAccent,
                        AuraCyanHighlight,
                        Color(0xFFF5C16C),
                        Color(0xFF101114)
                    ).forEach { col ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(2.dp, if (bgColor == col) AuraCyanHighlight else Color.Transparent, CircleShape)
                                .clickable { bgColor = col }
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (exportSuccessMessage != null) {
                    Text(
                        text = exportSuccessMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = AuraSuccess,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                CreativePrimaryButton(
                    text = "Export High-Res Collage",
                    icon = Icons.Default.FileDownload,
                    useAuraGradient = true,
                    onClick = {
                        if (collageBitmap != null) {
                            scope.launch(Dispatchers.IO) {
                                val path = ImageProcessingUtils.saveBitmap(
                                    context,
                                    collageBitmap!!,
                                    filename = "aura_collage_${System.currentTimeMillis()}.png",
                                    format = Bitmap.CompressFormat.PNG
                                )
                                val file = File(path)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Collage"))
                                withContext(Dispatchers.Main) {
                                    exportSuccessMessage = "Collage saved and ready to share!"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
