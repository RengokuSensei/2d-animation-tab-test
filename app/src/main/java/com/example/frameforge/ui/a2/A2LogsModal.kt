package com.example.frameforge.ui.a2

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.frameforge.log.AppLogger
import com.example.frameforge.log.LogEntry
import com.example.frameforge.log.LogLevel

@Composable
fun A2LogsModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ALL") }
    var refreshKey by remember { mutableStateOf(0) }
    var hasCrash by remember { mutableStateOf(AppLogger.hasCrashReport()) }
    var crashText by remember { mutableStateOf<String?>(null) }
    var logsList by remember { mutableStateOf<List<LogEntry>>(emptyList()) }

    LaunchedEffect(refreshKey, selectedCategory) {
        hasCrash = AppLogger.hasCrashReport()
        if (selectedCategory == "CRASHES") {
            crashText = AppLogger.getLatestCrashReportText() ?: "No crash report found on disk."
        } else {
            logsList = AppLogger.getRecentLogs(if (selectedCategory == "ALL") null else selectedCategory)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            GlassPill(
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .height(640.dp)
                    .clickable(enabled = false) {}, // Prevent dismissing when clicking inside dialog
                cornerRadius = 24.dp,
                elevation = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 1. TOP HEADER: Title, Crash Status, Refresh, Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "STUDIO ACTION & CRASH LOGS",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            if (hasCrash) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(A2Theme.Rose.copy(alpha = 0.25f))
                                        .border(1.dp, A2Theme.Rose, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(A2Theme.Rose)
                                        )
                                        Text(
                                            text = "CRASH DETECTED",
                                            color = A2Theme.Rose,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { refreshKey++ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = A2Theme.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onDismissRequest,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = A2Theme.TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 2. DEVICE TELEMETRY BAR
                    val runtime = Runtime.getRuntime()
                    val usedMem = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
                    val maxMem = runtime.maxMemory() / (1024 * 1024)
                    Text(
                        text = "Device: ${Build.MANUFACTURER} ${Build.MODEL} · Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) · RAM: ${usedMem}MB / ${maxMem}MB heap",
                        color = A2Theme.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 3. CATEGORY FILTER CHIPS
                    val categories = listOf("ALL", "CRASHES", "UI", "TOUCH", "PLAYBACK", "VIDEO", "TIMELINE", "ENGINE", "ERRORS")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSel = selectedCategory == cat
                            val isCrashCat = cat == "CRASHES"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSel && isCrashCat -> A2Theme.Rose
                                            isSel -> A2Theme.Accent
                                            isCrashCat && hasCrash -> A2Theme.Rose.copy(alpha = 0.2f)
                                            else -> Color.White.copy(alpha = 0.08f)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isCrashCat && hasCrash) A2Theme.Rose else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = if (isCrashCat && hasCrash) "🚨 CRASHES" else cat,
                                    color = if (isSel) (if (isCrashCat) Color.White else A2Theme.Ink950) else A2Theme.TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. TERMINAL LOG VIEWPORT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF090D16))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        if (selectedCategory == "CRASHES") {
                            // Full Crash Report Text
                            val scrollState = rememberScrollState()
                            Text(
                                text = crashText ?: "No crash report.",
                                color = if (hasCrash) A2Theme.Rose else A2Theme.TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            )
                        } else {
                            val listState = rememberLazyListState()
                            LaunchedEffect(logsList.size) {
                                if (logsList.isNotEmpty()) {
                                    listState.scrollToItem(logsList.size - 1)
                                }
                            }

                            if (logsList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No logs recorded for category '$selectedCategory'",
                                        color = A2Theme.TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    items(logsList, key = { it.id }) { entry ->
                                        val entryColor = when (entry.level) {
                                            LogLevel.CRASH -> A2Theme.Rose
                                            LogLevel.ERROR -> Color(0xFFEF4444)
                                            LogLevel.WARN -> A2Theme.Amber
                                            LogLevel.INFO -> Color(0xFFE2E8F0)
                                            LogLevel.DEBUG -> Color(0xFF94A3B8)
                                        }

                                        Text(
                                            text = entry.formatDisplay(),
                                            color = entryColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. BOTTOM ACTIONS TOOLBAR: Copy All, Share Log File, Clear Logs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Copy All Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .clickable {
                                        val text = if (selectedCategory == "CRASHES") crashText else null
                                        AppLogger.copyLogsToClipboard(context, text)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Copy All",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Share / Export File Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(A2Theme.Accent.copy(alpha = 0.2f))
                                    .border(1.dp, A2Theme.Accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        AppLogger.shareLogFile(context)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = A2Theme.Accent,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "Share Log File",
                                        color = A2Theme.Accent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Clear Logs Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(A2Theme.Rose.copy(alpha = 0.15f))
                                .clickable {
                                    AppLogger.clearLogs()
                                    refreshKey++
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear",
                                    tint = A2Theme.Rose,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Clear Logs",
                                    color = A2Theme.Rose,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
