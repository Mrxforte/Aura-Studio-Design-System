package com.example.ui.screens

import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuraDatabase
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectType
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativePrimaryButton
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
import com.example.ui.theme.AuraGradient
import com.example.ui.theme.AuraInputSurface
import com.example.ui.theme.AuraPrimarySurface
import com.example.ui.theme.AuraSecondaryAccent
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary
import com.example.ui.theme.AuraToolbarSurface
import com.example.util.ImageProcessingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen(
    onOpenPhotoEditor: () -> Unit,
    onOpenBgRemover: () -> Unit,
    onOpenObjectEraser: () -> Unit,
    onOpenVideoEditor: () -> Unit,
    onOpenAiGenerator: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenCollage: () -> Unit,
    onOpenLogoMaker: () -> Unit,
    onOpenAuraChat: () -> Unit,
    onOpenProjects: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AuraDatabase.getInstance(context) }
    val projects by db.projectDao().getAllProjects().collectAsState(initial = emptyList())

    var showCreateModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        // Studio Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AuraToolbarSurface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Aura Studio Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AURA STUDIO",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "PRO CREATIVE SUITE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraCyanHighlight,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onOpenAuraChat,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AuraElevatedSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Aura AI Chat",
                            tint = AuraAiHighlight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onOpenProjects,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AuraElevatedSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Projects",
                            tint = AuraTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Studio Dashboard
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero: Aura AI Creative Assistant Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, AuraBorderSubtle, RoundedCornerShape(18.dp))
                    .clickable { onOpenAuraChat() },
                color = AuraElevatedSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF251C3D),
                                    Color(0xFF17191E)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(AuraGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "AURA AI CREATIVE DIRECTOR",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraAiHighlight
                                )
                            }
                            Surface(
                                color = AuraToolbarSurface,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraCyanHighlight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Elevate your visual storytelling with multimodal editing intelligence.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AuraTextPrimary
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = AuraToolbarSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorderSubtle),
                                modifier = Modifier.clickable { onOpenAuraChat() }
                            ) {
                                Text(
                                    text = "✨ Enhance Photo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                color = AuraToolbarSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorderSubtle),
                                modifier = Modifier.clickable { onOpenBgRemover() }
                            ) {
                                Text(
                                    text = "✂️ Cutout Subject",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraTextSecondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Primary Creative Tools Grid
            Text(
                text = "STUDIO WORKSPACE",
                style = MaterialTheme.typography.labelLarge,
                color = AuraTextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioToolCard(
                    title = "Photo Studio",
                    subtitle = "Adjust, filters, layers",
                    icon = Icons.Default.Tune,
                    accentColor = AuraCreativeAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenPhotoEditor
                )
                StudioToolCard(
                    title = "AI Generator",
                    subtitle = "Text to 8K imagery",
                    icon = Icons.Default.AutoAwesome,
                    accentColor = AuraSecondaryAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenAiGenerator
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioToolCard(
                    title = "Cutout AI",
                    subtitle = "Smart BG remover",
                    icon = Icons.Default.ContentCut,
                    accentColor = AuraCyanHighlight,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenBgRemover
                )
                StudioToolCard(
                    title = "Object Eraser",
                    subtitle = "Content-aware inpaint",
                    icon = Icons.Default.Brush,
                    accentColor = Color(0xFFFF737D),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenObjectEraser
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioToolCard(
                    title = "Video Editor",
                    subtitle = "Timeline & trimming",
                    icon = Icons.Default.Movie,
                    accentColor = Color(0xFFF5C16C),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenVideoEditor
                )
                StudioToolCard(
                    title = "Collage Maker",
                    subtitle = "Multi-photo grid",
                    icon = Icons.Default.GridView,
                    accentColor = Color(0xFF55D6A2),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenCollage
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StudioToolCard(
                    title = "Templates",
                    subtitle = "Social, covers, flyers",
                    icon = Icons.Default.Dashboard,
                    accentColor = AuraAiHighlight,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenTemplates
                )
                StudioToolCard(
                    title = "Logo Maker",
                    subtitle = "Vector brand identity",
                    icon = Icons.Default.Layers,
                    accentColor = AuraCyanHighlight,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenLogoMaker
                )
            }

            Spacer(Modifier.height(24.dp))

            // Recent Projects Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PROJECTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = AuraTextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All (${projects.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = AuraCyanHighlight,
                    modifier = Modifier.clickable { onOpenProjects() }
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "New Project" Card
                Surface(
                    modifier = Modifier
                        .size(130.dp, 160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, AuraBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { showCreateModal = true },
                    color = AuraElevatedSurface
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AuraCreativeAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "New Project",
                            style = MaterialTheme.typography.titleSmall,
                            color = AuraTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Existing Projects
                projects.take(6).forEach { project ->
                    Surface(
                        modifier = Modifier
                            .size(130.dp, 160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, AuraBorderSubtle, RoundedCornerShape(14.dp))
                            .clickable {
                                if (project.type == ProjectType.VIDEO.name) {
                                    onOpenVideoEditor()
                                } else {
                                    onOpenPhotoEditor()
                                }
                            },
                        color = AuraElevatedSurface
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(AuraPrimarySurface),
                                contentAlignment = Alignment.Center
                            ) {
                                val thumbFile = project.thumbnailUri?.let { File(it) }
                                if (thumbFile != null && thumbFile.exists()) {
                                    val bmp = remember(project.thumbnailUri) {
                                        BitmapFactory.decodeFile(thumbFile.absolutePath)
                                    }
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = project.name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = AuraTextMuted,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AuraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = project.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create New Project Modal
    if (showCreateModal) {
        AlertDialog(
            onDismissRequest = { showCreateModal = false },
            title = { Text("Create New Project", color = AuraTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Canvas Ratio:", style = MaterialTheme.typography.labelMedium, color = AuraTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CreativeChip(text = "1:1 Square", isSelected = true, onClick = {
                            showCreateModal = false
                            onOpenPhotoEditor()
                        })
                        CreativeChip(text = "9:16 Story", isSelected = false, onClick = {
                            showCreateModal = false
                            onOpenPhotoEditor()
                        })
                        CreativeChip(text = "16:9 Video", isSelected = false, onClick = {
                            showCreateModal = false
                            onOpenVideoEditor()
                        })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showCreateModal = false
                    onOpenPhotoEditor()
                }) {
                    Text("Start Studio", color = AuraCreativeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateModal = false }) {
                    Text("Cancel", color = AuraTextSecondary)
                }
            },
            containerColor = AuraElevatedSurface
        )
    }
}

@Composable
fun StudioToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, AuraBorderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = AuraElevatedSurface
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = AuraTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
