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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.BrushCategory
import com.example.frameforge.model.BrushDefinition

private val PopoverBg = Color(0xF218181B)
private val PopoverBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun BrushStudioPopover(
    activeCategory: BrushCategory,
    activeBrush: BrushDefinition,
    onSelectCategory: (BrushCategory) -> Unit,
    onSelectBrush: (BrushDefinition) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var spacing by remember(activeBrush) { mutableStateOf(activeBrush.spacing) }
    var streamline by remember(activeBrush) { mutableStateOf(activeBrush.streamline) }
    var jitter by remember(activeBrush) { mutableStateOf(activeBrush.jitter) }
    var wetMix by remember(activeBrush) { mutableStateOf(activeBrush.wetMix) }

    // Scratchpad test doodle points
    val scratchpadStrokes = remember { mutableStateListOf<List<Offset>>() }
    var currentScratchStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Card(
        modifier = modifier
            .size(540.dp, 380.dp)
            .border(1.dp, PopoverBorder, RoundedCornerShape(16.dp))
            .testTag("brush_studio_popover"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PopoverBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Brush, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Brush Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${activeBrush.name}", color = ProcreateBlue, fontSize = 12.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body: 3-column layout (Categories | Brushes | Tuning & Scratchpad)
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Col 1: Categories
                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .fillMaxHeight()
                        .background(Color(0x18FFFFFF), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BrushCategory.values().forEach { cat ->
                        val isSel = cat == activeCategory
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) ProcreateBlue else Color.Transparent)
                                .clickable { onSelectCategory(cat) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("brush_category_${cat.name.lowercase()}")
                        ) {
                            Text(
                                text = cat.title,
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Col 2: Brushes in Selected Category
                val brushesInCat = remember(activeCategory) {
                    BrushDefinition.defaultBrushes().filter { it.category == activeCategory }
                }

                Column(
                    modifier = Modifier
                        .width(140.dp)
                        .fillMaxHeight()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(10.dp))
                        .padding(4.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    brushesInCat.forEach { brush ->
                        val isSel = brush.id == activeBrush.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSel) Color(0xFF1E293B) else Color.Transparent)
                                .border(if (isSel) 1.dp else 0.dp, if (isSel) ProcreateBlue else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { onSelectBrush(brush) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("brush_preset_${brush.id}"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = brush.name,
                                color = if (isSel) ProcreateBlue else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Col 3: Tuning Sliders & Live Test Scratchpad
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tuning Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stroke Path & Dynamics", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("StreamLine ${(streamline * 100).toInt()}%", color = ProcreateBlue, fontSize = 10.sp)
                        }

                        // Streamline Stabilization
                        Slider(
                            value = streamline,
                            onValueChange = { streamline = it },
                            colors = SliderDefaults.colors(thumbColor = ProcreateBlue, activeTrackColor = ProcreateBlue),
                            modifier = Modifier.height(20.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Spacing ${(spacing * 100).toInt()}%", color = Color.LightGray, fontSize = 10.sp)
                            Text("Wet Mix ${(wetMix * 100).toInt()}%", color = Color.LightGray, fontSize = 10.sp)
                        }

                        Slider(
                            value = spacing,
                            onValueChange = { spacing = it },
                            colors = SliderDefaults.colors(thumbColor = ProcreateBlue, activeTrackColor = ProcreateBlue),
                            modifier = Modifier.height(20.dp)
                        )
                    }

                    // Interactive Scratchpad Drawing Pad
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Live Scratchpad (Doodle Test)", color = Color.Gray, fontSize = 10.sp)
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear Test Pad",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { scratchpadStrokes.clear() }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222630))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentScratchStroke = listOf(offset)
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            currentScratchStroke = currentScratchStroke + change.position
                                        },
                                        onDragEnd = {
                                            if (currentScratchStroke.isNotEmpty()) {
                                                scratchpadStrokes.add(currentScratchStroke)
                                                currentScratchStroke = emptyList()
                                            }
                                        }
                                    )
                                }
                                .testTag("brush_scratchpad_canvas")
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeColor = Color.White
                                for (stroke in scratchpadStrokes) {
                                    if (stroke.size >= 2) {
                                        val path = Path()
                                        path.moveTo(stroke[0].x, stroke[0].y)
                                        for (i in 1 until stroke.size) {
                                            path.lineTo(stroke[i].x, stroke[i].y)
                                        }
                                        drawPath(path, strokeColor, style = Stroke(width = activeBrush.defaultSize * 0.7f))
                                    }
                                }
                                if (currentScratchStroke.size >= 2) {
                                    val path = Path()
                                    path.moveTo(currentScratchStroke[0].x, currentScratchStroke[0].y)
                                    for (i in 1 until currentScratchStroke.size) {
                                        path.lineTo(currentScratchStroke[i].x, currentScratchStroke[i].y)
                                    }
                                    drawPath(path, strokeColor, style = Stroke(width = activeBrush.defaultSize * 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
