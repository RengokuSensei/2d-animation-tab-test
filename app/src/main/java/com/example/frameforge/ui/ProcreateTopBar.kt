package com.example.frameforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CallToAction
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.ToolType

private val ProcreateGlassBg = Color(0xD918181B)
private val ProcreateBorder = Color(0x2EFFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun ProcreateTopBar(
    projectName: String,
    currentTool: ToolType,
    brushColor: Color,
    isActionsOpen: Boolean,
    isAdjustmentsOpen: Boolean,
    isSelectionActive: Boolean,
    isTransformActive: Boolean,
    isBrushStudioOpen: Boolean,
    isLayersOpen: Boolean,
    isColorPickerOpen: Boolean,
    isAnimationAssistVisible: Boolean,
    isDreamsTracksVisible: Boolean,
    isNodeGraphActive: Boolean,
    isExcalidrawOpen: Boolean = false,
    isAutoAdvanceEnabled: Boolean = false,
    isVfxOpen: Boolean = false,
    onToggleAutoAdvance: () -> Unit = {},
    onOpenVfx: () -> Unit = {},
    onGalleryClick: () -> Unit,
    onActionsClick: () -> Unit,
    onAdjustmentsClick: () -> Unit,
    onSelectionClick: () -> Unit,
    onTransformClick: () -> Unit,
    onExcalidrawClick: () -> Unit = {},
    onToggleAnimationAssist: () -> Unit,
    onToggleDreamsTracks: () -> Unit,
    onToggleNodeGraph: () -> Unit,
    onPaintClick: () -> Unit,
    onSmudgeClick: () -> Unit,
    onEraserClick: () -> Unit,
    onLayersClick: () -> Unit,
    onColorPickerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .border(1.dp, ProcreateBorder, RoundedCornerShape(14.dp))
            .testTag("procreate_top_bar"),
        shape = RoundedCornerShape(14.dp),
        color = ProcreateGlassBg,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- LEFT CLUSTER (Gallery, Actions, Adjustments, Selection, Transform) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gallery button
                Text(
                    text = "Gallery",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onGalleryClick() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("gallery_button")
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Actions (Wrench)
                IconButton(
                    onClick = onActionsClick,
                    modifier = Modifier.size(34.dp).testTag("actions_tool_button")
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = "Actions",
                        tint = if (isActionsOpen) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Adjustments (Magic Wand)
                IconButton(
                    onClick = onAdjustmentsClick,
                    modifier = Modifier.size(34.dp).testTag("adjustments_tool_button")
                ) {
                    Icon(
                        Icons.Outlined.AutoFixHigh,
                        contentDescription = "Adjustments",
                        tint = if (isAdjustmentsOpen) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Selection (Ribbon / S icon)
                IconButton(
                    onClick = onSelectionClick,
                    modifier = Modifier.size(34.dp).testTag("selection_tool_button")
                ) {
                    Icon(
                        Icons.Outlined.Gesture,
                        contentDescription = "Selection",
                        tint = if (isSelectionActive) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Transform (Cursor Arrow)
                IconButton(
                    onClick = onTransformClick,
                    modifier = Modifier.size(34.dp).testTag("transform_tool_button")
                ) {
                    Icon(
                        Icons.Default.NearMe,
                        contentDescription = "Transform",
                        tint = if (isTransformActive) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Excalidraw AI & Diagrams
                IconButton(
                    onClick = onExcalidrawClick,
                    modifier = Modifier.size(34.dp).testTag("excalidraw_tool_button")
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = "Excalidraw AI & Diagrams",
                        tint = if (isExcalidrawOpen) Color(0xFFA5B4FC) else Color(0xFF818CF8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // --- CENTER CLUSTER (Animation Assist / Dreams Tracks / Node Graph Switcher) ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                // Animation Assist pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAnimationAssistVisible && !isNodeGraphActive) ProcreateBlue else Color.Transparent)
                        .clickable { onToggleAnimationAssist() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_animation_assist_button")
                ) {
                    Text(
                        text = "Timeline",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Dreams Tracks pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDreamsTracksVisible && !isNodeGraphActive) Color(0xFF38BDF8) else Color.Transparent)
                        .clickable { onToggleDreamsTracks() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_dreams_tracks_button")
                ) {
                    Text(
                        text = "Tracks",
                        color = if (isDreamsTracksVisible) Color.Black else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Node Graph pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNodeGraphActive) Color(0xFF10B981) else Color.Transparent)
                        .clickable { onToggleNodeGraph() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_node_graph_button")
                ) {
                    Text(
                        text = "Node Graph",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // ⚡ Auto-Cel (Freehand Auto-Advance) Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAutoAdvanceEnabled) Color(0xFFFFB300) else Color.Transparent)
                        .clickable { onToggleAutoAdvance() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_auto_advance_button")
                ) {
                    Text(
                        text = if (isAutoAdvanceEnabled) "⚡ Auto ON" else "⚡ Auto",
                        color = if (isAutoAdvanceEnabled) Color.Black else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // ✨ Cinema VFX Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isVfxOpen) Color(0xFFF59E0B) else Color.Transparent)
                        .clickable { onOpenVfx() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("toggle_cinema_vfx_button")
                ) {
                    Text(
                        text = "✨ VFX",
                        color = if (isVfxOpen) Color.Black else Color.LightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // --- RIGHT CLUSTER (Paint, Smudge, Eraser, Layers, Color Disc) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPaintActive = currentTool != ToolType.SMUDGE &&
                        currentTool != ToolType.ERASER &&
                        currentTool != ToolType.SOFT_ERASER &&
                        currentTool != ToolType.TRANSFORM &&
                        currentTool != ToolType.LASSO &&
                        currentTool != ToolType.RECT_SELECT &&
                        currentTool != ToolType.ELLIPSE_SELECT

                // Paint / Brush Studio
                IconButton(
                    onClick = onPaintClick,
                    modifier = Modifier.size(34.dp).testTag("brush_tool_button")
                ) {
                    Icon(
                        Icons.Default.Brush,
                        contentDescription = "Brush Studio",
                        tint = if (isPaintActive || isBrushStudioOpen) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Smudge (Finger)
                IconButton(
                    onClick = onSmudgeClick,
                    modifier = Modifier.size(34.dp).testTag("smudge_tool_button")
                ) {
                    Icon(
                        Icons.Outlined.PanTool,
                        contentDescription = "Smudge",
                        tint = if (currentTool == ToolType.SMUDGE) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Eraser
                IconButton(
                    onClick = onEraserClick,
                    modifier = Modifier.size(34.dp).testTag("eraser_tool_button")
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Eraser",
                        tint = if (currentTool == ToolType.ERASER || currentTool == ToolType.SOFT_ERASER) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Layers (Double Square)
                IconButton(
                    onClick = onLayersClick,
                    modifier = Modifier.size(34.dp).testTag("layers_tool_button")
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = if (isLayersOpen) ProcreateBlue else Color.LightGray,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Color Disc Picker (Circle with active color and outer ring)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (isColorPickerOpen) ProcreateBlue else Color(0x66FFFFFF), CircleShape)
                        .background(brushColor)
                        .clickable { onColorPickerClick() }
                        .testTag("color_disc_button")
                )
            }
        }
    }
}
