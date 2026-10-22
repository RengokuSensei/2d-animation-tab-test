package com.example.frameforge.ui.popovers

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.BlendModeType

private val PopoverBg = Color(0xF218181B)
private val PopoverBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun LayersPopover(
    project: AnimationProject,
    activeLayerId: String,
    onSelectLayer: (String) -> Unit,
    onAddLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onMergeLayerDown: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onSetOpacity: (String, Float) -> Unit,
    onSetBlendMode: (String, BlendModeType) -> Unit,
    onToggleOnionSkinPin: (String) -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(340.dp)
            .height(420.dp)
            .border(1.dp, PopoverBorder, RoundedCornerShape(16.dp))
            .testTag("layers_popover"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PopoverBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Header: Layers title, "+" add button, close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Layers & Objects", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAddLayer("Layer ${project.layers.size + 1}") },
                        modifier = Modifier.size(28.dp).testTag("add_layer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = ProcreateBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Layers List (displayed top-to-bottom, matching rendering order)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reverse to show top layer first like Procreate
                project.layers.reversed().forEach { layer ->
                    val isSelected = layer.id == activeLayerId

                    LayerItemCard(
                        layer = layer,
                        isSelected = isSelected,
                        onSelect = { onSelectLayer(layer.id) },
                        onToggleVisibility = { onToggleVisibility(layer.id) },
                        onToggleLock = { onToggleLock(layer.id) },
                        onSetOpacity = { onSetOpacity(layer.id, it) },
                        onSetBlendMode = { onSetBlendMode(layer.id, it) },
                        onToggleOnionPin = { onToggleOnionSkinPin(layer.id) },
                        onDuplicate = { onDuplicateLayer(layer.id) },
                        onMergeDown = { onMergeLayerDown(layer.id) },
                        onDelete = { onDeleteLayer(layer.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerItemCard(
    layer: AnimationLayer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onSetOpacity: (Float) -> Unit,
    onSetBlendMode: (BlendModeType) -> Unit,
    onToggleOnionPin: () -> Unit = {},
    onDuplicate: () -> Unit,
    onMergeDown: () -> Unit,
    onDelete: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("layer_card_${layer.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF181C24)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) ProcreateBlue else Color(0x22FFFFFF)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mini thumbnail preview square
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = layer.name.take(1),
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = layer.name,
                                color = if (isSelected) ProcreateBlue else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (layer.posZ != 0f) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "3D",
                                    color = Color(0xFF34D399),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "${(layer.opacity * 100).toInt()}% • ${layer.blendMode.label}",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Onion Skin Pin icon (🧅)
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (layer.onionSkinPinned) Color(0x33FF5252) else Color.Transparent)
                            .clickable { onToggleOnionPin() }
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🧅",
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Lock icon
                    IconButton(onClick = onToggleLock, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock",
                            tint = if (layer.isLocked) Color(0xFFF59E0B) else Color.Gray,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Visibility icon
                    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Visibility",
                            tint = if (layer.isVisible) ProcreateBlue else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Layer Controls (Opacity slider, Blend mode, Actions)
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Opacity", color = Color.LightGray, fontSize = 10.sp)
                    Slider(
                        value = layer.opacity,
                        onValueChange = onSetOpacity,
                        modifier = Modifier.weight(1f).height(18.dp).padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = ProcreateBlue, activeTrackColor = ProcreateBlue)
                    )
                    Text("${(layer.opacity * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    // Duplicate
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Merge Down
                    IconButton(onClick = onMergeDown, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MergeType, contentDescription = "Merge Down", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Delete
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
