package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiStudioService
import com.example.data.model.AuraChatMessage
import com.example.ui.components.CreativeCard
import com.example.ui.components.CreativeChip
import com.example.ui.components.CreativeTopBar
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
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AuraChatScreen(
    onNavigateBack: () -> Unit,
    onOpenTool: (String) -> Unit
) {
    BackHandler { onNavigateBack() }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            AuraChatMessage(
                id = "m1",
                isUser = false,
                message = "Welcome to Aura Studio! I am your AI creative director. I can help enhance lighting, formulate prompts, isolate subjects, remove objects, and coordinate your design workflow. What are we creating today?",
                suggestedTools = listOf("Remove Background", "Adjustments", "Filters", "AI Image Generator")
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isAuraThinking by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "Make this photo cinematic",
        "Remove background with precision",
        "Erase unwanted photobomber",
        "Design a cyberpunk logo",
        "Create an Instagram story design"
    )

    fun sendMessage(text: String) {
        if (text.isBlank() || isAuraThinking) return
        val userMsg = AuraChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            message = text.trim()
        )
        messages.add(userMsg)
        inputText = ""
        isAuraThinking = true

        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val history = messages.map { Pair(it.message, it.isUser) }
            val responseResult = GeminiStudioService.chatWithAura(userMsg.message, history)
            val auraMsg = if (responseResult.isSuccess) {
                val resp = responseResult.getOrNull()!!
                AuraChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    message = resp.message,
                    suggestedTools = resp.suggestedTools
                )
            } else {
                AuraChatMessage(
                    id = UUID.randomUUID().toString(),
                    isUser = false,
                    message = "I encountered a network issue, but you can directly access our studio editing tools below.",
                    suggestedTools = listOf("Adjustments", "Filters", "Remove Background")
                )
            }
            messages.add(auraMsg)
            isAuraThinking = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraDarkBackground)
    ) {
        CreativeTopBar(
            title = "Aura AI Assistant",
            subtitle = "Creative Director & Workflow Assistant",
            onBack = onNavigateBack
        )

        // Chat Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                if (msg.isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            color = AuraCreativeAccent,
                            shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AuraGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Aura",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = AuraElevatedSurface,
                                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorderSubtle)
                            ) {
                                Text(
                                    text = msg.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AuraTextPrimary,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                )
                            }

                            if (msg.suggestedTools.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    msg.suggestedTools.forEach { tool ->
                                        CreativeChip(
                                            text = tool,
                                            icon = Icons.Default.AutoAwesome,
                                            isSelected = true,
                                            onClick = { onOpenTool(tool) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isAuraThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(AuraGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Aura is composing thoughts...",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuraAiHighlight
                        )
                    }
                }
            }
        }

        // Quick prompts suggestions ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPrompts.forEach { prompt ->
                Surface(
                    color = AuraToolbarSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AuraBorderSubtle),
                    modifier = Modifier.clickable { sendMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = AuraTextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Input Panel
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AuraToolbarSurface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Aura to edit, compose, or generate...", color = AuraTextMuted) },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AuraTextPrimary,
                        unfocusedTextColor = AuraTextPrimary,
                        focusedBorderColor = AuraCreativeAccent,
                        unfocusedBorderColor = AuraBorderSubtle,
                        focusedContainerColor = AuraInputSurface,
                        unfocusedContainerColor = AuraInputSurface
                    )
                )

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { sendMessage(inputText) },
                    enabled = inputText.isNotBlank() && !isAuraThinking,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) AuraCreativeAccent else AuraElevatedSurface)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.White else AuraTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
