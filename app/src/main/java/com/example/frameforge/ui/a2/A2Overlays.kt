package com.example.frameforge.ui.a2

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.ColorGradingEngine
import com.example.frameforge.cinema.ColorGradingPreset
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.cinema.ColorWheelValues
import com.example.frameforge.cinema.CubicBezierCurve
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationNodeGraph
import com.example.frameforge.model.AnimationNodeType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// =========================================================================
// OVERLAY A: REAL INTERACTIVE NODE COMPOSITOR GRAPH
// =========================================================================
@Composable
fun A2NodeCompositorOverlay(
    graph: AnimationNodeGraph,
    onAddNode: (AnimationNodeType) -> Unit,
    onUpdateNodePosition: (String, Float, Float) -> Unit,
    onRemoveNode: (String) -> Unit,
    onAutoArrange: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = A2Theme.Purple, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Interactive Node Compositor",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• ${graph.nodes.size} Nodes · Live DAG Wire Routing",
                        color = A2Theme.Purple,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Auto Arrange Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onAutoArrange() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = A2Theme.Amber, modifier = Modifier.size(12.dp))
                            Text(text = "Auto Arrange", color = A2Theme.Amber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Done Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done · Draw",
                            color = A2Theme.Ink950,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Add Node Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink850)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "+ Add:", color = A2Theme.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                listOf(
                    AnimationNodeType.LAYER_SOURCE to "Layer",
                    AnimationNodeType.TRANSFORM to "Transform",
                    AnimationNodeType.COLOR_FX to "Color FX",
                    AnimationNodeType.AI_MOTION to "AI Motion",
                    AnimationNodeType.COMPOSITE_OUTPUT to "Output"
                ).forEach { (type, label) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(A2Theme.Ink900)
                            .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(6.dp))
                            .clickable { onAddNode(type) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+ $label", color = A2Theme.TextPrimary, fontSize = 9.5.sp)
                    }
                }
            }

            // Interactive Node Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(A2Theme.Ink950)
            ) {
                // Connection Wires between nodes
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val nodeMap = graph.nodes.associateBy { it.id }
                    for (conn in graph.connections) {
                        val fromNode = nodeMap[conn.fromNodeId] ?: continue
                        val toNode = nodeMap[conn.toNodeId] ?: continue

                        val p1 = Offset(fromNode.x + 160f, fromNode.y + 40f)
                        val p2 = Offset(toNode.x, toNode.y + 40f)

                        val dx = (p2.x - p1.x).coerceAtLeast(60f) * 0.5f
                        val wirePath = Path().apply {
                            moveTo(p1.x, p1.y)
                            cubicTo(p1.x + dx, p1.y, p2.x - dx, p2.y, p2.x, p2.y)
                        }
                        drawPath(wirePath, A2Theme.Purple, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                    }
                }

                // Draggable Node Cards
                graph.nodes.forEach { node ->
                    val nodeColor = when (node.nodeType) {
                        AnimationNodeType.LAYER_SOURCE -> A2Theme.Mint
                        AnimationNodeType.TRANSFORM -> A2Theme.Amber
                        AnimationNodeType.COLOR_FX -> A2Theme.Teal
                        AnimationNodeType.AI_MOTION -> A2Theme.Rose
                        AnimationNodeType.COMPOSITE_OUTPUT -> A2Theme.Purple
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(node.x.toInt(), node.y.toInt()) }
                            .width(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(A2Theme.Ink900)
                            .border(1.5.dp, nodeColor, RoundedCornerShape(12.dp))
                            .pointerInput(node.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val newX = (node.x + dragAmount.x).coerceAtLeast(10f)
                                    val newY = (node.y + dragAmount.y).coerceAtLeast(10f)
                                    onUpdateNodePosition(node.id, newX, newY)
                                }
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
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = { onRemoveNode(node.id) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Delete Node",
                                        tint = A2Theme.TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Type: ${node.nodeType.name}",
                                color = nodeColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Drag to reposition in graph",
                                color = A2Theme.TextMuted,
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// OVERLAY B: REAL INTERACTIVE UNIVERSAL VELOCITY CURVE & EASING GRAPH
// =========================================================================
@Composable
fun A2VelocityCurveOverlay(
    curve: CubicBezierCurve,
    onUpdateCurve: (CubicBezierCurve) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var p1x by remember(curve) { mutableFloatStateOf(curve.p1x) }
    var p1y by remember(curve) { mutableFloatStateOf(curve.p1y) }
    var p2x by remember(curve) { mutableFloatStateOf(curve.p2x) }
    var p2y by remember(curve) { mutableFloatStateOf(curve.p2y) }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedT by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Compute cubic bezier value at time t
    fun evalBezier(t: Float, c1x: Float, c1y: Float, c2x: Float, c2y: Float): Float {
        val u = 1f - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t
        return uuu * 0f + 3f * uu * t * c1y + 3f * u * tt * c2y + ttt * 1f
    }

    val easedProgress = evalBezier(animatedT, p1x, p1y, p2x, p2y).coerceIn(0f, 1.3f)

    GlassPill(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Universal Object Velocity & Easing",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Interactive Touch Handles",
                        color = A2Theme.Amber,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Presets & Done
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Ease In-Out" to CubicBezierCurve(0.42f, 0.0f, 0.58f, 1.0f, "Ease In-Out"),
                        "Elastic" to CubicBezierCurve(0.17f, 0.89f, 0.32f, 1.28f, "Elastic"),
                        "Ease In" to CubicBezierCurve(0.42f, 0.0f, 1.0f, 1.0f, "Ease In"),
                        "Ease Out" to CubicBezierCurve(0.0f, 0.0f, 0.58f, 1.0f, "Ease Out"),
                        "Linear" to CubicBezierCurve(0.0f, 0.0f, 1.0f, 1.0f, "Linear")
                    ).forEach { (label, preset) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    p1x = preset.p1x
                                    p1y = preset.p1y
                                    p2x = preset.p2x
                                    p2y = preset.p2y
                                    onUpdateCurve(preset)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = label, color = A2Theme.TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Done · Draw", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bezier Canvas + Motion Preview Orb
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Interactive Bezier Graph Canvas with Real Drag Gestures
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(A2Theme.Ink950)
                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(16.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val w = size.width.toFloat()
                                val h = size.height.toFloat()
                                val pad = 40f
                                val touch = change.position

                                val c1Pos = Offset(pad + (w - 2 * pad) * p1x, (h - pad) - (h - 2 * pad) * p1y)
                                val c2Pos = Offset(pad + (w - 2 * pad) * p2x, (h - pad) - (h - 2 * pad) * p2y)

                                val d1 = (touch - c1Pos).getDistance()
                                val d2 = (touch - c2Pos).getDistance()

                                if (d1 < d2) {
                                    p1x = ((touch.x - pad) / (w - 2 * pad)).coerceIn(0f, 1f)
                                    p1y = (((h - pad) - touch.y) / (h - 2 * pad)).coerceIn(-0.3f, 1.5f)
                                } else {
                                    p2x = ((touch.x - pad) / (w - 2 * pad)).coerceIn(0f, 1f)
                                    p2y = (((h - pad) - touch.y) / (h - 2 * pad)).coerceIn(-0.3f, 1.5f)
                                }
                                onUpdateCurve(CubicBezierCurve(p1x, p1y, p2x, p2y, "Custom Bezier"))
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pad = 40f

                        val start = Offset(pad, h - pad)
                        val end = Offset(w - pad, pad)
                        val c1 = Offset(pad + (w - 2 * pad) * p1x, (h - pad) - (h - 2 * pad) * p1y)
                        val c2 = Offset(pad + (w - 2 * pad) * p2x, (h - pad) - (h - 2 * pad) * p2y)

                        // Grid Lines
                        for (i in 1..4) {
                            val gx = pad + (w - 2 * pad) * (i / 5f)
                            val gy = pad + (h - 2 * pad) * (i / 5f)
                            drawLine(Color.White.copy(alpha = 0.08f), Offset(gx, pad), Offset(gx, h - pad), 1f)
                            drawLine(Color.White.copy(alpha = 0.08f), Offset(pad, gy), Offset(w - pad, gy), 1f)
                        }

                        // Axes
                        drawLine(A2Theme.GlassBorder, start, Offset(w - pad, h - pad), 1.5.dp.toPx())
                        drawLine(A2Theme.GlassBorder, start, Offset(pad, pad), 1.5.dp.toPx())

                        // Tangent Handles
                        drawLine(A2Theme.Amber, start, c1, 2.dp.toPx())
                        drawLine(A2Theme.Teal, end, c2, 2.dp.toPx())
                        drawCircle(A2Theme.Amber, 9.dp.toPx(), c1)
                        drawCircle(Color.White, 4.dp.toPx(), c1)
                        drawCircle(A2Theme.Teal, 9.dp.toPx(), c2)
                        drawCircle(Color.White, 4.dp.toPx(), c2)

                        // Cubic Bezier Motion Curve
                        val curvePath = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
                        }
                        drawPath(curvePath, A2Theme.Accent, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))
                    }

                    Text(
                        text = "Touch & drag orange or cyan control handles",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                    )
                }

                // Target Preview Orb
                Column(
                    modifier = Modifier
                        .width(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(A2Theme.Ink900)
                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Kinetic Motion Path", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Applies real-time cubic bezier timing function", color = A2Theme.TextSecondary, fontSize = 9.sp)
                    }

                    // Orb Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Ink950),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(x = (easedProgress * 170).dp)
                                .size(28.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(A2Theme.Amber, A2Theme.Rose)))
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Curve: (${String.format("%.2f", p1x)}, ${String.format("%.2f", p1y)}, ${String.format("%.2f", p2x)}, ${String.format("%.2f", p2y)})",
                            color = A2Theme.Amber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "f(t) = (1-t)³P₀ + 3(1-t)²tP₁ + 3(1-t)t²P₂ + t³P₃",
                            color = A2Theme.TextMuted,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// OVERLAY C: REAL INTERACTIVE LIVE STUDIO COLOR GRADE
// =========================================================================
@Composable
fun A2ColorGradeOverlay(
    state: ColorGradingState,
    onStateChange: (ColorGradingState) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Live Studio Color Grade",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lift · Gamma · Gain 3-Way Science · Hardware LUT",
                        color = A2Theme.Teal,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                onStateChange(ColorGradingEngine.applyPreset(ColorGradingPreset.NONE))
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Reset", color = A2Theme.TextSecondary, fontSize = 10.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Done · Draw", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Cinematic LUT Presets Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    ColorGradingPreset.TEAL_AND_ORANGE to "Teal & Orange",
                    ColorGradingPreset.KODAK_VISION3 to "Kodak 5219",
                    ColorGradingPreset.FUJI_ETERNA to "Fuji Eterna",
                    ColorGradingPreset.BLEACH_BYPASS to "Bleach Bypass",
                    ColorGradingPreset.MONOCHROME_NOIR to "Noir B&W",
                    ColorGradingPreset.ANIME_VIBRANT to "Anime Vibrant",
                    ColorGradingPreset.UPSC_STUDY_WARM to "UPSC Study Warm"
                ).forEach { (preset, label) ->
                    val isSel = (state.preset == preset && state.isEnabled)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) A2Theme.Teal.copy(alpha = 0.25f) else A2Theme.Ink850)
                            .border(1.dp, if (isSel) A2Theme.Teal else A2Theme.GlassBorder, RoundedCornerShape(8.dp))
                            .clickable { onStateChange(ColorGradingEngine.applyPreset(preset)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) A2Theme.Teal else A2Theme.TextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // 3-Way Wheels & Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interactive 3-Way Wheels Row (Lift, Gamma, Gain)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(A2Theme.Ink950)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // LIFT WHEEL
                    ColorWheelPuck(
                        title = "LIFT",
                        color = A2Theme.Accent,
                        curX = state.liftGammaGain.lift.red,
                        curY = state.liftGammaGain.lift.blue,
                        onDragPuck = { dx, dy ->
                            val updatedLift = state.liftGammaGain.lift.copy(
                                red = (state.liftGammaGain.lift.red + dx).coerceIn(-0.5f, 0.5f),
                                blue = (state.liftGammaGain.lift.blue + dy).coerceIn(-0.5f, 0.5f)
                            )
                            onStateChange(state.copy(isEnabled = true, liftGammaGain = state.liftGammaGain.copy(lift = updatedLift)))
                        }
                    )

                    // GAMMA WHEEL
                    ColorWheelPuck(
                        title = "GAMMA",
                        color = A2Theme.Teal,
                        curX = state.liftGammaGain.gamma.red,
                        curY = state.liftGammaGain.gamma.blue,
                        onDragPuck = { dx, dy ->
                            val updatedGamma = state.liftGammaGain.gamma.copy(
                                red = (state.liftGammaGain.gamma.red + dx).coerceIn(-0.5f, 0.5f),
                                blue = (state.liftGammaGain.gamma.blue + dy).coerceIn(-0.5f, 0.5f)
                            )
                            onStateChange(state.copy(isEnabled = true, liftGammaGain = state.liftGammaGain.copy(gamma = updatedGamma)))
                        }
                    )

                    // GAIN WHEEL
                    ColorWheelPuck(
                        title = "GAIN",
                        color = A2Theme.Amber,
                        curX = state.liftGammaGain.gain.red,
                        curY = state.liftGammaGain.gain.blue,
                        onDragPuck = { dx, dy ->
                            val updatedGain = state.liftGammaGain.gain.copy(
                                red = (state.liftGammaGain.gain.red + dx).coerceIn(-0.5f, 0.5f),
                                blue = (state.liftGammaGain.gain.blue + dy).coerceIn(-0.5f, 0.5f)
                            )
                            onStateChange(state.copy(isEnabled = true, liftGammaGain = state.liftGammaGain.copy(gain = updatedGain)))
                        }
                    )
                }

                // Sliders Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Contrast
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Contrast", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Text(text = "${(state.contrast * 100).toInt()}%", color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = state.contrast,
                        onValueChange = { onStateChange(state.copy(isEnabled = true, contrast = it)) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = A2Theme.Ink800)
                    )

                    // Saturation
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Saturation", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Text(text = "${(state.cdl.saturation * 100).toInt()}%", color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = state.cdl.saturation,
                        onValueChange = { onStateChange(state.copy(isEnabled = true, cdl = state.cdl.copy(saturation = it))) },
                        valueRange = 0f..2.5f,
                        colors = SliderDefaults.colors(thumbColor = A2Theme.Teal, activeTrackColor = A2Theme.Teal, inactiveTrackColor = A2Theme.Ink800)
                    )

                    // Color Temperature (Kelvin)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Temperature (White Balance)", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Text(text = "${state.whiteBalance.kelvin.toInt()}K", color = A2Theme.Amber, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = state.whiteBalance.kelvin,
                        onValueChange = { onStateChange(state.copy(isEnabled = true, whiteBalance = state.whiteBalance.copy(kelvin = it))) },
                        valueRange = 2500f..9000f,
                        colors = SliderDefaults.colors(thumbColor = A2Theme.Amber, activeTrackColor = A2Theme.Amber, inactiveTrackColor = A2Theme.Ink800)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorWheelPuck(
    title: String,
    color: Color,
    curX: Float,
    curY: Float,
    onDragPuck: (Float, Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, color = A2Theme.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(A2Theme.Ink900)
                .border(1.dp, color.copy(alpha = 0.4f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDragPuck(dragAmount.x * 0.01f, dragAmount.y * 0.01f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Target Puck
            Box(
                modifier = Modifier
                    .offset(x = (curX * 30).dp, y = (curY * 30).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, color, CircleShape)
            )
        }
    }
}

// =========================================================================
// OVERLAY D: REAL INTERACTIVE 3D MULTIPLANE STAGE
// =========================================================================
@Composable
fun A2Stage3DOverlay(
    layers: List<AnimationLayer>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var orbitAngleY by remember { mutableFloatStateOf(0f) }
    var orbitAngleX by remember { mutableFloatStateOf(10f) }
    var zSpread by remember { mutableFloatStateOf(100f) }

    GlassPill(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "3D Multiplane Depth Stage",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Orbit camera around ${layers.size} live 2D cels in 3D depth",
                        color = A2Theme.Rose,
                        fontSize = 10.sp,
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
                        Text(text = "Z-Spread", color = A2Theme.TextSecondary, fontSize = 10.sp)
                        Slider(
                            value = zSpread,
                            onValueChange = { zSpread = it },
                            valueRange = 40f..220f,
                            colors = SliderDefaults.colors(thumbColor = A2Theme.Rose, activeTrackColor = A2Theme.Rose),
                            modifier = Modifier.width(100.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Done · Draw", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Interactive 3D Orbit Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(A2Theme.Ink950)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            orbitAngleY = (orbitAngleY + dragAmount.x * 0.25f).coerceIn(-65f, 65f)
                            orbitAngleX = (orbitAngleX - dragAmount.y * 0.25f).coerceIn(-40f, 40f)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val radY = Math.toRadians(orbitAngleY.toDouble())
                    val radX = Math.toRadians(orbitAngleX.toDouble())

                    val layerCount = layers.size.coerceAtLeast(1)
                    layers.forEachIndexed { i, layer ->
                        val z = (i - (layerCount - 1) / 2f) * zSpread
                        val depthScale = (1.0f + (z / 600f)).coerceAtLeast(0.4f)
                        val pw = 360f * depthScale
                        val ph = 200f * depthScale
                        val px = cx + (z * sin(radY)).toFloat()
                        val py = cy - (z * sin(radX)).toFloat()

                        val layerColor = when (i % 4) {
                            0 -> A2Theme.Rose
                            1 -> A2Theme.Accent
                            2 -> A2Theme.Amber
                            else -> A2Theme.Mint
                        }

                        // Plane Box
                        drawRect(
                            color = layerColor.copy(alpha = 0.2f),
                            topLeft = Offset(px - pw / 2f, py - ph / 2f),
                            size = Size(pw, ph)
                        )
                        drawRect(
                            color = layerColor,
                            topLeft = Offset(px - pw / 2f, py - ph / 2f),
                            size = Size(pw, ph),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Instructions & telemetry
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Drag horizontally/vertically to orbit virtual multiplane camera",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Camera: Azimuth ${orbitAngleY.toInt()}° · Elevation ${orbitAngleX.toInt()}° · Z-Depth ${zSpread.toInt()}px",
                        color = A2Theme.Rose,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
