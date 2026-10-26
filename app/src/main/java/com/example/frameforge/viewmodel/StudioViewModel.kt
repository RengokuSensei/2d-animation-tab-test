package com.example.frameforge.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.frameforge.ai.AiMotionAssistant
import com.example.frameforge.ai.ColorPaletteSuggestion
import com.example.frameforge.engine.DrawingEngine
import com.example.frameforge.engine.InverseKinematics
import com.example.frameforge.engine.ProceduralMotion
import com.example.frameforge.export.ProjectStorage
import com.example.frameforge.log.AppLogger
import com.example.frameforge.model.A2VideoClip
import com.example.frameforge.model.VideoPipTransform
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.BoneKeyframe
import com.example.frameforge.model.BonePose
import com.example.frameforge.model.DrawingCel
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.EasingCurve
import com.example.frameforge.model.ObjectTransformTrack
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.PlaybackMode
import com.example.frameforge.model.SkeletonRig
import com.example.frameforge.model.StrokePoint
import com.example.frameforge.model.ToolType
import com.example.frameforge.model.TransformKeyframe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.example.frameforge.pdf.PdfDocumentTrack
import com.example.frameforge.pdf.PdfEngine
import com.example.frameforge.pdf.PdfImportConfig
import com.example.frameforge.pdf.PdfImportMode
import java.io.File
import java.io.FileOutputStream
import java.util.Stack
import kotlin.math.max
import kotlin.math.min

import com.example.frameforge.model.ActionsTab
import com.example.frameforge.model.AdjustmentState
import com.example.frameforge.model.AdjustmentType
import com.example.frameforge.model.AnimationNodeGraph
import com.example.frameforge.model.AnimationNodeType
import com.example.frameforge.model.BlendModeType
import com.example.frameforge.model.BrushCategory
import com.example.frameforge.model.BrushDefinition
import com.example.frameforge.model.CanvasResolutionPreset
import com.example.frameforge.model.ColorPickerTab
import com.example.frameforge.model.GraphNode
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve
import com.example.frameforge.model.SelectionMode
import com.example.frameforge.model.SelectionState
import com.example.frameforge.model.TransformMode
import com.example.frameforge.model.TransformState
import com.example.frameforge.cinema.AutoAdvanceFlipbookController
import com.example.frameforge.cinema.AutoAdvanceMode
import com.example.frameforge.cinema.AutoAdvanceState
import com.example.frameforge.cinema.CinematicShaderPipeline
import com.example.frameforge.cinema.CinematicVfxConfig
import com.example.frameforge.cinema.ClipEditMode
import com.example.frameforge.cinema.ClipEditorEngine
import com.example.frameforge.cinema.MultiplaneLayer3D
import com.example.frameforge.cinema.MultiplaneStageState
import com.example.frameforge.cinema.PsychovisualTransitionConfig
import com.example.frameforge.cinema.PsychovisualTransitionType
import com.example.frameforge.cinema.ShiftAndTraceState
import com.example.frameforge.cinema.SnapGuide
import com.example.frameforge.cinema.TimelineClip
import com.example.frameforge.cinema.TimelineClipType
import com.example.frameforge.cinema.VelocityCurveState
import com.example.frameforge.cinema.VirtualCamera3D
import com.example.frameforge.cinema.CubicBezierCurve
import com.example.frameforge.cinema.ColorGradingEngine
import com.example.frameforge.cinema.ColorGradingPreset
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.cinema.ChromaKeyConfig
import com.example.frameforge.cinema.ChromaKeyType
import com.example.frameforge.cinema.RotoscopingState
import com.example.frameforge.cinema.RotoShape
import com.example.frameforge.cinema.MotionTrackingState
import com.example.frameforge.cinema.CurveEditorState
import com.example.frameforge.network.RenderFarmBridge
import com.example.frameforge.network.RenderFarmNode
import com.example.frameforge.network.RenderJobRequest
import com.example.frameforge.network.RenderProgressUpdate
import com.example.frameforge.network.RenderJobState
import androidx.compose.ui.geometry.Offset

enum class SidePanelType {

