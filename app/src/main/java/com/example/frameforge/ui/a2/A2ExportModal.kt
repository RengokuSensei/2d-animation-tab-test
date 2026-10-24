package com.example.frameforge.ui.a2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

enum class ExportFormat(val label: String, val subtitle: String) {
    MP4("MP4", "Video Animation"),
    PNG("PNG", "Lossless"),
    SVG("SVG", "Vector"),
    DXF("DXF", "CAD Vector"),
    JPG("JPG", "Compressed")
}

enum class ExportRegion(val label: String) {
    ARTBOARD("Artboard"),
    ENTIRE_DRAWING("Entire Drawing"),
    SCREENSHOT("Screenshot"),
    SELECTION("Selection")
}

@Composable
fun A2ExportModal(
    onTriggerExport: (ExportFormat, ExportRegion, Boolean, Boolean, Int) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var format by remember { mutableStateOf(ExportFormat.MP4) }
    var region by remember { mutableStateOf(ExportRegion.ARTBOARD) }
    var includeBg by remember { mutableStateOf(true) }
    var includeGrid by remember { mutableStateOf(false) }
    var exportScale by remember { mutableStateOf(100) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassPill(
            modifier = Modifier
                .width(420.dp)
                .clickable(enabled = false) {},
            cornerRadius = 24.dp,
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Export Animation & Artwork", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Format Picker (MP4, PNG, SVG, DXF, JPG)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Format", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExportFormat.values().forEach { fmt ->
                            val isSel = (format == fmt)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) A2Theme.Accent.copy(alpha = 0.25f) else A2Theme.Ink850)
                                    .border(1.5.dp, if (isSel) A2Theme.Accent else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { format = fmt }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = fmt.label,
                                    color = if (isSel) Color.White else A2Theme.TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = fmt.subtitle,
                                    color = if (isSel) A2Theme.Accent else A2Theme.TextMuted,
                                    fontSize = 8.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Region Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Region", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExportRegion.values().forEach { reg ->
                            val isSel = (region == reg)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color.White else A2Theme.Ink850)
                                    .clickable { region = reg }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reg.label,
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Options (Include Background, Include Grid)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(A2Theme.Ink850)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Include Background", color = Color.White, fontSize = 11.sp)
                        Switch(checked = includeBg, onCheckedChange = { includeBg = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = A2Theme.Accent))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Include Precision Grid", color = Color.White, fontSize = 11.sp)
                        Switch(checked = includeGrid, onCheckedChange = { includeGrid = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = A2Theme.Accent))
                    }
                }

                // Scale Selector (100%, 200%, 400%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Resolution Scale", color = A2Theme.TextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(100, 200, 400).forEach { sc ->
                            val isSel = (exportScale == sc)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) A2Theme.Amber else A2Theme.Ink850)
                                    .clickable { exportScale = sc }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$sc%",
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Blue Export Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2563EB))
                        .clickable { onTriggerExport(format, region, includeBg, includeGrid, exportScale) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Export ${format.label} to Movies/FrameForge",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
