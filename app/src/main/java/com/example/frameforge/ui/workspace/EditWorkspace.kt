package com.example.frameforge.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.TimelineClip
import com.example.frameforge.cinema.TimelineClipType
import com.example.frameforge.model.AnimationProject

private val WorkspaceDarkBg = Color(0xFF0F1115)
private val PanelBg = Color(0xFF16181F)
private val PanelBorder = Color(0x2EFFFFFF)
private val AccentBlue = Color(0xFF0A84FF)

@Composable
fun EditWorkspace(
    project: AnimationProject,
    currentFrame: Int,
    isPlaying: Boolean,
    timelineClips: List<TimelineClip>,
    selectedClipId: String?,
    isRippleEditActive: Boolean,
    isMagneticSnappingActive: Boolean,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onSelectClip: (String) -> Unit,
    onSplitClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onToggleRipple: () -> Unit,
    onToggleSnap: () -> Unit,
    onImportVideoClick: () -> Unit,
    onImportAudioClick: () -> Unit,
    onImportPdfClick: () -> Unit,
    onInsertPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMediaMenuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkspaceDarkBg)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- TOP HALF: 16:9 PREVIEW MONITOR & TIMECODE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.42f),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Monitor Frame Canvas
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .fillMaxHeight(0.75f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Frame ${currentFrame + 1} Preview (${project.canvasWidth}x${project.canvasHeight})",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transport Bar (Timecode & Playback)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val seconds = currentFrame / project.fps
                        val frameRemainder = currentFrame % project.fps
                        val timecode = String.format("00:00:%02d:%02d", seconds, frameRemainder)

                        Text(
                            text = timecode,
                            color = Color(0xFF00E5FF),
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        IconButton(
                            onClick = { onSelectFrame((currentFrame - 1).coerceAtLeast(0)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Step Back", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFFEF4444) else AccentBlue)
                        ) {
                            Icon(
                                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSelectFrame((currentFrame + 1).coerceAtMost(project.totalFrames - 1)) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Step Forward", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- MIDDLE ACTION BAR: EDITING CONTROLS & ADD MEDIA ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, PanelBorder, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            color = PanelBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: + Add Media Dropdown
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentBlue)
                            .clickable { isMediaMenuOpen = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("add_media_dropdown_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Media", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Media", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    DropdownMenu(
                        expanded = isMediaMenuOpen,
                        onDismissRequest = { isMediaMenuOpen = false },
                        modifier = Modifier.background(Color(0xFF1E2128))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import 4K Video Clip", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                            onClick = {
                                isMediaMenuOpen = false
                                onImportVideoClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import Audio Track (MP3/WAV)", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color(0xFF10B981)) },
                            onClick = {
                                isMediaMenuOpen = false
                                onImportAudioClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Import PDF Document Pages", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                isMediaMenuOpen = false
                                onImportPdfClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Insert Reference Photo", color = Color.White, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color(0xFF00E5FF)) },
                            onClick = {
                                isMediaMenuOpen = false
                                onInsertPhotoClick()
                            }
                        )
                    }
                }

                // Center: Razor Split / Ripple Delete / Magnetic Snap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Razor Split
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable { onSplitClip() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("razor_split_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = "Split", tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Razor Split", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    // Ripple Edit Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRippleEditActive) Color(0x33A855F7) else Color(0x22FFFFFF))
                            .clickable { onToggleRipple() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("ripple_toggle_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRippleEditActive) "Ripple ON" else "Ripple OFF",
                            color = if (isRippleEditActive) Color(0xFFC084FC) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Magnetic Snap Toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMagneticSnappingActive) Color(0x3310B981) else Color(0x22FFFFFF))
                            .clickable { onToggleSnap() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("snap_toggle_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMagneticSnappingActive) "Snap 🧲" else "Snap OFF",
                            color = if (isMagneticSnappingActive) Color(0xFF34D399) else Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Delete Selected Clip
                    if (selectedClipId != null) {
                        IconButton(
                            onClick = onDeleteClip,
                            modifier = Modifier.size(32.dp).testTag("delete_clip_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Clip", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- BOTTOM HALF: DEDICATED MULTI-TRACK TIMELINE ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.58f),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Timeline Track Headers & Lanes
                val tracks = listOf(
                    Triple("track_v2_cels", "V2 ✏️ 2D Animation Cels", Color(0xFF0A84FF)),
                    Triple("track_v1_video", "V1 🎬 Video Footage", Color(0xFF8B5CF6)),
                    Triple("track_a1_narration", "A1 🎙️ Narration (Voice)", Color(0xFF10B981)),
                    Triple("track_a2_audio", "A2 🎵 Music & Ambience", Color(0xFFF59E0B))
                )

                tracks.forEach { (trackId, trackLabel, trackColor) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .border(0.5.dp, Color(0x1AFFFFFF)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Track Header
                        Box(
                            modifier = Modifier
                                .width(170.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF13151B))
                                .border(0.5.dp, Color(0x22FFFFFF))
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(trackColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = trackLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Right Track Lane (Clips Strip)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(Color(0xFF1A1D24))
                                .horizontalScroll(rememberScrollState())
                        ) {
                            val clipsInTrack = timelineClips.filter { it.trackId == trackId }

                            if (clipsInTrack.isEmpty()) {
                                // Default placeholder bar
                                Box(
                                    modifier = Modifier
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                        .width(360.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(trackColor.copy(alpha = 0.2f))
                                        .border(1.dp, trackColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Empty lane · Tap '+ Add Media' to insert",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxHeight(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    clipsInTrack.forEach { clip ->
                                        val isSelected = clip.id == selectedClipId
                                        val clipWidth = (clip.durationFrames * 12).coerceAtLeast(80).dp

                                        Box(
                                            modifier = Modifier
                                                .padding(vertical = 4.dp)
                                                .width(clipWidth)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) Color(clip.colorArgb) else Color(clip.colorArgb).copy(alpha = 0.6f))
                                                .border(
                                                    width = if (isSelected) 2.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                                .clickable { onSelectClip(clip.id) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Column {
                                                Text(
                                                    text = clip.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${clip.durationFrames} frames",
                                                    color = Color(0xCCFFFFFF),
                                                    fontSize = 9.sp
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
        }
    }
}
