package com.example.frameforge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.frameforge.model.ToolType
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.ui.popovers.ActionsPopover
import com.example.frameforge.ui.popovers.AdjustmentsPopover
import com.example.frameforge.ui.popovers.BrushStudioPopover
import com.example.frameforge.ui.popovers.ColorPickerPopover
import com.example.frameforge.ui.popovers.GalleryDialog
import com.example.frameforge.ui.popovers.LayersPopover
import com.example.frameforge.ui.popovers.SelectionDock
import com.example.frameforge.ui.popovers.TransformDock
import com.example.frameforge.ui.popovers.CinematicVfxDialog
import com.example.frameforge.ui.popovers.ColorGradingDialog
import com.example.frameforge.ui.popovers.ChromaKeyDialog
import com.example.frameforge.ui.popovers.RotoscopingStudioDialog
import com.example.frameforge.ui.popovers.CurveEditorDopeSheetDialog
import com.example.frameforge.ui.popovers.RenderFarmDialog
import com.example.frameforge.ui.popovers.VelocityGraphDialog
import com.example.frameforge.ui.popovers.MultiplaneStageDialog
import com.example.frameforge.ui.popovers.SceneTransitionsDialog
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
    val playbackMode by viewModel.playbackMode.collectAsState()
    val onionSkinConfig by viewModel.onionSkinConfig.collectAsState()
    val selectedBoneId by viewModel.selectedBoneId.collectAsState()
    val isIkMode by viewModel.isIkMode.collectAsState()
    val currentStroke by viewModel.currentStroke.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val palmRejectionMode by viewModel.palmRejectionMode.collectAsState()
    val pressureCurve by viewModel.pressureCurve.collectAsState()
    val recentColors by viewModel.recentColors.collectAsState()
    val customPalette by viewModel.customPalette.collectAsState()
    val nodeGraph by viewModel.nodeGraph.collectAsState()
    val isNodeGraphViewActive by viewModel.isNodeGraphViewActive.collectAsState()

    // Procreate Overhaul Popover and HUD States
    val isActionsPopoverOpen by viewModel.isActionsPopoverOpen.collectAsState()
    val actionsTab by viewModel.actionsTab.collectAsState()
    val isAdjustmentsPopoverOpen by viewModel.isAdjustmentsPopoverOpen.collectAsState()
    val adjustmentState by viewModel.adjustmentState.collectAsState()
    val isBrushStudioOpen by viewModel.isBrushStudioOpen.collectAsState()
    val activeBrushCategory by viewModel.activeBrushCategory.collectAsState()
    val activeBrush by viewModel.activeBrush.collectAsState()
    val isLayersPopoverOpen by viewModel.isLayersPopoverOpen.collectAsState()
    val isColorPickerOpen by viewModel.isColorPickerOpen.collectAsState()
    val colorPickerTab by viewModel.colorPickerTab.collectAsState()
    val isGalleryDialogOpen by viewModel.isGalleryDialogOpen.collectAsState()
    val isReferenceCompanionOpen by viewModel.isReferenceCompanionOpen.collectAsState()
    val isRightHandedInterface by viewModel.isRightHandedInterface.collectAsState()
    val isAnimationAssistVisible by viewModel.isAnimationAssistVisible.collectAsState()
    val isDreamsMultiTrackVisible by viewModel.isDreamsMultiTrackVisible.collectAsState()
    val selectionState by viewModel.selectionState.collectAsState()
    val transformState by viewModel.transformState.collectAsState()
    val isPdfImportDialogOpen by viewModel.isPdfImportDialogOpen.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    val pdfDocumentTitle by viewModel.pdfDocumentTitle.collectAsState()
    val isExcalidrawModalOpen by viewModel.isExcalidrawModalOpen.collectAsState()
    val isCinematicVfxDialogOpen by viewModel.isCinematicVfxDialogOpen.collectAsState()
    val cinematicVfxConfig by viewModel.cinematicVfxConfig.collectAsState()
    val isVelocityGraphDialogOpen by viewModel.isVelocityGraphDialogOpen.collectAsState()
    val velocityCurveState by viewModel.velocityCurveState.collectAsState()
    val isMultiplaneStageDialogOpen by viewModel.isMultiplaneStageDialogOpen.collectAsState()
    val multiplaneStageState by viewModel.multiplaneStageState.collectAsState()
    val isSceneTransitionsDialogOpen by viewModel.isSceneTransitionsDialogOpen.collectAsState()
    val psychovisualTransitionConfig by viewModel.psychovisualTransitionConfig.collectAsState()
    val shiftAndTraceState by viewModel.shiftAndTraceState.collectAsState()
    val timelineClips by viewModel.timelineClips.collectAsState()
    val selectedClipId by viewModel.selectedClipId.collectAsState()
    val isRippleEditActive by viewModel.isRippleEditActive.collectAsState()
    val isMagneticSnappingActive by viewModel.isMagneticSnappingActive.collectAsState()
    val autoAdvanceState by viewModel.autoAdvanceState.collectAsState()
    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsState()
    val isCanvasLocked by viewModel.isCanvasLocked.collectAsState()

    val isColorGradingDialogOpen by viewModel.isColorGradingDialogOpen.collectAsState()
    val colorGradingState by viewModel.colorGradingState.collectAsState()

    val isChromaKeyDialogOpen by viewModel.isChromaKeyDialogOpen.collectAsState()
    val chromaKeyState by viewModel.chromaKeyState.collectAsState()

    val isRotoscopingDialogOpen by viewModel.isRotoscopingDialogOpen.collectAsState()
    val rotoscopingState by viewModel.rotoscopingState.collectAsState()

    val motionTrackingState by viewModel.motionTrackingState.collectAsState()

    val isCurveEditorDialogOpen by viewModel.isCurveEditorDialogOpen.collectAsState()
    val curveEditorState by viewModel.curveEditorState.collectAsState()

    val isRenderFarmDialogOpen by viewModel.isRenderFarmDialogOpen.collectAsState()
    val renderFarmNode by viewModel.renderFarmNode.collectAsState()
    val renderFarmProgress by viewModel.renderFarmProgress.collectAsState()

    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.openPdfImportDialogWithUri(uri)
        }
    }

    val hasModalOpen = isGalleryDialogOpen || isActionsPopoverOpen || isAdjustmentsPopoverOpen ||
        isBrushStudioOpen || isLayersPopoverOpen || isColorPickerOpen || isExportDialogOpen ||
        transformState.isActive || selectionState.isActive || isNodeGraphViewActive || isPdfImportDialogOpen ||
        isExcalidrawModalOpen || isCinematicVfxDialogOpen || isVelocityGraphDialogOpen ||
        isMultiplaneStageDialogOpen || isSceneTransitionsDialogOpen ||
        isColorGradingDialogOpen || isChromaKeyDialogOpen || isRotoscopingDialogOpen ||
        isCurveEditorDialogOpen || isRenderFarmDialogOpen

    BackHandler(enabled = hasModalOpen) {
        when {
            isColorGradingDialogOpen -> viewModel.closeColorGradingDialog()
            isChromaKeyDialogOpen -> viewModel.closeChromaKeyDialog()
            isRotoscopingDialogOpen -> viewModel.closeRotoscopingDialog()
            isCurveEditorDialogOpen -> viewModel.closeCurveEditorDialog()
            isRenderFarmDialogOpen -> viewModel.closeRenderFarmDialog()
            isExportDialogOpen -> viewModel.toggleExportDialog()
            isCinematicVfxDialogOpen -> viewModel.closeCinematicVfxDialog()
            isVelocityGraphDialogOpen -> viewModel.closeVelocityGraphDialog()
            isMultiplaneStageDialogOpen -> viewModel.closeMultiplaneStageDialog()
            isSceneTransitionsDialogOpen -> viewModel.closeSceneTransitionsDialog()
            isExcalidrawModalOpen -> viewModel.closeExcalidrawModal()
            isPdfImportDialogOpen -> viewModel.closePdfImportDialog()
            isGalleryDialogOpen -> viewModel.toggleGalleryDialog()
            isColorPickerOpen -> viewModel.toggleColorPicker()
            isLayersPopoverOpen -> viewModel.toggleLayersPopover()
            isBrushStudioOpen -> viewModel.toggleBrushStudio()
            isAdjustmentsPopoverOpen -> viewModel.toggleAdjustmentsPopover()
            isActionsPopoverOpen -> viewModel.toggleActionsPopover()
            transformState.isActive -> viewModel.toggleTransformMode()
            selectionState.isActive -> viewModel.clearSelection()
            isNodeGraphViewActive -> viewModel.toggleNodeGraphView()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Color(0xFF0F1115)
    ) { innerPadding ->
        // 100% Edge-to-Edge Full-Screen Workspace (no permanent bulky sidebars shrinking the canvas!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- 1. FULLSCREEN CANVAS LAYER ---
            if (isNodeGraphViewActive) {
                NodeCanvas(
                    graph = nodeGraph,
                    onUpdateNodePosition = { id, x, y -> viewModel.updateNodePosition(id, x, y) },
                    onAddNode = { type -> viewModel.addGraphNode(type) },
                    onDeleteNode = { id -> viewModel.deleteGraphNode(id) },
                    onUpdateParameter = { id, k, v -> viewModel.updateNodeParameter(id, k, v) },
                    onAutoArrange = { viewModel.autoArrangeNodeGraph() }
                )
            } else {
                StudioCanvas(
                    project = project,
                    currentFrame = currentFrame,
                    currentTool = currentTool,
                    currentStroke = currentStroke,
                    onionSkinConfig = onionSkinConfig,
                    selectedBoneId = selectedBoneId,
                    isIkMode = isIkMode,
                    isCanvasLocked = isCanvasLocked,
                    palmRejectionMode = palmRejectionMode,
                    pressureCurve = pressureCurve,
                    adjustmentState = adjustmentState,
                    selectionState = selectionState,
                    transformState = transformState,
                    cinematicVfxConfig = cinematicVfxConfig,
                    shiftAndTraceState = shiftAndTraceState,
                    autoAdvanceState = autoAdvanceState,
                    rotoscopingState = rotoscopingState,
                    motionTrackingState = motionTrackingState,
                    colorGradingState = colorGradingState,
                    isReferenceCompanionOpen = isReferenceCompanionOpen,
                    onStartStroke = { x, y, p -> viewModel.startStroke(x, y, p) },
                    onUpdateStroke = { x, y, p -> viewModel.updateStroke(x, y, p) },
                    onUpdateStrokeBatch = { pts -> viewModel.updateStrokeBatch(pts) },
                    onFinishStroke = { viewModel.finishStroke() },
                    onSelectBone = { viewModel.setSelectedBone(it) },
                    onSolveIk = { x, y -> viewModel.solveIkForLimb(x, y) },
                    onUpdateAdjustmentIntensity = { viewModel.updateAdjustmentIntensity(it) },
                    onApplyAdjustment = { viewModel.applyAdjustment() },
                    onCancelAdjustment = { viewModel.cancelAdjustment() },
                    onCloseReferenceCompanion = { viewModel.toggleReferenceCompanion() },
                    onResetPegs = { viewModel.resetShiftAndTracePegs() },
                    onUpdateLightGizmoPosition = { x, y -> viewModel.updateLightGizmoPosition(x, y) },
                    onUpdateShiftAndTrace = { dx, dy, dr, ds -> viewModel.updateShiftAndTrace(dx, dy, dr, ds) },
                    onToggleShiftAndTraceSpace = { viewModel.toggleShiftAndTraceSpace() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- 2. FLOATING PROCREATE TOP HUD BAR ---
            ProcreateTopBar(
                projectName = project.name,
                currentTool = currentTool,
                brushColor = brushColor,
                isActionsOpen = isActionsPopoverOpen,
                isAdjustmentsOpen = isAdjustmentsPopoverOpen,
                isSelectionActive = selectionState.isActive,
                isTransformActive = transformState.isActive,
                isBrushStudioOpen = isBrushStudioOpen,
                isLayersOpen = isLayersPopoverOpen,
                isColorPickerOpen = isColorPickerOpen,
                isAnimationAssistVisible = isAnimationAssistVisible,
                isDreamsTracksVisible = isDreamsMultiTrackVisible,
                isNodeGraphActive = isNodeGraphViewActive,
                isExcalidrawOpen = isExcalidrawModalOpen,
                isExportOpen = isExportDialogOpen,
                isCanvasLocked = isCanvasLocked,
                palmRejectionMode = palmRejectionMode,
                onToggleCanvasLock = { viewModel.toggleCanvasLock() },
                onTogglePalmRejectionMode = {
                    val nextMode = when (palmRejectionMode) {
                        PalmRejectionMode.AUTOMATIC -> PalmRejectionMode.STYLUS_ONLY
                        PalmRejectionMode.STYLUS_ONLY -> PalmRejectionMode.FINGER_AND_STYLUS
                        PalmRejectionMode.FINGER_AND_STYLUS -> PalmRejectionMode.AUTOMATIC
                        else -> PalmRejectionMode.AUTOMATIC
                    }
                    viewModel.setPalmRejectionMode(nextMode)
                },
                onExportClick = { viewModel.toggleExportDialog() },
                isAutoAdvanceEnabled = autoAdvanceState.isEnabled,
                autoAdvanceState = autoAdvanceState,
                isVfxOpen = isCinematicVfxDialogOpen,
                onToggleAutoAdvance = { viewModel.toggleAutoAdvanceActive() },
                onSetAutoAdvanceMode = { viewModel.setAutoAdvanceMode(it) },
                onSetAutoAdvanceDebounce = { viewModel.setAutoAdvanceDebounce(it) },
                onSetAutoAdvanceFps = { viewModel.autoAdvanceController.setLiveLoopFps(it) },
                onOpenVfx = { viewModel.toggleCinematicVfxDialog() },
                onGalleryClick = { viewModel.toggleGalleryDialog() },
                onActionsClick = { viewModel.toggleActionsPopover() },
                onAdjustmentsClick = { viewModel.toggleAdjustmentsPopover() },
                onSelectionClick = { viewModel.setSelectionMode(if (selectionState.isActive) com.example.frameforge.model.SelectionMode.NONE else com.example.frameforge.model.SelectionMode.FREEHAND) },
                onTransformClick = { viewModel.toggleTransformMode() },
                onExcalidrawClick = { viewModel.toggleExcalidrawModal() },
                onToggleAnimationAssist = { viewModel.toggleAnimationAssist() },
                onToggleDreamsTracks = { viewModel.toggleDreamsMultiTrack() },
                onToggleNodeGraph = { viewModel.toggleNodeGraphView() },
                onPaintClick = {
                    if (currentTool != ToolType.PEN && currentTool != ToolType.PENCIL && currentTool != ToolType.BRUSH) {
                        viewModel.setTool(ToolType.PEN)
                    } else {
                        viewModel.toggleBrushStudio()
                    }
                },
                onSmudgeClick = { viewModel.setTool(ToolType.SMUDGE) },
                onEraserClick = { viewModel.setTool(ToolType.ERASER) },
                onLayersClick = { viewModel.toggleLayersPopover() },
                onColorPickerClick = { viewModel.toggleColorPicker() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )

            // --- 3. FLOATING VERTICAL SLIDER HUD (Brush Size & Opacity) ---
            if (!isNodeGraphViewActive) {
                ProcreateSliderHud(
                    brushSize = brushSize,
                    brushOpacity = brushOpacity,
                    onSetBrushSize = { viewModel.setBrushSize(it) },
                    onSetBrushOpacity = { viewModel.setBrushOpacity(it) },
                    onQuickMenuClick = { viewModel.setTool(ToolType.EYEDROPPER) },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    modifier = Modifier
                        .align(if (isRightHandedInterface) Alignment.CenterEnd else Alignment.CenterStart)
                        .padding(horizontal = 8.dp)
                )
            }

            // --- 4. FLOATING CONTEXTUAL SELECTION DOCK ---
            if (selectionState.isActive && !isNodeGraphViewActive) {
                SelectionDock(
                    selectionState = selectionState,
                    onSetMode = { viewModel.setSelectionMode(it) },
                    onInvert = { viewModel.invertSelection() },
                    onClear = { viewModel.clearSelection() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isAnimationAssistVisible) 110.dp else 24.dp)
                )
            }

            // --- 5. FLOATING CONTEXTUAL TRANSFORM DOCK ---
            if (transformState.isActive && !isNodeGraphViewActive) {
                TransformDock(
                    transformState = transformState,
                    onSetMode = { viewModel.setTransformMode(it) },
                    onFlipH = { viewModel.flipTransformH() },
                    onFlipV = { viewModel.flipTransformV() },
                    onRotate45 = { viewModel.rotateTransform45() },
                    onReset = { viewModel.resetTransform() },
                    onDone = { viewModel.toggleTransformMode() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (isAnimationAssistVisible) 110.dp else 24.dp)
                )
            }

            // --- 6. FLOATING BOTTOM PROCREATE ANIMATION ASSIST & DREAMS TIMELINE ---
            if (isAnimationAssistVisible && !isNodeGraphViewActive) {
                ProcreateTimeline(
                    project = project,
                    currentFrame = currentFrame,
                    isPlaying = isPlaying,
                    playbackMode = playbackMode,
                    onionSkinConfig = onionSkinConfig,
                    activeLayerId = activeLayerId,
                    isDreamsTracksVisible = isDreamsMultiTrackVisible,
                    onSelectFrame = { viewModel.setCurrentFrame(it) },
                    onTogglePlay = { viewModel.togglePlayback() },
                    onStepFrame = { viewModel.stepFrame(it) },
                    onToggleOnionSkin = { viewModel.toggleOnionSkin() },
                    onAddFrame = { viewModel.addFrame() },
                    onDuplicateFrame = { viewModel.duplicateCurrentFrame() },
                    onDeleteFrame = { viewModel.deleteCurrentFrame() },
                    onSetFps = { viewModel.setFps(it) },
                    onToggleRotoscopingVideo = { viewModel.toggleRotoscopingVideo() },
                    onSetRotoscopingOpacity = { viewModel.setRotoscopingOpacity(it) },
                    onToggleAudioTrack = { viewModel.toggleAudioTrack() },
                    onSetAudioVolume = { viewModel.setAudioVolume(it) },
                    onSplitClip = { viewModel.splitClipAtPlayhead() },
                    isRippleEdit = isRippleEditActive,
                    onToggleRippleEdit = { viewModel.toggleRippleEdit() },
                    isSnappingActive = isMagneticSnappingActive,
                    onToggleSnapping = { viewModel.toggleMagneticSnapping() },
                    onOpenVelocityGraph = { viewModel.toggleVelocityGraphDialog() },
                    onOpenTransitions = { viewModel.toggleSceneTransitionsDialog() },
                    onOpenMultiplane = { viewModel.toggleMultiplaneStageDialog() },
                    timelineClips = timelineClips,
                    selectedClipId = selectedClipId,
                    onSelectClip = { viewModel.selectClip(it) },
                    onTrimIn = { id, start -> viewModel.trimClipIn(id, start) },
                    onTrimOut = { id, dur -> viewModel.trimClipOut(id, dur) },
                    isTimeStretch = viewModel.isTimeStretchActive.collectAsState().value,
                    onToggleTimeStretch = { viewModel.toggleTimeStretch() },
                    isShiftAndTraceActive = shiftAndTraceState.isActive,
                    onToggleShiftAndTrace = { viewModel.toggleShiftAndTrace() },
                    onOpenColorGrading = { viewModel.toggleColorGradingDialog() },
                    onOpenChromaKey = { viewModel.toggleChromaKeyDialog() },
                    onOpenRotoscoping = { viewModel.toggleRotoscopingDialog() },
                    onOpenCurveEditor = { viewModel.toggleCurveEditorDialog() },
                    onOpenRenderFarm = { viewModel.toggleRenderFarmDialog() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // --- 7. FLOATING STATUS TOAST ---
            if (statusMessage != null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 56.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE1E293B)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300E5FF))
                ) {
                    Text(
                        text = statusMessage!!,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // --- 8. FLOATING POPOVER DIALOGS ---

            // Actions Popover (Wrench)
            if (isActionsPopoverOpen) {
                ActionsPopover(
                    activeTab = actionsTab,
                    isAnimationAssistVisible = isAnimationAssistVisible,
                    isReferenceWindowOpen = isReferenceCompanionOpen,
                    isRightHanded = isRightHandedInterface,
                    palmRejectionMode = palmRejectionMode,
                    pressureCurve = pressureCurve,
                    onSelectTab = { viewModel.setActionsTab(it) },
                    onClose = { viewModel.toggleActionsPopover() },
                    onSetCanvasResolution = { viewModel.setCanvasResolution(it) },
                    onToggleAnimationAssist = { viewModel.toggleAnimationAssist() },
                    onToggleReferenceWindow = { viewModel.toggleReferenceCompanion() },
                    onToggleRightHanded = { viewModel.toggleRightHandedInterface() },
                    onSetPalmRejection = { viewModel.setPalmRejectionMode(it) },
                    onSetPressureCurve = { viewModel.setPressureCurve(it) },
                    onExportMp4 = { viewModel.exportMp4 {} },
                    onExportGif = { bg -> viewModel.exportGif(bg) {} },
                    onExportPngSequence = { viewModel.exportPngSequence {} },
                    onSaveProject = { viewModel.saveCurrentProject() },
                    onImportVideoRotoscoping = { viewModel.toggleRotoscopingVideo() },
                    onImportAudioTrack = { viewModel.toggleAudioTrack() },
                    onImportPdfDocument = {
                        viewModel.toggleActionsPopover()
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    },
                    onLoadSamplePdf = {
                        viewModel.toggleActionsPopover()
                        viewModel.loadSamplePdfDocument()
                    },
                    onOpenExcalidrawStudio = {
                        viewModel.toggleActionsPopover()
                        viewModel.toggleExcalidrawModal()
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 54.dp, start = 48.dp)
                )
            }

            // Adjustments Popover (Magic Wand)
            if (isAdjustmentsPopoverOpen) {
                AdjustmentsPopover(
                    onSelectAdjustment = { viewModel.startAdjustment(it) },
                    onClose = { viewModel.toggleAdjustmentsPopover() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 54.dp, start = 80.dp)
                )
            }

            // Brush Studio Popover
            if (isBrushStudioOpen) {
                BrushStudioPopover(
                    activeCategory = activeBrushCategory,
                    activeBrush = activeBrush,
                    onSelectCategory = { viewModel.selectBrushCategory(it) },
                    onSelectBrush = { viewModel.selectBrush(it) },
                    onClose = { viewModel.toggleBrushStudio() },
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }

            // Layers Popover
            if (isLayersPopoverOpen) {
                LayersPopover(
                    project = project,
                    activeLayerId = activeLayerId,
                    onSelectLayer = { viewModel.setActiveLayer(it) },
                    onAddLayer = { viewModel.addLayer(it) },
                    onDeleteLayer = { viewModel.deleteLayer(it) },
                    onDuplicateLayer = { viewModel.duplicateLayer(it) },
                    onMergeLayerDown = { viewModel.mergeLayerDown(it) },
                    onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onSetOpacity = { id, op -> viewModel.setLayerOpacity(id, op) },
                    onSetBlendMode = { id, bm -> viewModel.setLayerBlendMode(id, bm) },
                    onToggleOnionSkinPin = { viewModel.toggleLayerOnionSkinPin(it) },
                    onClose = { viewModel.toggleLayersPopover() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 54.dp, end = 48.dp)
                )
            }

            // Color Picker Popover
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
                        .padding(top = 54.dp, end = 12.dp)
                )
            }

            // Fullscreen Gallery / Projects Dialog
            if (isGalleryDialogOpen) {
                GalleryDialog(
                    savedProjects = savedProjects,
                    onLoadProject = { viewModel.loadProject(it) },
                    onNewProject = { viewModel.createNewProject(it) },
                    onClose = { viewModel.toggleGalleryDialog() }
                )
            }

            // Multi-Page PDF Import Studio Dialog
            if (isPdfImportDialogOpen) {
                com.example.frameforge.ui.popovers.PdfImportDialog(
                    pdfFile = pdfFile,
                    documentTitle = pdfDocumentTitle,
                    onDismiss = { viewModel.closePdfImportDialog() },
                    onLoadSamplePdf = { viewModel.loadSamplePdfDocument() },
                    onConfirmImport = { file, config -> viewModel.commitPdfImport(file, config) }
                )
            }

            // Excalidraw Smart Studio & AI Modal
            if (isExcalidrawModalOpen) {
                com.example.frameforge.ui.popovers.ExcalidrawStudioModal(
                    onDismiss = { viewModel.closeExcalidrawModal() },
                    onStampStrokes = { strokes, desc ->
                        viewModel.stampStrokesOntoActiveLayer(strokes, desc)
                    }
                )
            }

            // 1. Cinematic VFX & Shaders Dialog
            if (isCinematicVfxDialogOpen) {
                CinematicVfxDialog(
                    config = cinematicVfxConfig,
                    onUpdateConfig = { viewModel.updateCinematicVfxConfig(it) },
                    onClose = { viewModel.closeCinematicVfxDialog() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 2. Velocity & Cubic Bezier Curve Graph Dialog
            if (isVelocityGraphDialogOpen) {
                VelocityGraphDialog(
                    state = velocityCurveState,
                    onUpdateCurve = { viewModel.updateVelocityCurve(it) },
                    onUpdateMode = { mode -> viewModel.applyVelocityCurvePreset(velocityCurveState.curve, mode.label) },
                    onToggleStepMotion = { step -> viewModel.applyVelocityCurvePreset(if (step) com.example.frameforge.cinema.BezierPresets.STEPPED_STOP_MOTION else com.example.frameforge.cinema.BezierPresets.CINEMA_SNAP, "Step Motion") },
                    onClose = { viewModel.closeVelocityGraphDialog() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 3. 2.5D / 3D Multiplane Stage Dialog
            if (isMultiplaneStageDialogOpen) {
                MultiplaneStageDialog(
                    state = multiplaneStageState,
                    onUpdateCamera = { viewModel.updateMultiplaneCamera(it) },
                    onUpdateLayer = { plane -> viewModel.updateLayer3DDepth(plane.layerId, plane.posZ, plane.autoScaleCompensation) },
                    onToggleOrbit = { viewModel.toggleMultiplaneStageDialog() },
                    onSelectLayer = { viewModel.setActiveLayer(it) },
                    onClose = { viewModel.closeMultiplaneStageDialog() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 4. Psychovisual Scene Transitions Dialog
            if (isSceneTransitionsDialogOpen) {
                SceneTransitionsDialog(
                    config = psychovisualTransitionConfig,
                    onUpdateConfig = { viewModel.applyPsychovisualTransition(it) },
                    onApplyTransition = { viewModel.applyPsychovisualTransition(psychovisualTransitionConfig); viewModel.closeSceneTransitionsDialog() },
                    onClose = { viewModel.closeSceneTransitionsDialog() },
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 5. Dedicated Export Studio Dialog (MP4, GIF, PNG, Sequence)
            if (isExportDialogOpen) {
                com.example.frameforge.ui.popovers.ExportStudioDialog(
                    totalFrames = project.totalFrames,
                    fps = project.fps,
                    currentFrame = currentFrame,
                    onExportMp4 = { cb -> viewModel.exportMp4(cb) },
                    onExportGif = { bg, cb -> viewModel.exportGif(bg, cb) },
                    onExportCurrentFramePng = { cb -> viewModel.exportCurrentFramePng(cb) },
                    onExportPngSequence = { cb -> viewModel.exportPngSequence(cb) },
                    onDismiss = { viewModel.toggleExportDialog() }
                )
            }

            // 6. Pro Color Grading Dialog (DaVinci Resolve / ASC CDL Wheels)
            if (isColorGradingDialogOpen) {
                ColorGradingDialog(
                    state = colorGradingState,
                    onStateChange = { viewModel.updateColorGradingState(it) },
                    onDismiss = { viewModel.closeColorGradingDialog() }
                )
            }

            // 7. Chroma Keying & Despill Dialog
            if (isChromaKeyDialogOpen) {
                ChromaKeyDialog(
                    config = chromaKeyState,
                    onConfigChange = { viewModel.updateChromaKeyState(it) },
                    onDismiss = { viewModel.closeChromaKeyDialog() }
                )
            }

            // 8. Rotoscoping Splines with Per-Vertex Feathering Dialog
            if (isRotoscopingDialogOpen) {
                RotoscopingStudioDialog(
                    state = rotoscopingState,
                    onStateChange = { viewModel.updateRotoscopingState(it) },
                    currentFrame = currentFrame,
                    onDismiss = { viewModel.closeRotoscopingDialog() }
                )
            }

            // 9. Glaxnimate / Blender Graph Curve Editor & Dope Sheet
            if (isCurveEditorDialogOpen) {
                CurveEditorDopeSheetDialog(
                    state = curveEditorState,
                    currentFrame = currentFrame.toFloat(),
                    totalFrames = project.totalFrames,
                    onStateChange = { viewModel.updateCurveEditorState(it) },
                    onDismiss = { viewModel.closeCurveEditorDialog() }
                )
            }

            // 10. Distributed GPU Render Farm Dialog
            if (isRenderFarmDialogOpen) {
                RenderFarmDialog(
                    node = renderFarmNode,
                    progress = renderFarmProgress,
                    onStartRender = { req -> viewModel.dispatchRenderFarmJob(req) },
                    onDismiss = { viewModel.closeRenderFarmDialog() }
                )
            }
        }
    }
}
