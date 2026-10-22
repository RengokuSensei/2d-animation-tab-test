package com.example.frameforge.ui.popovers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import java.io.File

private val ModalDarkBg = Color(0xF2121316)
private val CardInnerBg = Color(0xFF1E2026)
private val ProcreateBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val ProcreateGreen = Color(0xFF10B981)

enum class ExportTab(val title: String) {
    MP4_VIDEO("🎬 MP4 Video"),
    ANIMATED_GIF("🎞️ Animated GIF"),
    FRAME_PNG("🖼️ Frame PNG"),
    PNG_SEQUENCE("📚 PNG Sequence")
}

@Composable
fun ExportStudioDialog(
    totalFrames: Int,
    fps: Int,
    currentFrame: Int,
    onExportMp4: ((File?) -> Unit) -> Unit,
    onExportGif: (Boolean, (File?) -> Unit) -> Unit,
    onExportCurrentFramePng: ((File?) -> Unit) -> Unit,
    onExportPngSequence: ((List<File>) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(ExportTab.MP4_VIDEO) }
    var isExporting by remember { mutableStateOf(false) }
    var exportStatusText by remember { mutableStateOf<String?>(null) }
    var exportedFile by remember { mutableStateOf<File?>(null) }
    var exportedFilesCount by remember { mutableStateOf<Int?>(null) }
    var gifWithBackground by remember { mutableStateOf(true) }

    fun shareExportedFile(file: File, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Exported Animation"))
        } catch (e: Exception) {
            Toast.makeText(context, "Saved to: ${file.name}", Toast.LENGTH_LONG).show()
        }
    }

    Dialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ProcreateBorder, RoundedCornerShape(20.dp))
                .testTag("export_studio_dialog"),
            color = ModalDarkBg,
            tonalElevation = 16.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ProcreateBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Export Studio", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("Broadcast & Master Animation Exporters ($totalFrames frames @ $fps FPS)", color = Color.Gray, fontSize = 11.sp)
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isExporting,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ExportTab.values().forEach { tab ->
                        val isSel = tab == activeTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ProcreateBlue else Color.Transparent)
                                .clickable(enabled = !isExporting) {
                                    activeTab = tab
                                    exportStatusText = null
                                    exportedFile = null
                                    exportedFilesCount = null
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                tab.title,
                                color = if (isSel) Color.White else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardInnerBg),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        when (activeTab) {
                            ExportTab.MP4_VIDEO -> {
                                Text("🎬 H.264 MP4 Cinema Video", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Stream-encoded H.264 video with hardware MediaCodec acceleration. Broadcast-ready for YouTube, Kdenlive, and Premier Pro.", color = Color.Gray, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0x3300E5FF), modifier = Modifier.weight(1f)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Format", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("MP4 (H.264 / AVC)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0x3300E5FF), modifier = Modifier.weight(1f)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Timeline Length", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            val sec = String.format("%.2f", totalFrames.toFloat() / fps.coerceAtLeast(1))
                                            Text("$totalFrames frames (${sec}s)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        isExporting = true
                                        exportStatusText = "Rendering & Encoding MP4..."
                                        onExportMp4 { file ->
                                            isExporting = false
                                            exportedFile = file
                                            exportStatusText = if (file != null) "✅ MP4 Exported: ${file.name} (${file.length() / 1024} KB)" else "❌ MP4 Export failed"
                                        }
                                    },
                                    enabled = !isExporting,
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_mp4_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateBlue)
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Streaming Hardware Encode...", fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export MP4 Video", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            ExportTab.ANIMATED_GIF -> {
                                Text("🎞️ Looped Animated GIF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("High-performance pure Kotlin GIF89a encoder. Ideal for social previews, web animations, and chat stickers.", color = Color.Gray, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = gifWithBackground,
                                        onCheckedChange = { gifWithBackground = it },
                                        colors = CheckboxDefaults.colors(checkedColor = ProcreateBlue)
                                    )
                                    Text("Include solid paper background (uncheck for transparent alpha)", color = Color.White, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        isExporting = true
                                        exportStatusText = "Generating animated GIF..."
                                        onExportGif(gifWithBackground) { file ->
                                            isExporting = false
                                            exportedFile = file
                                            exportStatusText = if (file != null) "✅ GIF Exported: ${file.name} (${file.length() / 1024} KB)" else "❌ Export failed"
                                        }
                                    },
                                    enabled = !isExporting,
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_gif_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Encoding GIF...", fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.Animation, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export Animated GIF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            ExportTab.FRAME_PNG -> {
                                Text("🖼️ Active Cel High-Res PNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Save a crisp full-resolution transparent PNG snapshot of Frame ${currentFrame + 1}.", color = Color.Gray, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        isExporting = true
                                        exportStatusText = "Saving Frame ${currentFrame + 1} PNG..."
                                        onExportCurrentFramePng { file ->
                                            isExporting = false
                                            exportedFile = file
                                            exportStatusText = if (file != null) "✅ Saved Frame ${currentFrame + 1} PNG: ${file.name}" else "❌ Save failed"
                                        }
                                    },
                                    enabled = !isExporting,
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_single_png_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateGreen)
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Frame ${currentFrame + 1} PNG Snapshot", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            ExportTab.PNG_SEQUENCE -> {
                                Text("📚 Numbered PNG Sequence (VFX Pipe)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Renders all $totalFrames animation frames into clean numbered transparent PNGs (frame_001.png, frame_002.png) ready for Kdenlive, Blender Grease Pencil, or Natron compositing.", color = Color.Gray, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        isExporting = true
                                        exportStatusText = "Exporting PNG Sequence..."
                                        onExportPngSequence { files ->
                                            isExporting = false
                                            exportedFilesCount = files.size
                                            if (files.isNotEmpty()) {
                                                exportedFile = files.first().parentFile
                                                exportStatusText = "✅ Exported ${files.size} transparent PNG frames to ${files.first().parentFile?.name}"
                                            } else {
                                                exportStatusText = "❌ Sequence export failed"
                                            }
                                        }
                                    },
                                    enabled = !isExporting,
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_png_seq_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export $totalFrames Frames as PNG Sequence", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Success / Action banner
                        if (exportStatusText != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (exportedFile != null) Color(0x3310B981) else Color(0x33EF4444),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (exportedFile != null) Color(0xFF10B981) else Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        exportStatusText!!,
                                        color = if (exportedFile != null) Color(0xFF34D399) else Color(0xFFF87171),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (exportedFile != null && exportedFile!!.isFile) {
                                        val mime = when (activeTab) {
                                            ExportTab.MP4_VIDEO -> "video/mp4"
                                            ExportTab.ANIMATED_GIF -> "image/gif"
                                            ExportTab.FRAME_PNG -> "image/png"
                                            ExportTab.PNG_SEQUENCE -> "application/octet-stream"
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { shareExportedFile(exportedFile!!, mime) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ProcreateGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Share / Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
