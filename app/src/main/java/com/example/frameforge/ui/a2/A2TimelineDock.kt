package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.A2VideoClip

@Composable
fun A2TimelineDock(
    currentFrame: Int,
    totalFrames: Int,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    onSelectFrame: (Int) -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    fps: Int,
    onChangeFps: (Int) -> Unit,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    // Real NLE Video Clips & Actions
    videoClips: List<A2VideoClip> = emptyList(),
    selectedClipId: String? = null,
    onSelectClip: (String) -> Unit = {},
    onAddVideoClip: () -> Unit,
    onImportRealVideo: () -> Unit = {},
    onRazorSplit: () -> Unit,
    onTrimIn: () -> Unit,
    onTrimOut: () -> Unit,
    onRippleDelete: () -> Unit,
    onCycleClipSpeed: () -> Unit = {},
    onTogglePipVisible: () -> Unit = {},
    onTogglePipUnderlay: () -> Unit = {},
    isPipVisible: Boolean = true,
    isUnderlayMode: Boolean = false,
    onToggleRecordVoiceover: () -> Unit,
    isRecordingVoiceover: Boolean,
    modifier: Modifier = Modifier
) {
    var a1Gain by remember { mutableFloatStateOf(0f) }
    var a2Gain by remember { mutableFloatStateOf(-2f) }
    var a1Muted by remember { mutableStateOf(false) }
    var a2Muted by remember { mutableStateOf(false) }

    GlassPill(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // COMPACT 40dp SCRUBBER STRIP (Always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(A2Theme.Accent)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = A2Theme.Ink950,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Step Prev
                IconButton(onClick = onStepPrev, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev Frame",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Step Next
                IconButton(onClick = onStepNext, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Frame",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Frame Readout: F 01 / 08
                Text(
                    text = "F ${String.format("%02d", currentFrame + 1)} / ${String.format("%02d", totalFrames)}",
                    color = A2Theme.Amber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(68.dp)
                )

                // Diamond Keyframe Scrubber Strip
                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items((0 until totalFrames).toList()) { idx ->
                        val isCurr = (idx == currentFrame)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurr) A2Theme.Accent else Color.White.copy(alpha = 0.1f))
                                .clickable { onSelectFrame(idx) }
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "◆ ${idx + 1}",
                                color = if (isCurr) A2Theme.Ink950 else A2Theme.TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Add Frame Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onAddFrame() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Frame",
                            tint = A2Theme.Accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Frame",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Duplicate Frame Button
                IconButton(onClick = onDuplicateFrame, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Frame",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // FPS Picker
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Ink850)
                        .clickable {
                            val nextFps = when (fps) {
                                8 -> 12
                                12 -> 24
                                24 -> 8
                                else -> 12
                            }
                            onChangeFps(nextFps)
                        }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$fps fps",
                        color = A2Theme.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // NLE Studio Expand Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(A2Theme.Accent.copy(alpha = 0.2f))
                        .border(1.dp, A2Theme.Accent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { onToggleExpand() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "Expand NLE",
                            tint = A2Theme.Accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "NLE Studio",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // EXPANDED NLE DRAWER (Video, 2D Cels, Voiceover A1, Foley A2)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(A2Theme.Ink950.copy(alpha = 0.95f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // NLE Quick Toolbar: + Add Video, Razor Split, Trim In/Out, Ripple Del, Rec Voiceover VU
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // + Add Video Clip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Accent.copy(alpha = 0.2f))
                                    .border(1.dp, A2Theme.Accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onAddVideoClip() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoLibrary,
                                        contentDescription = "Add Video",
                                        tint = A2Theme.Accent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "+ Video Clip",
                                        color = A2Theme.Accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Import Real Video
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Mint.copy(alpha = 0.15f))
                                    .border(1.dp, A2Theme.Mint.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { onImportRealVideo() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = "Import Video",
                                        tint = A2Theme.Mint,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(text = "Import MP4", color = A2Theme.Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Razor Split
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { onRazorSplit() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCut,
                                        contentDescription = "Split",
                                        tint = A2Theme.Amber,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(text = "Split", color = A2Theme.TextPrimary, fontSize = 10.sp)
                                }
                            }

                            // Trim In
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { onTrimIn() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(text = "[ In", color = A2Theme.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Trim Out
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { onTrimOut() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Out ]", color = A2Theme.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }

                            // Ripple Delete
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Rose.copy(alpha = 0.15f))
                                    .clickable { onRippleDelete() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Ripple Del", color = A2Theme.Rose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            // Clip Speed
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { onCycleClipSpeed() }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Speed",
                                        tint = A2Theme.TextSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(text = "Speed", color = A2Theme.TextPrimary, fontSize = 10.sp)
                                }
                            }

                            // PiP Floating vs Full Underlay Stage Mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isUnderlayMode) A2Theme.Purple.copy(alpha = 0.3f) else A2Theme.Ink850)
                                    .border(1.dp, if (isUnderlayMode) A2Theme.Purple else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { onTogglePipUnderlay() }
                                    .padding(horizontal = 7.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureInPicture,
                                        contentDescription = "PiP Mode",
                                        tint = if (isUnderlayMode) A2Theme.Purple else A2Theme.TextSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (isUnderlayMode) "Underlay" else "PiP Box",
                                        color = if (isUnderlayMode) Color.White else A2Theme.TextPrimary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Right: Record Voiceover & VU Meter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isRecordingVoiceover) A2Theme.Rose else A2Theme.Rose.copy(alpha = 0.2f))
                                    .border(1.dp, A2Theme.Rose, RoundedCornerShape(8.dp))
                                    .clickable { onToggleRecordVoiceover() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isRecordingVoiceover) Color.White else A2Theme.Rose)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Mic",
                                        tint = if (isRecordingVoiceover) Color.White else A2Theme.Rose,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isRecordingVoiceover) "Recording..." else "Rec Voice",
                                        color = if (isRecordingVoiceover) Color.White else A2Theme.Rose,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // VU Level Meter
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf(4.dp, 8.dp, 12.dp, 10.dp, 6.dp, 14.dp, 4.dp).forEachIndexed { i, h ->
                                    val barColor = if (i < 4) A2Theme.Mint else if (i < 6) A2Theme.Amber else A2Theme.Rose
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(barColor)
                                    )
                                }
                            }
                        }
                    }

                    // Track 1: 2D Animation Cels (C1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "C1 2D Cels",
                            color = A2Theme.Accent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(A2Theme.Accent.copy(alpha = 0.15f))
                                .border(1.dp, A2Theme.Accent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Cel Keyframes (F1 - F$totalFrames)",
                                color = A2Theme.Accent,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Track 2: Video PiP Track (V1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "V1 Video PiP",
                            color = A2Theme.Purple,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp)
                        )
                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        ) {
                            val trackWidth = maxWidth
                            val playPct = if (totalFrames > 1) currentFrame.toFloat() / (totalFrames - 1) else 0f

                            if (videoClips.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "No B-Roll clips on Track V1. Tap '+ Video Clip' to add.",
                                        color = A2Theme.TextSecondary,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                videoClips.forEach { clip ->
                                    val isSel = clip.id == selectedClipId
                                    val startFraction = (clip.startPct / 100f).coerceIn(0f, 1f)
                                    val endFraction = (clip.endPct / 100f).coerceIn(0f, 1f)
                                    val clipWidthFraction = (endFraction - startFraction).coerceAtLeast(0.06f)

                                    val clipLeft = trackWidth * startFraction
                                    val clipW = trackWidth * clipWidthFraction
                                    val clipColor = Color(clip.colorHex)

                                    Box(
                                        modifier = Modifier
                                            .offset(x = clipLeft)
                                            .width(clipW)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(clipColor.copy(alpha = if (isSel) 0.95f else 0.75f))
                                            .border(
                                                if (isSel) 2.dp else 1.dp,
                                                if (isSel) A2Theme.Amber else Color.White.copy(alpha = 0.3f),
                                                RoundedCornerShape(5.dp)
                                            )
                                            .clickable { onSelectClip(clip.id) }
                                            .padding(horizontal = 4.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // In Handle
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 5.dp, height = 16.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color.White.copy(alpha = 0.85f))
                                                    .clickable { onTrimIn() }
                                            )

                                            // Title and Speed
                                            Row(
                                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "🎬 ${clip.title}",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${clip.speed}x",
                                                    color = A2Theme.Amber,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Out Handle
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 5.dp, height = 16.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(Color.White.copy(alpha = 0.85f))
                                                    .clickable { onTrimOut() }
                                            )
                                        }
                                    }
                                }
                            }

                            // Needle
                            val playheadX = trackWidth * playPct.coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .offset(x = (playheadX - 1.dp).coerceAtLeast(0.dp))
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(A2Theme.Amber)
                            )
                        }
                    }

                    // Track 3: Audio A1 (Voiceover)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "A1 Voice",
                            color = A2Theme.Rose,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(A2Theme.Rose.copy(alpha = 0.15f))
                                .border(1.dp, A2Theme.Rose.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Narration Voice Track · Gain: ${a1Gain.toInt()}dB",
                                color = A2Theme.Rose,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Track 4: Audio A2 (Foley / BGM)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "A2 Foley",
                            color = A2Theme.Teal,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(70.dp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(A2Theme.Teal.copy(alpha = 0.15f))
                                .border(1.dp, A2Theme.Teal.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Ambient Space Foley & BGM · Gain: ${a2Gain.toInt()}dB",
                                color = A2Theme.Teal,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}
