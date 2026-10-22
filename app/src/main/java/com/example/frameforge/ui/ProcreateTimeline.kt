package com.example.frameforge.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.PlaybackMode

private val TimelineGlassBg = Color(0xF218181B)
private val TimelineBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun ProcreateTimeline(
    project: AnimationProject,
    currentFrame: Int,
    isPlaying: Boolean,
    playbackMode: PlaybackMode,
    onionSkinConfig: OnionSkinConfig,
    activeLayerId: String,
    isDreamsTracksVisible: Boolean,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onStepFrame: (Int) -> Unit,
    onToggleOnionSkin: () -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onDeleteFrame: () -> Unit,
    onSetFps: (Int) -> Unit,
    onToggleRotoscopingVideo: () -> Unit,
    onSetRotoscopingOpacity: (Float) -> Unit,
    onToggleAudioTrack: () -> Unit,
    onSetAudioVolume: (Float) -> Unit,
    onSplitClip: () -> Unit = {},
    isRippleEdit: Boolean = false,
    onToggleRippleEdit: () -> Unit = {},
    isSnappingActive: Boolean = true,
    onToggleSnapping: () -> Unit = {},
    onOpenVelocityGraph: () -> Unit = {},
    onOpenTransitions: () -> Unit = {},
    onOpenMultiplane: () -> Unit = {},
    timelineClips: List<com.example.frameforge.cinema.TimelineClip> = emptyList(),
    selectedClipId: String? = null,
    onSelectClip: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentFrame) {
        val layoutInfo = lazyListState.layoutInfo
        val visibleIndices = layoutInfo.visibleItemsInfo.map { it.index }
        if (currentFrame !in visibleIndices && project.totalFrames > 0) {
            val target = (currentFrame - 2).coerceAtLeast(0)
            lazyListState.animateScrollToItem(target)
        }
    }

    val strokeCountByFrame = remember(project.cels, project.totalFrames) {
        val counts = IntArray(project.totalFrames)
        for (cel in project.cels) {
            if (cel.frameIndex in 0 until project.totalFrames) {
                counts[cel.frameIndex] += cel.strokes.size
            }
        }
        counts
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .border(1.dp, TimelineBorder, RoundedCornerShape(16.dp))
            .testTag("procreate_timeline"),
        shape = RoundedCornerShape(16.dp),
        color = TimelineGlassBg,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // --- TOP CONTROL & TRANSPORT ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Playback Transport Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step Back
                    IconButton(
                        onClick = { onStepFrame(-1) },
                        modifier = Modifier.size(32.dp).testTag("step_back_button")
                    ) {
                        Icon(Icons.Default.FastRewind, contentDescription = "Step Back", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }

                    // Play / Pause
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isPlaying) Color(0xFFEF4444) else ProcreateBlue, CircleShape)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Step Forward
                    IconButton(
                        onClick = { onStepFrame(1) },
                        modifier = Modifier.size(32.dp).testTag("step_forward_button")
                    ) {
                        Icon(Icons.Default.FastForward, contentDescription = "Step Forward", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Frame Counter Pill
                    Box(
                        modifier = Modifier
                            .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentFrame + 1} / ${project.totalFrames}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // FPS Pill dropdown
                    FpsPill(currentFps = project.fps, onSelectFps = onSetFps)
                }

                // Onion Skin & Action Tools
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // NLE Razor Split
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable { onSplitClip() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("split_clip_button")
                    ) {
                        Text("✂️ Split", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Ripple Edit Mode Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRippleEdit) Color(0xFF8B5CF6) else Color(0x33FFFFFF))
                            .clickable { onToggleRippleEdit() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("ripple_edit_button")
                    ) {
                        Text(if (isRippleEdit) "Ripple ON" else "Ripple", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Magnetic Snapping Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSnappingActive) Color(0xFFFFD700) else Color(0x33FFFFFF))
                            .clickable { onToggleSnapping() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("snap_toggle_button")
                    ) {
                        Text("🧲", color = if (isSnappingActive) Color.Black else Color.LightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Velocity & Bezier Graph Editor
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable { onOpenVelocityGraph() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("open_velocity_graph_button")
                    ) {
                        Text("📈 Curve", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Psychovisual Scene Transitions
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable { onOpenTransitions() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("open_transitions_button")
                    ) {
                        Text("🔀 FX", color = Color(0xFFA78BFA), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 2.5D / 3D Multiplane Space
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable { onOpenMultiplane() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("open_multiplane_button")
                    ) {
                        Text("🌐 3D", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Onion Skin Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (onionSkinConfig.enabled) Color(0xFF10B981) else Color(0x33FFFFFF))
                            .clickable { onToggleOnionSkin() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("onion_skin_pill")
                    ) {
                        Text(
                            text = if (onionSkinConfig.enabled) "Onion Skin ON" else "Onion Skin",
                            color = if (onionSkinConfig.enabled) Color.Black else Color.LightGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Duplicate Frame
                    IconButton(
                        onClick = onDuplicateFrame,
                        modifier = Modifier.size(30.dp).testTag("duplicate_frame_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Frame", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }

                    // Add Frame
                    IconButton(
                        onClick = onAddFrame,
                        modifier = Modifier.size(30.dp).testTag("add_frame_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Frame", tint = ProcreateBlue, modifier = Modifier.size(20.dp))
                    }

                    // Delete Frame
                    IconButton(
                        onClick = onDeleteFrame,
                        modifier = Modifier.size(30.dp).testTag("delete_frame_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Frame", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // --- ANIMATION ASSIST FILMSTRIP (FRAME CEL CARDS) ---
            LazyRow(
                state = lazyListState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(count = project.totalFrames, key = { it }) { frameIdx ->
                    val isSelected = frameIdx == currentFrame
                    val totalStrokes = if (frameIdx < strokeCountByFrame.size) strokeCountByFrame[frameIdx] else 0

                    Card(
                        modifier = Modifier
                            .width(68.dp)
                            .height(52.dp)
                            .clickable { onSelectFrame(frameIdx) }
                            .testTag("frame_thumb_$frameIdx"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF14171E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) ProcreateBlue else Color(0x33FFFFFF)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${frameIdx + 1}",
                                    color = if (isSelected) ProcreateBlue else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (totalStrokes > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(ProcreateBlue, CircleShape)
                                    )
                                }
                            }
                            Text(
                                text = if (totalStrokes > 0) "$totalStrokes marks" else "Blank",
                                color = if (totalStrokes > 0) Color.LightGray else Color(0xFF64748B),
                                fontSize = 8.sp
                            )
                        }
                    }
                }

                // Add Frame trailing button
                item(key = "add_frame_button") {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable { onAddFrame() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Frame", tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // --- PROCREATE DREAMS MULTI-TRACK VIEW (ROTOSCOPING & AUDIO WAVEFORM) ---
            AnimatedVisibility(visible = isDreamsTracksVisible) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // TRACK 1: Video Rotoscoping Track
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1A1F2C), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Movie, contentDescription = "Video Track", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Video Rotoscoping (4K / 30-min)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Opacity ${(project.videoTrack.opacity * 100).toInt()}%",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (project.videoTrack.isEnabled) Color(0xFF0284C7) else Color(0x33FFFFFF))
                                    .clickable { onToggleRotoscopingVideo() }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (project.videoTrack.isEnabled) "TRACK ON" else "TRACK OFF",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // TRACK 2: Audio Track with Interactive Waveform
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF18222A), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Audiotrack, contentDescription = "Audio Track", tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audio Track: ${project.audioTrack.title}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Mini Audio Waveform Visualization
                        Row(
                            modifier = Modifier
                                .width(180.dp)
                                .height(20.dp)
                                .background(Color(0x33000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val samples = project.audioTrack.waveformPeaks.take(28)
                            val progressRatio = (currentFrame.toFloat() / project.totalFrames.coerceAtLeast(1))
                            val playheadIdx = (progressRatio * samples.size).toInt()

                            samples.forEachIndexed { i, peak ->
                                val isPast = i <= playheadIdx
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height((16f * peak).dp.coerceAtLeast(3.dp))
                                        .background(if (isPast) Color(0xFF10B981) else Color(0x66FFFFFF), RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (project.audioTrack.isEnabled) Color(0xFF059669) else Color(0x33FFFFFF))
                                .clickable { onToggleAudioTrack() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (project.audioTrack.isEnabled) "AUDIO ON" else "MUTED",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FpsPill(
    currentFps: Int,
    onSelectFps: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(12, 24, 30, 60)

    Box {
        Box(
            modifier = Modifier
                .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("fps_pill")
        ) {
            Text(
                text = "$currentFps fps",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (expanded) {
            Surface(
                modifier = Modifier
                    .padding(top = 28.dp)
                    .border(1.dp, TimelineBorder, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xF218181B)
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    options.forEach { fps ->
                        Text(
                            text = "$fps fps",
                            color = if (fps == currentFps) ProcreateBlue else Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable {
                                    onSelectFps(fps)
                                    expanded = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
