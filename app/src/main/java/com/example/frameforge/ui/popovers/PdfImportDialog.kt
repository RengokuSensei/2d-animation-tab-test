package com.example.frameforge.ui.popovers

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewDay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.frameforge.pdf.PdfEngine
import com.example.frameforge.pdf.PdfImportConfig
import com.example.frameforge.pdf.PdfImportMode
import com.example.frameforge.pdf.PdfPageInfo
import com.example.frameforge.pdf.PdfResolutionPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private val DarkGlassBg = Color(0xF518181B)
private val CardBorderColor = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val ProcreateCyan = Color(0xFF00E5FF)

@Composable
fun PdfImportDialog(
    pdfFile: File?,
    documentTitle: String,
    onDismiss: () -> Unit,
    onLoadSamplePdf: () -> Unit,
    onConfirmImport: (File, PdfImportConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pagesInfo by remember { mutableStateOf<List<PdfPageInfo>>(emptyList()) }
    var isLoadingThumbnails by remember { mutableStateOf(false) }
    var selectedPages by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var pageRangeText by remember { mutableStateOf("") }
    var importMode by remember { mutableStateOf(PdfImportMode.SEQUENTIAL_CELS) }
    var resolutionPreset by remember { mutableStateOf(PdfResolutionPreset.STANDARD_1080P) }
    var holdDurationFrames by remember { mutableFloatStateOf(24f) }
    var removeWhiteBackground by remember { mutableStateOf(false) }
    var invertColors by remember { mutableStateOf(false) }
    var createLinkedLayer by remember { mutableStateOf(true) }
    var paintDirectlyOnPdf by remember { mutableStateOf(false) }

    // Load PDF Page Thumbnails asynchronously via PdfRenderer on Dispatchers.IO
    LaunchedEffect(pdfFile) {
        if (pdfFile == null || !pdfFile.exists()) {
            pagesInfo = emptyList()
            selectedPages = emptySet()
            return@LaunchedEffect
        }

        isLoadingThumbnails = true
        withContext(Dispatchers.IO) {
            try {
                val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                val total = renderer.pageCount
                val list = mutableListOf<PdfPageInfo>()

                for (i in 0 until total) {
                    val page = renderer.openPage(i)
                    val ptW = page.width
                    val ptH = page.height

                    // Generate crisp UI thumbnail
                    val thumbW = 280
                    val thumbH = ((thumbW.toFloat() / ptW) * ptH).toInt().coerceAtLeast(1)
                    val thumbBmp = Bitmap.createBitmap(thumbW, thumbH, Bitmap.Config.ARGB_8888)
                    thumbBmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(thumbBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    list.add(
                        PdfPageInfo(
                            pageIndex = i,
                            widthPt = ptW,
                            heightPt = ptH,
                            thumbnailBitmap = thumbBmp
                        )
                    )
                }
                renderer.close()
                pfd.close()

                pagesInfo = list
                selectedPages = list.indices.toSet() // default select all
                pageRangeText = "1-${list.size}"
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingThumbnails = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.92f)
                .border(1.dp, CardBorderColor, RoundedCornerShape(18.dp))
                .testTag("pdf_import_dialog"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = DarkGlassBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

                // --- HEADER ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ProcreateBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Studio",
                                tint = ProcreateBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PDF Import Studio",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x33FFFFFF))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pagesInfo.size} Pages",
                                        color = ProcreateCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Text(
                                text = documentTitle.ifEmpty { "Select or Load Multi-Page Document" },
                                color = Color.Gray,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = onLoadSamplePdf,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300E5FF)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("load_sample_pdf_button")
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ProcreateCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load Sample 8-Page Storyboard PDF", color = ProcreateCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp).testTag("close_pdf_import_dialog")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- MAIN CONTENT SPLIT (Left: Grid Thumbnails, Right: Configurator) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // --- LEFT COLUMN: PAGE THUMBNAILS GRID & SELECTION FILTERS ---
                    Card(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x66000000)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {

                            // Selection Quick Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    QuickFilterChip("All") {
                                        selectedPages = pagesInfo.indices.toSet()
                                        pageRangeText = if (pagesInfo.isNotEmpty()) "1-${pagesInfo.size}" else ""
                                    }
                                    QuickFilterChip("None") {
                                        selectedPages = emptySet()
                                        pageRangeText = ""
                                    }
                                    QuickFilterChip("Invert") {
                                        val total = pagesInfo.indices.toSet()
                                        selectedPages = total - selectedPages
                                        pageRangeText = selectedPages.map { it + 1 }.joinToString(", ")
                                    }
                                    QuickFilterChip("Odd") {
                                        selectedPages = pagesInfo.indices.filter { it % 2 == 0 }.toSet()
                                        pageRangeText = selectedPages.map { it + 1 }.joinToString(", ")
                                    }
                                    QuickFilterChip("Even") {
                                        selectedPages = pagesInfo.indices.filter { it % 2 != 0 }.toSet()
                                        pageRangeText = selectedPages.map { it + 1 }.joinToString(", ")
                                    }
                                }

                                Text(
                                    text = "${selectedPages.size} of ${pagesInfo.size} Selected",
                                    color = if (selectedPages.isNotEmpty()) ProcreateBlue else Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Page Range Input Field
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = pageRangeText,
                                    onValueChange = { input ->
                                        pageRangeText = input
                                        selectedPages = PdfEngine.parsePageRange(input, pagesInfo.size)
                                    },
                                    placeholder = { Text("Page syntax e.g. 1-3, 5, 8", fontSize = 11.sp, color = Color.Gray) },
                                    label = { Text("Page Range Filter", fontSize = 11.sp) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pdf_page_range_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ProcreateBlue,
                                        unfocusedBorderColor = Color(0x33FFFFFF),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.LightGray,
                                        cursorColor = ProcreateBlue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Thumbnails Grid
                            if (isLoadingThumbnails) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = ProcreateBlue, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Rasterizing PDF Vector Pages...", color = Color.LightGray, fontSize = 12.sp)
                                    }
                                }
                            } else if (pagesInfo.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No PDF Document Loaded", color = Color.LightGray, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("Tap 'Load Sample 8-Page Storyboard PDF' above to test immediately", color = Color.Gray, fontSize = 11.sp)
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 130.dp),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("pdf_page_grid"),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(pagesInfo, key = { it.pageIndex }) { pageInfo ->
                                        val isSelected = selectedPages.contains(pageInfo.pageIndex)
                                        PdfPageCard(
                                            pageInfo = pageInfo,
                                            isSelected = isSelected,
                                            onClick = {
                                                selectedPages = if (isSelected) {
                                                    selectedPages - pageInfo.pageIndex
                                                } else {
                                                    selectedPages + pageInfo.pageIndex
                                                }
                                                pageRangeText = selectedPages.map { it + 1 }.sorted().joinToString(", ")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // --- RIGHT COLUMN: IMPORT MODE & CONFIGURATION OPTIONS ---
                    Card(
                        modifier = Modifier
                            .weight(1.0f)
                            .fillMaxHeight(),
                        colors = CardDefaults.cardColors(containerColor = Color(0x44000000)),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "ANIMATION IMPORT WORKFLOW",
                                color = ProcreateBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mode A / B / C Buttons
                            ImportModeCard(
                                mode = PdfImportMode.SEQUENTIAL_CELS,
                                currentMode = importMode,
                                icon = Icons.Default.ViewCarousel,
                                onSelect = { importMode = it }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ImportModeCard(
                                mode = PdfImportMode.SEPARATE_LAYERS,
                                currentMode = importMode,
                                icon = Icons.Default.Layers,
                                onSelect = { importMode = it }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ImportModeCard(
                                mode = PdfImportMode.STITCHED_SCROLL,
                                currentMode = importMode,
                                icon = Icons.Default.ViewDay,
                                onSelect = { importMode = it }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Hold Duration Slider (when sequential cels active)
                            if (importMode == PdfImportMode.SEQUENTIAL_CELS) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Hold Duration Per Page", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${holdDurationFrames.toInt()} Frames (${String.format("%.1f", holdDurationFrames / 24f)}s)",
                                        color = ProcreateCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Slider(
                                    value = holdDurationFrames,
                                    onValueChange = { holdDurationFrames = it },
                                    valueRange = 1f..120f,
                                    steps = 11,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ProcreateBlue,
                                        activeTrackColor = ProcreateBlue,
                                        inactiveTrackColor = Color(0x33FFFFFF)
                                    ),
                                    modifier = Modifier.testTag("pdf_hold_duration_slider")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Resolution Preset Selector
                            Text("Rasterization Resolution", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x22FFFFFF), RoundedCornerShape(8.dp))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                PdfResolutionPreset.values().forEach { preset ->
                                    val isSelected = preset == resolutionPreset
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) ProcreateBlue else Color.Transparent)
                                            .clickable { resolutionPreset = preset }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset.label.split(" ")[0], // e.g. "Standard", "High-Res", "Ultra"
                                            color = if (isSelected) Color.White else Color.Gray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Pre-processing Toggles
                            SettingToggleRow(
                                title = "Extract Paper to Transparent",
                                subtitle = "Luminance-to-alpha pass makes white paper 100% transparent",
                                isChecked = removeWhiteBackground,
                                onToggle = { removeWhiteBackground = it }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            SettingToggleRow(
                                title = "Dark Mode / Invert Ink",
                                subtitle = "Converts black ink into glowing white chalkboard visuals",
                                isChecked = invertColors,
                                onToggle = { invertColors = it }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            SettingToggleRow(
                                title = "Create Linked Draw Layer",
                                subtitle = "Draw & animate directly above PDF without overwriting pixels",
                                isChecked = createLinkedLayer,
                                onToggle = { createLinkedLayer = it }
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            // Bottom Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel", color = Color.LightGray, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {
                                        if (pdfFile != null && selectedPages.isNotEmpty()) {
                                            val config = PdfImportConfig(
                                                selectedPages = selectedPages,
                                                importMode = importMode,
                                                resolutionPreset = resolutionPreset,
                                                holdDurationFrames = holdDurationFrames.toInt().coerceAtLeast(1),
                                                removeWhiteBackground = removeWhiteBackground,
                                                invertColors = invertColors,
                                                createLinkedDrawingLayerAbove = createLinkedLayer,
                                                paintDirectlyOnPdf = paintDirectlyOnPdf
                                            )
                                            onConfirmImport(pdfFile, config)
                                        }
                                    },
                                    enabled = pdfFile != null && selectedPages.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("commit_pdf_import_button")
                                ) {
                                    Icon(Icons.Default.Movie, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Import ${selectedPages.size} Pages to Video",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
private fun PdfPageCard(
    pageInfo: PdfPageInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ProcreateBlue else Color(0x22FFFFFF),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .testTag("pdf_page_card_${pageInfo.pageIndex}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222A))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (pageInfo.thumbnailBitmap != null) {
                Image(
                    bitmap = pageInfo.thumbnailBitmap.asImageBitmap(),
                    contentDescription = "Page ${pageInfo.displayPageNumber}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .background(Color.White),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.75f)
                        .background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...", color = Color.White, fontSize = 11.sp)
                }
            }

            // Page badge in bottom left
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "P.${pageInfo.displayPageNumber} (${pageInfo.widthPt}×${pageInfo.heightPt})",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Selection badge in top right
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(ProcreateBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickFilterChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x22FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ImportModeCard(
    mode: PdfImportMode,
    currentMode: PdfImportMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: (PdfImportMode) -> Unit
) {
    val isSelected = mode == currentMode
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 1.5f.dp else 1.dp,
                color = if (isSelected) ProcreateBlue else Color(0x22FFFFFF),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onSelect(mode) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ProcreateBlue.copy(alpha = 0.15f) else Color(0x22FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ProcreateBlue else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = mode.title,
                    color = if (isSelected) Color.White else Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = mode.description,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 9.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ProcreateBlue,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color(0x33FFFFFF)
            )
        )
    }
}
