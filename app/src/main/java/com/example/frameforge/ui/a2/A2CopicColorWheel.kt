package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Colorize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CopicSwatch(
    val code: String,
    val name: String,
    val color: Color
)

enum class ColorWheelMode {
    COPIC,
    HSL,
    RGB
}

@Composable
fun A2CopicColorWheel(
    activeColor: Color,
    onSelectColor: (Color) -> Unit,
    onEyedropper: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(ColorWheelMode.COPIC) }
    var activeCopicLabel by remember { mutableStateOf("R27 · Cadmium Red") }

    // HSL/RGB Sliders state
    var slider1 by remember { mutableFloatStateOf(12f) }  // Hue or Red
    var slider2 by remember { mutableFloatStateOf(85f) }  // Sat or Green
    var slider3 by remember { mutableFloatStateOf(62f) }  // Light or Blue

    // 12 Chromatic COPIC swatches arranged circularly
    val copicSwatches = remember {
        listOf(
            CopicSwatch("R27", "Cadmium Red", Color(0xFFEF6461)),
            CopicSwatch("YR04", "Chrome Orange", Color(0xFFFF8C42)),
            CopicSwatch("Y15", "Cadmium Yellow", Color(0xFFFFBF69)),
            CopicSwatch("YG03", "Yellow Green", Color(0xFFA7C957)),
            CopicSwatch("G07", "Nile Green", Color(0xFF4ECCA3)),
            CopicSwatch("BG15", "Aqua", Color(0xFF2DD4BF)),
            CopicSwatch("B14", "Light Blue", Color(0xFF38BDF8)),
            CopicSwatch("B29", "Cobalt Blue", Color(0xFF6366F1)),
            CopicSwatch("BV04", "Blueberry", Color(0xFF898BFF)),
            CopicSwatch("V06", "Lavender", Color(0xFFA855F7)),
            CopicSwatch("RV29", "Crimson", Color(0xFFEC4899)),
            CopicSwatch("W5", "Warm Gray", Color(0xFF78716C))
        )
    }

    Box(
        modifier = modifier
            .size(340.dp)
            .shadow(28.dp, CircleShape)
            .clip(CircleShape)
            .background(A2Theme.Ink950.copy(alpha = 0.94f))
            .border(2.dp, A2Theme.GlassBorder, CircleShape)
    ) {
        // SVG/Compose Canvas Arc Swatch Fan
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(copicSwatches) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = tapOffset.x - centerX
                        val dy = tapOffset.y - centerY
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        // Outer arc ring between 80dp and 170dp
                        if (dist in 75f..centerX) {
                            var angle = atan2(dy, dx) * (180f / PI.toFloat())
                            if (angle < 0) angle += 360f
                            val sliceAngle = 360f / copicSwatches.size
                            val swatchIndex = (angle / sliceAngle).toInt().coerceIn(0, copicSwatches.size - 1)
                            val selected = copicSwatches[swatchIndex]
                            activeCopicLabel = "${selected.code} · ${selected.name}"
                            onSelectColor(selected.color)
                        }
                    }
                }
        ) {
            val radius = size.width / 2f
            val innerRadius = radius * 0.46f
            val center = Offset(radius, radius)
            val count = copicSwatches.size
            val sliceAngle = 360f / count

            for (i in 0 until count) {
                val swatch = copicSwatches[i]
                val startAngle = i * sliceAngle
                val isSelected = (swatch.color == activeColor)

                // Swatch Arc Wedge
                drawArc(
                    color = swatch.color,
                    startAngle = startAngle + 1f,
                    sweepAngle = sliceAngle - 2f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius + 4f, center.y - radius + 4f),
                    size = Size((radius - 4f) * 2, (radius - 4f) * 2),
                    style = Stroke(width = (radius - innerRadius) - 8f)
                )

                if (isSelected) {
                    drawArc(
                        color = Color.White,
                        startAngle = startAngle,
                        sweepAngle = sliceAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius + 2f, center.y - radius + 2f),
                        size = Size((radius - 2f) * 2, (radius - 2f) * 2),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Inner Cavity Border
            drawCircle(
                color = A2Theme.GlassBorderHighlight,
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Inner Cavity UI: Mode Buttons, Eyedropper, Close, Active COPIC Badge
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mode Selectors Row: COPIC | HSL | RGB
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorWheelMode.values().forEach { m ->
                    val isSel = (mode == m)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color.White else A2Theme.Ink850)
                            .clickable { mode = m }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = m.name,
                            color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Actions: Eyedropper & Close
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Eyedropper Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(A2Theme.Ink850)
                        .border(1.dp, A2Theme.GlassBorder, CircleShape)
                        .clickable { onEyedropper() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Colorize,
                        contentDescription = "Eyedropper",
                        tint = A2Theme.Amber,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(A2Theme.Ink850)
                        .border(1.dp, A2Theme.GlassBorder, CircleShape)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Active Color Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = activeCopicLabel,
                    color = A2Theme.Amber,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Concepts Monochrome Grayscale Swatches Strip (White, C1, C5, C9, Black)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    CopicSwatch("0", "Pure White", Color.White),
                    CopicSwatch("C1", "Cool Gray 1", Color(0xFFE2E8F0)),
                    CopicSwatch("C5", "Cool Gray 5", Color(0xFF94A3B8)),
                    CopicSwatch("C9", "Cool Gray 9", Color(0xFF334155)),
                    CopicSwatch("100", "Deep Black", Color(0xFF0F172A))
                ).forEach { gray ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(gray.color)
                            .border(1.dp, A2Theme.GlassBorder, CircleShape)
                            .clickable {
                                activeCopicLabel = "${gray.code} · ${gray.name}"
                                onSelectColor(gray.color)
                            }
                    )
                }
            }
        }
    }
}
