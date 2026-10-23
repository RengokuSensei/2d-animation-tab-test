package com.example.frameforge.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.StudioWorkspace
import com.example.frameforge.model.ToolType
import com.example.frameforge.ui.popovers.BrushStudioPopover
import com.example.frameforge.ui.popovers.ColorPickerPopover
import com.example.frameforge.ui.popovers.GalleryDialog
import com.example.frameforge.ui.popovers.LayersPopover
import com.example.frameforge.ui.popovers.PdfImportDialog
import com.example.frameforge.ui.workspace.ColorWorkspace
import com.example.frameforge.ui.workspace.DeliverWorkspace
import com.example.frameforge.ui.workspace.DrawWorkspace
import com.example.frameforge.ui.workspace.EditWorkspace
import com.example.frameforge.ui.workspace.NodesWorkspace
import com.example.frameforge.ui.workspace.WorkspaceTopBar
import com.example.frameforge.viewmodel.StudioViewModel

@Composable
fun StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val currentFrame by viewModel.currentFrame.collectAsState()
    val activeLayerId by viewModel.activeLayerId.collectAsState()
    val currentTool by viewModel.currentTool.collectAsState()
    val brushColor by viewModel.brushColor.collectAsState()
    val brushSize by viewModel.brushSize.collectAsState()
    val brushOpacity by viewModel.brushOpacity.collectAsState()
    val brushSmoothing by viewModel.brushSmoothing.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val onionSkinConfig by viewModel.onionSkinConfig.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val palmRejectionMode by viewModel.palmRejectionMode.collectAsState()
    val recentColors by viewModel.recentColors.collectAsState()
    val customPalette by viewModel.customPalette.collectAsState()
    val nodeGraph by viewModel.nodeGraph.collectAsState()
    val currentWorkspace by viewModel.currentWorkspace.collectAsState()

    // Overlay dialog states
    val isBrushStudioOpen by viewModel.isBrushStudioOpen.collectAsState()
    val activeBrushCategory by viewModel.activeBrushCategory.collectAsState()
    val activeBrush by viewModel.activeBrush.collectAsState()
    val isLayersPopoverOpen by viewModel.isLayersPopoverOpen.collectAsState()
    val isColorPickerOpen by viewModel.isColorPickerOpen.collectAsState()
    val colorPickerTab by viewModel.colorPickerTab.collectAsState()
    val isGalleryDialogOpen by viewModel.isGalleryDialogOpen.collectAsState()
    val isPdfImportDialogOpen by viewModel.isPdfImportDialogOpen.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    val pdfDocumentTitle by viewModel.pdfDocumentTitle.collectAsState()

    // Cinema & VFX states
    val timelineClips by viewModel.timelineClips.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val isRippleEditActive by viewModel.isRippleEditActive.collectAsState()
    val isMagneticSnappingActive by viewModel.isMagneticSnappingActive.collectAsState()
    val colorGradingState by viewModel.colorGradingState.collectAsState()
    val renderFarmNode by viewModel.renderFarmNode.collectAsState()
    val renderFarmProgress by viewModel.renderFarmProgress.collectAsState()

    // --- REAL NATIVE ANDROID STORAGE ACCESS FRAMEWORK (SAF) PICKERS ---
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.openPdfImportDialogWithUri(uri)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importVideoClip(uri)
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importAudioTrack(uri)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importReferenceImage(uri)
        }
    }

    val hasModalOpen = isGalleryDialogOpen || isLayersPopoverOpen || isColorPickerOpen ||
        isBrushStudioOpen || isPdfImportDialogOpen

    BackHandler(enabled = hasModalOpen) {
        when {
            isPdfImportDialogOpen -> viewModel.closePdfImportDialog()
            isGalleryDialogOpen -> viewModel.toggleGalleryDialog()
            isColorPickerOpen -> viewModel.toggleColorPicker()
            isLayersPopoverOpen -> viewModel.toggleLayersPopover()
            isBrushStudioOpen -> viewModel.toggleBrushStudio()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFF0F1115)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- 1. SLEEK DAVINCI RESOLVE WORKSPACE TOP BAR ---
            WorkspaceTopBar(
                projectName = project.name,
                currentWorkspace = currentWorkspace,
                onSelectWorkspace = { viewModel.setWorkspace(it) },
                onGalleryClick = { viewModel.toggleGalleryDialog() },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                brushColor = brushColor,
                palmRejectionMode = palmRejectionMode,
                onTogglePalmRejectionMode = {
                    val nextMode = when (palmRejectionMode) {
                        PalmRejectionMode.AUTOMATIC -> PalmRejectionMode.STYLUS_ONLY
                        PalmRejectionMode.STYLUS_ONLY -> PalmRejectionMode.FINGER_AND_STYLUS
                        PalmRejectionMode.FINGER_AND_STYLUS -> PalmRejectionMode.AUTOMATIC
                        else -> PalmRejectionMode.AUTOMATIC
                    }
                    viewModel.setPalmRejectionMode(nextMode)
                },
                onLayersClick = { viewModel.toggleLayersPopover() },
                onColorPickerClick = { viewModel.toggleColorPicker() }
            )

            // --- 2. DEDICATED WORKSPACE CONTAINER ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentWorkspace) {
                    StudioWorkspace.DRAW -> {
                        DrawWorkspace(
                            project = project,
                            currentFrame = currentFrame,
                            currentTool = currentTool,
                            brushColor = brushColor,
                            brushSize = brushSize,
                            brushOpacity = brushOpacity,
                            brushSmoothing = brushSmoothing,
                            isPlaying = isPlaying,
                            onionSkinConfig = onionSkinConfig,
                            onSetTool = { viewModel.setTool(it) },
                            onSetBrushSize = { viewModel.setBrushSize(it) },
                            onSetBrushOpacity = { viewModel.setBrushOpacity(it) },
                            onSelectFrame = { viewModel.selectFrame(it) },
                            onAddFrame = { viewModel.addFrame() },
                            onDuplicateFrame = { viewModel.duplicateFrame(currentFrame) },
                            onDeleteFrame = { viewModel.deleteFrame(currentFrame) },
                            onTogglePlay = { viewModel.togglePlay() },
                            onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                            onOpenBrushStudio = { viewModel.toggleBrushStudio() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    StudioWorkspace.EDIT -> {
                        EditWorkspace(
                            project = project,
                            currentFrame = currentFrame,
                            isPlaying = isPlaying,
                            timelineClips = timelineClips,
                            selectedClipId = selectedClipId,
                            isRippleEditActive = isRippleEditActive,
                            isMagneticSnappingActive = isMagneticSnappingActive,
                            onSelectFrame = { viewModel.selectFrame(it) },
                            onTogglePlay = { viewModel.togglePlay() },
                            onSelectClip = { viewModel.selectClip(it) },
                            onSplitClip = { viewModel.splitClipAtPlayhead() },
                            onDeleteClip = { viewModel.deleteSelectedClip() },
                            onToggleRipple = { viewModel.toggleRippleEdit() },
                            onToggleSnap = { viewModel.toggleMagneticSnapping() },
                            onImportVideoClick = { videoPickerLauncher.launch(arrayOf("video/*")) },
                            onImportAudioClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                            onImportPdfClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            onInsertPhotoClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    StudioWorkspace.NODES -> {
                        NodesWorkspace(
                            graph = nodeGraph,
                            onUpdateNodePosition = { id, x, y -> viewModel.updateNodePosition(id, x, y) },
                            onAddNode = { type -> viewModel.addGraphNode(type) },
                            onDeleteNode = { id -> viewModel.deleteGraphNode(id) },
                            onUpdateParameter = { id, k, v -> viewModel.updateNodeParameter(id, k, v) },
                            onAutoArrange = { viewModel.autoArrangeNodeGraph() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    StudioWorkspace.COLOR -> {
                        ColorWorkspace(
                            project = project,
                            currentFrame = currentFrame,
                            state = colorGradingState,
                            onStateChange = { viewModel.updateColorGradingState(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    StudioWorkspace.DELIVER -> {
                        DeliverWorkspace(
                            project = project,
                            renderFarmNode = renderFarmNode,
                            renderFarmProgress = renderFarmProgress,
                            onStartLocalExport = { preset -> viewModel.startMediaCodecExport(preset) },
                            onDispatchToRenderFarm = { viewModel.startRenderFarmJob() },
                            onCancelRender = { viewModel.cancelRenderFarmJob() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // --- 3. MODAL OVERLAYS (Brush Studio, Layers, Color Picker, PDF, Gallery) ---
                if (isBrushStudioOpen) {
                    BrushStudioPopover(
                        activeCategory = activeBrushCategory,
                        activeBrush = activeBrush,
                        onSelectCategory = { viewModel.selectBrushCategory(it) },
                        onSelectBrush = { viewModel.selectBrush(it) },
                        onClose = { viewModel.toggleBrushStudio() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (isLayersPopoverOpen) {
                    LayersPopover(
                        project = project,
                        activeLayerId = activeLayerId,
                        onSelectLayer = { viewModel.setActiveLayer(it) },
                        onAddLayer = { viewModel.addLayer(it) },
                        onDeleteLayer = { viewModel.deleteLayer(it) },
                        onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                        onSetOpacity = { id, op -> viewModel.setLayerOpacity(id, op) },
                        onClose = { viewModel.toggleLayersPopover() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 12.dp)
                    )
                }

                if (isColorPickerOpen) {
                    ColorPickerPopover(
                        currentColor = brushColor,
                        onColorSelected = { viewModel.setBrushColor(it) },
                        recentColors = recentColors,
                        customPalette = customPalette,
                        onClose = { viewModel.toggleColorPicker() },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 12.dp)
                    )
                }

                if (isGalleryDialogOpen) {
                    GalleryDialog(
                        savedProjects = savedProjects,
                        onLoadProject = { viewModel.loadProject(it) },
                        onNewProject = { viewModel.createNewProject(it) },
                        onClose = { viewModel.toggleGalleryDialog() }
                    )
                }

                if (isPdfImportDialogOpen) {
                    PdfImportDialog(
                        pdfFile = pdfFile,
                        documentTitle = pdfDocumentTitle,
                        onDismiss = { viewModel.closePdfImportDialog() },
                        onLoadSamplePdf = { viewModel.loadSamplePdfDocument() },
                        onConfirmImport = { file, config -> viewModel.commitPdfImport(file, config) }
                    )
                }

                // --- 4. FLOATING STATUS TOAST BAR ---
                AnimatedVisibility(
                    visible = statusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xEB0F172A),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3338BDF8)),
                        tonalElevation = 6.dp
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
