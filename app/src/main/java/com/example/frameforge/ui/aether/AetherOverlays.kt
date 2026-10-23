package com.example.frameforge.ui.aether

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.model.AnimationNodeGraph
import com.example.frameforge.model.AnimationNodeType
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// --- 1. NODE COMPOSITOR OVERLAY ---
@Composable
fun AetherNodesOverlay(
    graph: AnimationNodeGraph,
    onClose: () -> Unit,
    onAddNode: (AnimationNodeType) -> Unit,
    onUpdateNodePosition: (String, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0x33000000))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = "Compositor",
                        tint = AetherColors.Purple,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NODE COMPOSITOR GRAPH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "• Drag nodes or toggle FX bypass",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33C084FC))
                            .clickable { onAddNode(AnimationNodeType.COLOR_FX) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Node",
                            tint = AetherColors.Purple,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Add FX Node",
                            color = AetherColors.Purple,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = AetherColors.Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done · Draw",
                            color = AetherColors.Ink950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Node Graph Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AetherColors.Ink950)
            ) {
                // Background Dot Grid
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 24.dp.toPx()
                    val cols = (size.width / step).toInt()
                    val rows = (size.height / step).toInt()
                    for (c in 0..cols) {
                        for (r in 0..rows) {
                            drawCircle(
                                color = Color(0x1AFFFFFF),
                                radius = 1.2f,
                                center = Offset(c * step, r * step)
                            )
                        }
                    }

                    // Draw Bezier Connection Wires between Nodes
                    graph.connections.forEach { conn ->
                        val fromNode = graph.nodes.find { it.id == conn.fromNodeId }
                        val toNode = graph.nodes.find { it.id == conn.toNodeId }
                        if (fromNode != null && toNode != null) {
                            val startX = fromNode.x + 190.dp.toPx()
                            val startY = fromNode.y + 40.dp.toPx()
                            val endX = toNode.x
                            val endY = toNode.y + 40.dp.toPx()

                            val dx = kotlin.math.abs(endX - startX) * 0.5f
                            val path = Path().apply {
                                moveTo(startX, startY)
                                cubicTo(
                                    startX + dx, startY,
                                    endX - dx, endY,
                                    endX, endY
                                )
                            }
                            drawPath(
                                path = path,
                                color = AetherColors.Accent,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }

                // Node Cards
                graph.nodes.forEach { node ->
                    var offsetX by remember(node.x) { mutableFloatStateOf(node.x) }
                    var offsetY by remember(node.y) { mutableFloatStateOf(node.y) }

                    val headerColor = when (node.nodeType) {
                        AnimationNodeType.LAYER_SOURCE -> AetherColors.Accent
                        AnimationNodeType.COLOR_FX -> AetherColors.Mint
                        AnimationNodeType.TRANSFORM -> AetherColors.Purple
                        AnimationNodeType.COMPOSITE_OUTPUT -> AetherColors.Amber
                        else -> AetherColors.Rose
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                            .width(190.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(AetherColors.Ink900)
                            .border(1.dp, headerColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .pointerInput(node.id) {
                                detectDragGestures(
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetX += dragAmount.x
                                        offsetY += dragAmount.y
                                    },
                                    onDragEnd = {
                                        onUpdateNodePosition(node.id, offsetX, offsetY)
                                    }
                                )
                            }
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = node.title,
                                    color = headerColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(headerColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = node.nodeType.name.take(3),
                                        color = headerColor,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0x1AFFFFFF))
                            )

                            Text(
                                text = node.parameters.entries.take(2).joinToString(" · ") { "${it.key}: ${it.value}" }
                                    .ifEmpty { "Active · Normal" },
                                color = Color(0xFFA1A1AA),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 2. VELOCITY CURVE & EASING OVERLAY ---
@Composable
fun AetherVelocityOverlay(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cp1X by remember { mutableFloatStateOf(0.35f) }
    var cp1Y by remember { mutableFloatStateOf(0.80f) }
    var cp2X by remember { mutableFloatStateOf(0.65f) }
    var cp2Y by remember { mutableFloatStateOf(0.15f) }

    val coroutineScope = rememberCoroutineScope()
    val orbAnimOffset = remember { Animatable(0f) }

    GlassPill(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0x33000000))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = "Velocity",
                        tint = AetherColors.Amber,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VELOCITY CURVE & FRAME PACING",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PresetButton("Ease In-Out") { cp1X = 0.42f; cp1Y = 0.95f; cp2X = 0.58f; cp2Y = 0.05f }
                    PresetButton("Snap Overshoot") { cp1X = 0.15f; cp1Y = 0.02f; cp2X = 0.85f; cp2Y = 0.98f }
                    PresetButton("Linear") { cp1X = 0.33f; cp1Y = 0.67f; cp2X = 0.67f; cp2Y = 0.33f }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = AetherColors.Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done · Draw",
                            color = AetherColors.Ink950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Curve Editor & Preview Strip
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Bezier Graph Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AetherColors.Ink950)
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val pad = 36f
                                    val w = size.width - pad * 2
                                    val h = size.height - pad * 2
                                    val nx = ((change.position.x - pad) / w).coerceIn(0f, 1f)
                                    val ny = ((change.position.y - pad) / h).coerceIn(0f, 1f)

                                    val d1 = kotlin.math.hypot(nx - cp1X, ny - cp1Y)
                                    val d2 = kotlin.math.hypot(nx - cp2X, ny - cp2Y)
                                    if (d1 < d2) {
                                        cp1X = nx
                                        cp1Y = ny
                                    } else {
                                        cp2X = nx
                                        cp2Y = ny
                                    }
                                }
                            }
                    ) {
                        val pad = 36f
                        val w = size.width - pad * 2
                        val h = size.height - pad * 2

                        val start = Offset(pad, size.height - pad)
                        val end = Offset(size.width - pad, pad)
                        val h1 = Offset(pad + cp1X * w, pad + cp1Y * h)
                        val h2 = Offset(pad + cp2X * w, pad + cp2Y * h)

                        // Control lines
                        drawLine(
                            color = AetherColors.Amber.copy(alpha = 0.5f),
                            start = start,
                            end = h1,
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = AetherColors.Amber.copy(alpha = 0.5f),
                            start = end,
                            end = h2,
                            strokeWidth = 2.dp.toPx()
                        )

                        // Bezier Spline
                        val spline = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(h1.x, h1.y, h2.x, h2.y, end.x, end.y)
                        }
                        drawPath(spline, color = AetherColors.Accent, style = Stroke(width = 4.dp.toPx()))

                        // Amber Handle Points
                        drawCircle(color = AetherColors.Amber, radius = 9.dp.toPx(), center = h1)
                        drawCircle(color = AetherColors.Amber, radius = 9.dp.toPx(), center = h2)
                    }

                    // Peak speed badge in top corner
                    Text(
                        text = "Peak: ${(kotlin.math.abs(cp1Y - cp2Y) * 1600 + 400).toInt()} px/s",
                        color = AetherColors.Amber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    )
                }

                // Live Easing Preview Orb Card
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AetherColors.Ink950)
                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "LIVE MOTION PREVIEW",
                            color = Color(0xFFA1A1AA),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Preview how custom bezier curves sculpt kinetic bounce.",
                            color = Color(0xFF71717A),
                            fontSize = 10.sp
                        )
                    }

                    // Preview Track with Orb
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AetherColors.Ink900),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = (orbAnimOffset.value * 70).dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(AetherColors.Coral, AetherColors.Amber)
                                    )
                                )
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                orbAnimOffset.snapTo(-1f)
                                orbAnimOffset.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = 750,
                                        easing = CubicBezierEasing(cp1X, 1f - cp1Y, cp2X, 1f - cp2Y)
                                    )
                                )
                                orbAnimOffset.animateTo(0f, tween(300))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AetherColors.Amber),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test",
                            tint = AetherColors.Ink950,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pulse Motion Test",
                            color = AetherColors.Ink950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x1AFFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFD4D4D8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- 3. COLOR GRADING OVERLAY ---
