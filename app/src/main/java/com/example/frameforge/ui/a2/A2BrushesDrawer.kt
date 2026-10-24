package com.example.frameforge.ui.a2

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ToolType

data class BrushCatalogItem(
    val tool: ToolType,
    val name: String,
    val category: String,
    val description: String,
    val accentColor: Color
)

@Composable
fun A2BrushesDrawer(
    currentTool: ToolType,
    onSelectTool: (ToolType) -> Unit,
    activeColor: Color,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scratchPoints = remember { mutableStateListOf<Offset>() }

    val brushCatalog = remember {
        listOf(
            BrushCatalogItem(ToolType.PEN, "Studio Technical Pen", "Basics", "Uniform vector precision with sub-pixel Catmull-Rom smoothing", Color(0xFF898BFF)),
            BrushCatalogItem(ToolType.INK, "Fountain Calligraphy", "Basics", "Expressive velocity-sensitive dynamic line width", Color(0xFF6366F1)),
            BrushCatalogItem(ToolType.PENCIL, "Soft 2B Graphite", "Basics", "Authentic grainy graphite texture with tilt shading", Color(0xFF94A3B8)),
            BrushCatalogItem(ToolType.CHARCOAL, "Fixed Width Wire", "Basics", "Continuous uniform vector stroke for wireframe schematics", Color(0xFF64748B)),
            BrushCatalogItem(ToolType.WATERCOLOR, "Layered Watercolor Wash", "Artistic", "Semi-transparent fluid pigment with edge pooling", Color(0xFF2DD4BF)),
            BrushCatalogItem(ToolType.BRUSH, "Oils & Acrylic Impasto", "Artistic", "Rich blended brush with textural bristle tracks", Color(0xFF4ECCA3)),
            BrushCatalogItem(ToolType.AIRBRUSH, "Soft Airbrush Mist", "Artistic", "Feathered gradient mist for glow and ambient lighting", Color(0xFFFFBF69)),
            BrushCatalogItem(ToolType.FILL, "ColorDrop Flood Fill", "Tools", "Scanline flood fill with edge tolerance", Color(0xFFEF6461)),
            BrushCatalogItem(ToolType.LASSO, "Lasso Selection", "Tools", "Freehand lasso bounding box to move and scale elements", Color(0xFF38BDF8)),
            BrushCatalogItem(ToolType.TRANSFORM, "Nudge & Warp", "Tools", "Push and stretch vector strokes like wire", Color(0xFFF59E0B)),
            BrushCatalogItem(ToolType.SOFT_ERASER, "Slice Knife", "Tools", "Slice through vector lines to trim segments", Color(0xFFEC4899)),
            BrushCatalogItem(ToolType.ERASER, "Hard Mask", "Tools", "Clean vector erase and stroke carving", Color(0xFF94A3B8)),
            BrushCatalogItem(ToolType.PEN, "Crosshatching Fine Pen", "Brush Market", "Multi-stroke hatching for architectural depth", Color(0xFF818CF8)),
            BrushCatalogItem(ToolType.CHARCOAL, "Ink & Sponge Blot", "Brush Market", "Textured organic ink blots and spatter", Color(0xFF34D399)),
            BrushCatalogItem(ToolType.PIXEL, "Halftone Comic Dots", "Brush Market", "Classic pop-art offset printing screen dots", Color(0xFFF472B6)),
            BrushCatalogItem(ToolType.PENCIL, "Wood Parquet Tiling", "Brush Market", "Procedural wood texture fill", Color(0xFFD97706)),
            BrushCatalogItem(ToolType.PEN, "Architectural Slat Wall", "Brush Market", "Precision parallel facade slat texture", Color(0xFFA78BFA)),
            BrushCatalogItem(ToolType.HIGHLIGHTER, "Smart Documentary Marker", "Specialty", "Semi-transparent Multiply overlay for textbook analysis", Color(0xFFFACC15)),
            BrushCatalogItem(ToolType.LASER_POINTER, "Luminous Laser Pointer", "Specialty", "Vibrant Screen glow ribbon for lecturing & presentation", Color(0xFFEF6461))
        )
    }

    GlassPill(
        modifier = modifier
            .width(340.dp)
            .height(560.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Drawer Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = A2Theme.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Brushes & Textures",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { scratchPoints.clear() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Clear Scratchpad",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Interactive Live Brush Scratchpad Banner (340dp x 90dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(A2Theme.Ink950)
                    .border(0.5.dp, A2Theme.GlassBorder)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                scratchPoints.clear()
                                scratchPoints.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                scratchPoints.add(change.position)
                            }
                        )
                    }
            ) {
                // Stroke Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (scratchPoints.size > 1) {
                        val path = Path().apply {
                            moveTo(scratchPoints[0].x, scratchPoints[0].y)
                            for (i in 1 until scratchPoints.size) {
                                val p0 = scratchPoints[i - 1]
                                val p1 = scratchPoints[i]
                                quadraticBezierTo(p0.x, p0.y, (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                            }
                        }
                        drawPath(
                            path = path,
                            color = activeColor,
                            style = Stroke(
                                width = 8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                // Scratchpad Hint Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Scratch here to test brush",
                        color = A2Theme.TextSecondary,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Scrollable Categorized Brush Packs
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(brushCatalog) { item ->
                    val isSelected = (item.tool == currentTool)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) A2Theme.Accent.copy(alpha = 0.18f) else A2Theme.Ink850)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) A2Theme.Accent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectTool(item.tool) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brush Dot Icon
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(item.accentColor.copy(alpha = 0.2f))
                                .border(1.dp, item.accentColor.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = item.name,
                                tint = item.accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Brush Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    color = if (isSelected) Color.White else A2Theme.TextPrimary,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.category,
                                    color = A2Theme.Amber,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = item.description,
                                color = A2Theme.TextSecondary,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
