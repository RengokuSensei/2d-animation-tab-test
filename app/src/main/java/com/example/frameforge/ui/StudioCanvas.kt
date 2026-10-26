package com.example.frameforge.ui

import android.graphics.Bitmap
import android.graphics.Paint
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.engine.DrawingEngine
import com.example.frameforge.engine.RotoscopingEngine
import com.example.frameforge.model.AdjustmentState
import com.example.frameforge.model.AdjustmentType
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.JointWorldTransform
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve
import com.example.frameforge.model.SelectionState
import com.example.frameforge.model.ToolType
import com.example.frameforge.model.TransformState
import androidx.compose.ui.platform.LocalContext
import com.example.frameforge.log.AppLogger
import com.example.frameforge.ui.a2.CanvasPaperPreset
import com.example.frameforge.ui.a2.FingerTouchMode
import com.example.frameforge.ui.a2.GridType
import com.example.frameforge.ui.a2.GuideType
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StudioCanvas(
    project: AnimationProject,
    currentFrame: Int,
    currentTool: ToolType,
    currentStroke: DrawingStroke?,
    onionSkinConfig: OnionSkinConfig,
    selectedBoneId: String?,
    isIkMode: Boolean,
    isCanvasLocked: Boolean = false,
    paperPreset: CanvasPaperPreset = CanvasPaperPreset.HEAVYWEIGHT,
    isGridEnabled: Boolean = false,
    gridType: GridType = GridType.NO_GRID,
    isGuideEnabled: Boolean = false,
    guideType: GuideType = GuideType.ARC,
    isMeasureEnabled: Boolean = false,
    palmRejectionMode: PalmRejectionMode = PalmRejectionMode.FINGER_AND_STYLUS,
    fingerTouchMode: FingerTouchMode = FingerTouchMode.DRAW,
    pressureCurve: PressureCurve = PressureCurve.LINEAR,
    adjustmentState: AdjustmentState = AdjustmentState(),
    selectionState: SelectionState = SelectionState(),
    transformState: TransformState = TransformState(),
    cinematicVfxConfig: com.example.frameforge.cinema.CinematicVfxConfig = com.example.frameforge.cinema.CinematicVfxConfig(),
    shiftAndTraceState: com.example.frameforge.cinema.ShiftAndTraceState = com.example.frameforge.cinema.ShiftAndTraceState(),
    autoAdvanceState: com.example.frameforge.cinema.AutoAdvanceState = com.example.frameforge.cinema.AutoAdvanceState(),
    rotoscopingState: com.example.frameforge.cinema.RotoscopingState = com.example.frameforge.cinema.RotoscopingState(),
    motionTrackingState: com.example.frameforge.cinema.MotionTrackingState = com.example.frameforge.cinema.MotionTrackingState(),
    colorGradingState: com.example.frameforge.cinema.ColorGradingState = com.example.frameforge.cinema.ColorGradingState(),
    stagedPhotoBitmap: android.graphics.Bitmap? = null,
    photoOpacity: Float = 1.0f,
    photoFlipH: Boolean = false,
    photoFlipV: Boolean = false,
    isReferenceCompanionOpen: Boolean = false,
    onStartStroke: (Float, Float, Float) -> Unit,
    onUpdateStroke: (Float, Float, Float) -> Unit,
    onUpdateStrokeBatch: (List<com.example.frameforge.model.StrokePoint>) -> Unit = { pts -> for (p in pts) onUpdateStroke(p.x, p.y, p.pressure) },
    onFinishStroke: () -> Unit,
    onSelectBone: (String?) -> Unit,
    onSolveIk: (Float, Float) -> Unit,
    onUpdateAdjustmentIntensity: (Float) -> Unit = {},
    onApplyAdjustment: () -> Unit = {},
    onCancelAdjustment: () -> Unit = {},
    onCloseReferenceCompanion: () -> Unit = {},
    onResetPegs: () -> Unit = {},
    onUpdateLightGizmoPosition: (normX: Float, normY: Float) -> Unit = { _, _ -> },
    onUpdateShiftAndTrace: (dx: Float, dy: Float, dRot: Float, dScale: Float) -> Unit = { _, _, _, _ -> },
    onToggleShiftAndTraceSpace: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var initialDistance by remember { mutableFloatStateOf(0f) }
    var initialScale by remember { mutableFloatStateOf(1f) }
    var prevMidX by remember { mutableFloatStateOf(0f) }
    var prevMidY by remember { mutableFloatStateOf(0f) }
    var isPinching by remember { mutableStateOf(false) }

    var isTransforming by remember { mutableStateOf(false) }
    var isStylusStrokeInProgress by remember { mutableStateOf(false) }
    var isDraggingLightGizmo by remember { mutableStateOf(false) }

    fun mapPressure(raw: Float): Float {
        val p = raw.coerceIn(0.05f, 1.0f)
        return when (pressureCurve) {
            PressureCurve.LINEAR -> p
            PressureCurve.SOFT -> kotlin.math.sqrt(p)
            PressureCurve.HARD -> p * p
            PressureCurve.EXPONENTIAL -> (3f * p * p - 2f * p * p * p)
        }
    }

    val currPose = remember(project.boneKeyframes, currentFrame) {
        project.boneKeyframes.find { it.frameIndex == currentFrame }?.pose
    }
    val boneTransforms = remember(project.boneRig, currPose) {
        project.boneRig.computeWorldTransforms(currPose)
    }

    var prevPanFingerX by remember { mutableFloatStateOf(0f) }
    var prevPanFingerY by remember { mutableFloatStateOf(0f) }
    var isFingerPanningActive by remember { mutableStateOf(false) }

    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFF0F1115))
            .testTag("studio_canvas_viewport")
    ) {
        val density = LocalDensity.current
        val containerWidthPx = with(density) { maxWidth.toPx() }
        val containerHeightPx = with(density) { maxHeight.toPx() }

        fun screenToDoc(screenX: Float, screenY: Float): Offset {
            val cx = containerWidthPx / 2f + offsetX
            val cy = containerHeightPx / 2f + offsetY
            val docX = (screenX - cx) / scale + project.canvasWidth / 2f
            val docY = (screenY - cy) / scale + project.canvasHeight / 2f
            return Offset(docX, docY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { motionEvent ->
                    val toolType = motionEvent.getToolType(0)
                    val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
                    val isLargeContact = motionEvent.touchMajor > 200f // Real palm resting on glass

                    val isFingerBlocked = !isStylus && (fingerTouchMode == FingerTouchMode.IGNORE)
                    val isFingerPan = !isStylus && (fingerTouchMode == FingerTouchMode.PAN)

                    // Smart Palm Rejection:
                    // 1. If STYLUS_ONLY, reject all non-stylus touches completely from drawing.
                    // 2. If AUTOMATIC, reject if large palm contact or if a stylus stroke is already in progress,
                    //    or if multiple fingers are touching.
                    // 3. If FINGER_AND_STYLUS, both finger and pen draw cleanly out-of-the-box.
                    val reject = when (palmRejectionMode) {
                        PalmRejectionMode.STYLUS_ONLY -> !isStylus
                        PalmRejectionMode.AUTOMATIC -> (!isStylus && (isLargeContact || isStylusStrokeInProgress || isFingerBlocked || motionEvent.pointerCount > 1))
                        PalmRejectionMode.FINGER_AND_STYLUS -> (!isStylus && (isLargeContact || isFingerBlocked || motionEvent.pointerCount > 1))
                    }

                    // Single-Finger Pan Mode
                    if (isFingerPan && motionEvent.pointerCount == 1) {
                        when (motionEvent.actionMasked) {
                            MotionEvent.ACTION_DOWN -> {
                                isFingerPanningActive = true
                                prevPanFingerX = motionEvent.x
                                prevPanFingerY = motionEvent.y
                                return@pointerInteropFilter true
                            }
                            MotionEvent.ACTION_MOVE -> {
                                if (isFingerPanningActive && !isCanvasLocked) {
                                    val dx = motionEvent.x - prevPanFingerX
                                    val dy = motionEvent.y - prevPanFingerY
                                    offsetX += dx
                                    offsetY += dy
                                    prevPanFingerX = motionEvent.x
                                    prevPanFingerY = motionEvent.y
                                }
                                return@pointerInteropFilter true
                            }
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                isFingerPanningActive = false
                                return@pointerInteropFilter true
                            }
                        }
                    }

                    // Two-Finger Pinch-Zoom & Pan Navigation
                    if (motionEvent.pointerCount >= 2) {
                        val p0x = motionEvent.getX(0)
                        val p0y = motionEvent.getY(0)
                        val p1x = motionEvent.getX(1)
                        val p1y = motionEvent.getY(1)
                        val dist = kotlin.math.hypot(p1x - p0x, p1y - p0y)
                        val midX = (p0x + p1x) / 2f
                        val midY = (p0y + p1y) / 2f

                        if (motionEvent.actionMasked == MotionEvent.ACTION_POINTER_DOWN || !isPinching) {
                            isPinching = true
                            initialDistance = dist
                            initialScale = scale
                            prevMidX = midX
                            prevMidY = midY
                            if (isStylusStrokeInProgress) {
                                onFinishStroke()
                            }
                            isStylusStrokeInProgress = false
                        } else if (motionEvent.actionMasked == MotionEvent.ACTION_MOVE && isPinching) {
                            if (!isCanvasLocked) {
                                if (initialDistance > 10f && dist > 10f) {
                                    val factor = dist / initialDistance
                                    scale = (initialScale * factor).coerceIn(0.1f, 10.0f)
                                }
                                val panX = midX - prevMidX
                                val panY = midY - prevMidY
                                offsetX += panX
                                offsetY += panY
                            }
                            prevMidX = midX
                            prevMidY = midY
                        }
                        if (motionEvent.actionMasked == MotionEvent.ACTION_POINTER_UP || motionEvent.actionMasked == MotionEvent.ACTION_UP) {
                            isPinching = false
                        }
                        return@pointerInteropFilter true
                    }

                    isPinching = false

                    // If adjustment mode is active, dragging left/right adjusts intensity dynamically
                    if (adjustmentState.activeType != AdjustmentType.NONE) {
                        if (motionEvent.actionMasked == MotionEvent.ACTION_MOVE || motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                            val ratio = (motionEvent.x / (containerWidthPx.coerceAtLeast(1f))).coerceIn(0.01f, 1.0f)
                            onUpdateAdjustmentIntensity(ratio)
                        }
                        return@pointerInteropFilter true
                    }

                    when (motionEvent.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            if (motionEvent.pointerCount == 1 && !reject) {
                                isTransforming = false
                                val docPt = screenToDoc(motionEvent.x, motionEvent.y)

                                // Check interactive Light Source Sun Gizmo touch
                                if (cinematicVfxConfig.lightGizmo.isEnabled) {
                                    val lightDocX = project.canvasWidth * cinematicVfxConfig.lightGizmo.x
                                    val lightDocY = project.canvasHeight * cinematicVfxConfig.lightGizmo.y
                                    val dist = kotlin.math.hypot(docPt.x - lightDocX, docPt.y - lightDocY)
                                    if (dist <= 45f / scale) {
                                        isDraggingLightGizmo = true
                                        return@pointerInteropFilter true
                                    }
                                }

                                if (isStylus) isStylusStrokeInProgress = true
                                val pressure = mapPressure(motionEvent.pressure)

                                if (currentTool == ToolType.BONE_RIG) {
                                    val tappedBone = findBoneNear(docPt.x, docPt.y, boneTransforms, 40f / scale)
                                    if (tappedBone != null) {
                                        onSelectBone(tappedBone)
                                    } else if (isIkMode) {
                                        onSolveIk(docPt.x, docPt.y)
                                    }
                                } else {
                                    onStartStroke(docPt.x, docPt.y, pressure)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val docPt = screenToDoc(motionEvent.x, motionEvent.y)
                            if (isDraggingLightGizmo) {
                                val normX = (docPt.x / project.canvasWidth).coerceIn(0.05f, 0.95f)
                                val normY = (docPt.y / project.canvasHeight).coerceIn(0.05f, 0.95f)
                                onUpdateLightGizmoPosition(normX, normY)
                                return@pointerInteropFilter true
                            }

                            if (motionEvent.pointerCount == 1 && !isTransforming && !reject) {
                                if (currentTool == ToolType.BONE_RIG && isIkMode) {
                                    onSolveIk(docPt.x, docPt.y)
                                } else if (currentTool != ToolType.BONE_RIG) {
                                    val tiltRad = motionEvent.getAxisValue(MotionEvent.AXIS_TILT)
                                    val tiltDeg = Math.toDegrees(tiltRad.toDouble()).toFloat()

                                    // 144Hz Stylus Precision: Extract all intermediate historical samples
                                    val batch = mutableListOf<com.example.frameforge.model.StrokePoint>()
                                    val historyCount = motionEvent.historySize
                                    for (h in 0 until historyCount) {
                                        val hDocPt = screenToDoc(motionEvent.getHistoricalX(h), motionEvent.getHistoricalY(h))
                                        val hPressure = mapPressure(motionEvent.getHistoricalPressure(h))
                                        batch.add(com.example.frameforge.model.StrokePoint(hDocPt.x, hDocPt.y, hPressure, tiltDeg = tiltDeg))
                                    }
                                    val currPressure = mapPressure(motionEvent.pressure)
                                    batch.add(com.example.frameforge.model.StrokePoint(docPt.x, docPt.y, currPressure, tiltDeg = tiltDeg))

                                    onUpdateStrokeBatch(batch)
                                }
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (isDraggingLightGizmo) {
                                isDraggingLightGizmo = false
                                return@pointerInteropFilter true
                            }
                            if (!isTransforming && currentTool != ToolType.BONE_RIG && !reject) {
                                onFinishStroke()
                            }
                            isTransforming = false
                            isStylusStrokeInProgress = false
                            true
                        }
                        else -> false
                    }
                }
        ) {
            val cx = size.width / 2f + offsetX
            val cy = size.height / 2f + offsetY

            val docLeft = cx - (project.canvasWidth * scale) / 2f
            val docTop = cy - (project.canvasHeight * scale) / 2f
            val docWidth = project.canvasWidth * scale
            val docHeight = project.canvasHeight * scale

            // Drop shadow behind paper
            drawRect(
                color = Color(0x88000000),
                topLeft = Offset(docLeft + 6f * scale, docTop + 10f * scale),
                size = Size(docWidth, docHeight)
            )

            // Authentic Concepts Paper base
            drawRect(
                color = paperPreset.color,
                topLeft = Offset(docLeft, docTop),
                size = Size(docWidth, docHeight)
            )

            // Canvas border
            drawRect(
                color = if (paperPreset == CanvasPaperPreset.DARK_CINEMA) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0x33000000),
                topLeft = Offset(docLeft, docTop),
                size = Size(docWidth, docHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )

            drawIntoCanvas { composeCanvas ->
                val nativeCanvas = composeCanvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.clipRect(docLeft, docTop, docLeft + docWidth, docTop + docHeight)
                nativeCanvas.translate(docLeft, docTop)
                nativeCanvas.scale(scale, scale)

                // 0. Draw Concepts Precision Grid (Dot, Square, Isometric)
                if (isGridEnabled) {
                    DrawingEngine.renderGrid(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth.toFloat(),
                        docHeight = project.canvasHeight.toFloat(),
                        gridType = gridType,
                        paperPreset = paperPreset
                    )
                }

                // Apply live hardware color grading filter across canvas
                val isColorGradingActive = colorGradingState.isEnabled
                if (isColorGradingActive) {
                    val gradePaint = android.graphics.Paint().apply {
                        colorFilter = android.graphics.ColorMatrixColorFilter(
                            com.example.frameforge.cinema.ColorGradingEngine.buildColorMatrix(colorGradingState)
                        )
                    }
                    nativeCanvas.saveLayer(null, gradePaint)
                }

                // 0. Draw Staged Photo Reference (if active)
                if (stagedPhotoBitmap != null && !stagedPhotoBitmap.isRecycled) {
                    nativeCanvas.save()
                    val photoPaint = android.graphics.Paint().apply {
                        alpha = (photoOpacity * 255).toInt().coerceIn(10, 255)
                        isFilterBitmap = true
                    }
                    val docW = project.canvasWidth.toFloat()
                    val docH = project.canvasHeight.toFloat()
                    val scaleFactor = kotlin.math.min(docW / stagedPhotoBitmap.width, docH / stagedPhotoBitmap.height)
                    val pw = stagedPhotoBitmap.width * scaleFactor
                    val ph = stagedPhotoBitmap.height * scaleFactor
                    val left = (docW - pw) / 2f
                    val top = (docH - ph) / 2f

                    if (photoFlipH || photoFlipV) {
                        nativeCanvas.scale(
                            if (photoFlipH) -1f else 1f,
                            if (photoFlipV) -1f else 1f,
                            docW / 2f,
                            docH / 2f
                        )
                    }
                    nativeCanvas.drawBitmap(
                        stagedPhotoBitmap,
                        null,
                        android.graphics.RectF(left, top, left + pw, top + ph),
                        photoPaint
                    )
                    nativeCanvas.restore()
                }

                // 1. Draw 4K Video Rotoscoping & Stage Underlay
                if (project.videoTrack.isEnabled && (project.videoTrack.isUnderlay || project.videoTrack.pipTransform.isUnderlayMode)) {
                    RotoscopingEngine.renderVideoStagePiP(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        totalFrames = project.totalFrames,
                        fps = project.fps,
                        track = project.videoTrack,
                        context = context
                    )
                }

                // 1b. Draw Multi-Page PDF Document Track (if enabled and set to underlay)
                if (project.pdfTrack.isEnabled && project.pdfTrack.isUnderlay) {
                    DrawingEngine.renderPdfTrackFrame(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        totalFrames = project.totalFrames,
                        track = project.pdfTrack
                    )
                }

                // 2. Draw Onion Skins
                if (onionSkinConfig.enabled) {
                    DrawingEngine.renderOnionSkins(
                        canvas = nativeCanvas,
                        width = project.canvasWidth,
                        height = project.canvasHeight,
                        currentFrame = currentFrame,
                        totalFrames = project.totalFrames,
                        layers = project.layers,
                        cels = project.cels,
                        config = onionSkinConfig,
                        shiftAndTrace = shiftAndTraceState
                    )
                }

                // 3. Draw Committed active frame cels
                val frameCels = project.cels.filter { it.frameIndex == currentFrame }
                val celsByLayer = frameCels.associateBy { it.layerId }

                for (layer in project.layers) {
                    if (!layer.isVisible) continue
                    val cel = celsByLayer[layer.id] ?: continue

                    val track = project.objectTracks.find { it.objectId == layer.id }
                    val kf = track?.keyframes?.find { it.frameIndex == currentFrame }
                    val tx = (kf?.translationX ?: 0f) + layer.offsetX
                    val ty = (kf?.translationY ?: 0f) + layer.offsetY
                    val rot = kf?.rotationDeg ?: 0f
                    val sx = (kf?.scaleX ?: 1f) * layer.scale
                    val sy = (kf?.scaleY ?: 1f) * layer.scale
                    val hasTransform = (tx != 0f || ty != 0f || rot != 0f || sx != 1f || sy != 1f)

                    if (hasTransform) {
                        nativeCanvas.save()
                        nativeCanvas.translate(tx, ty)
                        if (rot != 0f) {
                            nativeCanvas.rotate(rot, project.canvasWidth / 2f, project.canvasHeight / 2f)
                        }
                        if (sx != 1f || sy != 1f) {
                            nativeCanvas.scale(sx, sy, project.canvasWidth / 2f, project.canvasHeight / 2f)
                        }
                    }

                    for (stroke in cel.strokes) {
                        DrawingEngine.renderStroke(nativeCanvas, stroke)
                        if (isMeasureEnabled) {
                            DrawingEngine.renderMeasureTag(nativeCanvas, stroke)
                        }
                    }

                    if (hasTransform) {
                        nativeCanvas.restore()
                    }
                }

                // 4. Draw in-progress live stroke
                if (currentStroke != null) {
                    DrawingEngine.renderStroke(nativeCanvas, currentStroke)
                    if (isMeasureEnabled) {
                        DrawingEngine.renderMeasureTag(nativeCanvas, currentStroke)
                    }
                }

                // 5. Draw 4K Video Rotoscoping Overlay (if enabled and set to overlay)
                if (project.videoTrack.isEnabled && !project.videoTrack.isUnderlay) {
                    RotoscopingEngine.renderRotoscopingFrame(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        track = project.videoTrack
                    )
                }

                // 5b. Draw Multi-Page PDF Document Track (if enabled and set to overlay)
                if (project.pdfTrack.isEnabled && !project.pdfTrack.isUnderlay) {
                    DrawingEngine.renderPdfTrackFrame(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        totalFrames = project.totalFrames,
                        track = project.pdfTrack
                    )
                }

                // 6. Draw Rigging Skeleton Overlay (when Bone tool active)
                if (currentTool == ToolType.BONE_RIG && boneTransforms.isNotEmpty()) {
                    drawSkeletonOverlay(nativeCanvas, boneTransforms, selectedBoneId)
                }

                // 7. Draw Selection Marching Ants (if selection active)
                if (selectionState.isActive) {
                    DrawingEngine.renderSelectionOverlay(nativeCanvas, selectionState, scale)
                }

                // 8. Draw Transform Interactive Cage
                if (transformState.isActive) {
                    DrawingEngine.renderTransformCage(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        state = transformState,
                        scale = scale
                    )
                }

                // 8b. Draw Concepts Precision Guide Overlay (Line, Arc, Ellipse, Rectangle)
                if (isGuideEnabled) {
                    DrawingEngine.renderGuideOverlay(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth.toFloat(),
                        docHeight = project.canvasHeight.toFloat(),
                        guideType = guideType
                    )
                }

                // 9. Draw Cinematic Rim-Light & Cast Shadow
                if (cinematicVfxConfig.lightGizmo.isEnabled) {
                    com.example.frameforge.cinema.CinematicShaderPipeline.renderRimLightAndShadow(
                        canvas = nativeCanvas,
                        width = project.canvasWidth.toFloat(),
                        height = project.canvasHeight.toFloat(),
                        gizmo = cinematicVfxConfig.lightGizmo
                    )
                }

                // 10. Draw Volumetric God Rays
                if (cinematicVfxConfig.volumetricRays) {
                    com.example.frameforge.cinema.CinematicShaderPipeline.renderVolumetricGodRays(
                        canvas = nativeCanvas,
                        width = project.canvasWidth.toFloat(),
                        height = project.canvasHeight.toFloat(),
                        lightX = cinematicVfxConfig.lightGizmo.x,
                        lightY = cinematicVfxConfig.lightGizmo.y,
                        colorArgb = cinematicVfxConfig.lightGizmo.colorArgb,
                        intensity = cinematicVfxConfig.lightGizmo.intensity,
                        rayCount = cinematicVfxConfig.godRayCount
                    )
                }

                // 11. Draw Atmospheric Weather & Particles
                if (cinematicVfxConfig.particles != com.example.frameforge.cinema.AtmosphericParticleType.NONE) {
                    com.example.frameforge.cinema.CinematicShaderPipeline.renderAtmosphericParticles(
                        canvas = nativeCanvas,
                        width = project.canvasWidth.toFloat(),
                        height = project.canvasHeight.toFloat(),
                        frameIndex = currentFrame,
                        type = cinematicVfxConfig.particles,
                        count = cinematicVfxConfig.particleDensity
                    )
                }

                // 12. Draw Cinema Letterbox
                if (cinematicVfxConfig.letterbox != com.example.frameforge.cinema.CinemaAspectLetterbox.NONE) {
                    com.example.frameforge.cinema.CinematicShaderPipeline.renderCinemaLetterbox(
                        canvas = nativeCanvas,
                        width = project.canvasWidth.toFloat(),
                        height = project.canvasHeight.toFloat(),
                        letterbox = cinematicVfxConfig.letterbox
                    )
                }

                // 13. Draw Rotoscoping Splines & Per-Vertex Feather Guides
                if (rotoscopingState.isEnabled && rotoscopingState.showGuideSplines) {
                    for (shape in rotoscopingState.shapes) {
                        val vertices = shape.getVerticesForFrame(currentFrame)
                        com.example.frameforge.cinema.RotoscopingSplineEngine.renderRotoGuides(
                            canvas = nativeCanvas,
                            shape = shape,
                            vertices = vertices,
                            selectedVertexIndex = rotoscopingState.selectedVertexIndex,
                            isFeatherToolActive = rotoscopingState.isFeatherToolActive
                        )
                    }
                }

                // 14. Draw 2D Motion Tracking HUD
                if (motionTrackingState.isEnabled) {
                    for (tracker in motionTrackingState.trackers) {
                        com.example.frameforge.cinema.MotionTrackingEngine.renderTrackerHud(
                            canvas = nativeCanvas,
                            tracker = tracker,
                            currentFrame = currentFrame
                        )
                    }
                }

                // 15. Draw Video Stage PiP Floating Window (A2_studio.html specification)
                if (project.videoTrack.isEnabled && !project.videoTrack.pipTransform.isUnderlayMode && project.videoTrack.pipTransform.isVisible) {
                    RotoscopingEngine.renderVideoStagePiP(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        totalFrames = project.totalFrames,
                        fps = project.fps,
                        track = project.videoTrack,
                        context = context
                    )
                }

                if (isColorGradingActive) {
                    nativeCanvas.restore()
                }

                nativeCanvas.restore()
            }
        }

        // --- TOP ADJUSTMENT LIVE SLIDER HUD ---
        if (adjustmentState.activeType != AdjustmentType.NONE) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .width(420.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF018181B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = adjustmentState.activeType.label,
                            color = Color(0xFF0A84FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${(adjustmentState.intensity * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { adjustmentState.intensity },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF0A84FF),
                        trackColor = Color(0x33FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slide stylus left/right on canvas",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Row {
                            IconButton(onClick = onCancelAdjustment, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onApplyAdjustment, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Check, contentDescription = "Apply", tint = Color(0xFF0A84FF))
                            }
                        }
                    }
                }
            }
        }

        // --- REFERENCE COMPANION PIP WINDOW ---
        if (isReferenceCompanionOpen) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 64.dp, end = 20.dp)
                    .size(240.dp, 160.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF218181B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44FFFFFF))
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reference View", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Reference",
                            tint = Color.LightGray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onCloseReferenceCompanion() }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF222630), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Frame ${currentFrame + 1} / ${project.totalFrames}\n${project.canvasWidth}×${project.canvasHeight}",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- SHIFT AND TRACE (LIGHT TABLE / OUT-OF-PEGS) GIZMO HUD ---
        if (shiftAndTraceState.isActive) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 68.dp, top = 56.dp)
                    .testTag("shift_trace_hud"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xF218181B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0A84FF))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "🧅 Light Table",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Space toggle
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onToggleShiftAndTraceSpace() }
                            .background(if (shiftAndTraceState.isWorldSpace) Color(0x3300E5FF) else Color(0x33FF9500))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text(if (shiftAndTraceState.isWorldSpace) "World" else "Local", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Rotate -15
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUpdateShiftAndTrace(0f, 0f, -15f, 1f) }
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text("⤹ -15°", color = Color.White, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Rotate +15
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUpdateShiftAndTrace(0f, 0f, 15f, 1f) }
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text("⤸ +15°", color = Color.White, fontSize = 9.sp)
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Zoom -
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUpdateShiftAndTrace(0f, 0f, 0f, 0.9f) }
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text("－", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Zoom +
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onUpdateShiftAndTrace(0f, 0f, 0f, 1.1f) }
                            .background(Color(0x22FFFFFF))
                            .padding(horizontal = 5.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text("＋", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Reset Pegs
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onResetPegs() }
                            .background(Color(0xFF0A84FF))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        color = Color.Transparent
                    ) {
                        Text("Reset Pegs", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- AUTO-ADVANCE RADIAL COUNTDOWN INDICATOR ---
        if (autoAdvanceState.isEnabled && autoAdvanceState.countdownProgress > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp)
                    .background(Color(0x99000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { autoAdvanceState.countdownProgress },
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFFFB300),
                    trackColor = Color(0x33FFFFFF),
                    strokeWidth = 3.dp
                )
                Text("⚡", fontSize = 13.sp)
            }
        }

        // Floating Quick Reset Canvas button in bottom right
        IconButton(
            onClick = {
                scale = 1.0f
                offsetX = 0f
                offsetY = 0f
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp)
                .size(36.dp)
                .background(Color(0xBB1E293B), CircleShape)
                .testTag("fit_canvas_button")
        ) {
            Icon(Icons.Default.CropFree, contentDescription = "Fit Canvas", tint = Color(0xFF0A84FF), modifier = Modifier.size(18.dp))
        }
    }
}

