package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TemplateItem
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativeTopBar
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraBorderSubtle
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
import com.example.ui.theme.AuraInputSurface
import com.example.ui.theme.AuraPrimarySurface
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraTextSecondary

@Composable
fun TemplatesScreen(
    onNavigateBack: () -> Unit,
    onSelectTemplate: (Bitmap, String) -> Unit
) {
    BackHandler { onNavigateBack() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Social", "YouTube", "Marketing", "Music")

    val templates = remember {
        listOf(
            TemplateItem(
                id = "t1",
                title = "Cyberpunk Music Single",
                category = "Music",
                aspectRatio = "1:1",
                defaultText = "SYNTHWAVE 2099",
                subtitle = "NEW ALBUM RELEASE",
                primaryColorHex = "#8B5CF6",
                secondaryColorHex = "#67D8F0"
            ),
            TemplateItem(
                id = "t2",
                title = "Viral Instagram Story",
                category = "Social",
                aspectRatio = "9:16",
                defaultText = "FLASH SALE 50%",
                subtitle = "LIMITED TIME OFFER",
                primaryColorHex = "#FF512F",
                secondaryColorHex = "#DD2476"
            ),
            TemplateItem(
                id = "t3",
                title = "High-CTR YouTube Cover",
                category = "YouTube",
                aspectRatio = "16:9",
                defaultText = "I BUILT THIS IN 24H!",
                subtitle = "EPISODE 04",
                primaryColorHex = "#F5C16C",
                secondaryColorHex = "#8B5CF6"
            ),
            TemplateItem(
                id = "t4",
                title = "Minimal Fashion Editorial",
                category = "Social",
                aspectRatio = "4:5",
                defaultText = "SUMMER NOIR",
                subtitle = "COLLECTION XXIV",
                primaryColorHex = "#23262D",
                secondaryColorHex = "#67D8F0"
            ),
            TemplateItem(
                id = "t5",
                title = "Podcast Episode Card",
                category = "Marketing",
                aspectRatio = "1:1",
                defaultText = "THE FUTURE OF AI",
                subtitle = "AURA STUDIO TALKS",
                primaryColorHex = "#11998E",
                secondaryColorHex = "#38EF7D"
            ),
            TemplateItem(
                id = "t6",
                title = "Tech Conference Banner",
                category = "Marketing",
                aspectRatio = "16:9",
                defaultText = "DEVCON GLOBAL",
                subtitle = "REGISTER TODAY",
                primaryColorHex = "#17191E",
                secondaryColorHex = "#C084FC"
            )
        )
    }

    val filteredTemplates = templates.filter {
        (selectedCategory == "All" || it.category == selectedCategory) &&
                (searchQuery.isBlank() || it.title.contains(searchQuery, true) || it.defaultText.contains(searchQuery, true))
    }

    fun renderTemplateBitmap(template: TemplateItem): Bitmap {
        val w = 800
        val h = when (template.aspectRatio) {
            "9:16" -> (800 * 16 / 9)
            "16:9" -> (800 * 9 / 16)
            "4:5" -> (800 * 5 / 4)
            else -> 800
        }
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background Gradient
        val c1 = AndroidColor.parseColor(template.primaryColorHex)
        val c2 = AndroidColor.parseColor(template.secondaryColorHex)
        paint.shader = android.graphics.LinearGradient(
            0f, 0f, w.toFloat(), h.toFloat(), c1, c2, android.graphics.Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        paint.shader = null

        // Geometric decorative accents
        paint.color = AndroidColor.argb(40, 255, 255, 255)
        canvas.drawCircle(w * 0.8f, h * 0.2f, w * 0.35f, paint)

        // Header / Main Title
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 52f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(template.defaultText, w / 2f, h * 0.48f, textPaint)

        // Subtitle
        textPaint.textSize = 26f
        textPaint.color = AndroidColor.argb(220, 255, 255, 255)
        canvas.drawText(template.subtitle, w / 2f, h * 0.56f, textPaint)

        return bmp
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Template Library",
            subtitle = "Fully Editable Studio Designs",
            onBack = onNavigateBack
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search social posts, covers, flyers...", color = AuraTextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = AuraTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AuraTextPrimary,
                    unfocusedTextColor = AuraTextPrimary,
                    focusedBorderColor = AuraCreativeAccent,
                    unfocusedBorderColor = AuraBorderSubtle,
                    focusedContainerColor = AuraInputSurface,
                    unfocusedContainerColor = AuraInputSurface
                )
            )

            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    CreativeChip(
                        text = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }

        // Templates Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredTemplates) { template ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, AuraBorderSubtle, RoundedCornerShape(14.dp))
                        .clickable {
                            val bitmap = renderTemplateBitmap(template)
                            onSelectTemplate(bitmap, template.defaultText)
                        },
                    color = AuraElevatedSurface
                ) {
                    Column {
                        // Card Preview Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(android.graphics.Color.parseColor(template.primaryColorHex)),
                                            Color(android.graphics.Color.parseColor(template.secondaryColorHex))
                                        )
                                    )
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = template.defaultText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = template.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1
                                )
                            }
                        }

                        // Info Area
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = AuraTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = template.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AuraCyanHighlight
                                )
                                Text(
                                    text = template.aspectRatio,
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
}
