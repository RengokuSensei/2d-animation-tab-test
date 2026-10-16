package com.example.frameforge.ui.popovers

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ColorPickerTab
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val PopoverBg = Color(0xF218181B)
private val PopoverBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun ColorPickerPopover(
    currentColor: Color,
    activeTab: ColorPickerTab,
    recentColors: List<Color>,
    customPalette: List<Color>,
    onSelectTab: (ColorPickerTab) -> Unit,
    onColorSelected: (Color) -> Unit,
    onAddToPalette: (Color) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var hue by remember { mutableFloatStateOf(210f) }
    var saturation by remember { mutableFloatStateOf(0.85f) }
    var value by remember { mutableFloatStateOf(0.95f) }

    fun updateColorFromHsv(h: Float, s: Float, v: Float) {
        hue = h.coerceIn(0f, 360f)
        saturation = s.coerceIn(0f, 1f)
        value = v.coerceIn(0f, 1f)
        val argb = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
        onColorSelected(Color(argb))
    }

    Card(
        modifier = modifier
            .width(320.dp)
            .height(410.dp)
            .border(1.dp, PopoverBorder, RoundedCornerShape(16.dp))
            .testTag("color_picker_popover"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PopoverBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Top swatch comparison (active vs previous) + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp, 22.dp)
                            .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                            .background(currentColor)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp, 22.dp)
                            .clip(RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "#" + Integer.toHexString(currentColor.toArgb()).uppercase().takeLast(6),
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body depending on activeTab (Disc, Classic, Harmony, Value, Palettes)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (activeTab) {
                    ColorPickerTab.DISC -> {
                        ProcreateColorDisc(
                            hue = hue,
                            saturation = saturation,
                            value = value,
                            onHsvChange = { h, s, v -> updateColorFromHsv(h, s, v) }
                        )
                    }
                    ColorPickerTab.CLASSIC -> {
                        ProcreateClassicPicker(
                            hue = hue,
                            saturation = saturation,
                            value = value,
                            onHsvChange = { h, s, v -> updateColorFromHsv(h, s, v) }
                        )
                    }
                    ColorPickerTab.HARMONY -> {
                        ProcreateHarmonyPicker(
                            currentColor = currentColor,
                            onSelectColor = { onColorSelected(it) }
                        )
                    }
                    ColorPickerTab.VALUE -> {
                        ProcreateValuePicker(
                            hue = hue,
                            saturation = saturation,
                            value = value,
                            currentColor = currentColor,
                            onHsvChange = { h, s, v -> updateColorFromHsv(h, s, v) }
                        )
                    }
                    ColorPickerTab.PALETTES -> {
                        ProcreatePalettesView(
                            recentColors = recentColors,
                            customPalette = customPalette,
                            currentColor = currentColor,
                            onSelectColor = { onColorSelected(it) },
                            onAddToPalette = { onAddToPalette(currentColor) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Procreate 5-tab Bar (Disc, Classic, Harmony, Value, Palettes)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPickerTab.values().forEach { tab ->
                    val isSel = tab == activeTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) ProcreateBlue else Color.Transparent)
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            color = if (isSel) Color.White else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcreateColorDisc(
    hue: Float,
    saturation: Float,
    value: Float,
    onHsvChange: (Float, Float, Float) -> Unit
) {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val pos = change.position
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = pos.x - center.x
                        val dy = pos.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val outerRadius = size.width / 2f
                        val innerRadius = outerRadius * 0.72f

                        if (dist >= innerRadius * 0.8f) {
                            // Touch on outer Hue ring
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            onHsvChange(angle, saturation, value)
                        } else {
                            // Touch on inner Saturation/Value pad
                            val s = (pos.x / size.width).coerceIn(0f, 1f)
                            val v = (1f - pos.y / size.height).coerceIn(0f, 1f)
                            onHsvChange(hue, s, v)
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 6.dp.toPx()
            val innerRadius = outerRadius - 16.dp.toPx()

            // Outer Rainbow Hue Ring
            val steps = 72
            for (i in 0 until steps) {
                val startAngle = i * (360f / steps)
                val sweep = 360f / steps + 0.5f
                val hsvColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(startAngle, 1f, 1f)))
                drawArc(
                    color = hsvColor,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2),
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                )
            }

            // Outer Hue Handle circle
            val hueRad = Math.toRadians(hue.toDouble())
            val handleDist = outerRadius
            val hx = center.x + (cos(hueRad) * handleDist).toFloat()
            val hy = center.y + (sin(hueRad) * handleDist).toFloat()
            drawCircle(Color.White, 7.dp.toPx(), Offset(hx, hy))
            drawCircle(Color.Black, 5.dp.toPx(), Offset(hx, hy), style = Stroke(width = 2.dp.toPx()))

            // Inner Saturation / Brightness square
            val innerSize = innerRadius * 1.25f
            val left = center.x - innerSize / 2f
            val top = center.y - innerSize / 2f
            val pureHue = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
            drawRect(pureHue, Offset(left, top), androidx.compose.ui.geometry.Size(innerSize, innerSize))

            // Indicator node in inner square
            val nodeX = left + saturation * innerSize
            val nodeY = top + (1f - value) * innerSize
            drawCircle(Color.White, 6.dp.toPx(), Offset(nodeX, nodeY))
            drawCircle(Color.Black, 6.dp.toPx(), Offset(nodeX, nodeY), style = Stroke(width = 2.dp.toPx()))
        }
    }
}

@Composable
private fun ProcreateClassicPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onHsvChange: (Float, Float, Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 2D Square
        Box(
            modifier = Modifier
                .size(240.dp, 140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f))))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / size.width).coerceIn(0f, 1f)
                        val v = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                        onHsvChange(hue, s, v)
                    }
                }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Hue: ${hue.toInt()}°", color = Color.LightGray, fontSize = 10.sp)
        Slider(
            value = hue,
            onValueChange = { onHsvChange(it, saturation, value) },
            valueRange = 0f..360f,
            colors = SliderDefaults.colors(thumbColor = ProcreateBlue, activeTrackColor = ProcreateBlue),
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
private fun ProcreateHarmonyPicker(
    currentColor: Color,
    onSelectColor: (Color) -> Unit
) {
    val hsv = remember(currentColor) {
        val arr = FloatArray(3)
        android.graphics.Color.colorToHSV(currentColor.toArgb(), arr)
        arr
    }
    val compHue = (hsv[0] + 180f) % 360f
    val compColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(compHue, hsv[1], hsv[2])))
    val triadic1 = Color(android.graphics.Color.HSVToColor(floatArrayOf((hsv[0] + 120f) % 360f, hsv[1], hsv[2])))
    val triadic2 = Color(android.graphics.Color.HSVToColor(floatArrayOf((hsv[0] + 240f) % 360f, hsv[1], hsv[2])))

    Column(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Color Harmony (Complementary & Triadic)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HarmonyDot(currentColor, "Base") { onSelectColor(currentColor) }
            HarmonyDot(compColor, "Compl.") { onSelectColor(compColor) }
            HarmonyDot(triadic1, "Triad 1") { onSelectColor(triadic1) }
            HarmonyDot(triadic2, "Triad 2") { onSelectColor(triadic2) }
        }
    }
}

