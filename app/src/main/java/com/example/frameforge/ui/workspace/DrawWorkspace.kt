package com.example.frameforge.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.ToolType
import com.example.frameforge.ui.StudioCanvas

private val DockBg = Color(0xEB181A20)
private val DockBorder = Color(0x2EFFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun DrawWorkspace(
    project: AnimationProject,
    currentFrame: Int,
    currentTool: ToolType,
    brushColor: Color,
    brushSize: Float,
    brushOpacity: Float,
    brushSmoothing: Float,
    isPlaying: Boolean,
    onionSkinConfig: OnionSkinConfig,
    onSetTool: (ToolType) -> Unit,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onSelectFrame: (Int) -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onTogglePlay: () -> Unit,
    onToggleOnionSkin: () -> Unit,
    onOpenBrushStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // 1. Edge-to-Edge 144Hz Active Canvas
        StudioCanvas(
            project = project,
            currentFrame = currentFrame,
            currentTool = currentTool,
            brushColor = brushColor,
            brushSize = brushSize,
            brushOpacity = brushOpacity,
            brushSmoothing = brushSmoothing,
            onionSkinConfig = onionSkinConfig,
            selectedBoneId = null,
            isIkMode = false,
            currentStroke = null,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Left Floating Thumb Dock (Brush Size & Opacity Capsule)
        Surface(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .width(52.dp)
                .border(1.dp, DockBorder, RoundedCornerShape(26.dp))
                .testTag("left_thumb_dock"),
            shape = RoundedCornerShape(26.dp),
            color = DockBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterCenter,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pen / Brush Toggle
                IconButton(
                    onClick = {
                        if (currentTool == ToolType.PEN) onOpenBrushStudio() else onSetTool(ToolType.PEN)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (currentTool == ToolType.PEN || currentTool == ToolType.BRUSH) ProcreateBlue else Color.Transparent)
                ) {
                    Icon(
                        Icons.Default.Brush,
                        contentDescription = "Brush Tool",
                        tint = if (currentTool == ToolType.PEN || currentTool == ToolType.BRUSH) Color.White else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Smudge
                IconButton(
                    onClick = { onSetTool(ToolType.SMUDGE) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (currentTool == ToolType.SMUDGE) ProcreateBlue else Color.Transparent)
                ) {
                    Icon(
                        Icons.Outlined.Gesture,
                        contentDescription = "Smudge",
                        tint = if (currentTool == ToolType.SMUDGE) Color.White else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Eraser
                IconButton(
                    onClick = { onSetTool(ToolType.ERASER) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (currentTool == ToolType.ERASER) ProcreateBlue else Color.Transparent)
                ) {
                    Icon(
                        Icons.Outlined.AutoFixHigh,
                        contentDescription = "Eraser",
                        tint = if (currentTool == ToolType.ERASER) Color.White else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Brush Size Indicator & Quick Tap
                Text(
                    text = "${brushSize.toInt()}px",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )

                // Quick Size Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (brushSize <= 10f) ProcreateBlue else Color.DarkGray)
                            .clickable { onSetBrushSize(6f) }
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (brushSize in 11f..25f) ProcreateBlue else Color.DarkGray)
                            .clickable { onSetBrushSize(18f) }
                    )
                }
            }
        }

        // 3. Bottom Minimalist Flipbook Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .border(1.dp, DockBorder, RoundedCornerShape(18.dp))
                .testTag("draw_flipbook_bar"),
            shape = RoundedCornerShape(18.dp),
            color = DockBg,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause & Frame Counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFFEF4444) else ProcreateBlue)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Frame ${currentFrame + 1} / ${project.frameCount.coerceAtLeast(1)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${project.fps} FPS · ${project.width}x${project.height}",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Center: Scrollable Frame Cards Strip
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until project.frameCount) {
                        val isCurrent = i == currentFrame
                        val strokesInFrame = project.layers.sumOf { it.frames[i]?.strokes?.size ?: 0 }

                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) ProcreateBlue else Color(0xFF262A34))
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) Color.White else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectFrame(i) }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterCenter) {
                                Text(
                                    text = "${i + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                                if (strokesInFrame > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color.White else Color(0xFF00E5FF))
                                    )
                                }
                            }
                        }
                    }
                }

                // Right Actions: Add / Duplicate / Delete / Onion Skin
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Onion Skin Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (onionSkinConfig.isEnabled) Color(0x3310B981) else Color(0x22FFFFFF))
                            .clickable { onToggleOnionSkin() }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (onionSkinConfig.isEnabled) Color(0xFF10B981) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (onionSkinConfig.isEnabled) "Onion ON" else "Onion OFF",
                            color = if (onionSkinConfig.isEnabled) Color(0xFF10B981) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Duplicate Frame
                    IconButton(
                        onClick = onDuplicateFrame,
                        modifier = Modifier.size(32.dp).testTag("flipbook_duplicate_button")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Add Frame
                    IconButton(
                        onClick = onAddFrame,
                        modifier = Modifier.size(32.dp).testTag("flipbook_add_button")
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Frame",
                            tint = ProcreateBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Delete Frame
                    IconButton(
                        onClick = onDeleteFrame,
                        modifier = Modifier.size(32.dp).testTag("flipbook_delete_button")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Frame",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
