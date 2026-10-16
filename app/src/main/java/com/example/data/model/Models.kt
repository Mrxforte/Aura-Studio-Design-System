package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectType {
    PHOTO,
    VIDEO,
    DESIGN,
    COLLAGE,
    LOGO,
    BACKGROUND_REMOVER,
    OBJECT_ERASER
}

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // from ProjectType.name
    val lastModified: Long,
    val width: Int = 1080,
    val height: Int = 1080,
    val thumbnailUri: String? = null,
    val sourceImageUri: String? = null,
    val adjustmentsJson: String = "",
    val layersJson: String = ""
)

data class ImageAdjustments(
    val brightness: Float = 0f,      // -100 to 100
    val contrast: Float = 0f,        // -100 to 100
    val exposure: Float = 0f,        // -100 to 100
    val saturation: Float = 0f,      // -100 to 100
    val temperature: Float = 0f,     // -100 to 100
    val tint: Float = 0f,            // -100 to 100
    val sharpness: Float = 0f,       // 0 to 100
    val vignette: Float = 0f,        // 0 to 100
    val blur: Float = 0f,            // 0 to 100
    val rotation: Int = 0,           // 0, 90, 180, 270
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val filterPreset: String = "Normal",
    val filterIntensity: Float = 1.0f
)

enum class LayerType {
    IMAGE,
    TEXT,
    STICKER,
    SHAPE
}

data class StudioLayer(
    val id: String,
    val name: String,
    val type: LayerType,
    val text: String = "",
    val colorHex: String = "#FFFFFF",
    val fontSize: Float = 28f,
    val x: Float = 0.5f, // relative 0..1
    val y: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val opacity: Float = 1.0f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val shapeType: String = "RECTANGLE" // RECTANGLE, CIRCLE, STAR, BADGE
)

data class AuraChatMessage(
    val id: String,
    val isUser: Boolean,
    val message: String,
    val imageBase64: String? = null,
    val suggestedTools: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class TemplateItem(
    val id: String,
    val title: String,
    val category: String,
    val aspectRatio: String, // "1:1", "9:16", "16:9", "4:5"
    val defaultText: String,
    val subtitle: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val iconName: String = "sparkle"
)

data class VideoClip(
    val id: String,
    val title: String,
    val durationSeconds: Float,
    val startTrim: Float = 0f,
    val endTrim: Float = durationSeconds,
    val speed: Float = 1.0f,
    val colorHex: String = "#8B5CF6"
)
