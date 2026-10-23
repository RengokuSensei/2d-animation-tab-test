package com.example.frameforge.ui.aether

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.ChevronDown
import androidx.compose.material.icons.filled.ChevronUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AetherTimelineDock(
    currentFrame: Int,
    totalFrames: Int,
    isPlaying: Boolean,
    fps: Int,
    onSelectFrame: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onPrevFrame: () -> Unit,
    onNextFrame: () -> Unit,
    onAddFrame: () -> Unit,
    onDuplicateFrame: () -> Unit,
    onSelectFps: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var fpsMenuOpen by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    GlassPill(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // --- 1. COMPACT 40DP SCRUBBER STRIP ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AetherColors.Accent)
                        .clickable { onTogglePlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = AetherColors.Ink950,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Step Prev / Next Buttons
                IconButton(onClick = onPrevFrame, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Prev Frame",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(onClick = onNextFrame, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Frame",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Frame Readout
                Text(
                    text = "F ${(currentFrame + 1).toString().padStart(2, '0')} / ${totalFrames.toString().padStart(2, '0')}",
                    color = AetherColors.Amber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(66.dp)
                )

                // Diamond Keyframe Scrubber Strip
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x14FFFFFF))
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0 until totalFrames) {
                        val isCurrent = i == currentFrame
                        Box(
                            modifier = Modifier
                                .height(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) AetherColors.Amber else Color(0x1AFFFFFF))
                                .clickable { onSelectFrame(i) }
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${i + 1}",
                                    color = if (isCurrent) AetherColors.Ink950 else Color(0xFFA1A1AA),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "◆",
                                    color = if (isCurrent) AetherColors.Ink950 else AetherColors.Accent,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                // Quick Add Frame Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable { onAddFrame() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Frame",
                        tint = AetherColors.Accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Frame",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Duplicate Frame Button
                IconButton(onClick = onDuplicateFrame, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Frame",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // FPS Selector Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1AFFFFFF))
                            .clickable { fpsMenuOpen = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$fps fps",
                            color = Color(0xFFD4D4D8),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    DropdownMenu(
                        expanded = fpsMenuOpen,
                        onDismissRequest = { fpsMenuOpen = false }
                    ) {
                        listOf(8, 12, 24).forEach { f ->
                            DropdownMenuItem(
                                text = { Text("$f fps") },
                                onClick = {
                                    onSelectFps(f)
                                    fpsMenuOpen = false
                                }
                            )
                        }
                    }
                }

                // Expand / Collapse Tracks Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable { isExpanded = !isExpanded }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ChevronDown else Icons.Default.ChevronUp,
                        contentDescription = "Toggle Tracks",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Tracks",
                        color = Color(0xFFD4D4D8),
                        fontSize = 10.sp
                    )
                }
            }

            // --- 2. EXPANDABLE MULTI-TRACK NLE DRAWER ---
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33000000))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MULTI-TRACK NLE SEQUENCER",
                            color = Color(0xFF71717A),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AUDIO SYNC: LOCKED · MASTER 16:9",
                            color = Color(0xFF71717A),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Track 1: Ink Lineart
                    NleTrackRow(
                        trackName = "Ink Lineart",
                        icon = Icons.Default.Edit,
                        color = AetherColors.Accent,
                        totalFrames = totalFrames,
                        currentFrame = currentFrame,
                        onSelectFrame = onSelectFrame
                    )

                    // Track 2: Color Flats
                    NleTrackRow(
                        trackName = "Color Flats",
                        icon = Icons.Default.Palette,
                        color = AetherColors.Amber,
                        totalFrames = totalFrames,
                        currentFrame = currentFrame,
                        onSelectFrame = onSelectFrame
                    )

                    // Track 3: Camera & VFX Keyframe Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.width(96.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera",
                                tint = AetherColors.Purple,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Camera / FX",
                                color = AetherColors.Purple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x22C084FC))
                                .border(1.dp, Color(0x44C084FC), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "◆ Keyframe: Pan In (1.0x) · ◆ Curve: Ease-Out · ◆ Bloom: 18%",
                                color = Color(0xFFE9D5FF),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Track 4: Foley Audio Track
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.width(96.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio",
                                tint = AetherColors.Mint,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Foley Audio",
                                color = AetherColors.Mint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x222DD4BF))
                                .border(1.dp, Color(0x442DD4BF), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "foley_meadow_bounce.wav [ ▄█▄█▄█▄█▄█ ]",
                                color = Color(0xFF99F6E4),
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

@Composable
private fun NleTrackRow(
    trackName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    totalFrames: Int,
    currentFrame: Int,
    onSelectFrame: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.width(96.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = trackName,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = trackName,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .height(22.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            for (i in 0 until totalFrames) {
                val isCurrent = i == currentFrame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCurrent) color.copy(alpha = 0.4f) else Color(0x14FFFFFF))
                        .border(
                            1.dp,
                            if (isCurrent) color else Color(0x1AFFFFFF),
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { onSelectFrame(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "◆",
                        color = if (isCurrent) Color.White else color.copy(alpha = 0.6f),
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}
