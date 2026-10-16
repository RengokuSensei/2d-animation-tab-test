package com.example.frameforge.ui.popovers

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.CanvasResolutionPreset

private val GalleryBg = Color(0xF8121418)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun GalleryDialog(
    savedProjects: List<Pair<String, String>>,
    onLoadProject: (String) -> Unit,
    onNewProject: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showNewCanvasMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("gallery_fullscreen_view"),
        color = GalleryBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Gallery",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Procreate Dreams Animation Studio",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // New Canvas "+" button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ProcreateBlue)
                            .clickable { showNewCanvasMenu = true }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("new_canvas_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Canvas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Gallery", tint = Color.LightGray)
                    }
                }
            }

            // New Canvas Presets Dropdown
            if (showNewCanvasMenu) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E232F))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Select Canvas Template", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CanvasResolutionPreset.values().forEach { preset ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onNewProject(preset.label)
                                            showNewCanvasMenu = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF272D3D))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(preset.label.takeWhile { it != '(' }.trim(), color = ProcreateBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("${preset.width} × ${preset.height}", color = Color.LightGray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Projects Grid
            val allProjects = remember(savedProjects) {
                if (savedProjects.isEmpty()) {
                    listOf("Studio Sequence 01" to "24 frames • 1080p")
                } else savedProjects
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allProjects) { (title, subtitle) ->
                    Card(
                        modifier = Modifier
                            .height(160.dp)
                            .clickable {
                                onLoadProject(title)
                                onClose()
                            }
                            .testTag("gallery_project_card_$title"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1E28)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Thumbnail placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF242A38)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(36.dp))
                            }
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
