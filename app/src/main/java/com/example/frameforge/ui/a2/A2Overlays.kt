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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// OVERLAY A: INTERACTIVE NODE COMPOSITOR
// =========================================================================
@Composable
fun A2NodeCompositorOverlay(
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
                        text = "Interactive Node Compositor",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• Live DAG Wire Routing",
                        color = A2Theme.Purple,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

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

            // Canvas Area with Simulated Nodes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(A2Theme.Ink950)
            ) {
                // Connection Wires
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val p1 = Offset(240f, 260f)
                    val p2 = Offset(520f, 260f)
                    val p3 = Offset(800f, 260f)

                    val path1 = Path().apply {
                        moveTo(p1.x, p1.y)
                        cubicTo(p1.x + 100f, p1.y, p2.x - 100f, p2.y, p2.x, p2.y)
                    }
                    val path2 = Path().apply {
                        moveTo(p2.x, p2.y)
                        cubicTo(p2.x + 100f, p2.y, p3.x - 100f, p3.y, p3.x, p3.y)
                    }

                    drawPath(path1, A2Theme.Purple, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    drawPath(path2, A2Theme.Accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }

                // Node 1: Input Canvas
                Box(
                    modifier = Modifier
                        .offset(x = 60.dp, y = 70.dp)
                        .width(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(A2Theme.Ink900)
                        .border(1.5.dp, A2Theme.Purple, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Input Canvas", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Active Cels: 3 Layers", color = A2Theme.TextSecondary, fontSize = 9.sp)
                        Text(text = "RGBA8 · 1920x1080", color = A2Theme.Purple, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Node 2: 16mm Film Grain FX
                Box(
                    modifier = Modifier
                        .offset(x = 260.dp, y = 70.dp)
                        .width(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(A2Theme.Ink900)
                        .border(1.5.dp, A2Theme.Accent, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Film Grain 16mm", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Intensity: 35%", color = A2Theme.TextSecondary, fontSize = 9.sp)
                        Text(text = "Kodak 5219 Emulation", color = A2Theme.Accent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Node 3: Output Composite
                Box(
                    modifier = Modifier
                        .offset(x = 460.dp, y = 70.dp)
                        .width(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(A2Theme.Ink900)
                        .border(1.5.dp, A2Theme.Mint, RoundedCornerShape(14.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Output Composite", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Direct Stage Output", color = A2Theme.TextSecondary, fontSize = 9.sp)
                        Text(text = "Master Stream V1", color = A2Theme.Mint, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// =========================================================================
// OVERLAY B: UNIVERSAL VELOCITY CURVE & EASING GRAPH
// =========================================================================
@Composable
fun A2VelocityCurveOverlay(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var p1x by remember { mutableFloatStateOf(0.25f) }
    var p1y by remember { mutableFloatStateOf(0.10f) }
    var p2x by remember { mutableFloatStateOf(0.25f) }
    var p2y by remember { mutableFloatStateOf(1.00f) }

    val infiniteTransition = rememberInfiniteTransition()
    val animatedT by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

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
                        text = "Peak: 1,480 px/s",
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
                    listOf("Ease In-Out", "Elastic", "Linear").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    when (preset) {
                                        "Ease In-Out" -> { p1x = 0.42f; p1y = 0.0f; p2x = 0.58f; p2y = 1.0f }
                                        "Elastic" -> { p1x = 0.17f; p1y = 0.89f; p2x = 0.32f; p2y = 1.28f }
                                        "Linear" -> { p1x = 0.0f; p1y = 0.0f; p2x = 1.0f; p2y = 1.0f }
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = preset, color = A2Theme.TextSecondary, fontSize = 10.sp)
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
                // Interactive Bezier Graph Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(A2Theme.Ink950)
                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(16.dp))
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
                        drawLine(A2Theme.GlassBorder, Offset(pad, h - pad), Offset(w - pad, h - pad), 1.dp.toPx())
                        drawLine(A2Theme.GlassBorder, Offset(pad, pad), Offset(pad, h - pad), 1.dp.toPx())

                        // Tangent Handles
                        drawLine(A2Theme.Amber.copy(alpha = 0.6f), start, c1, 1.5.dp.toPx())
                        drawLine(A2Theme.Amber.copy(alpha = 0.6f), end, c2, 1.5.dp.toPx())
                        drawCircle(A2Theme.Amber, 6.dp.toPx(), c1)
                        drawCircle(A2Theme.Amber, 6.dp.toPx(), c2)

                        // Cubic Bezier Motion Curve
                        val curvePath = Path().apply {
                            moveTo(start.x, start.y)
                            cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
                        }
                        drawPath(curvePath, A2Theme.Accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                    }
                }

                // Target Preview Orb
                Column(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(A2Theme.Ink900)
                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "Kinetic Motion Path", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Applies Bezier timing curve across frames F1 to F8", color = A2Theme.TextSecondary, fontSize = 9.sp)
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
                                .offset(x = (animatedT * 150).dp)
                                .size(28.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(A2Theme.Amber, A2Theme.Rose)))
                        )
                    }

                    Text(
                        text = "Curve: (${String.format("%.2f", p1x)}, ${String.format("%.2f", p1y)}, ${String.format("%.2f", p2x)}, ${String.format("%.2f", p2y)})",
                        color = A2Theme.Amber,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// =========================================================================
// OVERLAY C: LIVE STUDIO COLOR GRADE
// =========================================================================
@Composable
fun A2ColorGradeOverlay(
    contrast: Float,
    onContrastChange: (Float) -> Unit,
    saturation: Float,
    onSaturationChange: (Float) -> Unit,
    warmth: Float,
    onWarmthChange: (Float) -> Unit,
    hueShift: Float,
    onHueShiftChange: (Float) -> Unit,
    onReset: () -> Unit,
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
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Lift · Gamma · Gain 3-Way Science",
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
                            .clickable { onReset() }
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

            // 3-Way Wheels & Sliders
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wheels Row (Lift, Gamma, Gain)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(A2Theme.Ink950)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    listOf("LIFT" to A2Theme.Accent, "GAMMA" to A2Theme.Teal, "GAIN" to A2Theme.Amber).forEach { (lbl, col) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = lbl, color = A2Theme.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(A2Theme.Ink900)
                                    .border(1.dp, col.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.5.dp, col, CircleShape)
                                )
                            }
                        }
                    }
                }

                // Sliders Grid
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Contrast
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Contrast", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Text(text = "${(contrast * 100).toInt()}%", color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = contrast,
                        onValueChange = onContrastChange,
                        valueRange = 0.7f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White, inactiveTrackColor = A2Theme.Ink800)
                    )

                    // Saturation
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Saturation", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Text(text = "${(saturation * 100).toInt()}%", color = Color.White, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = saturation,
                        onValueChange = onSaturationChange,
                        valueRange = 0f..2f,
                        colors = SliderDefaults.colors(thumbColor = A2Theme.Accent, activeTrackColor = A2Theme.Accent, inactiveTrackColor = A2Theme.Ink800)
                    )
                }
            }
        }
    }
}

