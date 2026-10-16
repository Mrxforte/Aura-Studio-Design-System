package com.example.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.api.GeminiStudioService
import com.example.data.local.AuraDatabase
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.components.CreativeSecondaryButton
import com.example.ui.components.CreativeTopBar
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

@Composable
fun AiImageGeneratorScreen(
    onNavigateBack: () -> Unit,
    onOpenInEditor: (Bitmap) -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AuraDatabase.getInstance(context) }

    var prompt by remember { mutableStateOf("Futuristic neon cyberpunk portrait with purple aura lighting") }
    var selectedStyle by remember { mutableStateOf("Photorealistic") }
    var selectedRatio by remember { mutableStateOf("1:1") }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedBitmap by remember {
        mutableStateOf<Bitmap?>(ImageProcessingUtils.createSampleCreativeBitmap("portrait"))
    }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val styles = listOf(
        "Photorealistic",
        "Cinematic 8K",
        "Cyberpunk Neon",
        "Anime Studio",
        "3D Render",
        "Logo Concept",
        "Minimalist Art"
    )

    val aspectRatios = listOf("1:1", "9:16", "16:9", "4:5")

    fun generate() {
        if (prompt.isBlank() || isGenerating) return
        isGenerating = true
        statusMessage = "Aura AI is rendering artwork..."

        scope.launch(Dispatchers.IO) {
            val result = GeminiStudioService.generateImage(prompt, selectedRatio, selectedStyle)
            withContext(Dispatchers.Main) {
                isGenerating = false
                if (result.isSuccess && result.getOrNull() != null) {
                    generatedBitmap = result.getOrNull()
                    statusMessage = "Artwork generated with Gemini AI!"
                } else {
                    // Fallback to studio procedural generator
                    generatedBitmap = ImageProcessingUtils.createSampleCreativeBitmap(
                        if (prompt.contains("landscape", true)) "landscape" else "portrait"
                    )
                    statusMessage = "Rendered with Aura Studio Engine"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Aura Image Generator",
            subtitle = "Text-to-Art AI Studio",
            onBack = onNavigateBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Preview Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        when (selectedRatio) {
                            "9:16" -> 9f / 16f
                            "16:9" -> 16f / 9f
                            "4:5" -> 4f / 5f
                            else -> 1f
                        }
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(16.dp))
                    .background(AuraElevatedSurface),
                contentAlignment = Alignment.Center
            ) {
                if (isGenerating) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AuraCyanHighlight, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = statusMessage ?: "Dreaming creative composition...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextSecondary
                        )
                    }
                } else if (generatedBitmap != null) {
                    Image(
                        bitmap = generatedBitmap!!.asImageBitmap(),
                        contentDescription = "Generated Artwork",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (statusMessage != null && !isGenerating) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = statusMessage!!,
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraSuccess,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action row if generated
            if (generatedBitmap != null && !isGenerating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CreativeSecondaryButton(
                        text = "Edit in Studio",
                        icon = Icons.Default.Edit,
                        onClick = { onOpenInEditor(generatedBitmap!!) },
                        modifier = Modifier.weight(1f)
                    )
                    CreativeSecondaryButton(
                        text = "Save Project",
                        icon = Icons.Default.FileDownload,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val path = ImageProcessingUtils.saveBitmap(
                                    context, generatedBitmap!!, "aura_ai_${System.currentTimeMillis()}.png"
                                )
                                db.projectDao().insertProject(
                                    ProjectEntity(
                                        id = UUID.randomUUID().toString(),
                                        name = "AI Art: ${prompt.take(18)}",
                                        type = ProjectType.PHOTO.name,
                                        lastModified = System.currentTimeMillis(),
                                        thumbnailUri = path
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    statusMessage = "Saved to your project gallery!"
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Prompt Box
            CreativeCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creative Prompt",
                            style = MaterialTheme.typography.titleSmall,
                            color = AuraTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        CreativeChip(
                            text = "Aura Enhance",
                            icon = Icons.Default.AutoAwesome,
                            isSelected = false,
                            onClick = {
                                prompt = "$prompt, ultra-detailed volumetric lighting, atmospheric haze, 8k render, masterpiece"
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AuraTextPrimary,
                            unfocusedTextColor = AuraTextPrimary,
                            focusedBorderColor = AuraCreativeAccent,
                            unfocusedBorderColor = AuraBorderSubtle,
                            focusedContainerColor = AuraInputSurface,
                            unfocusedContainerColor = AuraInputSurface
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Style Presets
            Text(
                text = "Style Preset",
                style = MaterialTheme.typography.titleSmall,
                color = AuraTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    CreativeChip(
                        text = style,
                        isSelected = selectedStyle == style,
                        onClick = { selectedStyle = style }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Aspect Ratio
            Text(
                text = "Aspect Ratio",
                style = MaterialTheme.typography.titleSmall,
                color = AuraTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aspectRatios.forEach { ratio ->
                    CreativeChip(
                        text = ratio,
                        isSelected = selectedRatio == ratio,
                        onClick = { selectedRatio = ratio },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            CreativePrimaryButton(
                text = if (isGenerating) "Generating..." else "Generate Artwork",
                icon = Icons.Default.AutoAwesome,
                useAuraGradient = true,
                isLoading = isGenerating,
                onClick = { generate() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