private fun DrawScope.drawCheckerboard(left: Float, top: Float, width: Float, height: Float, scale: Float) {
    val checkSize = (16f * scale).coerceIn(8f, 32f)
    var y = top
    var row = 0
    while (y < top + height) {
        var x = left
        var col = 0
        while (x < left + width) {
            if ((row + col) % 2 == 1) {
                val rw = (checkSize).coerceAtMost(left + width - x)
                val rh = (checkSize).coerceAtMost(top + height - y)
                drawRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(x, y),
                    size = Size(rw, rh)
                )
            }
            x += checkSize
            col++
        }
        y += checkSize
        row++
    }
}

private fun drawSkeletonOverlay(
    canvas: android.graphics.Canvas,
    transforms: Map<String, JointWorldTransform>,
    selectedId: String?
) {
    val bonePaint = Paint().apply {
        color = android.graphics.Color.argb(200, 0, 229, 255) // Cyan
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    val jointPaint = Paint().apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    val selectedJointPaint = Paint().apply {
        color = android.graphics.Color.rgb(255, 152, 0) // Amber
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    transforms.values.forEach { jt ->
        canvas.drawLine(jt.startX, jt.startY, jt.endX, jt.endY, bonePaint)

        val isSelected = jt.boneId == selectedId
        val p = if (isSelected) selectedJointPaint else jointPaint
        canvas.drawCircle(jt.startX, jt.startY, 8f, p)
        canvas.drawCircle(jt.endX, jt.endY, 6f, p)
    }
}

private fun findBoneNear(
    x: Float,
    y: Float,
    transforms: Map<String, JointWorldTransform>,
    threshold: Float
): String? {
    transforms.values.forEach { jt ->
        val d1 = Math.hypot((x - jt.startX).toDouble(), (y - jt.startY).toDouble())
        val d2 = Math.hypot((x - jt.endX).toDouble(), (y - jt.endY).toDouble())
        if (d1 < threshold || d2 < threshold) {
            return jt.boneId
        }
    }
    return null
}

