package com.example.frameforge.ui.workspace

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.frameforge.network.RenderFarmNode
import com.example.frameforge.network.RenderJobState
import com.example.frameforge.network.RenderProgressUpdate

private val WorkspaceDarkBg = Color(0xFF0F1115)
private val PanelBg = Color(0xFF16181F)
private val PanelBorder = Color(0x2EFFFFFF)
private val EmeraldGreen = Color(0xFF10B981)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun DeliverWorkspace(
    project: AnimationProject,
    renderFarmNode: RenderFarmNode,
    renderFarmProgress: RenderProgressUpdate,
    localExportState: com.example.frameforge.viewmodel.LocalExportState = com.example.frameforge.viewmodel.LocalExportState(),
    onStartLocalExport: (String) -> Unit,
    onDispatchToRenderFarm: () -> Unit,
    onCancelRender: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf("1080p Cinema") }
    var selectedTarget by remember { mutableStateOf("LOCAL") } // "LOCAL" or "FARM"

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(WorkspaceDarkBg)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- LEFT (50%): RENDER SETTINGS & PRESETS ---
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delivery & Export Hub", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // Project Info Capsule
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E2128)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(project.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${project.canvasWidth}x${project.canvasHeight} · ${project.fps} FPS · ${project.totalFrames} frames", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        val durationSec = project.totalFrames.toFloat() / project.fps
                        Text("${"%.1f".format(durationSec)}s", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Resolution Preset Selection
                Text("EXPORT RESOLUTION PRESETS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("4K UHD", "1080p Cinema", "720p Mobile").forEach { preset ->
                        val isSelected = preset == selectedPreset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ProcreateBlue else Color(0xFF1E2128))
                                .border(1.dp, if (isSelected) Color.White else Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { selectedPreset = preset }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(preset, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Render Engine Target
                Text("RENDER ACCELERATION ENGINE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Local Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTarget == "LOCAL") Color(0x330A84FF) else Color(0xFF1E2128))
                            .border(1.dp, if (selectedTarget == "LOCAL") ProcreateBlue else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                            .clickable { selectedTarget = "LOCAL" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("On-Device Hardware Encoder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Fast Android MediaCodec H.264/H.265 encoding directly to tablet storage", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    // Render Farm Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTarget == "FARM") Color(0x3310B981) else Color(0xFF1E2128))
                            .border(1.dp, if (selectedTarget == "FARM") EmeraldGreen else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                            .clickable { selectedTarget = "FARM" }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Computer, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Laptop NVIDIA GTX 1650 Render Farm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("LAN Wi-Fi dispatch to python daemon with GPU NVENC acceleration", color = Color.Gray, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Start Render Button
                Button(
                    onClick = {
                        if (selectedTarget == "FARM") {
                            onDispatchToRenderFarm()
                        } else {
                            onStartLocalExport(selectedPreset)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("start_deliver_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTarget == "FARM") EmeraldGreen else ProcreateBlue
                    )
                ) {
                    Icon(
                        if (selectedTarget == "FARM") Icons.Default.CloudUpload else Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (selectedTarget == "FARM") "Dispatch to Laptop GTX 1650" else "Export MP4 to Tablet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // --- RIGHT (50%): LIVE RENDER PROGRESS MONITOR ---
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                val isFarmRendering = renderFarmProgress.state == RenderJobState.RENDERING || renderFarmProgress.state == RenderJobState.UPLOADING
                val isLocalRendering = localExportState.isExporting
                val hasExported = localExportState.exportedFile != null
                val context = androidx.compose.ui.platform.LocalContext.current

                if (isLocalRendering) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { localExportState.progress },
                            modifier = Modifier.size(80.dp),
                            color = ProcreateBlue,
                            strokeWidth = 6.dp
                        )

                        Text(
                            text = "Exporting Frame ${localExportState.currentFrame} / ${localExportState.totalFrames}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "${(localExportState.progress * 100).toInt()}% Rendered to Tablet",
                            color = ProcreateBlue,
                            fontSize = 12.sp
                        )
                    }
                } else if (hasExported) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(54.dp))
                        Text("Export Complete!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Saved to Movies/FrameForge (${localExportState.exportedFile?.name})",
                            color = EmeraldGreen,
                            fontSize = 12.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            localExportState.galleryUri?.let { uri ->
                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "video/mp4")
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Video", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share Animation Video"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share MP4", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else if (isFarmRendering) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { renderFarmProgress.progressPercentage / 100f },
                            modifier = Modifier.size(80.dp),
                            color = EmeraldGreen,
                            strokeWidth = 6.dp
                        )

                        Text(
                            text = "Rendering Frame ${renderFarmProgress.currentFrame} / ${renderFarmProgress.totalFrames}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "${renderFarmProgress.progressPercentage.toInt()}% Completed · ${"%.1f".format(renderFarmProgress.currentFps)} FPS",
                            color = EmeraldGreen,
                            fontSize = 12.sp
                        )

                        Button(
                            onClick = onCancelRender,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel Render", fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                        Text("Ready to Render", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Select your settings on the left and tap export to start.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