@Composable
private fun HarmonyDot(color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, Color(0x44FFFFFF), CircleShape)
                .clickable { onClick() }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp)
    }
}

@Composable
private fun ProcreateValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    currentColor: Color,
    onHsvChange: (Float, Float, Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Hue", color = Color.Gray, fontSize = 10.sp)
            Text("${hue.toInt()}°", color = Color.White, fontSize = 10.sp)
        }
        Slider(value = hue, onValueChange = { onHsvChange(it, saturation, value) }, valueRange = 0f..360f, modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Saturation", color = Color.Gray, fontSize = 10.sp)
            Text("${(saturation * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
        }
        Slider(value = saturation, onValueChange = { onHsvChange(hue, it, value) }, modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Brightness", color = Color.Gray, fontSize = 10.sp)
            Text("${(value * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
        }
        Slider(value = value, onValueChange = { onHsvChange(hue, saturation, it) }, modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ProcreatePalettesView(
    recentColors: List<Color>,
    customPalette: List<Color>,
    currentColor: Color,
    onSelectColor: (Color) -> Unit,
    onAddToPalette: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Custom Swatches", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x33FFFFFF))
                    .clickable { onAddToPalette() }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Color", color = Color.White, fontSize = 9.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            customPalette.take(8).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(c)
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                        .clickable { onSelectColor(c) }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Recent History", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recentColors.take(8).forEach { c ->
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onSelectColor(c) }
                )
            }
        }
    }
}
