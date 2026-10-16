package com.example.frameforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.DrawingCel
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.PlaybackMode

@Composable
fun TimelineBar(
    project: AnimationProject,
    currentFrame: Int,
    isPlaying: Boolean,
    playbackMode: PlaybackMode,
    onionSkinConfig: OnionSkinConfig,
    activeLayerId: String,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onStepFrame: (Int) -> Unit,
    onToggleOnionSkin: () -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onClearCel: () -> Unit,
    onSetFps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showFpsMenu by remember { mutableStateOf(false) }

    // Auto-scroll timeline to follow playhead when playing
    LaunchedEffect(currentFrame) {
        val targetScroll = (currentFrame * 48) - 150
        if (targetScroll > 0) {
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("timeline_container"),
        color = Color(0xFF181B22),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            // Top row: Transport Controls and Frame Manipulation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Transport controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step Previous
                    IconButton(
                        onClick = { onStepFrame(-1) },
                        modifier = Modifier.size(36.dp).testTag("step_prev_button")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous frame", tint = Color.White)
                    }

                    // Play / Pause
                    FilledIconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier.size(40.dp).testTag("play_pause_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isPlaying) Color(0xFFFF9800) else Color(0xFF00E5FF),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play"
                        )
                    }

                    // Step Next
                    IconButton(
                        onClick = { onStepFrame(1) },
                        modifier = Modifier.size(36.dp).testTag("step_next_button")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next frame", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Frame Counter Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF282D37))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        val timeSeconds = if (project.fps > 0) currentFrame.toFloat() / project.fps else 0f
                        Text(
                            text = "Frame ${currentFrame + 1} / ${project.totalFrames} (${"%.2f".format(timeSeconds)}s)",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // FPS Picker
                    Box {
                        FilledTonalButton(
                            onClick = { showFpsMenu = true },
                            modifier = Modifier.height(32.dp).testTag("fps_dropdown")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Playback speed", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${project.fps} FPS", fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = showFpsMenu,
                            onDismissRequest = { showFpsMenu = false }
                        ) {
                            listOf(8, 12, 16, 24, 30, 60).forEach { fps ->
                                DropdownMenuItem(
                                    text = { Text("$fps FPS ${if (fps == 12) "(Animation on 2s)" else if (fps == 24) "(Standard Film)" else ""}") },
                                    onClick = {
                                        onSetFps(fps)
                                        showFpsMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Right: Onion Skin & Frame operations
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Onion Skin Toggle
                    FilledTonalIconButton(
                        onClick = onToggleOnionSkin,
                        modifier = Modifier.size(36.dp).testTag("onion_skin_toggle"),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (onionSkinConfig.enabled) Color(0xFF388E3C) else Color(0xFF2A2E39),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = "Onion Skinning toggle")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Add Frame
                    FilledTonalIconButton(
                        onClick = onAddFrame,
                        modifier = Modifier.size(36.dp).testTag("add_frame_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add frame")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Duplicate Frame
                    FilledTonalIconButton(
                        onClick = onDuplicateFrame,
                        modifier = Modifier.size(36.dp).testTag("duplicate_frame_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate frame")
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Delete Frame
                    FilledTonalIconButton(
                        onClick = onDeleteFrame,
                        modifier = Modifier.size(36.dp).testTag("delete_frame_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete frame")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Multi-Track Timeline Matrix
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                // Layer Headers column (fixed)
                Column(
                    modifier = Modifier
                        .width(110.dp)
                        .background(Color(0xFF14171D))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "Layers (${project.layers.size})",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    project.layers.take(2).forEach { layer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (layer.id == activeLayerId) Color(0xFF263238) else Color.Transparent)
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (layer.isVisible) Color(0xFF00E5FF) else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                layer.name,
                                color = if (layer.id == activeLayerId) Color.White else Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Scrollable frames grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .testTag("timeline_frames_row")
                ) {
                    for (frameIndex in 0 until project.totalFrames) {
                        val isCurrent = frameIndex == currentFrame
                        val celsAtFrame = project.cels.filter { it.frameIndex == frameIndex }
                        val activeCel = celsAtFrame.find { it.layerId == activeLayerId }
                        val hasDrawing = activeCel?.strokes?.isNotEmpty() == true
                        val hasBoneKf = project.boneKeyframes.any { it.frameIndex == frameIndex }

                        Column(
                            modifier = Modifier
                                .width(44.dp)
                                .clickable { onSelectFrame(frameIndex) }
                                .padding(horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Frame number ruler indicator
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(18.dp)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (isCurrent) Color(0xFFFF9800) else Color(0xFF232730)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${frameIndex + 1}",
                                    color = if (isCurrent) Color.Black else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            // Active layer cel cell
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(26.dp)
                                    .background(
                                        when {
                                            isCurrent -> Color(0xFF1E3A8A)
                                            hasDrawing -> Color(0xFF334155)
                                            else -> Color(0xFF1C2029)
                                        }
                                    )
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 0.5.dp,
                                        color = if (isCurrent) Color(0xFF60A5FA) else Color(0xFF334155)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasDrawing) {
                                    // Solid circle indicating drawn content in cel
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color(0xFF93C5FD) else Color(0xFFCBD5E1))
                                    )
                                }
                            }

                            // Rig bone keyframe indicator dot row
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(14.dp)
                                    .background(Color(0xFF14171D)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasBoneKf) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(Color(0xFFFFB74D))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
