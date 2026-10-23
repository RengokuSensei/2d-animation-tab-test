package com.example.frameforge.ui.aether

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ToolType

@Composable
fun AetherStylusDock(
    currentTool: ToolType,
    onSelectTool: (ToolType) -> Unit,
    brushColor: Color,
    onSelectColor: (Color) -> Unit,
    onOpenColorPicker: () -> Unit,
    brushSize: Float,
    onSetBrushSize: (Float) -> Unit,
    onClearLayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSwatches = listOf(
        Color(0xFF2B2D42),
        Color(0xFFEF6461),
        Color(0xFFFFBF69),
        Color(0xFFFFFFFF)
    )

    GlassPill(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterVertically,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // --- 1. TOOL BUTTONS ---
            StylusToolButton(
                icon = Icons.Default.Edit,
                description = "Ink Pen",
                isSelected = currentTool == ToolType.PEN || currentTool == ToolType.INK,
                onClick = { onSelectTool(ToolType.PEN) }
            )
            StylusToolButton(
                icon = Icons.Default.Create,
                description = "Sketch Pencil",
                isSelected = currentTool == ToolType.PENCIL,
                onClick = { onSelectTool(ToolType.PENCIL) }
            )
            StylusToolButton(
                icon = Icons.Default.Highlight,
                description = "Marker / Brush",
                isSelected = currentTool == ToolType.HIGHLIGHTER || currentTool == ToolType.BRUSH,
                onClick = { onSelectTool(ToolType.HIGHLIGHTER) }
            )
            StylusToolButton(
                icon = Icons.Default.AutoFixHigh,
                description = "Soft Eraser",
                isSelected = currentTool == ToolType.ERASER || currentTool == ToolType.SOFT_ERASER,
                onClick = { onSelectTool(ToolType.ERASER) }
            )
            StylusToolButton(
                icon = Icons.Default.FormatColorFill,
                description = "Quick Fill Wash",
                isSelected = currentTool == ToolType.FILL,
                onClick = { onSelectTool(ToolType.FILL) }
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            // --- 2. COLOR SWATCH & QUICK PALETTE ---
            // Active Color Ring
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(brushColor)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onOpenColorPicker() }
            )

            // 4 Mini Swatch Dots
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                quickSwatches.forEach { sw ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(sw)
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .clickable { onSelectColor(sw) }
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            // --- 3. PROCREATE-STYLE VERTICAL BRUSH SIZE SLIDER ---
            Column(
                horizontalAlignment = Alignment.CenterVertically,
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .height(76.dp)
                        .width(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Slider(
                        value = brushSize,
                        onValueChange = onSetBrushSize,
                        valueRange = 2f..48f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = AetherColors.Accent,
                            inactiveTrackColor = Color(0x33FFFFFF)
                        ),
                        modifier = Modifier
                            .size(width = 76.dp, height = 28.dp)
                            .rotate(-90f)
                    )
                }
                Text(
                    text = "${brushSize.toInt()}px",
                    color = Color(0xFFA1A1AA),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // --- 4. CLEAR LAYER BUTTON ---
            IconButton(
                onClick = onClearLayer,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Clear Current Layer",
                    tint = AetherColors.Rose,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StylusToolButton(
    icon: ImageVector,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AetherColors.Accent else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = if (isSelected) AetherColors.Ink950 else Color(0xFFD4D4D8),
            modifier = Modifier.size(17.dp)
        )
    }
}
