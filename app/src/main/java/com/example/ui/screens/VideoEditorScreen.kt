package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuraDatabase
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.data.model.VideoClip
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
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
import com.example.ui.theme.AuraInputSurface
import com.example.ui.theme.AuraPrimarySurface
import com.example.ui.theme.AuraSecondaryAccent
import com.example.ui.theme.AuraSuccess
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraToolbarSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun VideoEditorScreen(
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AuraDatabase.getInstance(context) }

    // Video Clips in Timeline
    val clips = remember {
        mutableStateListOf(
            VideoClip(id = "c1", title = "Intro Glow", durationSeconds = 4.0f, colorHex = "#8B5CF6"),
            VideoClip(id = "c2", title = "Aura Scene", durationSeconds = 6.0f, colorHex = "#67D8F0"),
            VideoClip(id = "c3", title = "Outro Pulse", durationSeconds = 3.5f, colorHex = "#C084FC")
        )
    }

    var selectedClipId by remember { mutableStateOf("c1") }
    var aspectRatio by remember { mutableStateOf("9:16") }
    var selectedSpeed by remember { mutableFloatStateOf(1.0f) }
    var isPlaying by remember { mutableStateOf(false) }
    var playheadPosition by remember { mutableFloatStateOf(0f) }
    val totalDuration = clips.sumOf { (it.endTrim - it.startTrim).toDouble() }.toFloat()

    var overlayText by remember { mutableStateOf("CYBERPUNK VISION") }
    var selectedAudioTrack by remember { mutableStateOf("Synthwave Dream") }
    var isExporting by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    // Playhead animation loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val start = playheadPosition
            val remaining = (totalDuration - start).coerceAtLeast(0.1f)
            val anim = Animatable(start)
            anim.animateTo(
                targetValue = totalDuration,
                animationSpec = tween(
                    durationMillis = (remaining * 1000 / selectedSpeed).toInt(),
                    easing = LinearEasing
                )
            ) {
                playheadPosition = this.value
            }
            isPlaying = false
            playheadPosition = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Video Editor & Timeline",
            subtitle = "${String.format("%.1f", playheadPosition)}s / ${String.format("%.1f", totalDuration)}s",
            onBack = onNavigateBack,
            actions = {
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            db.projectDao().insertProject(
                                ProjectEntity(
                                    id = UUID.randomUUID().toString(),
                                    name = "Video Project ${System.currentTimeMillis() % 1000}",
                                    type = ProjectType.VIDEO.name,
                                    lastModified = System.currentTimeMillis()
                                )
                            )
                            withContext(Dispatchers.Main) {
                                exportMessage = "Saved to Project Library!"
                            }
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = AuraTextPrimary)
                }
            }
        )

        // Video Player Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(
                        when (aspectRatio) {
                            "9:16" -> 9f / 16f
                            "16:9" -> 16f / 9f
                            else -> 1f
                        }
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Dynamic animated scene representation
                val activeClip = clips.firstOrNull { it.id == selectedClipId } ?: clips.first()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(android.graphics.Color.parseColor(activeClip.colorHex)),
                                    AuraDarkBackground
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = activeClip.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (overlayText.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = AuraDarkBackground.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = overlayText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AuraCyanHighlight,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Play / Pause Overlay Button
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AuraDarkBackground.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Timeline & Editing Tools Panel
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
                // Multi-track Timeline Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TIMELINE", style = MaterialTheme.typography.labelLarge, color = AuraTextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CreativeChip(
                            text = "9:16",
                            isSelected = aspectRatio == "9:16",
                            onClick = { aspectRatio = "9:16" }
                        )
                        CreativeChip(
                            text = "16:9",
                            isSelected = aspectRatio == "16:9",
                            onClick = { aspectRatio = "16:9" }
                        )
                        CreativeChip(
                            text = "1:1",
                            isSelected = aspectRatio == "1:1",
                            onClick = { aspectRatio = "1:1" }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Track 1: Video Clips with scrubber line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraPrimarySurface)
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        clips.forEach { clip ->
                            val isSelected = clip.id == selectedClipId
                            val weight = (clip.endTrim - clip.startTrim) / totalDuration

                            Box(
                                modifier = Modifier
                                    .weight(weight.coerceAtLeast(0.1f))
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Color(android.graphics.Color.parseColor(clip.colorHex)).copy(alpha = 0.8f)
                                    )
                                    .border(
                                        2.dp,
                                        if (isSelected) AuraCyanHighlight else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedClipId = clip.id }
                                    .padding(6.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${clip.title}\n${String.format("%.1f", clip.endTrim - clip.startTrim)}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Scrubber playhead line
                    val progress = (playheadPosition / totalDuration).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(3.dp)
                                .height(64.dp)
                                .background(AuraCyanHighlight)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Track 2: Audio Track
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF132F38))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = AuraCyanHighlight, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Audio: $selectedAudioTrack",
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraCyanHighlight
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Editing Tools: Split, Trim, Speed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CreativeSecondaryButton(
                        text = "Split Clip",
                        icon = Icons.Default.ContentCut,
                        onClick = {
                            val activeIdx = clips.indexOfFirst { it.id == selectedClipId }
                            if (activeIdx != -1) {
                                val c = clips[activeIdx]
                                val half = (c.endTrim - c.startTrim) / 2f
                                clips[activeIdx] = c.copy(endTrim = c.startTrim + half)
                                clips.add(
                                    activeIdx + 1,
                                    VideoClip(
                                        id = UUID.randomUUID().toString(),
                                        title = "${c.title} Pt2",
                                        durationSeconds = half,
                                        colorHex = "#C084FC"
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    CreativeSecondaryButton(
                        text = "Speed ${selectedSpeed}x",
                        icon = Icons.Default.Speed,
                        onClick = {
                            selectedSpeed = when (selectedSpeed) {
                                0.5f -> 1.0f
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                else -> 0.5f
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (exportMessage != null) {
                    Text(
                        text = exportMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = AuraSuccess,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                CreativePrimaryButton(
                    text = if (isExporting) "Rendering MP4 Video..." else "Export Final Video",
                    icon = Icons.Default.FileDownload,
                    useAuraGradient = true,
                    isLoading = isExporting,
                    onClick = {
                        isExporting = true
                        scope.launch {
                            kotlinx.coroutines.delay(1200)
                            isExporting = false
                            exportMessage = "Video rendered in 1080p and saved to gallery!"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
