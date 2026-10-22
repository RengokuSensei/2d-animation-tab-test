package com.example.frameforge.ui.popovers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.frameforge.model.ActionsTab
import com.example.frameforge.model.CanvasResolutionPreset
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve

private val PopoverBg = Color(0xF218181B)
private val PopoverBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun ActionsPopover(
    activeTab: ActionsTab,
    isAnimationAssistVisible: Boolean,
    isReferenceWindowOpen: Boolean,
    isRightHanded: Boolean,
    palmRejectionMode: PalmRejectionMode,
    pressureCurve: PressureCurve,
    onSelectTab: (ActionsTab) -> Unit,
    onClose: () -> Unit,
    onSetCanvasResolution: (CanvasResolutionPreset) -> Unit,
    onToggleAnimationAssist: () -> Unit,
    onToggleReferenceWindow: () -> Unit,
    onToggleRightHanded: () -> Unit,
    onSetPalmRejection: (PalmRejectionMode) -> Unit,
    onSetPressureCurve: (PressureCurve) -> Unit,
    onExportMp4: () -> Unit,
    onExportGif: (Boolean) -> Unit,
    onExportPngSequence: () -> Unit,
    onSaveProject: () -> Unit,
    onImportVideoRotoscoping: () -> Unit,
    onImportAudioTrack: () -> Unit,
    onImportPdfDocument: () -> Unit = {},
    onLoadSamplePdf: () -> Unit = {},
    onOpenExcalidrawStudio: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(360.dp)
            .border(1.dp, PopoverBorder, RoundedCornerShape(14.dp))
            .testTag("actions_popover"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PopoverBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header with Tab Bar & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                        .padding(2.dp)
                ) {
                    ActionsTab.values().forEach { tab ->
                        val isSelected = tab == activeTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ProcreateBlue else Color.Transparent)
                                .clickable { onSelectTab(tab) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("actions_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // TAB 1: ADD
            if (activeTab == ActionsTab.ADD) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionRowButton("Excalidraw AI Studio (Text to Diagram / Mermaid / Code)", Icons.Default.AutoAwesome) {
                        onOpenExcalidrawStudio()
                    }
                    ActionRowButton("Import PDF Document (Pages to Video / Layers)", Icons.Default.PictureAsPdf) {
                        onImportPdfDocument()
                    }
                    ActionRowButton("Load Sample Storyboard PDF (8 Pages)", Icons.Default.Description) {
                        onLoadSamplePdf()
                    }
                    ActionRowButton("Import Video for Rotoscoping (4K MP4/MKV)", Icons.Default.Movie) {
                        onImportVideoRotoscoping()
                    }
                    ActionRowButton("Import Audio Track (MP3/WAV/AAC)", Icons.Default.Audiotrack) {
                        onImportAudioTrack()
                    }
                    ActionRowButton("Insert Reference Photo", Icons.Default.Image) {
                        onSaveProject()
                    }
                    ActionRowButton("Add Animated Text Layer", Icons.Default.TextFields) {
                        onSaveProject()
                    }
                    ActionRowButton("Quick Save Project (.ffproj)", Icons.Default.Save) {
                        onSaveProject()
                    }
                }
            }

            // TAB 2: CANVAS (CROP & RESIZE UP TO 4K)
            if (activeTab == ActionsTab.CANVAS) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Canvas Resolution & Format", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    CanvasResolutionPreset.values().forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x18FFFFFF))
                                .clickable { onSetCanvasResolution(preset) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(preset.label, color = Color.White, fontSize = 11.sp)
                            Icon(Icons.Default.Crop, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Canvas Aids & Toggles", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Animation Assist", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = isAnimationAssistVisible,
                            onCheckedChange = { onToggleAnimationAssist() },
                            colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reference Companion Window", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = isReferenceWindowOpen,
                            onCheckedChange = { onToggleReferenceWindow() },
                            colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                        )
                    }
                }
            }

            // TAB 3: SHARE / EXPORT
            if (activeTab == ActionsTab.SHARE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Export Formats", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                    ActionRowButton("Export 4K MP4 Video (H.264 Hardware)", Icons.Default.Movie) {
                        onExportMp4()
                    }
                    ActionRowButton("Export Animated GIF (White Background)", Icons.Default.Animation) {
                        onExportGif(true)
                    }
                    ActionRowButton("Export Transparent Animated GIF", Icons.Default.Animation) {
                        onExportGif(false)
                    }
                    ActionRowButton("Export Transparent PNG Sequence (ZIP)", Icons.Default.FileDownload) {
                        onExportPngSequence()
                    }
                    ActionRowButton("Share Native Project (.ffproj)", Icons.Default.Share) {
                        onSaveProject()
                    }
                }
            }

            // TAB 4: PREFS
            if (activeTab == ActionsTab.PREFS) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Right-Handed Interface", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = isRightHanded,
                            onCheckedChange = { onToggleRightHanded() },
                            colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                        )
                    }

                    Text("Palm Rejection Mode", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    PalmRejectionMode.values().forEach { mode ->
                        val isSel = mode == palmRejectionMode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF1E293B) else Color(0x18FFFFFF))
                                .clickable { onSetPalmRejection(mode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(mode.label, color = if (isSel) ProcreateBlue else Color.White, fontSize = 11.sp)
                        }
                    }

                    Text("Pressure Curve Sensitivity", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    PressureCurve.values().forEach { curve ->
                        val isSel = curve == pressureCurve
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF1E293B) else Color(0x18FFFFFF))
                                .clickable { onSetPressureCurve(curve) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(curve.label, color = if (isSel) ProcreateBlue else Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRowButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x18FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
