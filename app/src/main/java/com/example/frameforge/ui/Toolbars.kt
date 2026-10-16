package com.example.frameforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ToolType

@Composable
fun LeftToolbar(
    currentTool: ToolType,
    brushColor: Color,
    brushSize: Float,
    brushOpacity: Float,
    onSelectTool: (ToolType) -> Unit,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onOpenColorPanel: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSliders by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .width(58.dp)
            .fillMaxHeight()
            .testTag("left_toolbar"),
        color = Color(0xFF161920),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Undo / Redo
            Row(horizontalArrangement = Arrangement.Center) {
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(26.dp).testTag("undo_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(26.dp).testTag("redo_button")
                ) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                }
            }

            Divider(color = Color(0xFF282D37), modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))

            // Center: Tools list with scrolling if needed
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ToolButton(ToolType.PENCIL, Icons.Default.Edit, currentTool, onSelectTool)
                ToolButton(ToolType.PEN, Icons.Default.Create, currentTool, onSelectTool)
                ToolButton(ToolType.INK, Icons.Default.Gesture, currentTool, onSelectTool)
                ToolButton(ToolType.BRUSH, Icons.Default.Brush, currentTool, onSelectTool)
                ToolButton(ToolType.CHARCOAL, Icons.Default.Edit, currentTool, onSelectTool)
                ToolButton(ToolType.WATERCOLOR, Icons.Default.ColorLens, currentTool, onSelectTool)
                ToolButton(ToolType.AIRBRUSH, Icons.Default.AutoAwesome, currentTool, onSelectTool)
                ToolButton(ToolType.PIXEL, Icons.Default.Create, currentTool, onSelectTool)
                ToolButton(ToolType.ERASER, Icons.Outlined.CleaningServices, currentTool, onSelectTool)
                ToolButton(ToolType.SOFT_ERASER, Icons.Outlined.CleaningServices, currentTool, onSelectTool)
                ToolButton(ToolType.FILL, Icons.Default.FormatColorFill, currentTool, onSelectTool)
                ToolButton(ToolType.BONE_RIG, Icons.Outlined.Architecture, currentTool, onSelectTool)
                ToolButton(ToolType.EYEDROPPER, Icons.Default.Colorize, currentTool, onSelectTool)
            }

            Divider(color = Color(0xFF282D37), modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))

            // Bottom: Color swatch circle & Brush size preview
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Color Swatch Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brushColor)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { onOpenColorPanel() }
                        .testTag("color_swatch_button")
                )

                // Brush Size indicator circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF282D37))
                        .clickable { showSliders = !showSliders }
                        .testTag("brush_size_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size((brushSize * 0.4f).coerceIn(4f, 24f).dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: ToolType,
    icon: ImageVector,
    currentTool: ToolType,
    onSelectTool: (ToolType) -> Unit
) {
    val isSelected = tool == currentTool
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF00E5FF) else Color.Transparent)
            .clickable { onSelectTool(tool) }
            .testTag("tool_${tool.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tool.label,
            tint = if (isSelected) Color.Black else Color(0xFFCBD5E1),
            modifier = Modifier.size(22.dp)
        )
    }
}
