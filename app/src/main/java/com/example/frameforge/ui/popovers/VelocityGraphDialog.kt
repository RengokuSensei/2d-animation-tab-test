package com.example.frameforge.ui.popovers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.CubicBezierCurve
import com.example.frameforge.cinema.VelocityCurveState
import com.example.frameforge.cinema.VelocityGraphMode

private val DialogDarkBg = Color(0xF218181B)
private val DialogBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val VelocityOrange = Color(0xFFFF9500)

@Composable
fun VelocityGraphDialog(
    state: VelocityCurveState,
    onUpdateCurve: (CubicBezierCurve) -> Unit,
    onUpdateMode: (VelocityGraphMode) -> Unit,
    onToggleStepMotion: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var p1x by remember(state.curve) { mutableStateOf(state.curve.p1x) }
    var p1y by remember(state.curve) { mutableStateOf(state.curve.p1y) }
    var p2x by remember(state.curve) { mutableStateOf(state.curve.p2x) }
    var p2y by remember(state.curve) { mutableStateOf(state.curve.p2y) }

    Card(
        modifier = modifier
            .width(420.dp)
            .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
            .testTag("velocity_graph_dialog"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DialogDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Velocity & Value Curve Editor", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Interactive Cubic Bezier Handles & dv/dt Speed Graph", color = Color.Gray, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector: Value Curve vs Velocity Speed Curve
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                    .padding(2.dp)
            ) {
                VelocityGraphMode.values().forEach { mode ->
                    val isSel = mode == state.mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) (if (mode == VelocityGraphMode.VALUE) ProcreateBlue else VelocityOrange) else Color.Transparent)
                            .clickable { onUpdateMode(mode) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            mode.label,
                            color = if (isSel) Color.White else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets Bar
            Text("Curve Presets:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CubicBezierCurve.defaultPresets().forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x220A84FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x440A84FF)),
                        modifier = Modifier.clickable {
                            p1x = preset.p1x
                            p1y = preset.p1y
                            p2x = preset.p2x
                            p2y = preset.p2y
                            onUpdateCurve(preset)
                        }
                    ) {
                        Text(preset.name, color = Color(0xFF7DD3FC), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // INTERACTIVE GRAPH CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F1115))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val touchPos = change.position

                            // Check proximity to Handle 1 or Handle 2
                            val h1Pos = Offset(p1x * canvasW, canvasH - (p1y * canvasH))
                            val h2Pos = Offset(p2x * canvasW, canvasH - (p2y * canvasH))

                            val dist1 = (touchPos - h1Pos).getDistance()
                            val dist2 = (touchPos - h2Pos).getDistance()

                            if (dist1 < dist2) {
                                p1x = (touchPos.x / canvasW).coerceIn(0f, 1f)
                                p1y = ((canvasH - touchPos.y) / canvasH).coerceIn(-0.5f, 1.8f)
                            } else {
                                p2x = (touchPos.x / canvasW).coerceIn(0f, 1f)
                                p2y = ((canvasH - touchPos.y) / canvasH).coerceIn(-0.5f, 1.8f)
                            }

                            onUpdateCurve(
                                CubicBezierCurve(p1x, p1y, p2x, p2y, "Custom Bezier")
                            )
                        }
                    }
                    .testTag("bezier_curve_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    for (i in 1..4) {
                        val gx = w * (i / 5f)
                        val gy = h * (i / 5f)
                        drawLine(Color(0x18FFFFFF), Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        drawLine(Color(0x18FFFFFF), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    }

                    // Baseline (0) and Apex (1) guides
                    drawLine(Color(0x33FFFFFF), Offset(0f, h), Offset(w, h), strokeWidth = 1.5f)
                    drawLine(Color(0x33FFFFFF), Offset(0f, 0f), Offset(w, 0f), strokeWidth = 1.5f)

                    val curve = CubicBezierCurve(p1x, p1y, p2x, p2y)

                    if (state.mode == VelocityGraphMode.VALUE) {
                        // Plot Value Curve
                        val path = Path()
                        path.moveTo(0f, h)

                        val steps = 60
                        for (i in 0..steps) {
                            val t = i.toFloat() / steps
                            val pt = curve.sample(t)
                            val px = pt.x * w
                            val py = h - (pt.y * h)
                            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        drawPath(path, ProcreateBlue, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5f))

                        // Tangent Handles
                        val h1Px = Offset(p1x * w, h - (p1y * h))
                        val h2Px = Offset(p2x * w, h - (p2y * h))

                        drawLine(Color(0x880A84FF), Offset(0f, h), h1Px, strokeWidth = 2f)
                        drawCircle(ProcreateBlue, radius = 6.dp.toPx(), center = h1Px)
                        drawCircle(Color.White, radius = 3.dp.toPx(), center = h1Px)

                        drawLine(Color(0x880A84FF), Offset(w, 0f), h2Px, strokeWidth = 2f)
                        drawCircle(ProcreateBlue, radius = 6.dp.toPx(), center = h2Px)
                        drawCircle(Color.White, radius = 3.dp.toPx(), center = h2Px)
                    } else {
                        // Plot Velocity Speed Curve (dv/dt mountain)
                        val velPath = Path()
                        val fillPath = Path()
                        fillPath.moveTo(0f, h)

                        val steps = 60
                        for (i in 0..steps) {
                            val t = i.toFloat() / steps
                            val vel = curve.evaluateVelocity(t)
                            val px = t * w
                            val py = h - (vel / 3f * h).coerceIn(0f, h)
                            if (i == 0) velPath.moveTo(px, py) else velPath.lineTo(px, py)
                            fillPath.lineTo(px, py)
                        }
                        fillPath.lineTo(w, h)
                        fillPath.close()

                        drawPath(fillPath, Color(0x33FF9500))
                        drawPath(velPath, VelocityOrange, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Coordinates Display & Step-Motion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "P1 (%.2f, %.2f)  •  P2 (%.2f, %.2f)".format(p1x, p1y, p2x, p2y),
                    color = Color.Gray,
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stop-Motion (On-Twos)", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = state.isStepMotionActive,
                        onCheckedChange = onToggleStepMotion,
                        colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                    )
                }
            }
        }
    }
}