    LAYERS,
    COLORS,
    SKELETON_RIG,
    AI_ASSISTANT,
    EXPORT,
    PROJECTS,
    SETTINGS
}

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = ProjectStorage(application)

    private val _project = MutableStateFlow(createInitialProject())
    val project: StateFlow<AnimationProject> = _project.asStateFlow()

    private val _currentFrame = MutableStateFlow(0)
    val currentFrame: StateFlow<Int> = _currentFrame.asStateFlow()

    private val _activeLayerId = MutableStateFlow("layer_sketch")
    val activeLayerId: StateFlow<String> = _activeLayerId.asStateFlow()

    private val _currentTool = MutableStateFlow(ToolType.PEN)
    val currentTool: StateFlow<ToolType> = _currentTool.asStateFlow()

    private val _brushColor = MutableStateFlow(Color(0xFF1E293B)) // Deep ink navy
    val brushColor: StateFlow<Color> = _brushColor.asStateFlow()

    private val _brushSize = MutableStateFlow(8f)
    val brushSize: StateFlow<Float> = _brushSize.asStateFlow()

    private val _brushOpacity = MutableStateFlow(1.0f)
    val brushOpacity: StateFlow<Float> = _brushOpacity.asStateFlow()

    private val _brushSmoothing = MutableStateFlow(0.4f)
    val brushSmoothing: StateFlow<Float> = _brushSmoothing.asStateFlow()

    private val _palmRejectionMode = MutableStateFlow(PalmRejectionMode.FINGER_AND_STYLUS)
    val palmRejectionMode: StateFlow<PalmRejectionMode> = _palmRejectionMode.asStateFlow()

    private val _pressureCurve = MutableStateFlow(PressureCurve.LINEAR)
    val pressureCurve: StateFlow<PressureCurve> = _pressureCurve.asStateFlow()

    private val _recentColors = MutableStateFlow<List<Color>>(
        listOf(
            Color(0xFF1E293B), Color(0xFFE2E8F0), Color(0xFFEF4444),
            Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B)
        )
    )
    val recentColors: StateFlow<List<Color>> = _recentColors.asStateFlow()

    private val _customPalette = MutableStateFlow<List<Color>>(
        listOf(
            Color(0xFF000000), Color(0xFFFFFFFF), Color(0xFFFF9800),
            Color(0xFF00E5FF), Color(0xFFE91E63), Color(0xFF4CAF50)
        )
    )
    val customPalette: StateFlow<List<Color>> = _customPalette.asStateFlow()

    private val _currentWorkspace = MutableStateFlow(com.example.frameforge.model.StudioWorkspace.DRAW)
    val currentWorkspace: StateFlow<com.example.frameforge.model.StudioWorkspace> = _currentWorkspace.asStateFlow()

    fun setWorkspace(workspace: com.example.frameforge.model.StudioWorkspace) {
        _currentWorkspace.value = workspace
    }

    private val _nodeGraph = MutableStateFlow(AnimationNodeGraph())
    val nodeGraph: StateFlow<AnimationNodeGraph> = _nodeGraph.asStateFlow()

    private val _isNodeGraphViewActive = MutableStateFlow(false)
    val isNodeGraphViewActive: StateFlow<Boolean> = _isNodeGraphViewActive.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.LOOP)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    private val _onionSkinConfig = MutableStateFlow(OnionSkinConfig())
    val onionSkinConfig: StateFlow<OnionSkinConfig> = _onionSkinConfig.asStateFlow()

    private val _activeSidePanel = MutableStateFlow<SidePanelType?>(null)
    val activeSidePanel: StateFlow<SidePanelType?> = _activeSidePanel.asStateFlow()

    private val _selectedBoneId = MutableStateFlow<String?>("bone_spine")
    val selectedBoneId: StateFlow<String?> = _selectedBoneId.asStateFlow()

    private val _isIkMode = MutableStateFlow(false)
    val isIkMode: StateFlow<Boolean> = _isIkMode.asStateFlow()

    private val _currentStroke = MutableStateFlow<DrawingStroke?>(null)
    val currentStroke: StateFlow<DrawingStroke?> = _currentStroke.asStateFlow()

    // Status message toast
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _savedProjects = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val savedProjects: StateFlow<List<Pair<String, String>>> = _savedProjects.asStateFlow()

    // --- PROCREATE FLOATING HUD & POPOVER STATES ---
    private val _isActionsPopoverOpen = MutableStateFlow(false)
    val isActionsPopoverOpen: StateFlow<Boolean> = _isActionsPopoverOpen.asStateFlow()

    private val _actionsTab = MutableStateFlow(ActionsTab.ADD)
    val actionsTab: StateFlow<ActionsTab> = _actionsTab.asStateFlow()

    private val _isAdjustmentsPopoverOpen = MutableStateFlow(false)
    val isAdjustmentsPopoverOpen: StateFlow<Boolean> = _isAdjustmentsPopoverOpen.asStateFlow()

    private val _adjustmentState = MutableStateFlow(AdjustmentState())
    val adjustmentState: StateFlow<AdjustmentState> = _adjustmentState.asStateFlow()

    private val _isBrushStudioOpen = MutableStateFlow(false)
    val isBrushStudioOpen: StateFlow<Boolean> = _isBrushStudioOpen.asStateFlow()

    private val _activeBrushCategory = MutableStateFlow(BrushCategory.SKETCHING)
    val activeBrushCategory: StateFlow<BrushCategory> = _activeBrushCategory.asStateFlow()

    private val _activeBrush = MutableStateFlow(BrushDefinition.defaultBrushes().first())
    val activeBrush: StateFlow<BrushDefinition> = _activeBrush.asStateFlow()

    private val _isLayersPopoverOpen = MutableStateFlow(false)
    val isLayersPopoverOpen: StateFlow<Boolean> = _isLayersPopoverOpen.asStateFlow()

    private val _isColorPickerOpen = MutableStateFlow(false)
    val isColorPickerOpen: StateFlow<Boolean> = _isColorPickerOpen.asStateFlow()

    private val _colorPickerTab = MutableStateFlow(ColorPickerTab.DISC)
    val colorPickerTab: StateFlow<ColorPickerTab> = _colorPickerTab.asStateFlow()

    private val _isGalleryDialogOpen = MutableStateFlow(false)
    val isGalleryDialogOpen: StateFlow<Boolean> = _isGalleryDialogOpen.asStateFlow()

    private val _isReferenceCompanionOpen = MutableStateFlow(false)
    val isReferenceCompanionOpen: StateFlow<Boolean> = _isReferenceCompanionOpen.asStateFlow()

    private val _isRightHandedInterface = MutableStateFlow(false)
    val isRightHandedInterface: StateFlow<Boolean> = _isRightHandedInterface.asStateFlow()

    private val _isAnimationAssistVisible = MutableStateFlow(true)
    val isAnimationAssistVisible: StateFlow<Boolean> = _isAnimationAssistVisible.asStateFlow()

    private val _isDreamsMultiTrackVisible = MutableStateFlow(false)
    val isDreamsMultiTrackVisible: StateFlow<Boolean> = _isDreamsMultiTrackVisible.asStateFlow()

    private val _selectionState = MutableStateFlow(SelectionState())
    val selectionState: StateFlow<SelectionState> = _selectionState.asStateFlow()

    private val _transformState = MutableStateFlow(TransformState())
    val transformState: StateFlow<TransformState> = _transformState.asStateFlow()

    // --- PDF IMPORT STUDIO STATE ---
    private val _isPdfImportDialogOpen = MutableStateFlow(false)
    val isPdfImportDialogOpen: StateFlow<Boolean> = _isPdfImportDialogOpen.asStateFlow()

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile.asStateFlow()

    private val _pdfDocumentTitle = MutableStateFlow("Document.pdf")
    val pdfDocumentTitle: StateFlow<String> = _pdfDocumentTitle.asStateFlow()

    // --- EXCALIDRAW SMART STUDIO STATE ---
    private val _isExcalidrawModalOpen = MutableStateFlow(false)
    val isExcalidrawModalOpen: StateFlow<Boolean> = _isExcalidrawModalOpen.asStateFlow()

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _isCanvasLocked = MutableStateFlow(false)
    val isCanvasLocked: StateFlow<Boolean> = _isCanvasLocked.asStateFlow()

    // --- CINEMA SYSTEMS STATE ---
    private val _isCinematicVfxDialogOpen = MutableStateFlow(false)
    val isCinematicVfxDialogOpen: StateFlow<Boolean> = _isCinematicVfxDialogOpen.asStateFlow()

    private val _cinematicVfxConfig = MutableStateFlow(CinematicVfxConfig())
    val cinematicVfxConfig: StateFlow<CinematicVfxConfig> = _cinematicVfxConfig.asStateFlow()

    private val _isVelocityGraphDialogOpen = MutableStateFlow(false)
    val isVelocityGraphDialogOpen: StateFlow<Boolean> = _isVelocityGraphDialogOpen.asStateFlow()

    private val _velocityCurveState = MutableStateFlow(VelocityCurveState())
    val velocityCurveState: StateFlow<VelocityCurveState> = _velocityCurveState.asStateFlow()

    private val _isMultiplaneStageDialogOpen = MutableStateFlow(false)
    val isMultiplaneStageDialogOpen: StateFlow<Boolean> = _isMultiplaneStageDialogOpen.asStateFlow()

    private val _multiplaneStageState = MutableStateFlow(MultiplaneStageState())
    val multiplaneStageState: StateFlow<MultiplaneStageState> = _multiplaneStageState.asStateFlow()

    private val _isSceneTransitionsDialogOpen = MutableStateFlow(false)
    val isSceneTransitionsDialogOpen: StateFlow<Boolean> = _isSceneTransitionsDialogOpen.asStateFlow()

    private val _psychovisualTransitionConfig = MutableStateFlow(PsychovisualTransitionConfig())
    val psychovisualTransitionConfig: StateFlow<PsychovisualTransitionConfig> = _psychovisualTransitionConfig.asStateFlow()

    private val _shiftAndTraceState = MutableStateFlow(ShiftAndTraceState())
    val shiftAndTraceState: StateFlow<ShiftAndTraceState> = _shiftAndTraceState.asStateFlow()

    // Pro Color Grading State
    private val _colorGradingState = MutableStateFlow(ColorGradingState())
    val colorGradingState: StateFlow<ColorGradingState> = _colorGradingState.asStateFlow()

    private val _isColorGradingDialogOpen = MutableStateFlow(false)
    val isColorGradingDialogOpen: StateFlow<Boolean> = _isColorGradingDialogOpen.asStateFlow()

    // Chroma Keying & Matte Tools State
    private val _chromaKeyState = MutableStateFlow(ChromaKeyConfig())
    val chromaKeyState: StateFlow<ChromaKeyConfig> = _chromaKeyState.asStateFlow()

    private val _isChromaKeyDialogOpen = MutableStateFlow(false)
    val isChromaKeyDialogOpen: StateFlow<Boolean> = _isChromaKeyDialogOpen.asStateFlow()

    // Rotoscoping & Per-Vertex Feathering State
    private val _rotoscopingState = MutableStateFlow(RotoscopingState())
    val rotoscopingState: StateFlow<RotoscopingState> = _rotoscopingState.asStateFlow()

    private val _isRotoscopingDialogOpen = MutableStateFlow(false)
    val isRotoscopingDialogOpen: StateFlow<Boolean> = _isRotoscopingDialogOpen.asStateFlow()

    // 2D Motion Tracking State
    private val _motionTrackingState = MutableStateFlow(MotionTrackingState())
    val motionTrackingState: StateFlow<MotionTrackingState> = _motionTrackingState.asStateFlow()

    // Curve Editor & Dope Sheet State
    private val _curveEditorState = MutableStateFlow(CurveEditorState())
    val curveEditorState: StateFlow<CurveEditorState> = _curveEditorState.asStateFlow()

    private val _isCurveEditorDialogOpen = MutableStateFlow(false)
    val isCurveEditorDialogOpen: StateFlow<Boolean> = _isCurveEditorDialogOpen.asStateFlow()

    // Headless GPU Render Farm State
    private val _renderFarmNode = MutableStateFlow(RenderFarmNode())
    val renderFarmNode: StateFlow<RenderFarmNode> = _renderFarmNode.asStateFlow()

    private val _renderFarmProgress = MutableStateFlow(RenderProgressUpdate())
    val renderFarmProgress: StateFlow<RenderProgressUpdate> = _renderFarmProgress.asStateFlow()

    private val _localExportState = MutableStateFlow(LocalExportState())
    val localExportState: StateFlow<LocalExportState> = _localExportState.asStateFlow()

    private val _isRenderFarmDialogOpen = MutableStateFlow(false)
    val isRenderFarmDialogOpen: StateFlow<Boolean> = _isRenderFarmDialogOpen.asStateFlow()

    private val _timelineClips = MutableStateFlow<List<TimelineClip>>(createInitialTimelineClips())
    val timelineClips: StateFlow<List<TimelineClip>> = _timelineClips.asStateFlow()

    private val _selectedClipId = MutableStateFlow<String?>("clip_main_sketch")
    val selectedClipId: StateFlow<String?> = _selectedClipId.asStateFlow()

    private val _isRippleEditActive = MutableStateFlow(false)
    val isRippleEditActive: StateFlow<Boolean> = _isRippleEditActive.asStateFlow()

    private val _isMagneticSnappingActive = MutableStateFlow(true)
    val isMagneticSnappingActive: StateFlow<Boolean> = _isMagneticSnappingActive.asStateFlow()

    private val _activeGuides = MutableStateFlow<List<SnapGuide>>(emptyList())
    val activeGuides: StateFlow<List<SnapGuide>> = _activeGuides.asStateFlow()

    private val _isTimeStretchActive = MutableStateFlow(false)
    val isTimeStretchActive: StateFlow<Boolean> = _isTimeStretchActive.asStateFlow()

    private val _isShapeRecognitionEnabled = MutableStateFlow(true)
    val isShapeRecognitionEnabled: StateFlow<Boolean> = _isShapeRecognitionEnabled.asStateFlow()

    fun setShapeRecognitionEnabled(enabled: Boolean) {
        _isShapeRecognitionEnabled.value = enabled
    }

    val autoAdvanceController = AutoAdvanceFlipbookController(viewModelScope) { shiftX, shiftY ->
        advanceFrameForAutoAdvance(shiftX, shiftY)
    }
    val autoAdvanceState: StateFlow<AutoAdvanceState> = autoAdvanceController.state



    // Undo / Redo history
    private val undoStack = Stack<AnimationProject>()
    private val redoStack = Stack<AnimationProject>()

    private var playbackJob: Job? = null

    init {
        refreshSavedProjects()
    }

    private fun createInitialProject(): AnimationProject {
        val proj = AnimationProject(
            name = "Studio Sequence 01",
            totalFrames = 24,
            fps = 12
        )
        // Pre-create initial cels for layers
        val initialCels = mutableListOf<DrawingCel>()
        proj.layers.forEach { layer ->
            for (f in 0 until proj.totalFrames) {
                initialCels.add(
                    DrawingCel(
                        frameIndex = f,
                        layerId = layer.id,
                        strokes = emptyList()
                    )
                )
            }
        }
        return proj.copy(cels = initialCels)
    }

    private fun pushHistory() {
        undoStack.push(_project.value)
        redoStack.clear()
        if (undoStack.size > 30) {
            undoStack.removeAt(0)
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.push(_project.value)
            _project.value = undoStack.pop()
            showStatus("Undo")
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.push(_project.value)
            _project.value = redoStack.pop()
            showStatus("Redo")
        }
    }

    fun showStatus(msg: String) {
        _statusMessage.value = msg
        viewModelScope.launch {
            delay(2200)
            if (_statusMessage.value == msg) {
                _statusMessage.value = null
            }
        }
    }

    // --- Tools & Color ---
    fun setTool(tool: ToolType) {
        _currentTool.value = tool
    }

    fun setColor(color: Color) {
        _brushColor.value = color
    }

    fun setBrushSize(size: Float) {
        _brushSize.value = size.coerceIn(1f, 150f)
    }

    fun setBrushOpacity(opacity: Float) {
        _brushOpacity.value = opacity.coerceIn(0.05f, 1.0f)
    }

    fun setBrushSmoothing(smoothing: Float) {
        _brushSmoothing.value = smoothing.coerceIn(0f, 0.95f)
    }

    fun toggleSidePanel(panel: SidePanelType) {
        _activeSidePanel.value = if (_activeSidePanel.value == panel) null else panel
    }

    fun closeSidePanel() {
        _activeSidePanel.value = null
    }

    // --- Drawing Stroke Handling ---
    fun startStroke(x: Float, y: Float, pressure: Float) {
        autoAdvanceController.onStrokeStarted()
        val stroke = DrawingStroke(
            points = listOf(StrokePoint(x, y, pressure)),
            colorArgb = _brushColor.value.toArgb(),
            toolType = _currentTool.value,
            size = _brushSize.value,
            opacity = _brushOpacity.value,
            smoothing = _brushSmoothing.value
        )
        _currentStroke.value = stroke
    }

    fun updateStroke(x: Float, y: Float, pressure: Float) {
        updateStrokeBatch(listOf(StrokePoint(x, y, pressure)))
    }

    fun updateStrokeBatch(points: List<StrokePoint>) {
        val curr = _currentStroke.value ?: return
        if (points.isEmpty()) return
        val updatedPoints = ArrayList<StrokePoint>(curr.points.size + points.size).apply {
            addAll(curr.points)
            addAll(points)
        }
        _currentStroke.value = curr.copy(points = updatedPoints)
    }

    fun finishStroke() {
        val stroke = _currentStroke.value ?: return
        _currentStroke.value = null
        if (stroke.points.isEmpty()) return

        pushHistory()

        val recognizedPoints = if (_isShapeRecognitionEnabled.value) {
            DrawingEngine.recognizeShape(stroke.points)
        } else null

        if (recognizedPoints != null) {
            showStatus("✨ Snapped to Geometric Shape")
        }

        val smoothedPoints = recognizedPoints ?: DrawingEngine.smoothPoints(stroke.points, stroke.smoothing)
        val finalStroke = stroke.copy(points = smoothedPoints)

        val frame = _currentFrame.value
        val layerId = _activeLayerId.value

        _project.update { p ->
            val existingCel = p.cels.find { it.frameIndex == frame && it.layerId == layerId }
            val newCels = p.cels.toMutableList()
            if (existingCel != null) {
                val updatedCel = existingCel.copy(strokes = existingCel.strokes + finalStroke)
                val idx = newCels.indexOf(existingCel)
                newCels[idx] = updatedCel
            } else {
                newCels.add(
                    DrawingCel(
                        frameIndex = frame,
                        layerId = layerId,
                        strokes = listOf(finalStroke)
                    )
                )
            }
            p.copy(cels = newCels, updatedAt = System.currentTimeMillis())
        }

        autoAdvanceController.onStrokeCompleted(finalStroke)
    }

    // --- Frame & Playback ---
    fun setCurrentFrame(frame: Int) {
        val total = _project.value.totalFrames
        val clamped = frame.coerceIn(0, (total - 1).coerceAtLeast(0))
        _currentFrame.value = clamped
        AppLogger.playback("SELECT_FRAME", "Frame ${clamped + 1}/$total")
    }

    fun stepFrame(delta: Int) {
        val total = _project.value.totalFrames
        if (total <= 0) return
        val next = (_currentFrame.value + delta + total) % total
        _currentFrame.value = next
        AppLogger.playback("STEP_FRAME", "Delta $delta -> Frame ${next + 1}/$total")
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _isPlaying.value = true
        val total = _project.value.totalFrames
        val fps = _project.value.fps.coerceIn(1, 60)
        AppLogger.playback("PLAY_START", "FPS: $fps, TotalFrames: $total, Mode: ${_playbackMode.value.name}")
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            var forward = true

            while (isActive && _isPlaying.value) {
                val currentFps = _project.value.fps.coerceIn(1, 60)
                val frameDelay = (1000L / currentFps).coerceAtLeast(16L)
                delay(frameDelay)
                if (!_isPlaying.value) break

                val frameCount = _project.value.totalFrames
                if (frameCount <= 1) continue

                val curr = _currentFrame.value
                when (_playbackMode.value) {
                    PlaybackMode.LOOP -> {
                        _currentFrame.value = (curr + 1) % frameCount
                    }
                    PlaybackMode.ONCE -> {
                        if (curr + 1 >= frameCount) {
                            _isPlaying.value = false
                            AppLogger.playback("PLAY_STOP_ONCE", "Reached final frame $frameCount")
                            break
                        } else {
                            _currentFrame.value = curr + 1
                        }
                    }
                    PlaybackMode.PING_PONG -> {
                        if (forward) {
                            if (curr + 1 >= frameCount) {
                                forward = false
                                _currentFrame.value = (curr - 1).coerceAtLeast(0)
                            } else {
                                _currentFrame.value = curr + 1
                            }
                        } else {
                            if (curr - 1 < 0) {
                                forward = true
                                _currentFrame.value = (curr + 1).coerceAtMost(frameCount - 1)
                            } else {
                                _currentFrame.value = curr - 1
                            }
                        }
                    }
                }
            }
        }
    }

    fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
        AppLogger.playback("PLAY_STOP", "Stopped at frame ${_currentFrame.value + 1}")
    }

    fun setFps(fps: Int) {
        val clamped = fps.coerceIn(1, 60)
        _project.update { it.copy(fps = clamped) }
        AppLogger.playback("FPS_SET", "$clamped fps")
        showStatus("Playback FPS set to $clamped")
        if (_isPlaying.value) {
            startPlayback()
        }
    }

    fun addFrame() {
        pushHistory()
        _project.update { p ->
            val newTotal = p.totalFrames + 1
            p.copy(totalFrames = newTotal)
        }
        _currentFrame.value = _project.value.totalFrames - 1
        showStatus("Added frame #${_currentFrame.value + 1}")
    }

    fun duplicateCurrentFrame() {
        pushHistory()
        val curr = _currentFrame.value
        _project.update { p ->
            val currCels = p.cels.filter { it.frameIndex == curr }
            val newTotal = p.totalFrames + 1

            // Shift cels that are past current frame
            val shifted = p.cels.map {
                if (it.frameIndex > curr) it.copy(frameIndex = it.frameIndex + 1) else it
            }.toMutableList()

            // Copy current cels to curr + 1
            currCels.forEach { cel ->
                shifted.add(
                    cel.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        frameIndex = curr + 1
                    )
                )
            }

            p.copy(totalFrames = newTotal, cels = shifted)
        }
        _currentFrame.value = curr + 1
        showStatus("Duplicated frame #${curr + 1}")
    }

    fun clearCurrentCel() {
        pushHistory()
        val frame = _currentFrame.value
        val layerId = _activeLayerId.value
        _project.update { p ->
            val updated = p.cels.map {
                if (it.frameIndex == frame && it.layerId == layerId) it.copy(strokes = emptyList()) else it
            }
            p.copy(cels = updated)
        }
        showStatus("Cleared cel on active layer")
    }

    fun deleteCurrentFrame() {
        if (_project.value.totalFrames <= 1) {
            showStatus("Cannot delete only remaining frame")
            return
        }
        pushHistory()
        val curr = _currentFrame.value
        _project.update { p ->
            val filtered = p.cels.filter { it.frameIndex != curr }.map {
                if (it.frameIndex > curr) it.copy(frameIndex = it.frameIndex - 1) else it
            }
            p.copy(totalFrames = p.totalFrames - 1, cels = filtered)
        }
        if (_currentFrame.value >= _project.value.totalFrames) {
            _currentFrame.value = _project.value.totalFrames - 1
        }
        showStatus("Deleted frame")
    }

    // --- Onion Skinning ---
    fun toggleOnionSkin() {
        _onionSkinConfig.update { it.copy(enabled = !it.enabled) }
        showStatus(if (_onionSkinConfig.value.enabled) "Onion Skin ON" else "Onion Skin OFF")
    }

    fun setOnionSkinConfig(config: OnionSkinConfig) {
        _onionSkinConfig.value = config
    }

    // --- Layers ---
    fun setActiveLayer(layerId: String) {
        _activeLayerId.value = layerId
    }

    fun addLayer(name: String = "Layer ${_project.value.layers.size + 1}") {
        pushHistory()
        val newLayer = AnimationLayer(name = name)
        _project.update { p ->
            p.copy(layers = p.layers + newLayer)
        }
        _activeLayerId.value = newLayer.id
        showStatus("Added $name")
    }

    fun toggleLayerVisibility(layerId: String) {
        _project.update { p ->
            p.copy(layers = p.layers.map {
                if (it.id == layerId) it.copy(isVisible = !it.isVisible) else it
            })
        }
    }

    fun toggleLayerLock(layerId: String) {
        _project.update { p ->
            p.copy(layers = p.layers.map {
                if (it.id == layerId) it.copy(isLocked = !it.isLocked) else it
            })
        }
    }

    fun setLayerOpacity(layerId: String, opacity: Float) {
        _project.update { p ->
            p.copy(layers = p.layers.map {
                if (it.id == layerId) it.copy(opacity = opacity.coerceIn(0f, 1f)) else it
            })
        }
    }

    fun deleteLayer(layerId: String) {
        if (_project.value.layers.size <= 1) {
            showStatus("Cannot delete the only layer")
            return
        }
        pushHistory()
        _project.update { p ->
            p.copy(
                layers = p.layers.filter { it.id != layerId },
                cels = p.cels.filter { it.layerId != layerId }
            )
        }
        if (_activeLayerId.value == layerId) {
            _activeLayerId.value = _project.value.layers.first().id
        }
        showStatus("Layer deleted")
    }

    // --- Rigging & Bones ---
    fun setSelectedBone(boneId: String?) {
        _selectedBoneId.value = boneId
    }

    fun toggleIkMode() {
        _isIkMode.update { !it }
        showStatus(if (_isIkMode.value) "IK Mode ON" else "FK Mode ON")
    }

    fun rotateSelectedBone(deltaDeg: Float) {
        val boneId = _selectedBoneId.value ?: return
        val currPose = getCurrentFramePose()
        val currAngle = currPose.boneAnglesDeg[boneId] ?: 0f
        val newAngles = currPose.boneAnglesDeg.toMutableMap()
        newAngles[boneId] = currAngle + deltaDeg
        savePoseToCurrentFrame(currPose.copy(boneAnglesDeg = newAngles))
    }

    fun setBoneAngle(boneId: String, angleDeg: Float) {
        val currPose = getCurrentFramePose()
        val newAngles = currPose.boneAnglesDeg.toMutableMap()
        newAngles[boneId] = angleDeg
        savePoseToCurrentFrame(currPose.copy(boneAnglesDeg = newAngles))
    }

    fun solveIkForLimb(targetX: Float, targetY: Float) {
        val boneId = _selectedBoneId.value ?: return
        val rig = _project.value.boneRig
        val currPose = getCurrentFramePose()

        val (upperId, lowerId) = when (boneId) {
            "bone_arm_r_low", "bone_arm_r_up" -> Pair("bone_arm_r_up", "bone_arm_r_low")
            "bone_arm_l_low", "bone_arm_l_up" -> Pair("bone_arm_l_up", "bone_arm_l_low")
            "bone_leg_r_low", "bone_leg_r_up" -> Pair("bone_leg_r_up", "bone_leg_r_low")
            "bone_leg_l_low", "bone_leg_l_up" -> Pair("bone_leg_l_up", "bone_leg_l_low")
            else -> return
        }

        val updatedPose = InverseKinematics.solveTwoBoneIK(
            rig = rig,
            currentPose = currPose,
            upperBoneId = upperId,
            lowerBoneId = lowerId,
            targetX = targetX,
            targetY = targetY
        )
        savePoseToCurrentFrame(updatedPose)
    }

    fun getCurrentFramePose(): BonePose {
        val frame = _currentFrame.value
        val kf = _project.value.boneKeyframes.find { it.frameIndex == frame }
        return kf?.pose ?: BonePose()
    }

    private fun savePoseToCurrentFrame(pose: BonePose) {
        val frame = _currentFrame.value
        _project.update { p ->
            val existing = p.boneKeyframes.find { it.frameIndex == frame }
            val updatedKf = p.boneKeyframes.toMutableList()
            if (existing != null) {
                val idx = updatedKf.indexOf(existing)
                updatedKf[idx] = existing.copy(pose = pose)
            } else {
                updatedKf.add(BoneKeyframe(frameIndex = frame, pose = pose))
            }
            p.copy(boneKeyframes = updatedKf)
        }
    }

    // --- AI Motion Assistant Features ---
    fun applyProceduralMotion(motionType: ProceduralMotion.MotionType) {
        pushHistory()
        val keyframes = ProceduralMotion.generateKeyframes(
            motionType = motionType,
            totalFrames = _project.value.totalFrames
        )
        _project.update { it.copy(boneKeyframes = keyframes) }
        showStatus("Generated ${motionType.title} across ${_project.value.totalFrames} frames")
    }

    fun applyPromptMotion(prompt: String) {
        pushHistory()
        val result = AiMotionAssistant.generateMotionFromPrompt(prompt, _project.value.totalFrames)
        _project.update { it.copy(boneKeyframes = result.keyframes) }
        showStatus("AI Motion: ${result.title}")
    }

    fun applyInBetweenFrames(count: Int) {
        pushHistory()
        val current = _currentFrame.value
        val total = _project.value.totalFrames
        val targetFrame = (current + count + 1).coerceAtMost(total - 1)

        val startPose = getCurrentFramePose()
        val endKf = _project.value.boneKeyframes.find { it.frameIndex == targetFrame }
        val endPose = endKf?.pose ?: BonePose()

        val generated = AiMotionAssistant.generateInBetweenBoneKeyframes(
            startFrame = current,
            startPose = startPose,
            endFrame = targetFrame,
            endPose = endPose
        )

        _project.update { p ->
            val mutableKfs = p.boneKeyframes.toMutableList()
            generated.forEach { gen ->
                mutableKfs.removeAll { it.frameIndex == gen.frameIndex }
                mutableKfs.add(gen)
            }
            p.copy(boneKeyframes = mutableKfs)
        }
        showStatus("Generated $count in-between keyframes")
    }

    fun applyPalette(palette: ColorPaletteSuggestion) {
        if (palette.colorsArgb.isNotEmpty()) {
            _brushColor.value = Color(palette.colorsArgb.first())
            showStatus("Applied palette: ${palette.themeName}")
        }
    }

    // --- Project Storage & Exports ---
    fun refreshSavedProjects() {
        _savedProjects.value = storage.listSavedProjects()
    }

    fun saveCurrentProject() {
        val success = storage.saveProject(_project.value)
        if (success) {
            refreshSavedProjects()
            showStatus("Project saved successfully")
        } else {
            showStatus("Failed to save project")
        }
    }

    fun loadProject(id: String) {
        val loaded = storage.loadProject(id)
        if (loaded != null) {
            _project.value = loaded
            _currentFrame.value = 0
            if (loaded.layers.isNotEmpty()) {
                _activeLayerId.value = loaded.layers.first().id
            }
            undoStack.clear()
            redoStack.clear()
            showStatus("Loaded project: ${loaded.name}")
        }
    }

    fun createNewProject(name: String, width: Int = 1280, height: Int = 720, fps: Int = 24) {
        pushHistory()
        val newProj = AnimationProject(
            name = name.ifBlank { "New Animation" },
            canvasWidth = width,
            canvasHeight = height,
            fps = fps,
            totalFrames = 16
        )
        val initialCels = mutableListOf<DrawingCel>()
        newProj.layers.forEach { layer ->
            for (f in 0 until newProj.totalFrames) {
                initialCels.add(DrawingCel(frameIndex = f, layerId = layer.id))
            }
        }
        _project.value = newProj.copy(cels = initialCels)
        _currentFrame.value = 0
        _activeLayerId.value = newProj.layers.first().id
        showStatus("Created new project: ${newProj.name}")
    }

    fun exportGif(withBackground: Boolean = true, onComplete: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { showStatus("Rendering GIF frames...") }
            val p = _project.value
            val renderedFrames = mutableListOf<Bitmap>()
            val bgColor = if (withBackground) android.graphics.Color.WHITE else null

            for (f in 0 until p.totalFrames) {
                val bmp = DrawingEngine.compositeFrame(
                    width = p.canvasWidth,
                    height = p.canvasHeight,
                    frameIndex = f,
                    layers = p.layers,
                    cels = p.cels,
                    backgroundColor = bgColor,
                    pdfTrack = p.pdfTrack,
                    totalFrames = p.totalFrames
                )
                renderedFrames.add(bmp)
            }

            val file = storage.exportGif(renderedFrames, p.fps)
            withContext(Dispatchers.Main) {
                if (file != null) {
                    showStatus("GIF Exported: ${file.name}")
                } else {
                    showStatus("GIF Export failed")
                }
                onComplete(file)
            }
        }
    }

    fun exportPngSequence(onComplete: (List<File>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { showStatus("Exporting PNG sequence...") }
            val p = _project.value

            val files = storage.exportPngSequenceStreaming(
                totalFrames = p.totalFrames
            ) { frameIndex ->
                DrawingEngine.compositeFrame(
                    width = p.canvasWidth,
                    height = p.canvasHeight,
                    frameIndex = frameIndex,
                    layers = p.layers,
                    cels = p.cels,
                    backgroundColor = android.graphics.Color.WHITE,
                    pdfTrack = p.pdfTrack,
                    totalFrames = p.totalFrames
                )
            }

            withContext(Dispatchers.Main) {
                showStatus("Exported ${files.size} PNG frames")
                onComplete(files)
            }
        }
    }

    fun exportMp4(onComplete: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val p = _project.value
            _localExportState.value = LocalExportState(
                isExporting = true,
                totalFrames = p.totalFrames
            )
            withContext(Dispatchers.Main) { showStatus("Encoding MP4 Video...") }

            val file = storage.exportMp4Streaming(
                width = p.canvasWidth,
                height = p.canvasHeight,
                fps = p.fps,
                totalFrames = p.totalFrames,
                onProgress = { curr, total ->
                    _localExportState.update {
                        it.copy(
                            currentFrame = curr,
                            progress = curr.toFloat() / total.coerceAtLeast(1)
                        )
                    }
                }
            ) { frameIndex ->
                DrawingEngine.compositeFrame(
                    width = p.canvasWidth,
                    height = p.canvasHeight,
                    frameIndex = frameIndex,
                    layers = p.layers,
                    cels = p.cels,
                    backgroundColor = android.graphics.Color.WHITE,
                    pdfTrack = p.pdfTrack,
                    totalFrames = p.totalFrames,
                    videoTrack = p.videoTrack,
                    colorGradingState = _colorGradingState.value,
                    context = getApplication(),
                    objectTracks = p.objectTracks
                )
            }

            val galleryUri = if (file != null) storage.publishToGallery(file) else null

            withContext(Dispatchers.Main) {
                if (file != null) {
                    _localExportState.value = LocalExportState(
                        isExporting = false,
                        currentFrame = p.totalFrames,
                        totalFrames = p.totalFrames,
                        progress = 1.0f,
                        exportedFile = file,
                        galleryUri = galleryUri
                    )
                    showStatus("Exported MP4: ${file.name} (Saved to Movies/FrameForge)")
                } else {
                    _localExportState.value = LocalExportState(
                        isExporting = false,
                        error = "Encoding failed"
                    )
                    showStatus("MP4 Export failed")
                }
                onComplete(file)
            }
        }
    }

    fun exportImage(format: com.example.frameforge.ui.a2.ExportFormat, onComplete: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val p = _project.value
                val bmp = DrawingEngine.compositeFrame(
                    width = p.canvasWidth,
                    height = p.canvasHeight,
                    frameIndex = _currentFrame.value,
                    layers = p.layers,
                    cels = p.cels,
                    backgroundColor = android.graphics.Color.WHITE,
                    pdfTrack = p.pdfTrack,
                    totalFrames = p.totalFrames,
                    videoTrack = p.videoTrack,
                    colorGradingState = _colorGradingState.value,
                    context = getApplication(),
                    objectTracks = p.objectTracks
                )
                val isJpg = (format == com.example.frameforge.ui.a2.ExportFormat.JPG)
                val file = if (isJpg) {
                    storage.exportSingleFrameJpg(bmp)
                } else {
                    storage.exportSingleFramePng(bmp)
                }
                if (file != null) {
                    storage.publishImageToGallery(file, if (isJpg) "image/jpeg" else "image/png")
                }
                withContext(Dispatchers.Main) {
                    if (file != null) {
                        showStatus("Exported ${format.label}: ${file.name}")
                    } else {
                        showStatus("Image export failed")
                    }
                    onComplete(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showStatus("Export failed: ${e.message}")
                    onComplete(null)
                }
            }
        }
    }

    fun startMediaCodecExport(preset: String = "1080p Cinema") {
        exportMp4 { file ->
            if (file != null) {
                showStatus("Exported MP4: ${file.name}")
            }
        }
    }

    fun exportCurrentFramePng(onComplete: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { showStatus("Saving Frame PNG...") }
            val p = _project.value
            val f = _currentFrame.value
            val bmp = DrawingEngine.compositeFrame(
                width = p.canvasWidth,
                height = p.canvasHeight,
                frameIndex = f,
                layers = p.layers,
                cels = p.cels,
                backgroundColor = android.graphics.Color.WHITE,
                pdfTrack = p.pdfTrack,
                totalFrames = p.totalFrames
            )
            val file = storage.exportSingleFramePng(bmp, "frame_${f + 1}_snapshot_${System.currentTimeMillis()}.png")
            withContext(Dispatchers.Main) {
                if (file != null) {
                    showStatus("Saved Frame ${f + 1} PNG: ${file.name}")
                } else {
                    showStatus("PNG export failed")
                }
                onComplete(file)
            }
        }
    }

    fun toggleExportDialog() {
        _isExportDialogOpen.value = !_isExportDialogOpen.value
    }

    fun toggleCanvasLock() {
        _isCanvasLocked.value = !_isCanvasLocked.value
        showStatus(if (_isCanvasLocked.value) "🔒 Canvas Locked (No Pan/Zoom)" else "🔓 Canvas Unlocked")
    }

    // --- Tablet Palm Rejection & Pressure Curves ---
    fun setPalmRejectionMode(mode: PalmRejectionMode) {
        _palmRejectionMode.value = mode
        showStatus("Palm Rejection: ${mode.name}")
    }

    fun setPressureCurve(curve: PressureCurve) {
        _pressureCurve.value = curve
        showStatus("Pressure Curve: ${curve.name}")
    }

    // --- Color Management & Recent Colors ---
    fun selectColor(color: Color) {
        _brushColor.value = color
        val list = _recentColors.value.toMutableList()
        list.remove(color)
        list.add(0, color)
        if (list.size > 12) list.removeAt(list.size - 1)
        _recentColors.value = list
    }

    fun addColorToCustomPalette(color: Color) {
        if (!_customPalette.value.contains(color)) {
            _customPalette.value = _customPalette.value + color
            showStatus("Added color to palette")
        }
    }

    fun removeColorFromCustomPalette(color: Color) {
        _customPalette.value = _customPalette.value.filter { it != color }
    }

    // --- Advanced Multi-Layer Operations ---
    fun duplicateLayer(layerId: String) {
        pushHistory()
        val p = _project.value
        val sourceLayer = p.layers.find { it.id == layerId } ?: return
        val newLayerId = "layer_${System.currentTimeMillis()}"
        val newLayer = sourceLayer.copy(
            id = newLayerId,
            name = "${sourceLayer.name} Copy"
        )

        // Duplicate all cels on this layer
        val newCels = p.cels.toMutableList()
        p.cels.filter { it.layerId == layerId }.forEach { cel ->
            newCels.add(
                cel.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    layerId = newLayerId
                )
            )
        }

        _project.update {
            it.copy(
                layers = it.layers + newLayer,
                cels = newCels
            )
        }
        _activeLayerId.value = newLayerId
        showStatus("Duplicated ${sourceLayer.name}")
    }

    fun mergeLayerDown(layerId: String) {
        val p = _project.value
        val idx = p.layers.indexOfFirst { it.id == layerId }
        if (idx <= 0) {
            showStatus("No layer below to merge into")
            return
        }
        pushHistory()
        val targetLayer = p.layers[idx - 1]
        val sourceCels = p.cels.filter { it.layerId == layerId }

        val updatedCels = p.cels.filter { it.layerId != layerId }.map { cel ->
            if (cel.layerId == targetLayer.id) {
                val matchingSrc = sourceCels.find { it.frameIndex == cel.frameIndex }
                if (matchingSrc != null) {
                    cel.copy(strokes = cel.strokes + matchingSrc.strokes)
                } else cel
            } else cel
        }

        _project.update {
            it.copy(
                layers = it.layers.filter { l -> l.id != layerId },
                cels = updatedCels
            )
        }
        _activeLayerId.value = targetLayer.id
        showStatus("Merged into ${targetLayer.name}")
    }

    fun setLayerBlendMode(layerId: String, blendMode: BlendModeType) {
        pushHistory()
        _project.update { p ->
            p.copy(layers = p.layers.map {
                if (it.id == layerId) it.copy(blendMode = blendMode) else it
            })
        }
        showStatus("Layer blend: ${blendMode.label}")
    }

    // --- Node-Based Animation & Compositing Graph ---
    fun toggleNodeGraphView() {
        _isNodeGraphViewActive.update { !it }
    }

    fun updateNodePosition(nodeId: String, newX: Float, newY: Float) {
        _nodeGraph.update { g ->
            g.copy(nodes = g.nodes.map {
                if (it.id == nodeId) it.copy(x = newX, y = newY) else it
            })
        }
    }

    fun autoArrangeNodeGraph() {
        _nodeGraph.update { g ->
            val col1X = 60f
            val col2X = 720f
            val col3X = 1380f
            var col1Y = 60f
            var col2Y = 60f
            var col3Y = 220f

            val updatedNodes = g.nodes.map { node ->
                when (node.nodeType) {
                    AnimationNodeType.LAYER_SOURCE, AnimationNodeType.AI_MOTION -> {
                        val n = node.copy(x = col1X, y = col1Y)
                        col1Y += 360f
                        n
                    }
                    AnimationNodeType.COLOR_FX, AnimationNodeType.TRANSFORM -> {
                        val n = node.copy(x = col2X, y = col2Y)
                        col2Y += 360f
                        n
                    }
                    AnimationNodeType.COMPOSITE_OUTPUT -> {
                        val n = node.copy(x = col3X, y = col3Y)
                        col3Y += 360f
                        n
                    }
                }
            }
            g.copy(nodes = updatedNodes)
        }
        showStatus("Nodes Auto-Arranged")
    }

    fun addGraphNode(type: AnimationNodeType) {
        val title = when (type) {
            AnimationNodeType.LAYER_SOURCE -> "Layer Source"
            AnimationNodeType.TRANSFORM -> "Transform Motion"
            AnimationNodeType.COLOR_FX -> "Color Filter"
            AnimationNodeType.AI_MOTION -> "AI Motion Driver"
            AnimationNodeType.COMPOSITE_OUTPUT -> "Output"
        }
        val newNode = GraphNode(
            title = title,
            nodeType = type,
            x = 300f + (_nodeGraph.value.nodes.size * 20f),
            y = 200f + (_nodeGraph.value.nodes.size * 20f),
            inputs = if (type == AnimationNodeType.LAYER_SOURCE) emptyList() else listOf(com.example.frameforge.model.NodeSocket("in_data", "Input", false)),
            outputs = if (type == AnimationNodeType.COMPOSITE_OUTPUT) emptyList() else listOf(com.example.frameforge.model.NodeSocket("out_data", "Output", true))
        )
        _nodeGraph.update { it.copy(nodes = it.nodes + newNode) }
        showStatus("Added node: $title")
    }

    fun deleteGraphNode(nodeId: String) {
        _nodeGraph.update { g ->
            g.copy(
                nodes = g.nodes.filter { it.id != nodeId },
                connections = g.connections.filter { it.fromNodeId != nodeId && it.toNodeId != nodeId }
            )
        }
        showStatus("Deleted node")
    }

    fun updateNodeParameter(nodeId: String, paramKey: String, paramValue: String) {
        _nodeGraph.update { g ->
            g.copy(nodes = g.nodes.map {
                if (it.id == nodeId) {
                    val p = it.parameters.toMutableMap()
                    p[paramKey] = paramValue
                    it.copy(parameters = p)
                } else it
            })
        }
    }

    // ==========================================
    // --- PROCREATE MASTER OVERHAUL ACTIONS ---
    // ==========================================

    fun closeAllPopovers() {
        _isActionsPopoverOpen.value = false
        _isAdjustmentsPopoverOpen.value = false
        _isBrushStudioOpen.value = false
        _isLayersPopoverOpen.value = false
        _isColorPickerOpen.value = false
        _isGalleryDialogOpen.value = false
    }

    fun toggleActionsPopover() {
        val next = !_isActionsPopoverOpen.value
        closeAllPopovers()
        _isActionsPopoverOpen.value = next
    }

    fun setActionsTab(tab: ActionsTab) {
        _actionsTab.value = tab
    }

    fun toggleAdjustmentsPopover() {
        val next = !_isAdjustmentsPopoverOpen.value
        closeAllPopovers()
        _isAdjustmentsPopoverOpen.value = next
    }

    fun startAdjustment(type: AdjustmentType) {
        _adjustmentState.value = AdjustmentState(activeType = type, intensity = 0.35f)
        _isAdjustmentsPopoverOpen.value = false
        showStatus("${type.label} Active • Slide on canvas to adjust")
    }

    fun updateAdjustmentIntensity(intensity: Float) {
        _adjustmentState.update { it.copy(intensity = intensity.coerceIn(0f, 1f)) }
    }

    fun applyAdjustment() {
        val adj = _adjustmentState.value
        if (adj.activeType != AdjustmentType.NONE) {
            pushHistory()
            showStatus("Applied ${adj.activeType.label}")
            _adjustmentState.value = AdjustmentState(activeType = AdjustmentType.NONE)
        }
    }

    fun cancelAdjustment() {
        _adjustmentState.value = AdjustmentState(activeType = AdjustmentType.NONE)
        showStatus("Cancelled Adjustment")
    }

    fun toggleBrushStudio() {
        val next = !_isBrushStudioOpen.value
        closeAllPopovers()
        _isBrushStudioOpen.value = next
    }

    fun selectBrushCategory(cat: BrushCategory) {
        _activeBrushCategory.value = cat
        val firstInCat = BrushDefinition.defaultBrushes().find { it.category == cat }
        if (firstInCat != null) {
            selectBrush(firstInCat)
        }
    }

    fun selectBrush(brush: BrushDefinition) {
        _activeBrush.value = brush
        _currentTool.value = brush.toolType
        _brushSize.value = brush.defaultSize
        _brushOpacity.value = brush.defaultOpacity
        _brushSmoothing.value = brush.streamline
        showStatus("Selected ${brush.name}")
    }

    fun toggleLayersPopover() {
        val next = !_isLayersPopoverOpen.value
        closeAllPopovers()
        _isLayersPopoverOpen.value = next
    }

    fun toggleColorPicker() {
        val next = !_isColorPickerOpen.value
        closeAllPopovers()
        _isColorPickerOpen.value = next
    }

    fun setColorPickerTab(tab: ColorPickerTab) {
        _colorPickerTab.value = tab
    }

    fun toggleGalleryDialog() {
        val next = !_isGalleryDialogOpen.value
        closeAllPopovers()
        _isGalleryDialogOpen.value = next
    }

    fun toggleReferenceCompanion() {
        _isReferenceCompanionOpen.update { !it }
        showStatus(if (_isReferenceCompanionOpen.value) "Reference Companion Opened" else "Reference Window Closed")
    }

    fun toggleRightHandedInterface() {
        _isRightHandedInterface.update { !it }
        showStatus(if (_isRightHandedInterface.value) "Right-handed HUD" else "Left-handed HUD")
    }

    fun toggleAnimationAssist() {
        _isAnimationAssistVisible.update { !it }
        showStatus(if (_isAnimationAssistVisible.value) "Animation Assist Shown" else "Animation Assist Hidden")
    }

    fun toggleDreamsMultiTrack() {
        _isDreamsMultiTrackVisible.update { !it }
        showStatus(if (_isDreamsMultiTrackVisible.value) "Procreate Dreams Tracks View" else "Single Track Assist View")
    }

    // --- CANVAS CROP & 4K RESOLUTION ---
    fun setCanvasResolution(preset: CanvasResolutionPreset) {
        pushHistory()
        _project.update {
            it.copy(
                canvasWidth = preset.width,
                canvasHeight = preset.height
            )
        }
        showStatus("Canvas set to ${preset.label}")
    }

    // --- 4K VIDEO ROTOSCOPING ENGINE ---
    fun toggleRotoscopingVideo() {
        _project.update {
            it.copy(videoTrack = it.videoTrack.copy(isEnabled = !it.videoTrack.isEnabled))
        }
        showStatus(if (_project.value.videoTrack.isEnabled) "Rotoscoping Video Track ON" else "Rotoscoping Track OFF")
    }

    fun setRotoscopingOpacity(opacity: Float) {
        _project.update {
            it.copy(videoTrack = it.videoTrack.copy(opacity = opacity.coerceIn(0.05f, 1f)))
        }
    }

    fun toggleRotoscopingUnderlay() {
        _project.update {
            it.copy(videoTrack = it.videoTrack.copy(isUnderlay = !it.videoTrack.isUnderlay))
        }
        showStatus(if (_project.value.videoTrack.isUnderlay) "Video Under Drawing (Trace Mode)" else "Video Over Drawing")
    }

    // --- NLE VIDEO TRACK & B-ROLL PiP ENGINE ---

    fun addVideoClip(title: String? = null, startPct: Float? = null, endPct: Float? = null) {
        pushHistory()
        val currentClips = _project.value.videoTrack.clips
        val nextIdx = currentClips.size + 1
        val clipTitle = title ?: "docu_cut_$nextIdx.mp4"

        val lastEnd = currentClips.maxOfOrNull { it.endPct } ?: 0f
        val computedStart = startPct ?: if (lastEnd < 85f) lastEnd + 2f else 15f
        val computedEnd = endPct ?: (computedStart + 35f).coerceAtMost(100f)

        val colors = listOf(0xFF0EA5E9, 0xFFA855F7, 0xFF10B981, 0xFFF59E0B, 0xFFEC4899, 0xFF6366F1)
        val colorHex = colors[(nextIdx - 1) % colors.size]

        val newClip = A2VideoClip(
            title = clipTitle,
            startPct = computedStart,
            endPct = computedEnd,
            speed = 1.0f,
            colorHex = colorHex
        )

        _project.update { p ->
            val updated = p.videoTrack.copy(
                isEnabled = true,
                clips = p.videoTrack.clips + newClip,
                selectedClipId = newClip.id,
                pipTransform = p.videoTrack.pipTransform.copy(isVisible = true)
            )
            p.copy(videoTrack = updated)
        }

        AppLogger.video("ADD_CLIP", "Added '${newClip.title}' [${newClip.startPct.toInt()}% - ${newClip.endPct.toInt()}%]")
        showStatus("Added B-Roll Clip: ${newClip.title}")
    }

    fun importRealVideo(uri: Uri, fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val cacheFile = File(context.cacheDir, "vid_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(cacheFile).use { out -> input.copyTo(out) }
                }
                val localPath = cacheFile.absolutePath

                var durationMs = 0L
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(localPath)
                    durationMs = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                    mmr.release()
                } catch (_: Exception) {}

                withContext(Dispatchers.Main) {
                    pushHistory()
                    val newClip = A2VideoClip(
                        title = fileName,
                        startPct = 0f,
                        endPct = 100f,
                        speed = 1.0f,
                        colorHex = 0xFF10B981,
                        isCustomVideo = true,
                        videoUri = localPath
                    )

                    _project.update { p ->
                        val updated = p.videoTrack.copy(
                            isEnabled = true,
                            clips = p.videoTrack.clips + newClip,
                            selectedClipId = newClip.id,
                            pipTransform = p.videoTrack.pipTransform.copy(isVisible = true)
                        )
                        p.copy(videoTrack = updated)
                    }

                    AppLogger.video("IMPORT_REAL_VIDEO", "Imported video '$fileName' ($durationMs ms) -> $localPath")
                    showStatus("Imported Video: $fileName")
                }
            } catch (e: Exception) {
                AppLogger.e("IMPORT_VIDEO_ERROR", "Failed to import video", e)
                withContext(Dispatchers.Main) {
                    showStatus("Failed to import video: ${e.message}")
                }
            }
        }
    }

    fun selectVideoClip(clipId: String) {
        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(selectedClipId = clipId))
        }
        AppLogger.video("SELECT_CLIP", "Selected clip: $clipId")
    }

    fun splitVideoClip(clipId: String? = null, atPct: Float? = null) {
        pushHistory()
        val track = _project.value.videoTrack
        val targetId = clipId ?: track.selectedClipId ?: track.clips.firstOrNull()?.id ?: return
        val clip = track.clips.find { it.id == targetId } ?: return

        val playheadPct = atPct ?: run {
            val total = _project.value.totalFrames
            if (total > 1) (_currentFrame.value.toFloat() / (total - 1)) * 100f else 50f
        }
        val splitPoint = if (playheadPct in (clip.startPct + 3f)..(clip.endPct - 3f)) {
            playheadPct
        } else {
            ((clip.startPct + clip.endPct) / 2f)
        }
        if (splitPoint <= clip.startPct + 2f || splitPoint >= clip.endPct - 2f) {
            showStatus("Cannot split at edge of clip")
            return
        }

        val origEnd = clip.endPct
        val firstClip = clip.copy(endPct = splitPoint - 1f)
        val secondClip = clip.copy(
            id = java.util.UUID.randomUUID().toString(),
            title = if (clip.title.contains('.')) clip.title.replace(".mp4", "_B.mp4") else "${clip.title}_B",
            startPct = splitPoint + 1f,
            endPct = origEnd,
            colorHex = 0xFFA855F7
        )

        val updatedClips = track.clips.map { if (it.id == clip.id) firstClip else it } + secondClip
        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(clips = updatedClips, selectedClipId = secondClip.id))
        }

        AppLogger.video("SPLIT_CLIP", "Split '${clip.title}' at ${splitPoint.toInt()}% into two clips")
        showStatus("Split Clip at ${splitPoint.toInt()}%")
    }

    fun trimVideoClipIn(clipId: String? = null, atPlayheadPct: Float? = null) {
        pushHistory()
        val track = _project.value.videoTrack
        val targetId = clipId ?: track.selectedClipId ?: track.clips.firstOrNull()?.id ?: return
        val clip = track.clips.find { it.id == targetId } ?: return

        val playheadPct = atPlayheadPct ?: run {
            val total = _project.value.totalFrames
            if (total > 1) (_currentFrame.value.toFloat() / (total - 1)) * 100f else 0f
        }
        val newStart = if (playheadPct in (clip.startPct + 1f)..(clip.endPct - 5f)) {
            playheadPct
        } else {
            (clip.startPct + 6f).coerceAtMost(clip.endPct - 5f)
        }
        val updated = clip.copy(startPct = newStart)

        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(clips = p.videoTrack.clips.map { if (it.id == targetId) updated else it }))
        }
        AppLogger.video("TRIM_IN", "Trimmed In to ${newStart.toInt()}%")
        showStatus("Trimmed In: ${newStart.toInt()}%")
    }

    fun trimVideoClipOut(clipId: String? = null, atPlayheadPct: Float? = null) {
        pushHistory()
        val track = _project.value.videoTrack
        val targetId = clipId ?: track.selectedClipId ?: track.clips.firstOrNull()?.id ?: return
        val clip = track.clips.find { it.id == targetId } ?: return

        val playheadPct = atPlayheadPct ?: run {
            val total = _project.value.totalFrames
            if (total > 1) (_currentFrame.value.toFloat() / (total - 1)) * 100f else 100f
        }
        val newEnd = if (playheadPct in (clip.startPct + 5f)..(clip.endPct - 1f)) {
            playheadPct
        } else {
            (clip.endPct - 6f).coerceAtLeast(clip.startPct + 5f)
        }
        val updated = clip.copy(endPct = newEnd)

        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(clips = p.videoTrack.clips.map { if (it.id == targetId) updated else it }))
        }
        AppLogger.video("TRIM_OUT", "Trimmed Out to ${newEnd.toInt()}%")
        showStatus("Trimmed Out: ${newEnd.toInt()}%")
    }

    fun rippleDeleteVideoClip(clipId: String? = null) {
        pushHistory()
        val track = _project.value.videoTrack
        val targetId = clipId ?: track.selectedClipId ?: track.clips.firstOrNull()?.id ?: return
        val targetIdx = track.clips.indexOfFirst { it.id == targetId }
        if (targetIdx == -1) return

        val removed = track.clips[targetIdx]
        val gap = (removed.endPct - removed.startPct).coerceAtLeast(0f)

        val remaining = track.clips.filterIndexed { idx, _ -> idx != targetIdx }.mapIndexed { idx, c ->
            if (idx >= targetIdx) {
                c.copy(
                    startPct = (c.startPct - gap).coerceAtLeast(0f),
                    endPct = (c.endPct - gap).coerceAtLeast(5f)
                )
            } else c
        }

        val nextSelectedId = remaining.getOrNull(targetIdx.coerceAtMost(remaining.size - 1))?.id

        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(clips = remaining, selectedClipId = nextSelectedId))
        }

        AppLogger.video("RIPPLE_DELETE", "Deleted '${removed.title}', rippled left by ${gap.toInt()}%")
        showStatus("Ripple Deleted: ${removed.title}")
    }

    fun cycleVideoClipSpeed(clipId: String? = null) {
        pushHistory()
        val track = _project.value.videoTrack
        val targetId = clipId ?: track.selectedClipId ?: track.clips.firstOrNull()?.id ?: return
        val clip = track.clips.find { it.id == targetId } ?: return

        val nextSpeed = when (clip.speed) {
            0.5f -> 1.0f
            1.0f -> 1.5f
            1.5f -> 2.0f
            2.0f -> 0.5f
            else -> 1.0f
        }

        val updated = clip.copy(speed = nextSpeed)
        _project.update { p ->
            p.copy(videoTrack = p.videoTrack.copy(clips = p.videoTrack.clips.map { if (it.id == targetId) updated else it }))
        }

        AppLogger.video("SET_SPEED", "Set speed of '${clip.title}' to ${nextSpeed}x")
        showStatus("Clip Speed: ${nextSpeed}x")
    }

    fun toggleVideoPipVisibility() {
        _project.update { p ->
            val cur = p.videoTrack.pipTransform.isVisible
            p.copy(videoTrack = p.videoTrack.copy(pipTransform = p.videoTrack.pipTransform.copy(isVisible = !cur)))
        }
        val isNowVis = _project.value.videoTrack.pipTransform.isVisible
        AppLogger.video("PIP_VISIBILITY", "PiP Box Visible: $isNowVis")
        showStatus(if (isNowVis) "PiP Stage Box Visible" else "PiP Stage Box Hidden")
    }

    fun toggleVideoUnderlayMode() {
        _project.update { p ->
            val cur = p.videoTrack.pipTransform.isUnderlayMode
            p.copy(videoTrack = p.videoTrack.copy(pipTransform = p.videoTrack.pipTransform.copy(isUnderlayMode = !cur)))
        }
        val isUnderlay = _project.value.videoTrack.pipTransform.isUnderlayMode
        AppLogger.video("PIP_UNDERLAY_MODE", "Full Underlay Mode: $isUnderlay")
        showStatus(if (isUnderlay) "Video Mode: Full Canvas Underlay (Trace)" else "Video Mode: Floating PiP Window")
    }

    // --- MULTI-PAGE PDF IMPORT STUDIO & KEYFRAME ENGINE ---

    fun openPdfImportDialogWithUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                val tempFile = File(context.cacheDir, "imported_${System.currentTimeMillis()}.pdf")
                if (inputStream != null) {
                    FileOutputStream(tempFile).use { out ->
                        inputStream.copyTo(out)
                    }
                    _pdfFile.value = tempFile
                    val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Imported Document.pdf"
                    _pdfDocumentTitle.value = displayName
                    _isPdfImportDialogOpen.value = true
                    showStatus("Loaded $displayName")
                } else {
                    showStatus("Failed to open PDF document")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Error loading PDF: ${e.message}")
            }
        }
    }

    fun loadSamplePdfDocument() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                showStatus("Generating 8-Page Storyboard PDF...")
                val file = PdfEngine.generateSamplePdfFile(getApplication())
                _pdfFile.value = file
                _pdfDocumentTitle.value = "Sample Storyboard & Animatic Script (8 Pages)"
                _isPdfImportDialogOpen.value = true
                showStatus("Loaded 8-Page Sample Storyboard PDF")
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Failed to generate sample PDF: ${e.message}")
            }
        }
    }

    fun importVideoClip(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_video.mp4"
                val tempFile = File(context.cacheDir, "vid_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { out -> input.copyTo(out) }
                }
                val newClip = com.example.frameforge.cinema.TimelineClip(
                    trackId = "track_v1_video",
                    name = name,
                    startFrame = 0,
                    durationFrames = 48,
                    clipType = com.example.frameforge.cinema.TimelineClipType.VIDEO,
                    colorArgb = 0xFF8B5CF6.toInt()
                )
                _timelineClips.update { it + newClip }
                showStatus("Imported Video: $name")
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Failed to import video: ${e.message}")
            }
        }
    }

    fun importAudioTrack(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "audio_track.mp3"
                val tempFile = File(context.cacheDir, "aud_${System.currentTimeMillis()}.mp3")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { out -> input.copyTo(out) }
                }
                val newClip = com.example.frameforge.cinema.TimelineClip(
                    trackId = "track_a1_narration",
                    name = name,
                    startFrame = 0,
                    durationFrames = 72,
                    clipType = com.example.frameforge.cinema.TimelineClipType.AUDIO,
                    colorArgb = 0xFF10B981.toInt()
                )
                _timelineClips.update { it + newClip }
                showStatus("Imported Audio Track: $name")
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Failed to import audio: ${e.message}")
            }
        }
    }

    fun importReferenceImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val name = uri.lastPathSegment?.substringAfterLast('/') ?: "reference.png"
                addLayer(name)
                showStatus("Inserted Reference Photo: $name")
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Failed to load reference image: ${e.message}")
            }
        }
    }

    fun closePdfImportDialog() {
        _isPdfImportDialogOpen.value = false
    }

    fun commitPdfImport(file: File, config: PdfImportConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            showStatus("Rasterizing & Importing ${config.selectedPages.size} PDF Pages...")
            try {
                pushHistory()
                val sessionId = java.util.UUID.randomUUID().toString()
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val renderedBitmaps = mutableListOf<Bitmap>()
                val sortedPages = config.selectedPages.sorted()

                val maxDim = config.resolutionPreset.maxDimensionPx

                for (pageIndex in sortedPages) {
                    if (pageIndex in 0 until renderer.pageCount) {
                        val page = renderer.openPage(pageIndex)
                        val pw = page.width
                        val ph = page.height
                        val scale = min(maxDim.toFloat() / pw, maxDim.toFloat() / ph)
                        val targetW = (pw * scale).toInt().coerceAtLeast(1)
                        val targetH = (ph * scale).toInt().coerceAtLeast(1)

                        val rawBmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
                        rawBmp.eraseColor(android.graphics.Color.WHITE)
                        page.render(rawBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        val processedBmp = if (config.removeWhiteBackground || config.invertColors) {
                            PdfEngine.extractWhiteToAlpha(
                                source = rawBmp,
                                tolerance = 0.12f,
                                invert = config.invertColors
                            )
                        } else {
                            rawBmp
                        }

                        // Store in LRU cache
                        val cacheKey = "${sessionId}_page_$pageIndex"
                        PdfEngine.BitmapCache.put(cacheKey, processedBmp)
                        renderedBitmaps.add(processedBmp)
                    }
                }

                if (config.importMode == PdfImportMode.STITCHED_SCROLL && renderedBitmaps.isNotEmpty()) {
                    val stitched = PdfEngine.stitchPagesVertically(renderedBitmaps, maxWidth = _project.value.canvasWidth)
                    val stitchedKey = "${sessionId}_stitched"
                    PdfEngine.BitmapCache.put(stitchedKey, stitched)
                }

                renderer.close()
                pfd.close()

                val totalRequiredFrames = when (config.importMode) {
                    PdfImportMode.SEQUENTIAL_CELS -> (sortedPages.size * config.holdDurationFrames).coerceAtLeast(24)
                    PdfImportMode.STITCHED_SCROLL -> 96
                    PdfImportMode.SEPARATE_LAYERS -> max(_project.value.totalFrames, 24)
                }

                _project.update { p ->
                    val newTotalFrames = max(p.totalFrames, totalRequiredFrames)
                    val newLayers = p.layers.toMutableList()
                    val newCels = p.cels.toMutableList()

                    // If separate layers mode: create layer per page
                    if (config.importMode == PdfImportMode.SEPARATE_LAYERS) {
                        for (pageIndex in sortedPages) {
                            val layerId = "pdf_layer_${pageIndex + 1}"
                            if (newLayers.none { it.id == layerId }) {
                                newLayers.add(
                                    AnimationLayer(
                                        id = layerId,
                                        name = "PDF Page ${pageIndex + 1}",
                                        opacity = 1.0f
                                    )
                                )
                            }
                        }
                    }

                    // If linked layer requested: add drawing layer directly above PDF
                    if (config.createLinkedDrawingLayerAbove) {
                        val linkedLayerId = "layer_draw_over_pdf_${System.currentTimeMillis() % 1000}"
                        val linkedLayer = AnimationLayer(
                            id = linkedLayerId,
                            name = "Draw & Animate over PDF",
                            opacity = 1.0f,
                            onionSkinEnabled = true
                        )
                        newLayers.add(linkedLayer)
                    }

                    // Pre-populate empty drawing cels for all frames
                    newLayers.forEach { layer ->
                        for (f in 0 until newTotalFrames) {
                            if (newCels.none { it.frameIndex == f && it.layerId == layer.id }) {
                                newCels.add(DrawingCel(frameIndex = f, layerId = layer.id, strokes = emptyList()))
                            }
                        }
                    }

                    val updatedPdfTrack = PdfDocumentTrack(
                        isEnabled = true,
                        documentTitle = _pdfDocumentTitle.value,
                        totalDocumentPages = sortedPages.size,
                        importMode = config.importMode,
                        pageIndices = sortedPages,
                        holdFramesPerPage = config.holdDurationFrames,
                        removeWhiteBackground = config.removeWhiteBackground,
                        invertColors = config.invertColors,
                        opacity = 1.0f,
                        isUnderlay = !config.paintDirectlyOnPdf,
                        isPaintDirectlyOnPdf = config.paintDirectlyOnPdf,
                        stitchedScrollDurationFrames = totalRequiredFrames,
                        resolutionPreset = config.resolutionPreset,
                        cacheSessionId = sessionId
                    )

                    p.copy(
                        totalFrames = newTotalFrames,
                        layers = newLayers,
                        cels = newCels,
                        pdfTrack = updatedPdfTrack,
                        updatedAt = System.currentTimeMillis()
                    )
                }

                _activeLayerId.value = _project.value.layers.last().id
                _isPdfImportDialogOpen.value = false
                showStatus("Imported ${sortedPages.size} PDF Pages to Studio Timeline")
            } catch (e: Exception) {
                e.printStackTrace()
                showStatus("Import failed: ${e.message}")
            }
        }
    }

    fun togglePdfTrack() {
        _project.update {
            it.copy(pdfTrack = it.pdfTrack.copy(isEnabled = !it.pdfTrack.isEnabled))
        }
        showStatus(if (_project.value.pdfTrack.isEnabled) "PDF Document Track ON" else "PDF Track OFF")
    }

    fun setPdfTrackOpacity(opacity: Float) {
        _project.update {
            it.copy(pdfTrack = it.pdfTrack.copy(opacity = opacity.coerceIn(0.05f, 1f)))
        }
    }

    fun togglePaintDirectlyOnPdf() {
        _project.update {
            val curr = it.pdfTrack.isPaintDirectlyOnPdf
            it.copy(pdfTrack = it.pdfTrack.copy(isPaintDirectlyOnPdf = !curr, isUnderlay = curr))
        }
        showStatus(if (_project.value.pdfTrack.isPaintDirectlyOnPdf) "Painting Directly on PDF Layer" else "Drawing on Layer Above PDF")
    }

    // --- AUDIO TRACK & WAVEFORM ENGINE ---
    fun toggleAudioTrack() {
        _project.update {
            it.copy(audioTrack = it.audioTrack.copy(isEnabled = !it.audioTrack.isEnabled))
        }
        showStatus(if (_project.value.audioTrack.isEnabled) "Audio Track Active" else "Audio Track Muted")
    }

    fun setAudioVolume(volume: Float) {
        _project.update {
            it.copy(audioTrack = it.audioTrack.copy(volume = volume.coerceIn(0f, 1f)))
        }
    }

    fun toggleAudioMute() {
        _project.update {
            it.copy(audioTrack = it.audioTrack.copy(isMuted = !it.audioTrack.isMuted))
        }
    }

    // --- SELECTION TOOLS ---
    fun setSelectionMode(mode: SelectionMode) {
        if (mode == SelectionMode.NONE) {
            _selectionState.value = SelectionState(mode = SelectionMode.NONE, isActive = false)
            _currentTool.value = ToolType.PEN
        } else {
            _selectionState.value = SelectionState(mode = mode, isActive = true)
            _currentTool.value = when (mode) {
                SelectionMode.FREEHAND -> ToolType.LASSO
                SelectionMode.RECTANGLE -> ToolType.RECT_SELECT
                SelectionMode.ELLIPSE -> ToolType.ELLIPSE_SELECT
                SelectionMode.AUTOMATIC -> ToolType.FILL
                else -> ToolType.LASSO
            }
            showStatus("Selection: ${mode.label}")
        }
    }

    fun clearSelection() {
        _selectionState.value = SelectionState(mode = SelectionMode.NONE, isActive = false)
        _currentTool.value = ToolType.PEN
        showStatus("Selection Cleared")
    }

    fun invertSelection() {
        _selectionState.update { it.copy(isInverted = !it.isInverted) }
        showStatus("Selection Inverted")
    }

    // --- TRANSFORM ENGINE ---
    fun toggleTransformMode() {
        val next = !_transformState.value.isActive
        _transformState.update { it.copy(isActive = next) }
        if (next) {
            _currentTool.value = ToolType.TRANSFORM
            showStatus("Transform Active")
        } else {
            _currentTool.value = ToolType.PEN
            showStatus("Transform Committed")
        }
    }

    fun setTransformMode(mode: TransformMode) {
        _transformState.update { it.copy(mode = mode) }
    }

    fun flipTransformH() {
        _transformState.update { it.copy(flipH = !it.flipH, scaleX = it.scaleX * -1f) }
        showStatus("Flipped Horizontal")
    }

    fun flipTransformV() {
        _transformState.update { it.copy(flipV = !it.flipV, scaleY = it.scaleY * -1f) }
        showStatus("Flipped Vertical")
    }

    fun rotateTransform45() {
        _transformState.update { it.copy(rotationDeg = (it.rotationDeg + 45f) % 360f) }
        showStatus("Rotated 45°")
    }

    fun resetTransform() {
        _transformState.value = TransformState(isActive = true)
        showStatus("Transform Reset")
    }

    // --- EXCALIDRAW STUDIO METHODS ---
    fun toggleExcalidrawModal() {
        _isExcalidrawModalOpen.update { !it }
    }

    fun closeExcalidrawModal() {
        _isExcalidrawModalOpen.value = false
    }

    fun stampStrokesOntoActiveLayer(strokes: List<DrawingStroke>, description: String) {
        if (strokes.isEmpty()) return
        pushHistory()
        val frame = _currentFrame.value
        val layerId = _activeLayerId.value

        _project.update { p ->
            val existingCel = p.cels.find { it.frameIndex == frame && it.layerId == layerId }
            val newCels = p.cels.toMutableList()
            if (existingCel != null) {
                val updatedCel = existingCel.copy(strokes = existingCel.strokes + strokes)
                val idx = newCels.indexOf(existingCel)
                newCels[idx] = updatedCel
            } else {
                newCels.add(DrawingCel(frameIndex = frame, layerId = layerId, strokes = strokes))
            }
            p.copy(cels = newCels)
        }
        showStatus(description)
    }

    // --- AUTO-ADVANCE FRAME ENGINE ---
    fun advanceFrameForAutoAdvance(shiftX: Float = 0f, shiftY: Float = 0f) {
        val curr = _currentFrame.value
        val total = _project.value.totalFrames
        if (curr >= total - 1) {
            addFrame()
        } else {
            _currentFrame.value = curr + 1
        }

        if (shiftX != 0f || shiftY != 0f) {
            val prevFrame = curr
            val nextFrame = _currentFrame.value
            val layerId = _activeLayerId.value
            _project.update { p ->
                val prevCel = p.cels.find { it.frameIndex == prevFrame && it.layerId == layerId }
                if (prevCel != null && prevCel.strokes.isNotEmpty()) {
                    val shiftedStrokes = prevCel.strokes.map { st ->
                        st.copy(
                            id = java.util.UUID.randomUUID().toString(),
                            points = st.points.map { pt -> pt.copy(x = pt.x + shiftX, y = pt.y + shiftY) }
                        )
                    }
                    val newCels = p.cels.toMutableList()
                    val targetCel = newCels.find { it.frameIndex == nextFrame && it.layerId == layerId }
                    if (targetCel != null) {
                        val idx = newCels.indexOf(targetCel)
                        newCels[idx] = targetCel.copy(strokes = targetCel.strokes + shiftedStrokes)
                    } else {
                        newCels.add(DrawingCel(frameIndex = nextFrame, layerId = layerId, strokes = shiftedStrokes))
                    }
                    p.copy(cels = newCels)
                } else {
                    p
                }
            }
        }
        showStatus("⚡ Auto-Advanced to Frame ${_currentFrame.value + 1}")
    }

    fun toggleAutoAdvanceActive() {
        autoAdvanceController.toggleAutoAdvance()
        val enabled = autoAdvanceState.value.isEnabled
        showStatus(if (enabled) "⚡ Auto-Advance Enabled" else "Auto-Advance Paused")
    }

    fun setAutoAdvanceMode(mode: AutoAdvanceMode) {
        autoAdvanceController.setMode(mode)
        showStatus("Auto-Advance: ${mode.label}")
    }

    fun setAutoAdvanceDebounce(ms: Long) {
        autoAdvanceController.setDebounceThreshold(ms)
        showStatus("Pause Threshold: ${ms}ms")
    }

    // --- INITIAL TIMELINE CLIPS ---
    private fun createInitialTimelineClips(): List<TimelineClip> {
        return listOf(
            TimelineClip(
                id = "clip_main_sketch",
                trackId = "layer_sketch",
                name = "Drawing Cel Sequence",
                startFrame = 0,
                durationFrames = 16,
                clipType = TimelineClipType.DRAWING_CEL,
                colorArgb = 0xFF0A84FF.toInt()
            ),
            TimelineClip(
                id = "clip_video_ref",
                trackId = "track_video",
                name = "Video Rotoscoping Track",
                startFrame = 0,
                durationFrames = 16,
                clipType = TimelineClipType.VIDEO,
                colorArgb = 0xFF9333EA.toInt()
            ),
            TimelineClip(
                id = "clip_audio_foley",
                trackId = "track_audio",
                name = "Cinematic Beat & Foley",
                startFrame = 0,
                durationFrames = 16,
                clipType = TimelineClipType.AUDIO,
                colorArgb = 0xFF10B981.toInt()
            )
        )
    }

    // --- PRECISION NLE CLIP EDITING ---
    fun splitClipAtPlayhead() {
        val playhead = _currentFrame.value
        val currentClips = _timelineClips.value
        val clipToSplit = currentClips.find { playhead > it.startFrame && playhead < it.endFrame }

        if (clipToSplit != null) {
            val splitResult = ClipEditorEngine.splitClipAt(clipToSplit, playhead)
            if (splitResult != null) {
                val newClips = currentClips.toMutableList()
                val idx = newClips.indexOf(clipToSplit)
                newClips.removeAt(idx)
                newClips.add(idx, splitResult.second)
                newClips.add(idx, splitResult.first)
                _timelineClips.value = newClips
                _selectedClipId.value = splitResult.first.id
                showStatus("✂️ Split Clip at Frame ${playhead + 1}")
            }
        } else {
            showStatus("No clip under playhead to split")
        }
    }

    fun selectClip(id: String?) {
        _selectedClipId.value = id
    }

    fun deleteSelectedClip() {
        val selectedId = _selectedClipId.value ?: return
        _timelineClips.update { clips -> clips.filterNot { it.id == selectedId } }
        _selectedClipId.value = null
        showStatus("Deleted Clip")
    }

    fun toggleRippleEdit() {
        _isRippleEditActive.update { !it }
        showStatus(if (_isRippleEditActive.value) "Ripple Edit ON" else "Ripple Edit OFF")
    }

    fun toggleMagneticSnapping() {
        _isMagneticSnappingActive.update { !it }
        showStatus(if (_isMagneticSnappingActive.value) "🧲 Magnetic Snapping ON" else "Snapping OFF")
    }

    fun toggleTimeStretch() {
        _isTimeStretchActive.update { !it }
        showStatus(if (_isTimeStretchActive.value) "Time-Stretch ON (25%-400%)" else "Time-Stretch OFF")
    }

    fun trimClipIn(clipId: String, newStart: Int) {
        val clips = _timelineClips.value
        val clip = clips.find { it.id == clipId } ?: return
        val subsequent = clips.filter { it.trackId == clip.trackId && it.startFrame > clip.startFrame }
        val res = ClipEditorEngine.trimInPoint(clip, newStart, _isRippleEditActive.value, subsequent)

        val updated = clips.map { c ->
            when (c.id) {
                clip.id -> res.trimmedClip
                in res.shiftedClips.map { it.id } -> res.shiftedClips.find { it.id == c.id } ?: c
                else -> c
            }
        }
        _timelineClips.value = updated
        showStatus("Trimmed In: ${res.trimmedClip.startFrame}f (${res.deltaFrames}f)")
    }

    fun trimClipOut(clipId: String, newDuration: Int) {
        val clips = _timelineClips.value
        val clip = clips.find { it.id == clipId } ?: return
        val subsequent = clips.filter { it.trackId == clip.trackId && it.startFrame > clip.startFrame }
        val res = ClipEditorEngine.trimOutPoint(clip, newDuration, _isRippleEditActive.value, subsequent)

        val updated = clips.map { c ->
            when (c.id) {
                clip.id -> res.trimmedClip
                in res.shiftedClips.map { it.id } -> res.shiftedClips.find { it.id == c.id } ?: c
                else -> c
            }
        }
        _timelineClips.value = updated
        showStatus("Trimmed Out: ${res.trimmedClip.durationFrames}f (${res.deltaFrames}f)")
    }

    // --- CINEMATIC VFX & SHADER METHODS ---
    fun toggleCinematicVfxDialog() {
        _isCinematicVfxDialogOpen.update { !it }
    }

    fun closeCinematicVfxDialog() {
        _isCinematicVfxDialogOpen.value = false
    }

    fun updateCinematicVfxConfig(config: CinematicVfxConfig) {
        _cinematicVfxConfig.value = config
    }

    fun updateLightGizmoPosition(normX: Float, normY: Float) {
        _cinematicVfxConfig.update { cfg ->
            cfg.copy(lightGizmo = cfg.lightGizmo.copy(x = normX.coerceIn(0.05f, 0.95f), y = normY.coerceIn(0.05f, 0.95f)))
        }
    }

    // --- VELOCITY & CUBIC BEZIER GRAPH METHODS ---
    fun toggleVelocityGraphDialog() {
        _isVelocityGraphDialogOpen.update { !it }
    }

    fun closeVelocityGraphDialog() {
        _isVelocityGraphDialogOpen.value = false
    }

    fun updateVelocityCurve(curve: CubicBezierCurve) {
        _velocityCurveState.update { it.copy(curve = curve) }
    }

    fun applyVelocityCurvePreset(presetCurve: CubicBezierCurve, name: String) {
        _velocityCurveState.update { it.copy(curve = presetCurve) }
        showStatus("Applied Curve: $name")
    }

    // --- 2.5D / 3D MULTIPLANE SPACE METHODS ---
    fun toggleMultiplaneStageDialog() {
        _isMultiplaneStageDialogOpen.update { !it }
        if (_isMultiplaneStageDialogOpen.value) {
            // Sync layers into stage state
            val planes = _project.value.layers.map { l ->
                MultiplaneLayer3D(
                    layerId = l.id,
                    name = l.name,
                    posZ = l.posZ,
                    rotX = l.rotX,
                    rotY = l.rotY,
                    autoScaleCompensation = l.autoScaleComp
                )
            }
            _multiplaneStageState.update { it.copy(layerPlanes = planes, selectedLayerId = _activeLayerId.value) }
        }
    }

    fun closeMultiplaneStageDialog() {
        _isMultiplaneStageDialogOpen.value = false
    }

    fun updateMultiplaneCamera(cam: VirtualCamera3D) {
        _multiplaneStageState.update { it.copy(camera = cam) }
    }

    fun updateLayer3DDepth(layerId: String, z: Float, autoScaleComp: Boolean) {
        updateLayerTransform3D(layerId = layerId, posZ = z, autoScaleComp = autoScaleComp)
        showStatus("Layer Z-Depth: ${z.toInt()}px")
    }

    fun updateLayerTransform3D(
        layerId: String,
        offsetX: Float? = null,
        offsetY: Float? = null,
        posZ: Float? = null,
        rotX: Float? = null,
        rotY: Float? = null,
        scale: Float? = null,
        opacity: Float? = null,
        autoScaleComp: Boolean? = null
    ) {
        _project.update { p ->
            val updated = p.layers.map { l ->
                if (l.id == layerId) {
                    l.copy(
                        offsetX = offsetX ?: l.offsetX,
                        offsetY = offsetY ?: l.offsetY,
                        posZ = posZ ?: l.posZ,
                        rotX = rotX ?: l.rotX,
                        rotY = rotY ?: l.rotY,
                        scale = scale ?: l.scale,
                        opacity = opacity ?: l.opacity,
                        autoScaleComp = autoScaleComp ?: l.autoScaleComp
                    )
                } else l
            }
            p.copy(layers = updated)
        }
        _multiplaneStageState.update { st ->
            val updatedPlanes = st.layerPlanes.map { plane ->
                if (plane.layerId == layerId) {
                    plane.copy(
                        posZ = posZ ?: plane.posZ,
                        rotX = rotX ?: plane.rotX,
                        rotY = rotY ?: plane.rotY,
                        autoScaleCompensation = autoScaleComp ?: plane.autoScaleCompensation
                    )
                } else plane
            }
            st.copy(layerPlanes = updatedPlanes)
        }
    }

    fun bakeMultiplaneParallaxToProject(panDistanceX: Float = 240f, dollyDistanceZ: Float = 300f) {
        val p = _project.value
        val totalFrames = p.totalFrames.coerceAtLeast(2)
        val layersWithDepth = p.layers.filter { kotlin.math.abs(it.posZ) > 1f }

        if (layersWithDepth.isEmpty()) {
            showStatus("All layers at Z=0. Adjust layer Z-depths first to bake 3D parallax.")
            return
        }

        val updatedTracks = p.objectTracks.toMutableList()

        layersWithDepth.forEach { layer ->
            val depthRatio = (1000f / (1000f + layer.posZ).coerceAtLeast(100f))
            val layerTravelX = -panDistanceX * depthRatio

            val kfs = mutableListOf<TransformKeyframe>()
            for (f in 0 until totalFrames) {
                val t = f.toFloat() / (totalFrames - 1)
                val easeT = (kotlin.math.sin((t - 0.5) * Math.PI).toFloat() + 1f) / 2f
                val tx = (easeT - 0.5f) * layerTravelX
                val relativeZ = layer.posZ - (easeT - 0.5f) * dollyDistanceZ
                val scaleFactor = if (layer.autoScaleComp) {
                    ((1000f + layer.posZ) / (1000f + relativeZ).coerceAtLeast(100f))
                } else 1.0f

                kfs.add(
                    TransformKeyframe(
                        frameIndex = f,
                        translationX = tx,
                        translationY = 0f,
                        rotationDeg = 0f,
                        scaleX = scaleFactor,
                        scaleY = scaleFactor,
                        opacity = layer.opacity,
                        easing = EasingCurve.EASE_IN_OUT
                    )
                )
            }

            val existingIdx = updatedTracks.indexOfFirst { it.objectId == layer.id }
            val newTrack = ObjectTransformTrack(
                objectId = layer.id,
                objectName = "${layer.name} 3D Parallax",
                keyframes = kfs
            )
            if (existingIdx >= 0) {
                updatedTracks[existingIdx] = newTrack
            } else {
                updatedTracks.add(newTrack)
            }
        }

        _project.update { it.copy(objectTracks = updatedTracks) }
        showStatus("✨ Baked 3D Parallax for ${layersWithDepth.size} layers across $totalFrames frames!")
    }

    fun toggleLayerOnionSkinPin(layerId: String) {
        _project.update { p ->
            val updated = p.layers.map { l ->
                if (l.id == layerId) l.copy(onionSkinPinned = !l.onionSkinPinned) else l
            }
            p.copy(layers = updated)
        }
        val isPinned = _project.value.layers.find { it.id == layerId }?.onionSkinPinned == true
        showStatus(if (isPinned) "🧅 Layer Pinned for Onion Skinning" else "Layer Unpinned")
    }

    // --- SHIFT AND TRACE (LIGHT TABLE / OUT-OF-PEGS) ---
    fun toggleShiftAndTrace() {
        _shiftAndTraceState.update { it.copy(isActive = !it.isActive) }
        showStatus(if (_shiftAndTraceState.value.isActive) "Shift & Trace Light Table ON" else "Light Table OFF")
    }

    fun updateShiftAndTrace(offsetX: Float, offsetY: Float, rotationDeg: Float, scale: Float) {
        _shiftAndTraceState.update {
            it.copy(
                offsetX = it.offsetX + offsetX,
                offsetY = it.offsetY + offsetY,
                rotationDeg = (it.rotationDeg + rotationDeg) % 360f,
                scale = (it.scale * scale).coerceIn(0.2f, 5.0f)
            )
        }
    }

    fun resetShiftAndTracePegs() {
        _shiftAndTraceState.value = ShiftAndTraceState(isActive = _shiftAndTraceState.value.isActive)
        showStatus("Pegs Reset to Canonical Coordinates")
    }

    fun toggleShiftAndTraceSpace() {
        _shiftAndTraceState.update { it.copy(isWorldSpace = !it.isWorldSpace) }
        showStatus(if (_shiftAndTraceState.value.isWorldSpace) "Light Table: World Space" else "Light Table: Local Object Space")
    }

    // --- PSYCHOVISUAL SCENE TRANSITIONS ---
    fun toggleSceneTransitionsDialog() {
        _isSceneTransitionsDialogOpen.update { !it }
    }

    fun closeSceneTransitionsDialog() {
        _isSceneTransitionsDialogOpen.value = false
    }

    fun applyPsychovisualTransition(config: PsychovisualTransitionConfig) {
        _psychovisualTransitionConfig.value = config
        showStatus("Applied ${config.type.title} (${config.durationFrames}f)")
    }

    // --- PRO COLOR GRADING ---
    fun updateColorGradingState(state: ColorGradingState) {
        _colorGradingState.value = state
    }
    fun toggleColorGradingDialog() {
        _isColorGradingDialogOpen.update { !it }
    }
    fun closeColorGradingDialog() {
        _isColorGradingDialogOpen.value = false
    }

    // --- CHROMA KEY & MATTE TOOLS ---
    fun updateChromaKeyState(config: ChromaKeyConfig) {
        _chromaKeyState.value = config
    }
    fun toggleChromaKeyDialog() {
        _isChromaKeyDialogOpen.update { !it }
    }
    fun closeChromaKeyDialog() {
        _isChromaKeyDialogOpen.value = false
    }

    // --- ROTOSCOPING & PER-VERTEX FEATHERING ---
    fun updateRotoscopingState(state: RotoscopingState) {
        _rotoscopingState.value = state
    }
    fun toggleRotoscopingDialog() {
        _isRotoscopingDialogOpen.update { !it }
    }
    fun closeRotoscopingDialog() {
        _isRotoscopingDialogOpen.value = false
    }

    // --- 2D MOTION TRACKING ---
    fun updateMotionTrackingState(state: MotionTrackingState) {
        _motionTrackingState.value = state
    }

    // --- CURVE EDITOR & DOPE SHEET ---
    fun updateCurveEditorState(state: CurveEditorState) {
        _curveEditorState.value = state
    }
    fun toggleCurveEditorDialog() {
        val next = !_isCurveEditorDialogOpen.value
        _isCurveEditorDialogOpen.value = next
        _curveEditorState.update { it.copy(isVisible = next) }
    }
    fun closeCurveEditorDialog() {
        _isCurveEditorDialogOpen.value = false
        _curveEditorState.update { it.copy(isVisible = false) }
    }

    // --- DISTRIBUTED GPU RENDER FARM ---
    fun toggleRenderFarmDialog() {
        _isRenderFarmDialogOpen.update { !it }
    }
    fun closeRenderFarmDialog() {
        _isRenderFarmDialogOpen.value = false
    }
    fun dispatchRenderFarmJob(request: RenderJobRequest) {
        viewModelScope.launch {
            RenderFarmBridge.dispatchRenderJob(_renderFarmNode.value, request).collect { progress ->
                _renderFarmProgress.value = progress
            }
        }
    }

    fun startRenderFarmJob() {
        val proj = _project.value
        dispatchRenderFarmJob(
            RenderJobRequest(
                projectId = proj.id,
                projectName = proj.name,
                targetWidth = proj.canvasWidth,
                targetHeight = proj.canvasHeight,
                targetFps = proj.fps,
                startFrame = 0,
                endFrame = proj.totalFrames
            )
        )
    }

    fun cancelRenderFarmJob() {
        _renderFarmProgress.value = RenderProgressUpdate(state = RenderJobState.IDLE)
    }
}

data class LocalExportState(
    val isExporting: Boolean = false,
    val currentFrame: Int = 0,
    val totalFrames: Int = 0,
    val progress: Float = 0f,
    val exportedFile: java.io.File? = null,
    val galleryUri: android.net.Uri? = null,
    val error: String? = null
)