// =========================================================================
// OVERLAY D: 3D MULTIPLANE STAGE
// =========================================================================
@Composable
fun A2Stage3DOverlay(
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var zSpread by remember { mutableFloatStateOf(110f) }

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
                        text = "• Orbit around live 2D cels in 3D depth",
                        color = A2Theme.Rose,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

            // Simulated 3D Depth Planes Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(A2Theme.Ink950)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f

                    // 3 Stacked Perspective Layer Rectangles
                    val layerOffsets = listOf(
                        Offset(cx - 180f, cy - 90f) to A2Theme.Rose.copy(alpha = 0.25f),
                        Offset(cx - 140f, cy - 50f) to A2Theme.Accent.copy(alpha = 0.35f),
                        Offset(cx - 100f, cy - 10f) to A2Theme.Amber.copy(alpha = 0.45f)
                    )
                    val planeSize = Size(360f, 200f)

                    for ((topL, col) in layerOffsets) {
                        drawRect(col, topL, planeSize)
                        drawRect(Color.White.copy(alpha = 0.6f), topL, planeSize, style = Stroke(width = 1.5.dp.toPx()))
                    }
                }

                // Layer Labels
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Layer 3 (Foreground Cels): Z = 0", color = A2Theme.Amber, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "Layer 2 (Hero Ink Character): Z = -50", color = A2Theme.Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "Layer 1 (Deep Space & Stars): Z = -110", color = A2Theme.Rose, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
