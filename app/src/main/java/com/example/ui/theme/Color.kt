package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Main Surfaces
val AuraDarkBackground = Color(0xFF101114)
val AuraPrimarySurface = Color(0xFF17191E)
val AuraElevatedSurface = Color(0xFF20232A)
val AuraToolbarSurface = Color(0xFF191B21)
val AuraInputSurface = Color(0xFF23262D)
val AuraBorderSubtle = Color(0xFF343740)

// Text Colors
val AuraTextPrimary = Color(0xFFF5F5F7)
val AuraTextSecondary = Color(0xFFB3B7C2)
val AuraTextMuted = Color(0xFF858B98)

// Accent Colors
val AuraCreativeAccent = Color(0xFF8B5CF6)
val AuraSecondaryAccent = Color(0xFFC084FC)
val AuraAiHighlight = Color(0xFFA78BFA)
val AuraCyanHighlight = Color(0xFF67D8F0)
val AuraSuccess = Color(0xFF55D6A2)
val AuraWarning = Color(0xFFF5C16C)
val AuraError = Color(0xFFFF737D)

// Canvas & Grid
val AuraCanvasCheckerDark = Color(0xFF1A1C22)
val AuraCanvasCheckerLight = Color(0xFF252830)

// Aura Gradients
val AuraGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF8B5CF6),
        Color(0xFFC084FC),
        Color(0xFFA78BFA),
        Color(0xFF67D8F0)
    )
)

val AuraSubtleGlow = Brush.radialGradient(
    colors = listOf(
        Color(0x338B5CF6),
        Color(0x1267D8F0),
        Color(0x00000000)
    )
)

val AuraCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1F222B),
        Color(0xFF17191E)
    )
)
