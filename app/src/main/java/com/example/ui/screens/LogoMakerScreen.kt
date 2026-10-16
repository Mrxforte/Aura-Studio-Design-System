package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.local.AuraDatabase
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeTopBar
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCanvasCheckerDark
import com.example.ui.theme.AuraCanvasCheckerLight
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

@Composable
fun LogoMakerScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AuraDatabase.getInstance(context) }

    var brandName by remember { mutableStateOf("NEXUS") }
    var slogan by remember { mutableStateOf("INTELLIGENT CREATIVE LABS") }
    var selectedSymbol by remember { mutableStateOf("Aperture") }
    var selectedStyle by remember { mutableStateOf("Modern Gradient") }
    var primaryColor by remember { mutableStateOf(AuraCreativeAccent) }
    var secondaryColor by remember { mutableStateOf(AuraCyanHighlight) }

    var logoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    val symbols = listOf("Aperture", "Diamond", "Sparkle", "Crown", "Circuit", "Infinity")
    val styles = listOf("Modern Gradient", "Geometric Minimal", "Monogram", "Cyberpunk Badge")

    fun renderLogo() {
        scope.launch(Dispatchers.Default) {
            val size = 900
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)

            // Transparent background (or subtle dark circular shield)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val c1 = primaryColor.toArgb()
            val c2 = secondaryColor.toArgb()

            val cx = size / 2f
            val cy = size * 0.42f

            // Draw Emblem Symbol
            paint.shader = android.graphics.LinearGradient(
                cx - 160f, cy - 160f, cx + 160f, cy + 160f,
                c1, c2, android.graphics.Shader.TileMode.CLAMP
            )

            when (selectedSymbol) {
                "Aperture" -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 24f
                    canvas.drawCircle(cx, cy, 140f, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, 40f, paint)
                }
                "Diamond" -> {
                    val path = Path().apply {
                        moveTo(cx, cy - 150f)
                        lineTo(cx + 150f, cy)
                        lineTo(cx, cy + 150f)
                        lineTo(cx - 150f, cy)
                        close()
                    }
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 24f
                    canvas.drawPath(path, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx, cy, 36f, paint)
                }
                "Crown" -> {
                    val path = Path().apply {
                        moveTo(cx - 140f, cy + 100f)
                        lineTo(cx - 150f, cy - 60f)
                        lineTo(cx - 70f, cy + 20f)
                        lineTo(cx, cy - 120f)
                        lineTo(cx + 70f, cy + 20f)
                        lineTo(cx + 150f, cy - 60f)
                        lineTo(cx + 140f, cy + 100f)
                        close()
                    }
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                }
                "Circuit" -> {
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 16f
                    canvas.drawRoundRect(RectF(cx - 130f, cy - 130f, cx + 130f, cy + 130f), 30f, 30f, paint)
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(cx - 80f, cy - 80f, 20f, paint)
                    canvas.drawCircle(cx + 80f, cy + 80f, 20f, paint)
                    canvas.drawCircle(cx, cy, 28f, paint)
                }
                else -> {
                    // Sparkle / Star
                    val path = Path().apply {
                        moveTo(cx, cy - 150f)
                        quadTo(cx, cy, cx + 150f, cy)
                        quadTo(cx, cy, cx, cy + 150f)
                        quadTo(cx, cy, cx - 150f, cy)
                        quadTo(cx, cy, cx, cy - 150f)
                        close()
                    }
                    paint.style = Paint.Style.FILL
                    canvas.drawPath(path, paint)
                }
            }
            paint.shader = null

            // Typography: Brand Name
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.WHITE
                textSize = 72f
                isFakeBoldText = true
                letterSpacing = 0.15f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(brandName.uppercase(), cx, size * 0.74f, textPaint)

            // Slogan
            if (slogan.isNotBlank()) {
                val sloganPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = AndroidColor.rgb(180, 185, 195)
                    textSize = 24f
                    letterSpacing = 0.25f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(slogan.uppercase(), cx, size * 0.82f, sloganPaint)
            }

            withContext(Dispatchers.Main) {
                logoBitmap = bmp
            }
        }
    }

    LaunchedEffect(brandName, slogan, selectedSymbol, selectedStyle, primaryColor, secondaryColor) {
        renderLogo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Aura Logo Maker",
            subtitle = "Vector Brand Identity Studio",
            onBack = onNavigateBack
        )

        // Logo Canvas with Transparent Checkerboard
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(16.dp))
                    .background(Color(0xFF14161C))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap!!.asImageBitmap(),
                        contentDescription = "Logo Design",
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
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
                // Name & Slogan Fields
                OutlinedTextField(
                    value = brandName,
                    onValueChange = { brandName = it },
                    label = { Text("Brand Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        focusedBorderColor = AuraCreativeAccent,
                        unfocusedBorderColor = AuraBorderSubtle,
                        focusedContainerColor = AuraInputSurface,
                        unfocusedContainerColor = AuraInputSurface
                    )
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = slogan,
                    onValueChange = { slogan = it },
                    label = { Text("Tagline / Slogan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        focusedBorderColor = AuraCreativeAccent,
                        unfocusedBorderColor = AuraBorderSubtle,
                        focusedContainerColor = AuraInputSurface,
                        unfocusedContainerColor = AuraInputSurface
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Symbol Selector
                Text("Emblem Symbol", style = MaterialTheme.typography.titleSmall, color = AuraTextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    symbols.forEach { sym ->
                        CreativeChip(
                            text = sym,
                            isSelected = selectedSymbol == sym,
                            onClick = { selectedSymbol = sym }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Color Themes
                Text("Color Scheme", style = MaterialTheme.typography.titleSmall, color = AuraTextPrimary)
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        Pair(AuraCreativeAccent, AuraCyanHighlight),
                        Pair(Color(0xFFFF512F), Color(0xFFDD2476)),
                        Pair(Color(0xFF11998E), Color(0xFF38EF7D)),
                        Pair(Color(0xFFF5C16C), Color(0xFFFF737D)),
                        Pair(Color.White, Color(0xFFB3B7C2))
                    ).forEach { (c1, c2) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(c1, c2)))
                                .border(2.dp, if (primaryColor == c1) AuraCyanHighlight else Color.Transparent, CircleShape)
                                .clickable {
                                    primaryColor = c1
                                    secondaryColor = c2
                                }
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
                    text = "Export Transparent Logo PNG",
                    icon = Icons.Default.FileDownload,
                    useAuraGradient = true,
                    onClick = {
                        if (logoBitmap != null) {
                            scope.launch(Dispatchers.IO) {
                                val path = ImageProcessingUtils.saveBitmap(
                                    context,
                                    logoBitmap!!,
                                    filename = "aura_logo_${System.currentTimeMillis()}.png",
                                    format = Bitmap.CompressFormat.PNG
                                )
                                db.projectDao().insertProject(
                                    ProjectEntity(
                                        id = UUID.randomUUID().toString(),
                                        name = "Logo: $brandName",
                                        type = ProjectType.LOGO.name,
                                        lastModified = System.currentTimeMillis(),
                                        thumbnailUri = path
                                    )
                                )
                                val file = File(path)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Logo"))
                                withContext(Dispatchers.Main) {
                                    exportSuccessMessage = "Vector PNG saved and ready to share!"
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
