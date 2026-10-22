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
    palmRejectionMode: PalmRejectionMode = PalmRejectionMode.AUTOMATIC,
    pressureCurve: PressureCurve = PressureCurve.LINEAR,
    adjustmentState: AdjustmentState = AdjustmentState(),
    selectionState: SelectionState = SelectionState(),
    transformState: TransformState = TransformState(),
    cinematicVfxConfig: com.example.frameforge.cinema.CinematicVfxConfig = com.example.frameforge.cinema.CinematicVfxConfig(),
    shiftAndTraceState: com.example.frameforge.cinema.ShiftAndTraceState = com.example.frameforge.cinema.ShiftAndTraceState(),
    autoAdvanceState: com.example.frameforge.cinema.AutoAdvanceState = com.example.frameforge.cinema.AutoAdvanceState(),
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFF0F1115))
            .testTag("studio_canvas_viewport")
    ) {
        val containerWidth = maxWidth.value
        val containerHeight = maxHeight.value

        fun screenToDoc(screenX: Float, screenY: Float): Offset {
            val cx = containerWidth / 2f + offsetX
            val cy = containerHeight / 2f + offsetY
            val docX = (screenX - cx) / scale + project.canvasWidth / 2f
            val docY = (screenY - cy) / scale + project.canvasHeight / 2f
            return Offset(docX, docY)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.1f, 10.0f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .pointerInteropFilter { motionEvent ->
                    val toolType = motionEvent.getToolType(0)
                    val isStylus = toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER
                    val isLargeContact = motionEvent.touchMajor > 40f

                    // Smart Palm Rejection:
                    // 1. If STYLUS_ONLY, any non-stylus is rejected.
                    // 2. If AUTOMATIC, reject if large palm contact or if a stylus stroke is already in progress,
                    //    or if multiple fingers are touching (pinching/panning).
                    val reject = when (palmRejectionMode) {
                        PalmRejectionMode.STYLUS_ONLY -> !isStylus
                        PalmRejectionMode.AUTOMATIC -> (!isStylus && (isLargeContact || isStylusStrokeInProgress || motionEvent.pointerCount > 1))
                        PalmRejectionMode.FINGER_AND_STYLUS -> motionEvent.pointerCount > 1
                    }

                    // If adjustment mode is active, dragging left/right adjusts intensity dynamically
                    if (adjustmentState.activeType != AdjustmentType.NONE) {
                        if (motionEvent.actionMasked == MotionEvent.ACTION_MOVE || motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                            val ratio = (motionEvent.x / (containerWidth.coerceAtLeast(1f))).coerceIn(0.01f, 1.0f)
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
                            } else if (motionEvent.pointerCount > 1) {
                                isTransforming = true
                                isStylusStrokeInProgress = false
                            }
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            if (isDraggingLightGizmo) {
                                isDraggingLightGizmo = false
                                return@pointerInteropFilter true
                            }
                            if (!isTransforming && currentTool != ToolType.BONE_RIG) {
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

            // Paper base
            drawRect(
                color = Color.White,
                topLeft = Offset(docLeft, docTop),
                size = Size(docWidth, docHeight)
            )

            // Transparency checkerboard
            drawCheckerboard(docLeft, docTop, docWidth, docHeight, scale)

            // Canvas border
            drawRect(
                color = Color(0xFF0A84FF), // Procreate Blue
                topLeft = Offset(docLeft, docTop),
                size = Size(docWidth, docHeight),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            drawIntoCanvas { composeCanvas ->
                val nativeCanvas = composeCanvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.clipRect(docLeft, docTop, docLeft + docWidth, docTop + docHeight)
                nativeCanvas.translate(docLeft, docTop)
                nativeCanvas.scale(scale, scale)

                // 1. Draw 4K Video Rotoscoping Underlay (if enabled and set to underlay)
                if (project.videoTrack.isEnabled && project.videoTrack.isUnderlay) {
                    RotoscopingEngine.renderRotoscopingFrame(
                        canvas = nativeCanvas,
                        docWidth = project.canvasWidth,
                        docHeight = project.canvasHeight,
                        frameIndex = currentFrame,
                        track = project.videoTrack
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
                    for (stroke in cel.strokes) {
                        DrawingEngine.renderStroke(nativeCanvas, stroke)
                    }
                }

                // 4. Draw in-progress live stroke
                if (currentStroke != null) {
                    DrawingEngine.renderStroke(nativeCanvas, currentStroke)
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
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 96.dp)
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

