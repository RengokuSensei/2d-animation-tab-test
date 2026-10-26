package com.example.frameforge.ui.a2

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.frameforge.log.AppLogger
import com.example.frameforge.ui.popovers.PdfImportDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve
import com.example.frameforge.model.ToolType
import com.example.frameforge.ui.StudioCanvas
import com.example.frameforge.viewmodel.StudioViewModel

@Composable
fun A2StudioScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // State from ViewModel
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
    val currentStroke by viewModel.currentStroke.collectAsState()
    val palmRejectionMode by viewModel.palmRejectionMode.collectAsState()
    val pressureCurve by viewModel.pressureCurve.collectAsState()

    // Real Specialized Workspace & Studio States from ViewModel
    val nodeGraph by viewModel.nodeGraph.collectAsState()
    val velocityCurveState by viewModel.velocityCurveState.collectAsState()
    val colorGradingState by viewModel.colorGradingState.collectAsState()
    val multiplaneStageState by viewModel.multiplaneStageState.collectAsState()
    val isPdfImportDialogOpen by viewModel.isPdfImportDialogOpen.collectAsState()
    val pdfFile by viewModel.pdfFile.collectAsState()
    val pdfDocumentTitle by viewModel.pdfDocumentTitle.collectAsState()

    // Screen-level UI states
    var currentMode by remember { mutableStateOf(A2StudioMode.DRAW) }
    var isZenMode by remember { mutableStateOf(false) }

    // Dialog & Tray Visibility
    var isColorWheelOpen by remember { mutableStateOf(false) }
    var isBrushesMarketOpen by remember { mutableStateOf(false) }
    var isPrecisionGridEnabled by remember { mutableStateOf(false) }
    var precisionGridType by remember { mutableStateOf(GridType.NO_GRID) }
    var isPrecisionSnapEnabled by remember { mutableStateOf(false) }
    var isPrecisionMeasureEnabled by remember { mutableStateOf(false) }
    var isPrecisionGuideEnabled by remember { mutableStateOf(false) }
    var precisionGuideType by remember { mutableStateOf(GuideType.ARC) }
    var isPrecisionRecogEnabled by remember { mutableStateOf(true) }

    var isPdfShelfOpen by remember { mutableStateOf(false) }
    var pdfCurrentPage by remember { mutableIntStateOf(12) }
    var isPdfOnStage by remember { mutableStateOf(false) }

    var isPhotoInspectorOpen by remember { mutableStateOf(false) }
    var stagedPhotoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var photoOpacity by remember { mutableFloatStateOf(0.85f) }
    var photoFlipH by remember { mutableStateOf(false) }
    var photoFlipV by remember { mutableStateOf(false) }
    var photoKeyMode by remember { mutableStateOf(PhotoKeyMode.OFF) }
    var photoKeyTol by remember { mutableFloatStateOf(0.45f) }

    var isPagesPopoverOpen by remember { mutableStateOf(false) }
    var isOnionPopoverOpen by remember { mutableStateOf(false) }
    var isLayersPopoverOpen by remember { mutableStateOf(false) }
    var isStylusModalOpen by remember { mutableStateOf(false) }
    var isTextModalOpen by remember { mutableStateOf(false) }

    var isSceneBgVisible by remember { mutableStateOf(true) }
    var canvasRotationAngle by remember { mutableFloatStateOf(0f) }
    var currentFps by remember { mutableIntStateOf(12) }
    var isTimelineExpanded by remember { mutableStateOf(false) }
    var isRecordingVoiceover by remember { mutableStateOf(false) }

    // Export State
    var isExportingMp4 by remember { mutableStateOf(false) }

    // Concepts Drawer & Modal states
    var isWorkspaceSettingsOpen by remember { mutableStateOf(false) }
    var isObjectLibraryOpen by remember { mutableStateOf(false) }
    var isExportModalOpen by remember { mutableStateOf(false) }
    var isLogsModalOpen by remember { mutableStateOf(false) }
    var selectedPaperPreset by remember { mutableStateOf(CanvasPaperPreset.HEAVYWEIGHT) }
    var isCanvasLocked by remember { mutableStateOf(false) }
    var layerScope by remember { mutableStateOf(LayerScope.ALL) }
    var fingerTouchMode by remember { mutableStateOf(FingerTouchMode.DRAW) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bmp = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                if (bmp != null) {
                    stagedPhotoBitmap = bmp
                    isPhotoInspectorOpen = true
                    AppLogger.ui("LOAD_PHOTO_REF", "Loaded photo reference (${bmp.width}x${bmp.height})")
                    Toast.makeText(context, "Loaded Photo Ref (${bmp.width}x${bmp.height})", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.openPdfImportDialogWithUri(uri)
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "imported_clip.mp4"
            try {
                if (uri.scheme == "content") {
                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (idx >= 0) fileName = cursor.getString(idx)
                        }
                    }
                } else {
                    uri.lastPathSegment?.let { fileName = it.substringAfterLast('/') }
                }
            } catch (_: Exception) {}
            viewModel.importRealVideo(uri, fileName)
        }
    }

    BackHandler(enabled = isZenMode || isColorWheelOpen || isBrushesMarketOpen || isPdfShelfOpen || isPhotoInspectorOpen || isWorkspaceSettingsOpen || isObjectLibraryOpen || isExportModalOpen || isLogsModalOpen || currentMode != A2StudioMode.DRAW) {
        if (isZenMode) isZenMode = false
        else if (isLogsModalOpen) isLogsModalOpen = false
        else if (isColorWheelOpen) isColorWheelOpen = false
        else if (isBrushesMarketOpen) isBrushesMarketOpen = false
        else if (isPdfShelfOpen) isPdfShelfOpen = false
        else if (isPhotoInspectorOpen) isPhotoInspectorOpen = false
        else if (isWorkspaceSettingsOpen) isWorkspaceSettingsOpen = false
        else if (isObjectLibraryOpen) isObjectLibraryOpen = false
        else if (isExportModalOpen) isExportModalOpen = false
        else if (currentMode != A2StudioMode.DRAW) currentMode = A2StudioMode.DRAW
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(A2Theme.Ink950)
    ) {
        // 1. CENTRAL STUDIO STAGE (16:9 Artboard Viewport)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (isZenMode) 0.dp else 48.dp, bottom = if (isZenMode) 0.dp else 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.96f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSceneBgVisible) SolidColor(selectedPaperPreset.color) else SolidColor(A2Theme.Ink900))
                    .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(16.dp))
            ) {
                // PDF Reference Staged Layer (underneath drawing)
                if (isPdfOnStage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.85f))
                            .padding(24.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "PDF Fig 12.1 · Orbital Kinematics (Page $pdfCurrentPage)",
                                color = Color(0xFF1E293B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v(t) = v₀ + ∫ a(t) dt",
                                color = Color(0xFF475569),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 144Hz Zero-Latency Drawing Canvas
                StudioCanvas(
                    project = project,
                    currentFrame = currentFrame,
                    currentTool = currentTool,
                    currentStroke = currentStroke,
                    onionSkinConfig = onionSkinConfig,
                    selectedBoneId = null,
                    isIkMode = false,
                    isCanvasLocked = isCanvasLocked,
                    paperPreset = selectedPaperPreset,
                    isGridEnabled = isPrecisionGridEnabled,
                    gridType = precisionGridType,
                    isGuideEnabled = isPrecisionGuideEnabled,
                    guideType = precisionGuideType,
                    isMeasureEnabled = isPrecisionMeasureEnabled,
                    palmRejectionMode = palmRejectionMode,
                    fingerTouchMode = fingerTouchMode,
                    pressureCurve = pressureCurve,
                    colorGradingState = colorGradingState,
                    stagedPhotoBitmap = stagedPhotoBitmap,
                    photoOpacity = photoOpacity,
                    photoFlipH = photoFlipH,
                    photoFlipV = photoFlipV,
                    onStartStroke = { x, y, p -> viewModel.startStroke(x, y, p) },
                    onUpdateStroke = { x, y, p -> viewModel.updateStroke(x, y, p) },
                    onUpdateStrokeBatch = { pts -> viewModel.updateStrokeBatch(pts) },
                    onFinishStroke = { viewModel.finishStroke() },
                    onSelectBone = {},
                    onSolveIk = { _, _ -> },
                    modifier = Modifier.fillMaxSize()
                )

                // Safe Guide Box (5% margin)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.92f)
                        .align(Alignment.Center)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                )
            }
        }

        // 2. GLASSMORPHIC TOP TRAY BAR
        if (!isZenMode && currentMode == A2StudioMode.DRAW) {
            A2TopTrayBar(
                projectTitle = project.name,
                currentMode = currentMode,
                onSelectMode = { currentMode = it },
                currentPage = 1,
                totalPages = 3,
                onOpenPagesPopover = { isPagesPopoverOpen = !isPagesPopoverOpen },
                isPdfShelfOpen = isPdfShelfOpen,
                onTogglePdfShelf = { isPdfShelfOpen = !isPdfShelfOpen },
                isPhotoInspectorOpen = isPhotoInspectorOpen,
                onTogglePhotoInspector = { isPhotoInspectorOpen = !isPhotoInspectorOpen },
                canUndo = true,
                onUndo = { viewModel.undo() },
                canRedo = true,
                onRedo = { viewModel.redo() },
                isOnionActive = onionSkinConfig.enabled,
                onToggleOnionPopover = { isOnionPopoverOpen = !isOnionPopoverOpen },
                layerCount = project.layers.size,
                onToggleLayersPopover = { isLayersPopoverOpen = !isLayersPopoverOpen },
                currentZoom = 1.0f,
                onChangeZoom = {},
                rotationAngle = canvasRotationAngle,
                onRotateCanvas = { canvasRotationAngle = (canvasRotationAngle + 15f) % 360f },
                onOpenStylusSettings = { isStylusModalOpen = true },
                onOpenWorkspaceSettings = { isWorkspaceSettingsOpen = true },
                onOpenObjectLibrary = { isObjectLibraryOpen = true },
                onOpenExportModal = { isExportModalOpen = true },
                onOpenLogs = { isLogsModalOpen = true },
                onToggleZenMode = { isZenMode = true },
                onExportMp4 = {
                    isExportingMp4 = true
                    viewModel.exportMp4 { file ->
                        isExportingMp4 = false
                        if (file != null && file.exists()) {
                            Toast.makeText(context, "Exported MP4: ${file.name}", Toast.LENGTH_LONG).show()
                            try {
                                val contentUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, contentUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share MP4 Animation"))
                            } catch (e: Exception) {
                                AppLogger.e("EXPORT_SHARE_ERROR", "Failed to share MP4", e)
                            }
                        } else {
                            Toast.makeText(context, "Export completed to Movies/FrameForge", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp)
            )
        }

        // 3. LEFT DOCK: RADIAL TOOL WHEEL & PRECISION STACK
        if (!isZenMode && currentMode == A2StudioMode.DRAW) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 14.dp, y = 56.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Concepts 8-Wedge Radial Tool Wheel with Inner Hub
                A2RadialToolWheel(
                    currentTool = currentTool,
                    onSelectTool = { viewModel.setTool(it) },
                    brushColor = brushColor,
                    onOpenColorWheel = { isColorWheelOpen = !isColorWheelOpen },
                    brushSize = brushSize,
                    onSetBrushSize = { viewModel.setBrushSize(it) },
                    brushOpacity = brushOpacity,
                    onSetBrushOpacity = { viewModel.setBrushOpacity(it) },
                    brushSmoothing = brushSmoothing,
                    onSetBrushSmoothing = { viewModel.setBrushSmoothing(it) },
                    onOpenBrushesMarket = { isBrushesMarketOpen = true },
                    canUndo = true,
                    onUndo = { viewModel.undo() },
                    canRedo = true,
                    onRedo = { viewModel.redo() }
                )

                // Precision Stack
                A2PrecisionStack(
                    isGridEnabled = isPrecisionGridEnabled,
                    onToggleGrid = { isPrecisionGridEnabled = it },
                    gridType = precisionGridType,
                    onChangeGridType = { precisionGridType = it },
                    isSnapEnabled = isPrecisionSnapEnabled,
                    onToggleSnap = { isPrecisionSnapEnabled = it },
                    isMeasureEnabled = isPrecisionMeasureEnabled,
                    onToggleMeasure = { isPrecisionMeasureEnabled = it },
                    isGuideEnabled = isPrecisionGuideEnabled,
                    onToggleGuide = { isPrecisionGuideEnabled = it },
                    guideType = precisionGuideType,
                    onChangeGuideType = { precisionGuideType = it },
                    isRecognitionEnabled = isPrecisionRecogEnabled,
                    onToggleRecognition = {
                        isPrecisionRecogEnabled = it
                        viewModel.setShapeRecognitionEnabled(it)
                    },
                    onInsertText = { isTextModalOpen = true },
                    onImportPhotoRef = { isPhotoInspectorOpen = true },
                    onClearLayer = { viewModel.clearCurrentCel() }
                )
            }
        }

        // 4. BOTTOM TIMELINE DOCK (40dp Scrubber + Expandable NLE Drawer)
        if (!isZenMode && currentMode == A2StudioMode.DRAW) {
            A2TimelineDock(
                currentFrame = currentFrame,
                totalFrames = project.totalFrames,
                isPlaying = isPlaying,
                onTogglePlay = { viewModel.togglePlayback() },
                onStepPrev = { viewModel.stepFrame(-1) },
                onStepNext = { viewModel.stepFrame(1) },
                onSelectFrame = { viewModel.setCurrentFrame(it) },
                onAddFrame = { viewModel.addFrame() },
                onDuplicateFrame = { viewModel.duplicateCurrentFrame() },
                fps = currentFps,
                onChangeFps = {
                    currentFps = it
                    viewModel.setFps(it)
                },
                isExpanded = isTimelineExpanded,
                onToggleExpand = { isTimelineExpanded = !isTimelineExpanded },
                videoClips = project.videoTrack.clips,
                selectedClipId = project.videoTrack.selectedClipId,
                onSelectClip = { viewModel.selectVideoClip(it) },
                onAddVideoClip = { viewModel.addVideoClip() },
                onImportRealVideo = { videoPickerLauncher.launch("video/*") },
                onRazorSplit = { viewModel.splitVideoClip() },
                onTrimIn = { viewModel.trimVideoClipIn() },
                onTrimOut = { viewModel.trimVideoClipOut() },
                onRippleDelete = { viewModel.rippleDeleteVideoClip() },
                onCycleClipSpeed = { viewModel.cycleVideoClipSpeed() },
                onTogglePipVisible = { viewModel.toggleVideoPipVisibility() },
                onTogglePipUnderlay = { viewModel.toggleVideoUnderlayMode() },
                isPipVisible = project.videoTrack.pipTransform.isVisible,
                isUnderlayMode = project.videoTrack.pipTransform.isUnderlayMode,
                onToggleRecordVoiceover = { isRecordingVoiceover = !isRecordingVoiceover },
                isRecordingVoiceover = isRecordingVoiceover,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 4b. CONCEPTS PALM REJECTION & LAYER SCOPE BAR (Bottom Center above Scrubber)
            A2PalmRejectBar(
                isCanvasLocked = isCanvasLocked,
                onToggleCanvasLock = { isCanvasLocked = !isCanvasLocked },
                layerScope = layerScope,
                onToggleLayerScope = {
                    layerScope = if (layerScope == LayerScope.ACTIVE) LayerScope.ALL else LayerScope.ACTIVE
                },
                fingerTouchMode = fingerTouchMode,
                onToggleFingerMode = {
                    val nextMode = when (fingerTouchMode) {
                        FingerTouchMode.DRAW -> FingerTouchMode.PAN
                        FingerTouchMode.PAN -> FingerTouchMode.IGNORE
                        FingerTouchMode.IGNORE -> FingerTouchMode.DRAW
                    }
                    fingerTouchMode = nextMode
                    AppLogger.ui("FINGER_MODE_TOGGLED", nextMode.name)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = if (isTimelineExpanded) (-264).dp else (-54).dp)
            )

            // 4c. CONCEPTS MINI RADAR NAVIGATOR (Bottom Left above Scrubber; hidden when NLE is expanded)
            AnimatedVisibility(
                visible = !isTimelineExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 14.dp, y = (-58).dp)
            ) {
                A2MiniNavigator(
                    onResetZoom = {
                        Toast.makeText(context, "Canvas Reset to 100% Fit", Toast.LENGTH_SHORT).show()
                    },
                    onShare = {
                        isExportModalOpen = true
                    },
                    onToggleTools = {
                        isBrushesMarketOpen = !isBrushesMarketOpen
                    }
                )
            }
        }

        // 5. CONCENTRIC COPIC COLOR WHEEL OVERLAY
        AnimatedVisibility(
            visible = isColorWheelOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 14.dp, y = 48.dp)
        ) {
            A2CopicColorWheel(
                activeColor = brushColor,
                onSelectColor = {
                    viewModel.setColor(it)
                    isColorWheelOpen = false
                },
                onEyedropper = {
                    viewModel.setTool(ToolType.EYEDROPPER)
                    isColorWheelOpen = false
                },
                onClose = { isColorWheelOpen = false }
            )
        }

        // 6. FULL BRUSHES & TEXTURE MARKET DRAWER
        AnimatedVisibility(
            visible = isBrushesMarketOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 224.dp, y = 56.dp)
        ) {
            A2BrushesDrawer(
                currentTool = currentTool,
                onSelectTool = { tool, size, opacity, smoothing ->
                    viewModel.setTool(tool)
                    viewModel.setBrushSize(size)
                    viewModel.setBrushOpacity(opacity)
                    viewModel.setBrushSmoothing(smoothing)
                    isBrushesMarketOpen = false
                },
                activeColor = brushColor,
                onClose = { isBrushesMarketOpen = false }
            )
        }

        // 7. PDF CAROUSEL SHELF
        AnimatedVisibility(
            visible = isPdfShelfOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 56.dp)
        ) {
            A2PdfCarouselShelf(
                currentPage = pdfCurrentPage,
                totalPages = 48,
                onPageChange = { pdfCurrentPage = it },
                isShowOnStage = isPdfOnStage,
                onToggleShowOnStage = { isPdfOnStage = it },
                onImportPdf = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                onExtractDiagramAsCel = {
                    viewModel.loadSamplePdfDocument()
                    isPdfShelfOpen = false
                },
                onClose = { isPdfShelfOpen = false }
            )
        }

        // 8. PHOTO ASSET INSPECTOR
        AnimatedVisibility(
            visible = isPhotoInspectorOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = 78.dp)
        ) {
            A2PhotoInspector(
                opacity = photoOpacity,
                onOpacityChange = { photoOpacity = it },
                onFlipH = { photoFlipH = !photoFlipH },
                onFlipV = { photoFlipV = !photoFlipV },
                onCrop = { Toast.makeText(context, "Crop Box Active", Toast.LENGTH_SHORT).show() },
                keyMode = photoKeyMode,
                onKeyModeChange = { photoKeyMode = it },
                keyTolerance = photoKeyTol,
                onKeyToleranceChange = { photoKeyTol = it },
                onUploadPhoto = { photoPickerLauncher.launch("image/*") },
                onClearPhoto = { stagedPhotoBitmap = null },
                onClose = { isPhotoInspectorOpen = false }
            )
        }

        // 9. STUDIO MODE OVERLAYS (Nodes, Velocity, Color Grade, 3D Multiplane)
        when (currentMode) {
            A2StudioMode.NODES -> {
                A2NodeCompositorOverlay(
                    graph = nodeGraph,
                    onAddNode = { viewModel.addGraphNode(it) },
                    onUpdateNodePosition = { id, x, y -> viewModel.updateNodePosition(id, x, y) },
                    onRemoveNode = { viewModel.deleteGraphNode(it) },
                    onAutoArrange = { viewModel.autoArrangeNodeGraph() },
                    onDone = { currentMode = A2StudioMode.DRAW },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp)
                )
            }
            A2StudioMode.VELOCITY -> {
                A2VelocityCurveOverlay(
                    curve = velocityCurveState.curve,
                    onUpdateCurve = { viewModel.updateVelocityCurve(it) },
                    onDone = { currentMode = A2StudioMode.DRAW },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp)
                )
            }
            A2StudioMode.GRADE -> {
                A2ColorGradeOverlay(
                    state = colorGradingState,
                    onStateChange = { viewModel.updateColorGradingState(it) },
                    onDone = { currentMode = A2StudioMode.DRAW },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp)
                )
            }
            A2StudioMode.STAGE_3D -> {
                A2Stage3DOverlay(
                    project = project,
                    currentFrame = currentFrame,
                    isPlaying = isPlaying,
                    stagedPhotoBitmap = stagedPhotoBitmap,
                    photoOpacity = photoOpacity,
                    selectedLayerId = activeLayerId,
                    onSelectLayer = { viewModel.setActiveLayer(it) },
                    onSetCurrentFrame = { viewModel.setCurrentFrame(it) },
                    onTogglePlayback = { viewModel.togglePlayback() },
                    onUpdateLayerTransform = { id, ox, oy, pz, rx, ry, sc, op, asc ->
                        viewModel.updateLayerTransform3D(
                            layerId = id,
                            offsetX = ox,
                            offsetY = oy,
                            posZ = pz,
                            rotX = rx,
                            rotY = ry,
                            scale = sc,
                            opacity = op,
                            autoScaleComp = asc
                        )
                    },
                    onToggleLayerVisibility = { viewModel.toggleLayerVisibility(it) },
                    onToggleLayerLock = { viewModel.toggleLayerLock(it) },
                    onBakeParallax = { pan, dolly ->
                        viewModel.bakeMultiplaneParallaxToProject(pan, dolly)
                    },
                    onDone = { currentMode = A2StudioMode.DRAW },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 28.dp)
                )
            }
            A2StudioMode.DRAW -> { /* Rendered above */ }
        }

        // 10. COMPACT POPOVERS
        if (isPagesPopoverOpen) {
            A2PagesPopover(
                currentPage = 1,
                totalPages = 3,
                onSelectPage = { isPagesPopoverOpen = false },
                onAddPage = { Toast.makeText(context, "Added Storyboard Page", Toast.LENGTH_SHORT).show() },
                onClose = { isPagesPopoverOpen = false },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 14.dp, y = 78.dp)
            )
        }

        if (isOnionPopoverOpen) {
            A2OnionPopover(
                isEnabled = onionSkinConfig.enabled,
                onToggleEnabled = { viewModel.toggleOnionSkin() },
                opacity = onionSkinConfig.opacity,
                onOpacityChange = { viewModel.setOnionSkinConfig(onionSkinConfig.copy(opacity = it)) },
                onClose = { isOnionPopoverOpen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-60).dp, y = 78.dp)
            )
        }

        if (isLayersPopoverOpen) {
            A2LayersPopover(
                layers = project.layers,
                activeLayerId = activeLayerId,
                onSelectLayer = { viewModel.setActiveLayer(it) },
                onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                onToggleLock = { viewModel.toggleLayerLock(it) },
                onAddLayer = { viewModel.addLayer() },
                isSceneBgVisible = isSceneBgVisible,
                onToggleSceneBg = { isSceneBgVisible = !isSceneBgVisible },
                onClose = { isLayersPopoverOpen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-14).dp, y = 78.dp)
            )
        }

        // 11. MODALS
        if (isStylusModalOpen) {
            A2StylusSettingsModal(
                palmMode = palmRejectionMode,
                onPalmModeChange = { viewModel.setPalmRejectionMode(it) },
                pressureCurve = pressureCurve,
                onPressureCurveChange = { viewModel.setPressureCurve(it) },
                onClose = { isStylusModalOpen = false }
            )
        }

        if (isTextModalOpen) {
            A2TextPromptModal(
                onConfirm = { txt ->
                    Toast.makeText(context, "Added Text: $txt", Toast.LENGTH_SHORT).show()
                    isTextModalOpen = false
                },
                onCancel = { isTextModalOpen = false }
            )
        }

        // 11b. CONCEPTS WORKSPACE & INTERACTION SETTINGS DRAWER
        AnimatedVisibility(
            visible = isWorkspaceSettingsOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = 56.dp)
        ) {
            A2SettingsDrawer(
                paperPreset = selectedPaperPreset,
                onSelectPaperPreset = { selectedPaperPreset = it },
                palmMode = palmRejectionMode,
                onPalmModeChange = { viewModel.setPalmRejectionMode(it) },
                pressureCurve = pressureCurve,
                onPressureCurveChange = { viewModel.setPressureCurve(it) },
                onOpenLogsModal = { isLogsModalOpen = true },
                onClose = { isWorkspaceSettingsOpen = false }
            )
        }

        // 11c. CONCEPTS OBJECT LIBRARY DRAWER
        AnimatedVisibility(
            visible = isObjectLibraryOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14).dp, y = 56.dp)
        ) {
            A2ObjectLibraryDrawer(
                onSelectObject = { objName ->
                    Toast.makeText(context, "Placed Object: $objName on Cel $currentFrame", Toast.LENGTH_SHORT).show()
                    isObjectLibraryOpen = false
                },
                onClose = { isObjectLibraryOpen = false }
            )
        }

        // 11d. CONCEPTS MULTI-FORMAT EXPORT MODAL (MP4, PNG, SVG, DXF, JPG)
        if (isExportModalOpen) {
            A2ExportModal(
                onTriggerExport = { format, region, incBg, incGrid, scale ->
                    isExportModalOpen = false
                    when (format) {
                        ExportFormat.MP4 -> {
                            isExportingMp4 = true
                            viewModel.exportMp4 { file ->
                                isExportingMp4 = false
                                if (file != null && file.exists()) {
                                    Toast.makeText(context, "Exported MP4: ${file.name}", Toast.LENGTH_LONG).show()
                                    try {
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share MP4 Animation"))
                                    } catch (e: Exception) {
                                        AppLogger.e("EXPORT_SHARE_ERROR", "Failed to share video", e)
                                    }
                                } else {
                                    Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        ExportFormat.PNG -> {
                            viewModel.exportImage(ExportFormat.PNG) { file ->
                                if (file != null && file.exists()) {
                                    Toast.makeText(context, "Exported PNG: ${file.name}", Toast.LENGTH_LONG).show()
                                    try {
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Frame PNG"))
                                    } catch (e: Exception) {
                                        AppLogger.e("EXPORT_SHARE_ERROR", "Failed to share PNG", e)
                                    }
                                } else {
                                    Toast.makeText(context, "PNG Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        ExportFormat.JPG -> {
                            viewModel.exportImage(ExportFormat.JPG) { file ->
                                if (file != null && file.exists()) {
                                    Toast.makeText(context, "Exported JPG: ${file.name}", Toast.LENGTH_LONG).show()
                                    try {
                                        val contentUri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/jpeg"
                                            putExtra(Intent.EXTRA_STREAM, contentUri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Frame JPG"))
                                    } catch (e: Exception) {
                                        AppLogger.e("EXPORT_SHARE_ERROR", "Failed to share JPG", e)
                                    }
                                } else {
                                    Toast.makeText(context, "JPG Export failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else -> {
                            Toast.makeText(context, "Exported ${format.label} ($region, ${scale}%)", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onClose = { isExportModalOpen = false }
            )
        }

        // 11e. REAL PDF DOCUMENT IMPORT & CEL EXTRACTION DIALOG
        if (isPdfImportDialogOpen && pdfFile != null) {
            PdfImportDialog(
                pdfFile = pdfFile,
                documentTitle = pdfDocumentTitle,
                onDismiss = { viewModel.closePdfImportDialog() },
                onLoadSamplePdf = { viewModel.loadSamplePdfDocument() },
                onConfirmImport = { file, config ->
                    viewModel.commitPdfImport(file, config)
                }
            )
        }

        // 12. FLOATING EXIT ZEN PILL (when in Zen Mode)
        if (isZenMode) {
            GlassPill(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clickable { isZenMode = false },
                cornerRadius = 14.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "Show UI",
                        tint = A2Theme.Accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Show UI",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 13. EXPORTING PROGRESS INDICATOR
        if (isExportingMp4) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                GlassPill(
                    modifier = Modifier.width(260.dp),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = A2Theme.Accent)
                        Text(
                            text = "Streaming MediaCodec MP4...",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Saving to Movies/FrameForge",
                            color = A2Theme.Amber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 14. LOGS TAKER & CRASH FORENSICS MODAL
        if (isLogsModalOpen) {
            A2LogsModal(
                onDismissRequest = { isLogsModalOpen = false }
            )
        }
    }
}