@Composable
fun AetherGradeOverlay(
    state: ColorGradingState,
    onStateChange: (ColorGradingState) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Grade",
                        tint = AetherColors.Mint,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LIVE STUDIO COLOR GRADE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF))
                            .clickable { onStateChange(ColorGradingState()) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Reset",
                            color = Color(0xFFD4D4D8),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = AetherColors.Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done · Draw",
                            color = AetherColors.Ink950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3-Way Wheels + Film Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 3 Color Wheels
                Row(
                    modifier = Modifier
                        .weight(1.2f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22000000))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AetherColorWheel("LIFT (Shadows)", AetherColors.Accent)
                    AetherColorWheel("GAMMA (Mids)", AetherColors.Mint)
                    AetherColorWheel("GAIN (Highs)", AetherColors.Amber)
                }

                // Film Sliders
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GradeSlider(
                        label = "Contrast",
                        value = state.contrast,
                        range = 0.5f..1.8f,
                        onValueChange = { onStateChange(state.copy(contrast = it)) }
                    )
                    GradeSlider(
                        label = "Saturation",
                        value = state.saturation,
                        range = 0.0f..2.0f,
                        onValueChange = { onStateChange(state.copy(saturation = it)) }
                    )
                    GradeSlider(
                        label = "Kelvin Warmth",
                        value = state.colorTemperature,
                        range = 2500f..9500f,
                        unit = "K",
                        onValueChange = { onStateChange(state.copy(colorTemperature = it)) }
                    )
                    GradeSlider(
                        label = "Exposure Offset",
                        value = state.exposure,
                        range = -2.0f..2.0f,
                        onValueChange = { onStateChange(state.copy(exposure = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AetherColorWheel(label: String, tint: Color) {
    var puckX by remember { mutableFloatStateOf(0f) }
    var puckY by remember { mutableFloatStateOf(0f) }

    Column(
        horizontalAlignment = Alignment.CenterVertically,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = Color(0xFFA1A1AA),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(AetherColors.Ink900)
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val maxR = 26f
                        val nx = puckX + dragAmount.x
                        val ny = puckY + dragAmount.y
                        val dist = sqrt(nx * nx + ny * ny)
                        if (dist <= maxR) {
                            puckX = nx
                            puckY = ny
                        } else {
                            puckX = (nx / dist) * maxR
                            puckY = (ny / dist) * maxR
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(puckX.toInt(), puckY.toInt()) }
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, tint, CircleShape)
            )
        }
    }
}

@Composable
private fun GradeSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String = "",
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color(0xFFA1A1AA), fontSize = 10.sp)
            Text(
                text = "${(value * 10).toInt() / 10f}$unit",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AetherColors.Mint,
                inactiveTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}

