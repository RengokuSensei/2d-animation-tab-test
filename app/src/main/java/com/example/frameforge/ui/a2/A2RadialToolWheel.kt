package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ToolType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class RadialToolWedge(
    val tool: ToolType,
    val label: String,
    val baseColor: Color
)

@Composable
fun A2RadialToolWheel(
    currentTool: ToolType,
    onSelectTool: (ToolType) -> Unit,
    brushColor: Color,
    onOpenColorWheel: () -> Unit,
    brushSize: Float,
    onSetBrushSize: (Float) -> Unit,
    brushOpacity: Float,
    onSetBrushOpacity: (Float) -> Unit,
    brushSmoothing: Float,
    onSetBrushSmoothing: (Float) -> Unit,
    onOpenBrushesMarket: () -> Unit,
    canUndo: Boolean = true,
    onUndo: () -> Unit = {},
    canRedo: Boolean = true,
    onRedo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSizePopoverOpen by remember { mutableStateOf(false) }
    var isSmoothPopoverOpen by remember { mutableStateOf(false) }
    var isOpacityPopoverOpen by remember { mutableStateOf(false) }

    val wedges = remember {
        listOf(
            RadialToolWedge(ToolType.PEN, "Pen", Color(0xFF898BFF)),
            RadialToolWedge(ToolType.PENCIL, "Soft Pencil", Color(0xFF64748B)),
            RadialToolWedge(ToolType.FILL, "Fill", Color(0xFFEF6461)),
            RadialToolWedge(ToolType.LASSO, "Lasso", Color(0xFF38BDF8)),
            RadialToolWedge(ToolType.TRANSFORM, "Nudge", Color(0xFFFFBF69)),
            RadialToolWedge(ToolType.SOFT_ERASER, "Slice", Color(0xFFF472B6)),
            RadialToolWedge(ToolType.ERASER, "Hard Mask", Color(0xFF94A3B8)),
            RadialToolWedge(ToolType.WATERCOLOR, "Watercolor", Color(0xFF2DD4BF))
        )
    }

    Box(modifier = modifier) {
        // Concepts Undo & Redo Wing Buttons (outer curved buttons attached to left rim)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-18).dp, y = (-42).dp)
                .size(32.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(A2Theme.Ink900.copy(alpha = 0.94f))
                .border(1.dp, A2Theme.GlassBorder, CircleShape)
                .clickable(enabled = canUndo) { onUndo() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Undo,
                contentDescription = "Undo",
                tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-18).dp, y = 42.dp)
                .size(32.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(A2Theme.Ink900.copy(alpha = 0.94f))
                .border(1.dp, A2Theme.GlassBorder, CircleShape)
                .clickable(enabled = canRedo) { onRedo() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Redo,
                contentDescription = "Redo",
                tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }

        // Main Wheel Disk Container (200dp x 200dp)
        Box(
            modifier = Modifier
                .size(200.dp)
                .shadow(24.dp, CircleShape)
                .clip(CircleShape)
                .background(A2Theme.Ink900.copy(alpha = 0.92f))
                .border(1.5.dp, A2Theme.GlassBorder, CircleShape)
                .pointerInput(wedges, currentTool) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        // Outer ring between r=52dp and r=100dp is the tool wedge area
                        if (dist in 48f..centerX) {
                            var angle = atan2(dy, dx) * (180f / PI.toFloat())
                            if (angle < 0) angle += 360f
                            // 8 wedges of 45 degrees each, offset by -22.5 deg so wedge 0 centers at right
                            val adjustedAngle = (angle + 22.5f) % 360f
                            val wedgeIndex = (adjustedAngle / 45f).toInt().coerceIn(0, 7)
                            onSelectTool(wedges[wedgeIndex].tool)
                        }
                    }
                }
        ) {
            // Draw 8 Radial Wedges & Separator Lines via Compose Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.width / 2f
                val innerRadius = radius * 0.52f
                val center = Offset(radius, radius)

                val sweepAngle = 45f
                for (i in 0 until 8) {
                    val wedge = wedges[i]
                    val startAngle = i * sweepAngle - 22.5f
                    val isSelected = (wedge.tool == currentTool)

                    // Draw wedge background if selected or subtle
                    if (isSelected) {
                        drawArc(
                            color = wedge.baseColor.copy(alpha = 0.35f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = radius - innerRadius)
                        )
                        // Active Arc rim
                        drawArc(
                            color = A2Theme.Accent,
                            startAngle = startAngle + 2f,
                            sweepAngle = sweepAngle - 4f,
                            useCenter = false,
                            topLeft = Offset(center.x - radius + 2f, center.y - radius + 2f),
                            size = Size((radius - 2f) * 2, (radius - 2f) * 2),
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Wedge divider line
                    val rad = (startAngle * PI / 180f).toFloat()
                    val p1 = Offset(center.x + innerRadius * cos(rad), center.y + innerRadius * sin(rad))
                    val p2 = Offset(center.x + radius * cos(rad), center.y + radius * sin(rad))
                    drawLine(
                        color = A2Theme.GlassBorder,
                        start = p1,
                        end = p2,
                        strokeWidth = 1.dp.toPx()
                    )

                    // Wedge label/icon dot
                    val midRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()
                    val dotDist = (innerRadius + radius) / 2f
                    val dotPos = Offset(center.x + dotDist * cos(midRad), center.y + dotDist * sin(midRad))
                    drawCircle(
                        color = if (isSelected) A2Theme.Accent else wedge.baseColor.copy(alpha = 0.8f),
                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                        center = dotPos
                    )
                }

                // Inner Hub Circle border
                drawCircle(
                    color = A2Theme.GlassBorderHighlight,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Central Hub Controls Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(42.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Hub: Stroke Size pts button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(A2Theme.Ink850)
                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            isSizePopoverOpen = !isSizePopoverOpen
                            isSmoothPopoverOpen = false
                            isOpacityPopoverOpen = false
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "= ${String.format("%.1f", brushSize)} pts",
                        color = if (isSizePopoverOpen) A2Theme.Amber else A2Theme.TextPrimary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Center Hub: Active Color Circle Button (taps to open COPIC Color Wheel)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(brushColor)
                        .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                        .clickable { onOpenColorWheel() }
                )

                // Bottom Hub: Smoothing & Opacity Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Smoothing Button (〰 10%)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(A2Theme.Ink850)
                            .clickable {
                                isSmoothPopoverOpen = !isSmoothPopoverOpen
                                isSizePopoverOpen = false
                                isOpacityPopoverOpen = false
                            }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "〰${(brushSmoothing * 100).toInt()}%",
                            color = if (isSmoothPopoverOpen) A2Theme.Accent else A2Theme.TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Opacity Button (◐ 100%)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(A2Theme.Ink850)
                            .clickable {
                                isOpacityPopoverOpen = !isOpacityPopoverOpen
                                isSizePopoverOpen = false
                                isSmoothPopoverOpen = false
                            }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "◐${(brushOpacity * 100).toInt()}%",
                            color = if (isOpacityPopoverOpen) A2Theme.Amber else A2Theme.TextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Corner "Brushes" Pill Button (opens Full Brush Market Drawer)
        GlassPill(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 6.dp)
                .clickable { onOpenBrushesMarket() },
            cornerRadius = 14.dp,
            elevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Brush,
                    contentDescription = "Brushes",
                    tint = A2Theme.Accent,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Brushes",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // FLOATING POPOVER: Size (pts)
        AnimatedVisibility(
            visible = isSizePopoverOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 215.dp, y = 0.dp)
        ) {
            GlassPill(
                modifier = Modifier.width(240.dp),
                cornerRadius = 16.dp,
                elevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Stroke Size",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format("%.1f", brushSize)} pts",
                            color = A2Theme.Accent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = brushSize,
                        onValueChange = onSetBrushSize,
                        valueRange = 1f..80f,
                        colors = SliderDefaults.colors(
                            thumbColor = A2Theme.Accent,
                            activeTrackColor = A2Theme.Accent,
                            inactiveTrackColor = A2Theme.Ink800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(3f, 8f, 30f, 37.5f).forEach { sz ->
                            val isSel = kotlin.math.abs(brushSize - sz) < 1f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color.White else A2Theme.Ink850)
                                    .clickable { onSetBrushSize(sz) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (sz % 1f == 0f) "${sz.toInt()} pt" else "$sz pt",
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING POPOVER: Smoothing (0% to 100% with Tick Marks)
        AnimatedVisibility(
            visible = isSmoothPopoverOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 215.dp, y = 20.dp)
        ) {
            GlassPill(
                modifier = Modifier.width(240.dp),
                cornerRadius = 16.dp,
                elevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Smoothing & Lazy Rope",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(brushSmoothing * 100).toInt()}%",
                            color = A2Theme.Accent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = brushSmoothing,
                        onValueChange = onSetBrushSmoothing,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = A2Theme.Accent,
                            activeTrackColor = A2Theme.Accent,
                            inactiveTrackColor = A2Theme.Ink800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0f, 0.10f, 0.50f, 1.0f).forEach { sm ->
                            val isSel = kotlin.math.abs(brushSmoothing - sm) < 0.05f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color.White else A2Theme.Ink850)
                                    .clickable { onSetBrushSmoothing(sm) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(sm * 100).toInt()}%",
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING POPOVER: Opacity (0% to 100% with Tick Marks)
        AnimatedVisibility(
            visible = isOpacityPopoverOpen,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 215.dp, y = 30.dp)
        ) {
            GlassPill(
                modifier = Modifier.width(240.dp),
                cornerRadius = 16.dp,
                elevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Brush Opacity",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(brushOpacity * 100).toInt()}%",
                            color = A2Theme.Amber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = brushOpacity,
                        onValueChange = onSetBrushOpacity,
                        valueRange = 0f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = A2Theme.Amber,
                            activeTrackColor = A2Theme.Amber,
                            inactiveTrackColor = A2Theme.Ink800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(0f, 0.25f, 0.50f, 1.0f).forEach { op ->
                            val isSel = kotlin.math.abs(brushOpacity - op) < 0.05f
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color.White else A2Theme.Ink850)
                                    .clickable { onSetBrushOpacity(op) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(op * 100).toInt()}%",
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
