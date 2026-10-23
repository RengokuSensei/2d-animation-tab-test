package com.example.frameforge.ui.aether

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.BlendModeType
import com.example.frameforge.model.ToolType
import com.example.frameforge.ui.StudioCanvas
import com.example.frameforge.ui.popovers.ColorPickerPopover
import com.example.frameforge.viewmodel.StudioViewModel

@Composable
fun AetherStudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Core Project State
    val project by viewModel.project.collectAsState()
    val currentFrame by viewModel.currentFrame.collectAsState()
    val activeLayerId by viewModel.activeLayerId.collectAsState()
    val currentTool by viewModel.currentTool.collectAsState()
    val brushColor by viewModel.brushColor.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val brushOpacity by viewModel.brushOpacity.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val onionSkinConfig by viewModel.onionSkinConfig.collectAsState()
    val currentStroke by viewModel.currentStroke.collectAsState()
    val colorGradingState by viewModel.colorGradingState.collectAsState()
    val nodeGraph by viewModel.nodeGraph.collectAsState()
    val localExportState by viewModel.localExportState.collectAsState()

    // Color picker tabs & palettes
    val isColorPickerOpen by viewModel.isColorPickerOpen.collectAsState()
    val colorPickerTab by viewModel.colorPickerTab.collectAsState()
    val recentColors by viewModel.recentColors.collectAsState()
    val customPalette by viewModel.customPalette.collectAsState()

    // Aether Studio Specific UI State
    var activeMode by remember { mutableStateOf(AetherWorkspaceMode.DRAW) }
    var isZenMode by remember { mutableStateOf(false) }
    var isDrawingActive by remember { mutableStateOf(false) }
    var sceneBgVisible by remember { mutableStateOf(true) }
    var currentZoom by remember { mutableFloatStateOf(1.0f) }

    // Popover Visibility States
    var isPagesPopoverOpen by remember { mutableStateOf(false) }
    var isOnionPopoverOpen by remember { mutableStateOf(false) }
    var isLayersPopoverOpen by remember { mutableStateOf(false) }

    // Storyboard pages list
    val storyboardPages = remember {
        listOf(
            "Page 1 · Meadow Bounce",
            "Page 2 · Close-Up Blink",
            "Page 3 · Sky Leap Cut"
        )
    }
    var currentPageIndex by remember { mutableStateOf(0) }

    // Close all popovers on Back button
    val hasPopoverOpen = isPagesPopoverOpen || isOnionPopoverOpen || isLayersPopoverOpen || isColorPickerOpen
    BackHandler(enabled = hasPopoverOpen || activeMode != AetherWorkspaceMode.DRAW || isZenMode) {
        when {
            isPagesPopoverOpen -> isPagesPopoverOpen = false
            isOnionPopoverOpen -> isOnionPopoverOpen = false
            isLayersPopoverOpen -> isLayersPopoverOpen = false
            isColorPickerOpen -> viewModel.toggleColorPicker()
            activeMode != AetherWorkspaceMode.DRAW -> activeMode = AetherWorkspaceMode.DRAW
            isZenMode -> isZenMode = false
        }
    }

    // Auto-Compact Transitions when drawing on canvas
    val shouldCompact = isDrawingActive && !isZenMode
    val topBarOffset by animateDpAsState(
        targetValue = if (shouldCompact || isZenMode) (-120).dp else 0.dp,
        animationSpec = tween(280),
        label = "topBarOffset"
    )
    val leftDockOffset by animateDpAsState(
        targetValue = if (shouldCompact || isZenMode) (-120).dp else 0.dp,
        animationSpec = tween(280),
        label = "leftDockOffset"
    )
    val bottomDockOffset by animateDpAsState(
        targetValue = if (shouldCompact) 70.dp else if (isZenMode) 120.dp else 0.dp,
        animationSpec = tween(280),
        label = "bottomDockOffset"
    )
    val chromeAlpha by animateFloatAsState(
        targetValue = if (shouldCompact) 0.25f else if (isZenMode) 0.0f else 1.0f,
        animationSpec = tween(280),
        label = "chromeAlpha"
    )

    // Full Viewport Container
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF222734), Color(0xFF0B0D12)),
                    radius = 1600f
                )
            )
    ) {
        // =========================================================================
        // 1. FULL-SCREEN ARTBOARD STAGE (CENTER HERO)
        // =========================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(currentZoom)
                    .fillMaxWidth(0.92f)
                    .aspectRatio(16f / 9f)
                    .shadow(elevation = 28.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(20.dp))
                    .background(
                        if (sceneBgVisible) {
                            Brush.linearGradient(
                                listOf(Color(0xFFBAD5D3), Color(0xFFD3DFD3), Color(0xFFE7D8C1))
                            )
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF181C26), Color(0xFF12151E)))
                        }
                    )
            ) {
                // Optional Illustrated Backdrop
                if (sceneBgVisible) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Soft Sun
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .offset(x = 520.dp, y = 50.dp)
                                .clip(CircleShape)
                                .background(Color(0xE6FFF4CB))
                        )
                        // Distant Hill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1.3f)
                                .height(160.dp)
                                .offset(x = 100.dp, y = 260.dp)
                                .clip(RoundedCornerShape(topStart = 200.dp, topEnd = 200.dp))
                                .background(Color(0xFF86AD9E))
                        )
                        // Foreground Meadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(1.4f)
                                .height(180.dp)
                                .offset(x = (-60).dp, y = 290.dp)
                                .clip(RoundedCornerShape(topStart = 220.dp, topEnd = 220.dp))
                                .background(Color(0xFF5F8D87))
                        )
                        // Safe Action Boundary Guide
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        )
                    }
                }

                // High-Performance Stylus Canvas (with 1:1 physical pixel tracking fix)
                StudioCanvas(
                    project = project,
                    currentFrame = currentFrame,
                    currentTool = currentTool,
                    currentStroke = currentStroke,
                    onionSkinConfig = onionSkinConfig,
                    selectedBoneId = null,
                    isIkMode = false,
                    onStartStroke = { x, y, p ->
                        isDrawingActive = true
                        isPagesPopoverOpen = false
                        isOnionPopoverOpen = false
                        isLayersPopoverOpen = false
                        viewModel.startStroke(x, y, p)
                    },
                    onUpdateStroke = { x, y, p ->
                        viewModel.updateStroke(x, y, p)
                    },
                    onFinishStroke = {
                        isDrawingActive = false
                        viewModel.finishStroke()
                    },
                    onSelectBone = {},
                    onSolveIk = { _, _ -> },
                    modifier = Modifier.fillMaxSize()
                )

                // Corner HUD Badge inside Canvas
                AnimatedVisibility(
                    visible = !shouldCompact && !isZenMode,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0x73000000))
                            .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(999.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AetherColors.Accent)
                            )
                            val currentLayerName = project.layers.find { it.id == activeLayerId }?.name ?: "Ink Lineart"
                            Text(
                                text = "Page ${currentPageIndex + 1} · Layer: $currentLayerName · ${(brushOpacity * 100).toInt()}%",
                                color = Color(0xCCFFFFFF),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 2. TOP FLOATING PILL ISLANDS
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = topBarOffset)
                .alpha(chromeAlpha)
        ) {
            AetherTopPills(
                projectTitle = project.name,
                onTitleChange = { /* editable title */ },
                currentPage = currentPageIndex + 1,
                totalPages = storyboardPages.size,
                onTogglePagesPopover = {
                    isPagesPopoverOpen = !isPagesPopoverOpen
                    isOnionPopoverOpen = false
                    isLayersPopoverOpen = false
                },
                activeMode = activeMode,
                onSelectMode = { mode ->
                    activeMode = mode
                    isPagesPopoverOpen = false
                    isOnionPopoverOpen = false
                    isLayersPopoverOpen = false
                },
                canUndo = true,
                canRedo = true,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                isOnionActive = onionSkinConfig.enabled,
                onToggleOnionPopover = {
                    isOnionPopoverOpen = !isOnionPopoverOpen
                    isPagesPopoverOpen = false
                    isLayersPopoverOpen = false
                },
                layerCount = project.layers.size,
                onToggleLayersPopover = {
                    isLayersPopoverOpen = !isLayersPopoverOpen
                    isPagesPopoverOpen = false
                    isOnionPopoverOpen = false
                },
                currentZoom = currentZoom,
                onSelectZoom = { currentZoom = it },
                onToggleZenMode = { isZenMode = true },
                onExportMp4 = {
                    viewModel.exportMp4 { file -> }
                }
            )
        }

        // =========================================================================
        // 3. LEFT FLOATING MICRO STYLUS DOCK
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .offset(x = leftDockOffset)
                .alpha(chromeAlpha)
        ) {
            AetherStylusDock(
                currentTool = currentTool,
                onSelectTool = { viewModel.setTool(it) },
                brushColor = brushColor,
                onSelectColor = { viewModel.selectColor(it) },
                onOpenColorPicker = { viewModel.toggleColorPicker() },
                brushSize = brushSize,
                onSetBrushSize = { viewModel.setBrushSize(it) },
                onClearLayer = { viewModel.deleteCurrentFrame() /* clears current */ }
            )
        }

        // =========================================================================
        // 4. BOTTOM TIMELINE DOCK
        // =========================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = bottomDockOffset)
                .alpha(chromeAlpha)
        ) {
            AetherTimelineDock(
                currentFrame = currentFrame,
                totalFrames = project.totalFrames,
                isPlaying = isPlaying,
                fps = project.fps,
                onSelectFrame = { viewModel.setCurrentFrame(it) },
                onTogglePlay = { viewModel.togglePlayback() },
                onPrevFrame = { viewModel.setCurrentFrame((currentFrame - 1).coerceAtLeast(0)) },
                onNextFrame = { viewModel.setCurrentFrame((currentFrame + 1).coerceAtMost(project.totalFrames - 1)) },
                onAddFrame = { viewModel.addFrame() },
                onDuplicateFrame = { viewModel.duplicateCurrentFrame() },
                onSelectFps = { /* updates project FPS */ }
            )
        }

        // =========================================================================
        // 5. CONTEXTUAL FLOATING POPOVERS (Pages, Onion, Layers)
        // =========================================================================
        if (isPagesPopoverOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 64.dp, start = 16.dp)
            ) {
                StoryboardPagesPopover(
                    pages = storyboardPages,
                    currentPageIndex = currentPageIndex,
                    onSelectPage = {
                        currentPageIndex = it
                        isPagesPopoverOpen = false
                    },
                    onAddPage = {
                        // adds sequence
                        isPagesPopoverOpen = false
                    }
                )
            }
        }

        if (isOnionPopoverOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 120.dp)
            ) {
                OnionSkinPopover(
                    isEnabled = onionSkinConfig.enabled,
                    onToggleEnabled = { viewModel.toggleOnionSkin() },
                    opacity = onionSkinConfig.opacity,
                    onOpacityChange = {
                        viewModel.setOnionSkinConfig(onionSkinConfig.copy(opacity = it))
                    }
                )
            }
        }

        if (isLayersPopoverOpen) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
            ) {
                AetherLayersPopover(
                    layers = project.layers,
                    activeLayerId = activeLayerId,
                    onSelectLayer = { viewModel.setActiveLayer(it) },
                    onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onSetOpacity = { id, op -> viewModel.setLayerOpacity(id, op) },
                    onSetBlendMode = { id, bm -> viewModel.setLayerBlendMode(id, bm) },
                    sceneBgVisible = sceneBgVisible,
                    onToggleSceneBg = { sceneBgVisible = !sceneBgVisible }
                )
            }
        }

        if (isColorPickerOpen) {
            ColorPickerPopover(
                currentColor = brushColor,
                activeTab = colorPickerTab,
                recentColors = recentColors,
                customPalette = customPalette,
                onSelectTab = { viewModel.setColorPickerTab(it) },
                onColorSelected = { viewModel.selectColor(it) },
                onAddToPalette = { viewModel.addColorToCustomPalette(it) },
                onClose = { viewModel.toggleColorPicker() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 16.dp)
            )
        }

        // =========================================================================
        // 6. STUDIO OVERLAYS (Nodes, Velocity, Color Grade, 3D Multiplane)
        // =========================================================================
        when (activeMode) {
            AetherWorkspaceMode.DRAW -> { /* Main Drawing Stage is Active */ }
            AetherWorkspaceMode.NODES -> {
                AetherNodesOverlay(
                    graph = nodeGraph,
                    onClose = { activeMode = AetherWorkspaceMode.DRAW },
                    onAddNode = { viewModel.addGraphNode(it) },
                    onUpdateNodePosition = { id, x, y -> viewModel.updateNodePosition(id, x, y) }
                )
            }
            AetherWorkspaceMode.VELOCITY -> {
                AetherVelocityOverlay(
                    onClose = { activeMode = AetherWorkspaceMode.DRAW }
                )
            }
            AetherWorkspaceMode.GRADE -> {
                AetherGradeOverlay(
                    state = colorGradingState,
                    onStateChange = { viewModel.updateColorGradingState(it) },
                    onClose = { activeMode = AetherWorkspaceMode.DRAW }
                )
            }
            AetherWorkspaceMode.STAGE3D -> {
                AetherStage3DOverlay(
                    onClose = { activeMode = AetherWorkspaceMode.DRAW }
                )
            }
        }

        // =========================================================================
        // 7. PURE ZEN MODE RESTORE BUTTON
        // =========================================================================
        if (isZenMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                GlassPill(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.clickable { isZenMode = false }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Show UI",
                            tint = AetherColors.Accent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Show UI",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 8. REAL-TIME EXPORT PROGRESS & GALLERY COMPLETION CARD
        // =========================================================================
        if (localExportState.isExporting || localExportState.exportedFile != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                GlassPill(shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (localExportState.isExporting) {
                            CircularProgressIndicator(
                                progress = { localExportState.progress },
                                color = AetherColors.Amber,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Encoding MP4...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Frame ${localExportState.currentFrame}/${localExportState.totalFrames} (${(localExportState.progress * 100).toInt()}%)",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else if (localExportState.exportedFile != null) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Saved",
                                tint = AetherColors.Mint,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Exported to Gallery!",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Movies/FrameForge/${localExportState.exportedFile?.name}",
                                    color = Color(0xFFA1A1AA),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Play Button
                            IconButton(
                                onClick = {
                                    val uri = localExportState.galleryUri
                                    if (uri != null) {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "video/mp4")
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Play Exported Video"))
                                    }
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AetherColors.Accent)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = AetherColors.Ink950,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    val uri = localExportState.galleryUri
                                    if (uri != null) {
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share MP4 Video"))
                                    }
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x33FFFFFF))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
