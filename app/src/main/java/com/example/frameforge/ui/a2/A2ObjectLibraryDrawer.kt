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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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

data class ObjectPack(
    val title: String,
    val isPro: Boolean,
    val items: List<String>
)

@Composable
fun A2ObjectLibraryDrawer(
    onSelectObject: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Object Packs") }

    val packs = remember {
        listOf(
            ObjectPack(
                "Hand Drawn Icons",
                true,
                listOf("3D Isometric Box", "Pointing Hand", "Sparkle Star", "Target Bullseye")
            ),
            ObjectPack(
                "People in Elevation",
                true,
                listOf("Architectural Figure 01", "Walking Figure", "Seated Silhouette")
            ),
            ObjectPack(
                "P&ID Engineering Symbols",
                true,
                listOf("Gate Valve 2-Way", "Centrifugal Pump", "Pressure Vessel Tank", "Flow Sensor")
            ),
            ObjectPack(
                "Nuts, Bolts & Mechanical",
                true,
                listOf("Hex Cap Screw M8", "Flat Washer", "Flanged Nut", "Threaded Rod")
            ),
            ObjectPack(
                "Perspective Templates",
                true,
                listOf("1-Point Horizon Grid", "2-Point Angular Grid", "3-Point Worm Eye")
            ),
            ObjectPack(
                "Ribbons & Callout Frames",
                true,
                listOf("Vintage Banner Ribbon", "Curved Scroll", "Modern Tech Box")
            )
        )
    }

    GlassPill(
        modifier = modifier
            .width(360.dp)
            .height(580.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Text(text = "Object Library", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Category Pills
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Favorites", "Object Packs").forEach { cat ->
                        val isSel = (cat == selectedCategory)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color.White else Color.Transparent)
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Search Bar
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "Search vector objects...", color = A2Theme.TextMuted, fontSize = 11.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = A2Theme.TextMuted, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = A2Theme.Accent,
                        unfocusedBorderColor = A2Theme.GlassBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Object Packs List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(packs) { pack ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(A2Theme.Ink850)
                            .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = pack.title, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(A2Theme.Purple.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "PRO PACK", color = A2Theme.Purple, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Horizontal list of vector items
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            pack.items.forEach { item ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(A2Theme.Ink900)
                                        .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(10.dp))
                                        .clickable { onSelectObject(item) }
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        color = A2Theme.TextSecondary,
                                        fontSize = 8.5.sp,
                                        maxLines = 2,
                                        lineHeight = 10.sp
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
