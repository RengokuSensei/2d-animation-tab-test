package com.example.frameforge.ui.a2

import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.engine.DrawingEngine
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.DrawingStroke
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.unit.IntOffset
import com.example.frameforge.cinema.ColorGradingEngine
import com.example.frameforge.cinema.ColorGradingPreset
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.cinema.ColorWheelValues
import com.example.frameforge.cinema.CubicBezierCurve
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
// OVERLAY D: REAL INTERACTIVE 3D MULTIPLANE STAGE & PARALLAX ENGINE
// =========================================================================

private enum class MultiplaneItemType {
    DRAWING, PHOTO, VIDEO, PDF
}

private data class MultiplaneVisualPlane(
    val id: String,
    val name: String,
    val type: MultiplaneItemType,
    val posZ: Float,
    val offsetX: Float,
    val offsetY: Float,
    val scale: Float,
    val opacity: Float,
    val isVisible: Boolean,
    val isLocked: Boolean,
    val autoScaleComp: Boolean,
    val strokes: List<DrawingStroke> = emptyList(),
    val bitmap: Bitmap? = null,
    val accentColor: Color
)

@Composable
fun A2Stage3DOverlay(
    project: AnimationProject,
    currentFrame: Int,
    isPlaying: Boolean,
    stagedPhotoBitmap: Bitmap?,
    photoOpacity: Float,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onSetCurrentFrame: (Int) -> Unit,
    onTogglePlayback: () -> Unit,
    onUpdateLayerTransform: (layerId: String, offsetX: Float, offsetY: Float, posZ: Float, rotX: Float, rotY: Float, scale: Float, opacity: Float, autoScaleComp: Boolean) -> Unit,
    onToggleLayerVisibility: (String) -> Unit,
    onToggleLayerLock: (String) -> Unit,
    onBakeParallax: (panDistanceX: Float, dollyDistanceZ: Float) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var orbitYaw by remember { mutableFloatStateOf(26f) }
    var orbitPitch by remember { mutableFloatStateOf(14f) }
    var cameraDolly by remember { mutableFloatStateOf(0f) }
    var isFlyThroughActive by remember { mutableStateOf(false) }
    var flyThroughPhase by remember { mutableFloatStateOf(0f) }
    var showBakeDialog by remember { mutableStateOf(false) }
    var panDistanceX by remember { mutableFloatStateOf(240f) }
    var dollyDistanceZ by remember { mutableFloatStateOf(300f) }

    // Parallax Fly-Through Camera Animation Loop
    LaunchedEffect(isFlyThroughActive) {
        if (!isFlyThroughActive) return@LaunchedEffect
        while (isActive) {
            delay(40L)
            flyThroughPhase = (flyThroughPhase + 0.04f) % (2f * Math.PI.toFloat())
            orbitYaw = 24f * kotlin.math.sin(flyThroughPhase)
            orbitPitch = 14f + 8f * kotlin.math.cos(flyThroughPhase * 0.5f)
            cameraDolly = 70f * kotlin.math.sin(flyThroughPhase * 0.5f)
        }
    }

    // Live Frame Playback Stepper in 3D Stage
    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        val intervalMs = (1000L / project.fps.coerceAtLeast(1).toLong()).coerceIn(16L, 100L)
        while (isActive) {
            delay(intervalMs)
            val nextFrame = (currentFrame + 1) % project.totalFrames.coerceAtLeast(1)
            onSetCurrentFrame(nextFrame)
        }
    }

    // Construct visual planes list for 3D stage
    val planes = remember(project, currentFrame, stagedPhotoBitmap, photoOpacity) {
        val list = mutableListOf<MultiplaneVisualPlane>()

        // 1. PDF Track Plane
        if (project.pdfTrack.isEnabled) {
            list.add(
                MultiplaneVisualPlane(
                    id = "plane_pdf_track",
                    name = "PDF Storyboard Deck",
                    type = MultiplaneItemType.PDF,
                    posZ = -700f,
                    offsetX = 0f,
                    offsetY = 0f,
                    scale = 1.0f,
                    opacity = 0.85f,
                    isVisible = true,
                    isLocked = false,
                    autoScaleComp = true,
                    accentColor = Color(0xFFF59E0B)
                )
            )
        }

        // 2. Video Track Plane
        if (project.videoTrack.isEnabled) {
            list.add(
                MultiplaneVisualPlane(
                    id = "plane_video_track",
                    name = project.videoTrack.videoTitle.ifEmpty { "Video Rotoscoping Track" },
                    type = MultiplaneItemType.VIDEO,
                    posZ = -450f,
                    offsetX = 0f,
                    offsetY = 0f,
                    scale = 1.0f,
                    opacity = project.videoTrack.opacity,
                    isVisible = true,
                    isLocked = false,
                    autoScaleComp = true,
                    accentColor = Color(0xFF0EA5E9)
                )
            )
        }

        // 3. Reference Photo Plane
        if (stagedPhotoBitmap != null && !stagedPhotoBitmap.isRecycled) {
            list.add(
                MultiplaneVisualPlane(
                    id = "plane_photo_ref",
                    name = "Reference Photo",
                    type = MultiplaneItemType.PHOTO,
                    posZ = -220f,
                    offsetX = 0f,
                    offsetY = 0f,
                    scale = 1.0f,
                    opacity = photoOpacity,
                    isVisible = true,
                    isLocked = false,
                    autoScaleComp = true,
                    bitmap = stagedPhotoBitmap,
                    accentColor = Color(0xFFA855F7)
                )
            )
        }

        // 4. Drawing Layers with active frame cels
        val frameCels = project.cels.filter { it.frameIndex == currentFrame }.associateBy { it.layerId }
        project.layers.forEachIndexed { i, layer ->
            val layerStrokes = frameCels[layer.id]?.strokes ?: emptyList()
            val color = when (i % 4) {
                0 -> Color(0xFFFB7185) // Rose
                1 -> Color(0xFF38BDF8) // Sky
                2 -> Color(0xFF34D399) // Mint
                else -> Color(0xFFFBBF24) // Amber
            }
            list.add(
                MultiplaneVisualPlane(
                    id = layer.id,
                    name = layer.name,
                    type = MultiplaneItemType.DRAWING,
                    posZ = layer.posZ,
                    offsetX = layer.offsetX,
                    offsetY = layer.offsetY,
                    scale = layer.scale,
                    opacity = layer.opacity,
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    autoScaleComp = layer.autoScaleComp,
                    strokes = layerStrokes,
                    accentColor = color
                )
            )
        }
        list
    }

    val selectedLayer = project.layers.find { it.id == selectedLayerId } ?: project.layers.firstOrNull()

    GlassPill(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        cornerRadius = 20.dp,
        elevation = 22.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER & TELEMETRY BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(A2Theme.Rose.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ViewInAr, contentDescription = null, tint = A2Theme.Rose, modifier = Modifier.size(16.dp))
                    }
                    Column {
                        Text(
                            text = "3D Multiplane Depth Stage",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Frame ${currentFrame + 1}/${project.totalFrames} · ${planes.size} Active Plates in Depth",
                            color = A2Theme.TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Camera Presets & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // View Presets
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        PresetPill(text = "Front 0°", isSelected = orbitYaw == 0f && orbitPitch == 0f) {
                            orbitYaw = 0f; orbitPitch = 0f
                        }
                        PresetPill(text = "Orbit 26°", isSelected = orbitYaw == 26f && orbitPitch == 14f) {
                            orbitYaw = 26f; orbitPitch = 14f
                        }
                        PresetPill(text = "Tower 76°", isSelected = orbitYaw == 76f && orbitPitch == 6f) {
                            orbitYaw = 76f; orbitPitch = 6f
                        }
                        PresetPill(text = "Top 80°", isSelected = orbitYaw == 0f && orbitPitch == 80f) {
                            orbitYaw = 0f; orbitPitch = 80f
                        }
                    }

                    // Parallax Fly-Through Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isFlyThroughActive) A2Theme.Rose else Color.White.copy(alpha = 0.08f))
                            .clickable { isFlyThroughActive = !isFlyThroughActive }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFlyThroughActive) "⏸ Stop Fly" else "▶ Fly-Through",
                            color = if (isFlyThroughActive) Color.White else A2Theme.Rose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Bake Parallax Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showBakeDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✨ Bake 3D Parallax",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Done Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Accent)
                            .clickable { onDone() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Done · Draw", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 2. MAIN WORKSPACE: 3D STAGE VIEWPORT (LEFT) + INSPECTOR (RIGHT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // 3D ORBIT VIEWPORT
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(Color(0xFF090A10))
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                orbitYaw = (orbitYaw + dragAmount.x * 0.28f).coerceIn(-85f, 85f)
                                orbitPitch = (orbitPitch - dragAmount.y * 0.25f).coerceIn(-45f, 85f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radYaw = Math.toRadians(orbitYaw.toDouble())
                        val radPitch = Math.toRadians(orbitPitch.toDouble())
                        val sinY = kotlin.math.sin(radYaw).toFloat()
                        val cosY = kotlin.math.cos(radYaw).toFloat()
                        val sinP = kotlin.math.sin(radPitch).toFloat()
                        val cosP = kotlin.math.cos(radPitch).toFloat()

                        // A. Draw 3D Ground Perspective Grid Floor
                        val gridFloorY = cy + 180f
                        val gridColor = Color(0x1800E5FF)
                        for (gx in -5..5) {
                            val lineStartX = cx + (gx * 90f)
                            val lineEndX = cx + (gx * 180f)
                            drawLine(
                                color = gridColor,
                                start = Offset(lineStartX, gridFloorY),
                                end = Offset(lineEndX, size.height),
                                strokeWidth = 1.2f
                            )
                        }
                        for (gy in 0..4) {
                            val lineY = gridFloorY + (gy * 40f)
                            val w = (size.width * 0.7f) + (gy * 90f)
                            drawLine(
                                color = gridColor,
                                start = Offset(cx - w / 2f, lineY),
                                end = Offset(cx + w / 2f, lineY),
                                strokeWidth = 1.2f
                            )
                        }

                        // B. Depth sort planes: furthest plane renders first, closest last
                        val sortedPlanes = planes.sortedByDescending { it.posZ * cosY * cosP }

                        val baseW = 440f
                        val baseH = 248f // 16:9 aspect ratio

                        sortedPlanes.forEach { plane ->
                            val effectiveZ = plane.posZ * cosY * cosP
                            val projX = cx + plane.offsetX + (plane.posZ * sinY)
                            val projY = cy + plane.offsetY - (plane.posZ * sinP)

                            val focalD = 950f
                            val denom = (focalD + effectiveZ - cameraDolly).coerceAtLeast(100f)
                            var perspScale = (focalD / denom)
                            if (plane.autoScaleComp) {
                                perspScale *= ((focalD + plane.posZ) / focalD)
                            }
                            val finalScale = (perspScale * plane.scale).coerceIn(0.15f, 4.0f)

                            val cam3D = Camera()
                            val matrix = Matrix()
                            cam3D.save()
                            cam3D.rotateX(orbitPitch)
                            cam3D.rotateY(orbitYaw)
                            cam3D.getMatrix(matrix)
                            cam3D.restore()

                            matrix.preScale(finalScale, finalScale)
                            matrix.preTranslate(-baseW / 2f, -baseH / 2f)
                            matrix.postTranslate(projX, projY)

                            drawIntoCanvas { composeCanvas ->
                                val nativeCanvas = composeCanvas.nativeCanvas
                                nativeCanvas.save()
                                nativeCanvas.concat(matrix)

                                val isSelected = (plane.id == selectedLayerId)
                                val cardRect = RectF(0f, 0f, baseW, baseH)

                                // 1. Frosted Glass Artboard Fill
                                val bgPaint = Paint().apply {
                                    color = if (isSelected) 0x3300E5FF.toInt() else 0x2213161F.toInt()
                                    style = Paint.Style.FILL
                                }
                                nativeCanvas.drawRoundRect(cardRect, 14f, 14f, bgPaint)

                                // 2. Artboard Border
                                val borderPaint = Paint().apply {
                                    color = if (isSelected) 0xFF00E5FF.toInt() else (plane.accentColor.toArgb() and 0x77FFFFFF.toInt())
                                    strokeWidth = if (isSelected) 3.5f else 1.8f
                                    style = Paint.Style.STROKE
                                    isAntiAlias = true
                                }
                                nativeCanvas.drawRoundRect(cardRect, 14f, 14f, borderPaint)

                                // 3. Registration Brackets when Selected
                                if (isSelected) {
                                    val cornerPaint = Paint().apply {
                                        color = 0xFF00E5FF.toInt()
                                        strokeWidth = 3f
                                        style = Paint.Style.STROKE
                                        isAntiAlias = true
                                    }
                                    val cl = 20f
                                    nativeCanvas.drawLine(0f, cl, 0f, 0f, cornerPaint)
                                    nativeCanvas.drawLine(0f, 0f, cl, 0f, cornerPaint)
                                    nativeCanvas.drawLine(baseW - cl, 0f, baseW, 0f, cornerPaint)
                                    nativeCanvas.drawLine(baseW, 0f, baseW, cl, cornerPaint)
                                    nativeCanvas.drawLine(0f, baseH - cl, 0f, baseH, cornerPaint)
                                    nativeCanvas.drawLine(0f, baseH, cl, baseH, cornerPaint)
                                    nativeCanvas.drawLine(baseW - cl, baseH, baseW, baseH, cornerPaint)
                                    nativeCanvas.drawLine(baseW, baseH - cl, baseW, baseH, cornerPaint)
                                }

                                // 4. Plate Badge (Name, Z-Depth & Visibility)
                                val badgeBgPaint = Paint().apply {
                                    color = 0xDD0C0E14.toInt()
                                    style = Paint.Style.FILL
                                }
                                nativeCanvas.drawRoundRect(RectF(8f, 8f, 260f, 38f), 8f, 8f, badgeBgPaint)

                                val badgeTextPaint = Paint().apply {
                                    color = if (isSelected) 0xFF00E5FF.toInt() else 0xFFF1F5F9.toInt()
                                    textSize = 13f
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                val vis = if (plane.isVisible) "👁" else "🚫"
                                val zText = "Z: ${plane.posZ.toInt()}px"
                                nativeCanvas.drawText("$vis ${plane.name} · $zText", 16f, 28f, badgeTextPaint)

                                // 5. Render Actual Plate Artwork & Content
                                when (plane.type) {
                                    MultiplaneItemType.DRAWING -> {
                                        if (plane.isVisible) {
                                            val sx = baseW / project.canvasWidth.toFloat()
                                            val sy = baseH / project.canvasHeight.toFloat()
                                            nativeCanvas.save()
                                            nativeCanvas.scale(sx, sy)

                                            if (plane.opacity < 0.999f) {
                                                val tempBmp = Bitmap.createBitmap(project.canvasWidth, project.canvasHeight, Bitmap.Config.ARGB_8888)
                                                val tempCanvas = android.graphics.Canvas(tempBmp)
                                                for (s in plane.strokes) {
                                                    DrawingEngine.renderStroke(tempCanvas, s)
                                                }
                                                val opPaint = Paint().apply {
                                                    alpha = (plane.opacity * 255).toInt().coerceIn(0, 255)
                                                }
                                                nativeCanvas.drawBitmap(tempBmp, 0f, 0f, opPaint)
                                                tempBmp.recycle()
                                            } else {
                                                for (s in plane.strokes) {
                                                    DrawingEngine.renderStroke(nativeCanvas, s)
                                                }
                                            }
                                            nativeCanvas.restore()

                                            if (plane.strokes.isEmpty()) {
                                                val emptyPaint = Paint().apply {
                                                    color = 0x5594A3B8.toInt()
                                                    textSize = 14f
                                                    textAlign = Paint.Align.CENTER
                                                    isAntiAlias = true
                                                }
                                                nativeCanvas.drawText("Empty Cel · F${currentFrame + 1}", baseW / 2f, baseH / 2f + 5f, emptyPaint)
                                            }
                                        }
                                    }
                                    MultiplaneItemType.PHOTO -> {
                                        if (plane.bitmap != null && !plane.bitmap.isRecycled) {
                                            val bmpPaint = Paint().apply {
                                                alpha = (plane.opacity * 255).toInt().coerceIn(0, 255)
                                                isFilterBitmap = true
                                            }
                                            val dstRect = RectF(10f, 44f, baseW - 10f, baseH - 10f)
                                            nativeCanvas.drawBitmap(plane.bitmap, null, dstRect, bmpPaint)
                                        }
                                    }
                                    MultiplaneItemType.VIDEO -> {
                                        val vidPaint = Paint().apply {
                                            color = 0xFF38BDF8.toInt()
                                            textSize = 15f
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        nativeCanvas.drawText("🎬 Video Rotoscoping Plane", baseW / 2f, baseH / 2f - 4f, vidPaint)
                                        val subPaint = Paint().apply {
                                            color = 0x8894A3B8.toInt()
                                            textSize = 11f
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        nativeCanvas.drawText("${project.videoTrack.clips.size} clip(s) active · Underlay", baseW / 2f, baseH / 2f + 16f, subPaint)
                                    }
                                    MultiplaneItemType.PDF -> {
                                        val pdfPaint = Paint().apply {
                                            color = 0xFFF59E0B.toInt()
                                            textSize = 15f
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        nativeCanvas.drawText("📄 PDF Storyboard Deck", baseW / 2f, baseH / 2f - 4f, pdfPaint)
                                        val subPaint = Paint().apply {
                                            color = 0x8894A3B8.toInt()
                                            textSize = 11f
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        nativeCanvas.drawText("${project.pdfTrack.totalDocumentPages} page(s) loaded · Underlay", baseW / 2f, baseH / 2f + 16f, subPaint)
                                    }
                                }

                                nativeCanvas.restore()
                            }
                        }
                    }

                    // Floating Camera HUD Telemetry
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Camera: Azimuth ${orbitYaw.toInt()}° · Elevation ${orbitPitch.toInt()}° · Dolly ${cameraDolly.toInt()}px",
                            color = A2Theme.Rose,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Active Plate: ${selectedLayer?.name ?: "Overview"} · Drag to Orbit",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // RIGHT INSPECTOR DRAWER
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .background(A2Theme.Ink900)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "MULTI-PLANE INSPECTOR",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Plate Selector Chips
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(planes) { p ->
                            val isSel = (p.id == selectedLayerId)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) A2Theme.Accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                    .border(1.dp, if (isSel) A2Theme.Accent else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (p.type == MultiplaneItemType.DRAWING) {
                                            onSelectLayer(p.id)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(p.accentColor)
                                    )
                                    Text(
                                        text = p.name,
                                        color = if (isSel) Color.White else Color.White.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                Text(
                                    text = "Z: ${p.posZ.toInt()}",
                                    color = if (isSel) A2Theme.Accent else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Selected Layer Controls
                    if (selectedLayer != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Adjust Plate: ${selectedLayer.name}",
                                color = A2Theme.Rose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Z-Depth Slider & Stepper
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Z-Depth (Multiplane Spacing)", color = A2Theme.TextSecondary, fontSize = 10.sp)
                                    Text(text = "${selectedLayer.posZ.toInt()} px", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Slider(
                                    value = selectedLayer.posZ,
                                    onValueChange = { newZ ->
                                        onUpdateLayerTransform(
                                            selectedLayer.id,
                                            selectedLayer.offsetX,
                                            selectedLayer.offsetY,
                                            newZ,
                                            selectedLayer.rotX,
                                            selectedLayer.rotY,
                                            selectedLayer.scale,
                                            selectedLayer.opacity,
                                            selectedLayer.autoScaleComp
                                        )
                                    },
                                    valueRange = -800f..2000f,
                                    colors = SliderDefaults.colors(thumbColor = A2Theme.Rose, activeTrackColor = A2Theme.Rose)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    QuickNudgeButton(text = "-200px") {
                                        onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, selectedLayer.posZ - 200f, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                    }
                                    QuickNudgeButton(text = "0px (Home)") {
                                        onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, 0f, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                    }
                                    QuickNudgeButton(text = "+200px") {
                                        onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, selectedLayer.posZ + 200f, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                    }
                                }
                            }

                            // Disney Auto-Scale Compensation Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Auto-Scale Compensation", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Preserves 2D frame size during Z shifts", color = A2Theme.TextSecondary, fontSize = 8.sp)
                                }
                                Switch(
                                    checked = selectedLayer.autoScaleComp,
                                    onCheckedChange = { comp ->
                                        onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, selectedLayer.posZ, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, comp)
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = A2Theme.Accent, checkedTrackColor = A2Theme.Accent.copy(alpha = 0.5f))
                                )
                            }

                            // X / Y Offsets
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Offset X: ${selectedLayer.offsetX.toInt()}px", color = A2Theme.TextSecondary, fontSize = 9.sp)
                                    Slider(
                                        value = selectedLayer.offsetX,
                                        onValueChange = { ox ->
                                            onUpdateLayerTransform(selectedLayer.id, ox, selectedLayer.offsetY, selectedLayer.posZ, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                        },
                                        valueRange = -300f..300f,
                                        colors = SliderDefaults.colors(thumbColor = A2Theme.Accent, activeTrackColor = A2Theme.Accent)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Offset Y: ${selectedLayer.offsetY.toInt()}px", color = A2Theme.TextSecondary, fontSize = 9.sp)
                                    Slider(
                                        value = selectedLayer.offsetY,
                                        onValueChange = { oy ->
                                            onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, oy, selectedLayer.posZ, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                        },
                                        valueRange = -200f..200f,
                                        colors = SliderDefaults.colors(thumbColor = A2Theme.Accent, activeTrackColor = A2Theme.Accent)
                                    )
                                }
                            }

                            // Scale & Opacity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Scale: ${String.format("%.2f", selectedLayer.scale)}x", color = A2Theme.TextSecondary, fontSize = 9.sp)
                                    Slider(
                                        value = selectedLayer.scale,
                                        onValueChange = { sc ->
                                            onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, selectedLayer.posZ, selectedLayer.rotX, selectedLayer.rotY, sc, selectedLayer.opacity, selectedLayer.autoScaleComp)
                                        },
                                        valueRange = 0.3f..2.5f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFBBF24), activeTrackColor = Color(0xFFFBBF24))
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Opacity: ${(selectedLayer.opacity * 100).toInt()}%", color = A2Theme.TextSecondary, fontSize = 9.sp)
                                    Slider(
                                        value = selectedLayer.opacity,
                                        onValueChange = { op ->
                                            onUpdateLayerTransform(selectedLayer.id, selectedLayer.offsetX, selectedLayer.offsetY, selectedLayer.posZ, selectedLayer.rotX, selectedLayer.rotY, selectedLayer.scale, op, selectedLayer.autoScaleComp)
                                        },
                                        valueRange = 0f..1f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF34D399), activeTrackColor = Color(0xFF34D399))
                                    )
                                }
                            }

                            // Visibility & Lock Quick Toggles
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedLayer.isVisible) Color.White.copy(alpha = 0.08f) else Color.Red.copy(alpha = 0.2f))
                                    .clickable { onToggleLayerVisibility(selectedLayer.id) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (selectedLayer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = if (selectedLayer.isVisible) Color.White else Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(text = if (selectedLayer.isVisible) "Visible" else "Hidden", color = Color.White, fontSize = 10.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedLayer.isLocked) Color.Red.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                                    .clickable { onToggleLayerLock(selectedLayer.id) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(
                                        imageVector = if (selectedLayer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (selectedLayer.isLocked) Color.Red else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(text = if (selectedLayer.isLocked) "Locked" else "Unlocked", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

            // 3. BOTTOM ANIMATION TRANSPORT & FRAME SCRUBBER BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink950)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Playback Stepper Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TransportMiniBtn(icon = Icons.Default.SkipPrevious) { onSetCurrentFrame(0) }
                    TransportMiniBtn(icon = Icons.Default.FastRewind) {
                        onSetCurrentFrame((currentFrame - 1).coerceAtLeast(0))
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(A2Theme.Accent)
                            .clickable { onTogglePlayback() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = A2Theme.Ink950,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    TransportMiniBtn(icon = Icons.Default.FastForward) {
                        onSetCurrentFrame((currentFrame + 1) % project.totalFrames)
                    }
                    TransportMiniBtn(icon = Icons.Default.SkipNext) {
                        onSetCurrentFrame(project.totalFrames - 1)
                    }
                }

                // Time Indicator
                Text(
                    text = "F ${String.format("%02d", currentFrame + 1)}/${String.format("%02d", project.totalFrames)}",
                    color = A2Theme.Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Scrollable Frame Strip
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(project.totalFrames) { fIndex ->
                        val isCurr = (fIndex == currentFrame)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurr) A2Theme.Accent else Color.White.copy(alpha = 0.06f))
                                .clickable { onSetCurrentFrame(fIndex) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "F ${String.format("%02d", fIndex + 1)}",
                                color = if (isCurr) A2Theme.Ink950 else Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp,
                                fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // Bake Parallax Dialog Modal
    if (showBakeDialog) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = A2Theme.Ink900),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✨ Bake 3D Multiplane Parallax", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showBakeDialog = false }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }

                Text(
                    text = "Generates automatic multiplane pan & dolly keyframes for all layers based on their 3D Z-depth. Closer layers move faster, creating true cinematic parallax depth for 2D playback & MP4 export.",
                    color = A2Theme.TextSecondary,
                    fontSize = 10.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Camera Pan Travel (X)", color = A2Theme.TextSecondary, fontSize = 10.sp)
                        Text(text = "${panDistanceX.toInt()} px", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = panDistanceX,
                        onValueChange = { panDistanceX = it },
                        valueRange = 80f..480f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8))
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Camera Dolly Travel (Z)", color = A2Theme.TextSecondary, fontSize = 10.sp)
                        Text(text = "${dollyDistanceZ.toInt()} px", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = dollyDistanceZ,
                        onValueChange = { dollyDistanceZ = it },
                        valueRange = 0f..600f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFF38BDF8), activeTrackColor = Color(0xFF38BDF8))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF38BDF8))
                            .clickable {
                                onBakeParallax(panDistanceX, dollyDistanceZ)
                                showBakeDialog = false
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Confirm & Bake Parallax", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetPill(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) A2Theme.Rose else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun QuickNudgeButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.White.copy(alpha = 0.85f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun TransportMiniBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}
