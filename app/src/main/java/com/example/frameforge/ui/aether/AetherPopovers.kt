package com.example.frameforge.ui.aether

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.frameforge.model.BlendModeType
import com.example.frameforge.model.Layer

// --- 1. STORYBOARD PAGES POPOVER ---
@Composable
fun StoryboardPagesPopover(
    pages: List<String>,
    currentPageIndex: Int,
    onSelectPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.width(280.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORYBOARD PAGES",
                    color = Color(0xFFA1A1AA),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AetherColors.Accent)
                        .clickable { onAddPage() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Page",
                        tint = AetherColors.Ink950,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "New Page",
                        color = AetherColors.Ink950,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pages List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                pages.forEachIndexed { index, name ->
                    val isSelected = index == currentPageIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x33898BFF) else Color(0x0DFFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) AetherColors.Accent else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectPage(index) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = name,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sequence #${index + 1} · 3 Layers",
                                color = Color(0xFFA1A1AA),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x1AFFFFFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${index + 1}",
                                color = Color(0xFFD4D4D8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- 2. ONION SKIN POPOVER ---
@Composable
fun OnionSkinPopover(
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header with Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Onion Skinning",
                        tint = AetherColors.Accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Onion Skinning",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AetherColors.Accent
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            // Opacity Slider
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ghost Opacity",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        color = AetherColors.Accent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.05f..0.90f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AetherColors.Accent,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }

            // Legend indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AetherColors.Sky)
                    )
                    Text(
                        text = "Prev (-1f)",
                        color = Color(0xFFD4D4D8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AetherColors.Rose)
                    )
                    Text(
                        text = "Next (+1f)",
                        color = Color(0xFFD4D4D8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// --- 3. MULTI-LAYER POPOVER ---
@Composable
fun AetherLayersPopover(
    layers: List<Layer>,
    activeLayerId: String,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onSetOpacity: (String, Float) -> Unit,
    onSetBlendMode: (String, BlendModeType) -> Unit,
    sceneBgVisible: Boolean,
    onToggleSceneBg: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLayer = layers.find { it.id == activeLayerId } ?: layers.firstOrNull()
    var blendMenuOpen by remember { mutableStateOf(false) }

    GlassPill(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.width(300.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = AetherColors.Amber,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "PAGE LAYERS",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable { onToggleSceneBg() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Scene BG: ${if (sceneBgVisible) "ON" else "OFF"}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AetherColors.InkLine)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Layers Stack (Rendered top to bottom)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                layers.asReversed().forEach { layer ->
                    val isSelected = layer.id == activeLayerId
                    val tagColor = when (layer.name.lowercase()) {
                        "ink lineart", "ink" -> AetherColors.Accent
                        "color flats", "color" -> AetherColors.Amber
                        else -> AetherColors.Mint
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0x33898BFF) else Color(0x0DFFFFFF))
                            .border(
                                1.dp,
                                if (isSelected) AetherColors.Accent else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectLayer(layer.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { onToggleVisibility(layer.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Visibility",
                                tint = if (layer.isVisible) Color.White else Color(0x44FFFFFF),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(tagColor)
                        )

                        Text(
                            text = layer.name,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { onToggleLock(layer.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Toggle Lock",
                                tint = if (layer.isLocked) AetherColors.Rose else Color(0x44FFFFFF),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Active Layer Controls
            if (activeLayer != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(AetherColors.InkLine)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Blend Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Blend Mode",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1AFFFFFF))
                                .clickable { blendMenuOpen = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeLayer.blendMode.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        DropdownMenu(
                            expanded = blendMenuOpen,
                            onDismissRequest = { blendMenuOpen = false }
                        ) {
                            listOf(
                                BlendModeType.NORMAL,
                                BlendModeType.MULTIPLY,
                                BlendModeType.SCREEN,
                                BlendModeType.OVERLAY
                            ).forEach { bm ->
                                DropdownMenuItem(
                                    text = { Text(bm.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = {
                                        onSetBlendMode(activeLayer.id, bm)
                                        blendMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Layer Opacity Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Layer Opacity",
                        color = Color(0xFFA1A1AA),
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(activeLayer.opacity * 100).toInt()}%",
                        color = AetherColors.Accent,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = activeLayer.opacity,
                    onValueChange = { onSetOpacity(activeLayer.id, it) },
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = AetherColors.Accent,
                        inactiveTrackColor = Color(0x33FFFFFF)
                    )
                )
            }
        }
    }
}