// --- 4. 3D MULTIPLANE STAGE OVERLAY ---
@Composable
fun AetherStage3DOverlay(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zSpread by remember { mutableFloatStateOf(110f) }
    var orbitAngle by remember { mutableFloatStateOf(25f) }

    GlassPill(
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color(0x33000000))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "3D Stage",
                        tint = AetherColors.Rose,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "3D MULTIPLANE DEPTH STAGE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "• Orbit camera around live layers in 3D perspective",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Z-Spread", color = Color(0xFFA1A1AA), fontSize = 11.sp)
                        Slider(
                            value = zSpread,
                            onValueChange = { zSpread = it },
                            valueRange = 40f..220f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = AetherColors.Rose
                            ),
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = AetherColors.Accent),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done · Draw",
                            color = AetherColors.Ink950,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3D Perspective Canvas Simulation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AetherColors.Ink950)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            orbitAngle = (orbitAngle + dragAmount.x * 0.2f).coerceIn(-60f, 60f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val rad = Math.toRadians(orbitAngle.toDouble())

                    // Draw 3 Layer Planes in 3D Perspective
                    val layerColors = listOf(
                        AetherColors.Mint.copy(alpha = 0.5f) to "Layer 0: Rough & Shadow",
                        AetherColors.Amber.copy(alpha = 0.5f) to "Layer 1: Color Flats",
                        AetherColors.Accent.copy(alpha = 0.7f) to "Layer 2: Ink Lineart"
                    )

                    layerColors.forEachIndexed { i, (col, _) ->
                        val z = (i - 1) * zSpread
                        val depthScale = 1.0f + (z / 600f)
                        val pw = 360f * depthScale
                        val ph = 200f * depthScale
                        val px = cx + (z * sin(rad)).toFloat()
                        val py = cy - 20f

                        drawRect(
                            color = Color(0x3312151E),
                            topLeft = Offset(px - pw / 2, py - ph / 2),
                            size = androidx.compose.ui.geometry.Size(pw, ph)
                        )
                        drawRect(
                            color = col,
                            topLeft = Offset(px - pw / 2, py - ph / 2),
                            size = androidx.compose.ui.geometry.Size(pw, ph),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Text(
                    text = "Drag horizontally to orbit 3D multiplane camera",
                    color = Color(0x66FFFFFF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}
