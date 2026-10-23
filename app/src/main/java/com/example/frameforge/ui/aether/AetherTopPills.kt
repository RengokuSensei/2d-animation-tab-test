package com.example.frameforge.ui.aether

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronDown
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AetherWorkspaceMode {
    DRAW, NODES, VELOCITY, GRADE, STAGE3D
}

@Composable
fun AetherTopPills(
    projectTitle: String,
    onTitleChange: (String) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onTogglePagesPopover: () -> Unit,
    activeMode: AetherWorkspaceMode,
    onSelectMode: (AetherWorkspaceMode) -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    isOnionActive: Boolean,
    onToggleOnionPopover: () -> Unit,
    layerCount: Int,
    onToggleLayersPopover: () -> Unit,
    currentZoom: Float,
    onSelectZoom: (Float) -> Unit,
    onToggleZenMode: () -> Unit,
    onExportMp4: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    var zoomMenuOpen by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 1. LEFT PILL: Project Title + Storyboard Page Switcher ---
        GlassPill(shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AetherColors.Accent)
                )

                // Editable Project Title
                BasicTextField(
                    value = projectTitle,
                    onValueChange = onTitleChange,
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(AetherColors.Accent),
                    modifier = Modifier.width(110.dp),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                        .background(AetherColors.InkLine)
                )

                // Storyboard Page Switcher Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable { onTogglePagesPopover() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Pages",
                        tint = AetherColors.Accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Page $currentPage/$totalPages",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronDown,
                        contentDescription = "Expand",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // --- 2. CENTER PILL: Mode Switcher ---
        GlassPill(shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeButton(
                    label = "Draw",
                    icon = Icons.Default.Edit,
                    color = AetherColors.Accent,
                    isSelected = activeMode == AetherWorkspaceMode.DRAW,
                    onClick = { onSelectMode(AetherWorkspaceMode.DRAW) }
                )
                ModeButton(
                    label = "Nodes",
                    icon = Icons.Default.Hub,
                    color = AetherColors.Purple,
                    isSelected = activeMode == AetherWorkspaceMode.NODES,
                    onClick = { onSelectMode(AetherWorkspaceMode.NODES) }
                )
                ModeButton(
                    label = "Velocity",
                    icon = Icons.Default.Timeline,
                    color = AetherColors.Amber,
                    isSelected = activeMode == AetherWorkspaceMode.VELOCITY,
                    onClick = { onSelectMode(AetherWorkspaceMode.VELOCITY) }
                )
                ModeButton(
                    label = "Grade",
                    icon = Icons.Default.Tune,
                    color = AetherColors.Mint,
                    isSelected = activeMode == AetherWorkspaceMode.GRADE,
                    onClick = { onSelectMode(AetherWorkspaceMode.GRADE) }
                )
                ModeButton(
                    label = "3D",
                    icon = Icons.Default.Category,
                    color = AetherColors.Rose,
                    isSelected = activeMode == AetherWorkspaceMode.STAGE3D,
                    onClick = { onSelectMode(AetherWorkspaceMode.STAGE3D) }
                )
            }
        }

        // --- 3. RIGHT PILL: History, Onion, Layers, Zoom, Zen & Export ---
        GlassPill(shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color(0x44FFFFFF),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color(0x44FFFFFF),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                        .background(AetherColors.InkLine)
                )

                // Onion Skin Pill Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isOnionActive) Color(0x33898BFF) else Color(0x1AFFFFFF))
                        .clickable { onToggleOnionPopover() }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Onion",
                        tint = AetherColors.Accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Onion",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Layers Pill Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable { onToggleLayersPopover() }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = AetherColors.Amber,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${layerCount}L",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Zoom Select Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x1AFFFFFF))
                            .clickable { zoomMenuOpen = true }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(currentZoom * 100).toInt()}%",
                            color = Color(0xFFD4D4D8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    DropdownMenu(
                        expanded = zoomMenuOpen,
                        onDismissRequest = { zoomMenuOpen = false }
                    ) {
                        listOf(0.75f, 0.9f, 1.0f, 1.25f, 1.5f).forEach { z ->
                            DropdownMenuItem(
                                text = { Text("${(z * 100).toInt()}%") },
                                onClick = {
                                    onSelectZoom(z)
                                    zoomMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Zen Fullscreen Button
                IconButton(
                    onClick = onToggleZenMode,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Zen Fullscreen",
                        tint = Color(0xFFD4D4D8),
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Export MP4 Button
                IconButton(
                    onClick = onExportMp4,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x33FFBF69))
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export MP4",
                        tint = AetherColors.Amber,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) AetherColors.Ink950 else color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = if (isSelected) AetherColors.Ink950 else Color(0xFFE4E4E7),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
