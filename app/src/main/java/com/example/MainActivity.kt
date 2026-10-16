package com.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ProjectEntity
import com.example.ui.screens.AiImageGeneratorScreen
import com.example.ui.screens.AuraChatScreen
import com.example.ui.screens.BackgroundRemoverScreen
import com.example.ui.screens.CollageMakerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LogoMakerScreen
import com.example.ui.screens.ObjectEraserScreen
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.AuraAiHighlight
import com.example.ui.theme.AuraCreativeAccent
import com.example.ui.theme.AuraCyanHighlight
import com.example.ui.theme.AuraDarkBackground
import com.example.ui.theme.AuraElevatedSurface
import com.example.ui.theme.AuraStudioTheme
import com.example.ui.theme.AuraTextMuted
import com.example.ui.theme.AuraTextPrimary
import com.example.ui.theme.AuraToolbarSurface

enum class StudioDestination {
    HOME,
    PHOTO_EDITOR,
    BG_REMOVER,
    OBJECT_ERASER,
    VIDEO_EDITOR,
    AI_GENERATOR,
    TEMPLATES,
    COLLAGE,
    LOGO_MAKER,
    AURA_CHAT,
    PROJECTS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuraStudioTheme {
                StudioAppContent()
            }
        }
    }
}

@Composable
fun StudioAppContent() {
    var currentDestination by remember { mutableStateOf(StudioDestination.HOME) }
    var destinationBackStack by remember { mutableStateOf(listOf(StudioDestination.HOME)) }

    // Inter-screen shared payload state
    var sharedEditorBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeProjectId by remember { mutableStateOf<String?>(null) }

    fun navigateTo(dest: StudioDestination) {
        destinationBackStack = destinationBackStack + dest
        currentDestination = dest
    }

    fun navigateBack() {
        if (destinationBackStack.size > 1) {
            destinationBackStack = destinationBackStack.dropLast(1)
            currentDestination = destinationBackStack.last()
        } else {
            currentDestination = StudioDestination.HOME
        }
    }

    BackHandler(enabled = currentDestination != StudioDestination.HOME) {
        navigateBack()
    }

    // Determine whether to show bottom navigation bar
    val showBottomBar = currentDestination in listOf(
        StudioDestination.HOME,
        StudioDestination.TEMPLATES,
        StudioDestination.PROJECTS,
        StudioDestination.AURA_CHAT
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AuraDarkBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = AuraToolbarSurface,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentDestination == StudioDestination.HOME,
                        onClick = { currentDestination = StudioDestination.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Studio") },
                        label = { Text("Studio") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AuraCyanHighlight,
                            indicatorColor = AuraCreativeAccent,
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination == StudioDestination.TEMPLATES,
                        onClick = { currentDestination = StudioDestination.TEMPLATES },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Templates") },
                        label = { Text("Explore") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AuraCyanHighlight,
                            indicatorColor = AuraCreativeAccent,
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            sharedEditorBitmap = null
                            activeProjectId = null
                            navigateTo(StudioDestination.PHOTO_EDITOR)
                        },
                        icon = {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Create",
                                tint = AuraCyanHighlight
                            )
                        },
                        label = { Text("Create") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraCyanHighlight,
                            selectedTextColor = AuraCyanHighlight,
                            unselectedIconColor = AuraCyanHighlight,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination == StudioDestination.PROJECTS,
                        onClick = { currentDestination = StudioDestination.PROJECTS },
                        icon = { Icon(Icons.Default.FolderOpen, contentDescription = "Projects") },
                        label = { Text("Projects") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AuraCyanHighlight,
                            indicatorColor = AuraCreativeAccent,
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )

                    NavigationBarItem(
                        selected = currentDestination == StudioDestination.AURA_CHAT,
                        onClick = { currentDestination = StudioDestination.AURA_CHAT },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Aura AI") },
                        label = { Text("Aura AI") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AuraAiHighlight,
                            indicatorColor = AuraCreativeAccent,
                            unselectedIconColor = AuraTextMuted,
                            unselectedTextColor = AuraTextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                StudioDestination.HOME -> HomeScreen(
                    onOpenPhotoEditor = {
                        sharedEditorBitmap = null
                        activeProjectId = null
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    },
                    onOpenBgRemover = { navigateTo(StudioDestination.BG_REMOVER) },
                    onOpenObjectEraser = { navigateTo(StudioDestination.OBJECT_ERASER) },
                    onOpenVideoEditor = { navigateTo(StudioDestination.VIDEO_EDITOR) },
                    onOpenAiGenerator = { navigateTo(StudioDestination.AI_GENERATOR) },
                    onOpenTemplates = { navigateTo(StudioDestination.TEMPLATES) },
                    onOpenCollage = { navigateTo(StudioDestination.COLLAGE) },
                    onOpenLogoMaker = { navigateTo(StudioDestination.LOGO_MAKER) },
                    onOpenAuraChat = { navigateTo(StudioDestination.AURA_CHAT) },
                    onOpenProjects = { navigateTo(StudioDestination.PROJECTS) }
                )

                StudioDestination.PHOTO_EDITOR -> PhotoEditorScreen(
                    initialBitmap = sharedEditorBitmap,
                    projectId = activeProjectId,
                    onNavigateBack = { navigateBack() }
                )

                StudioDestination.BG_REMOVER -> BackgroundRemoverScreen(
                    inputBitmap = sharedEditorBitmap,
                    onNavigateBack = { navigateBack() },
                    onOpenInEditor = { bmp ->
                        sharedEditorBitmap = bmp
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    }
                )

                StudioDestination.OBJECT_ERASER -> ObjectEraserScreen(
                    inputBitmap = sharedEditorBitmap,
                    onNavigateBack = { navigateBack() },
                    onOpenInEditor = { bmp ->
                        sharedEditorBitmap = bmp
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    }
                )

                StudioDestination.VIDEO_EDITOR -> VideoEditorScreen(
                    onNavigateBack = { navigateBack() }
                )

                StudioDestination.AI_GENERATOR -> AiImageGeneratorScreen(
                    onNavigateBack = { navigateBack() },
                    onOpenInEditor = { bmp ->
                        sharedEditorBitmap = bmp
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    }
                )

                StudioDestination.TEMPLATES -> TemplatesScreen(
                    onNavigateBack = { navigateBack() },
                    onSelectTemplate = { bmp, title ->
                        sharedEditorBitmap = bmp
                        activeProjectId = null
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    }
                )

                StudioDestination.COLLAGE -> CollageMakerScreen(
                    onNavigateBack = { navigateBack() }
                )

                StudioDestination.LOGO_MAKER -> LogoMakerScreen(
                    onNavigateBack = { navigateBack() }
                )

                StudioDestination.AURA_CHAT -> AuraChatScreen(
                    onNavigateBack = { navigateBack() },
                    onOpenTool = { toolName ->
                        when {
                            toolName.contains("Adjust", true) || toolName.contains("Filter", true) -> {
                                navigateTo(StudioDestination.PHOTO_EDITOR)
                            }
                            toolName.contains("Background", true) -> {
                                navigateTo(StudioDestination.BG_REMOVER)
                            }
                            toolName.contains("Eraser", true) || toolName.contains("Object", true) -> {
                                navigateTo(StudioDestination.OBJECT_ERASER)
                            }
                            toolName.contains("Video", true) -> {
                                navigateTo(StudioDestination.VIDEO_EDITOR)
                            }
                            toolName.contains("Logo", true) -> {
                                navigateTo(StudioDestination.LOGO_MAKER)
                            }
                            toolName.contains("Collage", true) -> {
                                navigateTo(StudioDestination.COLLAGE)
                            }
                            toolName.contains("AI", true) || toolName.contains("Image", true) -> {
                                navigateTo(StudioDestination.AI_GENERATOR)
                            }
                            else -> navigateTo(StudioDestination.PHOTO_EDITOR)
                        }
                    }
                )

                StudioDestination.PROJECTS -> ProjectsScreen(
                    onNavigateBack = { navigateBack() },
                    onOpenProject = { project ->
                        activeProjectId = project.id
                        sharedEditorBitmap = null
                        if (project.type == "VIDEO") {
                            navigateTo(StudioDestination.VIDEO_EDITOR)
                        } else {
                            navigateTo(StudioDestination.PHOTO_EDITOR)
                        }
                    },
                    onCreateNew = {
                        sharedEditorBitmap = null
                        activeProjectId = null
                        navigateTo(StudioDestination.PHOTO_EDITOR)
                    }
                )
            }
        }
    }
}
