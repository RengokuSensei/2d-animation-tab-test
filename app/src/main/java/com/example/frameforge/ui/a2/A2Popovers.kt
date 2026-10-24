package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve

// =========================================================================
// 1. STORYBOARD PAGES POPOVER
// =========================================================================
@Composable
fun A2PagesPopover(
    currentPage: Int,
    totalPages: Int,
    onSelectPage: (Int) -> Unit,
    onAddPage: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier.width(260.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storyboard Pages",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Accent)
                            .clickable { onAddPage() }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "+ Page", color = A2Theme.Ink950, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..totalPages).forEach { p ->
                    val isCurr = (p == currentPage)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurr) A2Theme.Accent.copy(alpha = 0.2f) else A2Theme.Ink850)
                            .border(1.dp, if (isCurr) A2Theme.Accent else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onSelectPage(p) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Page $p · Key Sequence",
                            color = if (isCurr) Color.White else A2Theme.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isCurr) {
                            Text(text = "Active", color = A2Theme.Accent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 2. ONION SKINNING POPOVER
// =========================================================================
@Composable
fun A2OnionPopover(
    isEnabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier.width(240.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Onion Skinning", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isEnabled,
                        onCheckedChange = onToggleEnabled,
                        colors = CheckboxDefaults.colors(checkedColor = A2Theme.Accent, uncheckedColor = A2Theme.TextSecondary),
                        modifier = Modifier.size(24.dp)
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Ghost Opacity", color = A2Theme.TextSecondary, fontSize = 10.sp)
                    Text(text = "${(opacity * 100).toInt()}%", color = A2Theme.Accent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.1f..0.9f,
                    colors = SliderDefaults.colors(thumbColor = A2Theme.Accent, activeTrackColor = A2Theme.Accent, inactiveTrackColor = A2Theme.Ink800)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
                    Text(text = "Prev (-1f)", color = A2Theme.TextSecondary, fontSize = 9.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFF43F5E)))
                    Text(text = "Next (+1f)", color = A2Theme.TextSecondary, fontSize = 9.sp)
                }
            }
        }
    }
}

// =========================================================================
// 3. LAYERS POPOVER
// =========================================================================
@Composable
fun A2LayersPopover(
    layers: List<AnimationLayer>,
    activeLayerId: String,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onAddLayer: () -> Unit,
    isSceneBgVisible: Boolean,
    onToggleSceneBg: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier.width(280.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Page Layers", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSceneBgVisible) A2Theme.Amber.copy(alpha = 0.2f) else A2Theme.Ink850)
                            .clickable { onToggleSceneBg() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isSceneBgVisible) "BG: ON" else "BG: OFF",
                            color = if (isSceneBgVisible) A2Theme.Amber else A2Theme.TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Accent)
                            .clickable { onAddLayer() }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(text = "+ Layer", color = A2Theme.Ink950, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Layer Stack
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                layers.reversed().forEach { layer ->
                    val isCurr = (layer.id == activeLayerId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurr) A2Theme.Accent.copy(alpha = 0.2f) else A2Theme.Ink850)
                            .border(1.dp, if (isCurr) A2Theme.Accent else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onSelectLayer(layer.id) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(onClick = { onToggleVisibility(layer.id) }, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Vis",
                                    tint = if (layer.isVisible) Color.White else A2Theme.TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = layer.name,
                                color = if (isCurr) Color.White else A2Theme.TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isCurr) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        IconButton(onClick = { onToggleLock(layer.id) }, modifier = Modifier.size(20.dp)) {
                            Icon(
                                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Lock",
                                tint = if (layer.isLocked) A2Theme.Amber else A2Theme.TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. STYLUS & PALM REJECTION SETTINGS MODAL
// =========================================================================
@Composable
fun A2StylusSettingsModal(
    palmMode: PalmRejectionMode,
    onPalmModeChange: (PalmRejectionMode) -> Unit,
    pressureCurve: PressureCurve,
    onPressureCurveChange: (PressureCurve) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        GlassPill(
            modifier = Modifier
                .width(380.dp)
                .clickable(enabled = false) {},
            cornerRadius = 24.dp,
            elevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stylus & Palm Rejection",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(26.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = A2Theme.TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Finger Action (Palm Rejection)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Finger Action (Palm Rejection)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Separates finger touch from active 144Hz stylus pen", color = A2Theme.TextSecondary, fontSize = 9.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            PalmRejectionMode.AUTOMATIC to "Auto Reject",
                            PalmRejectionMode.STYLUS_ONLY to "Stylus Only",
                            PalmRejectionMode.FINGER_AND_STYLUS to "Allow Touch"
                        ).forEach { (m, lbl) ->
                            val isSel = (palmMode == m)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) A2Theme.Amber else A2Theme.Ink850)
                                    .clickable { onPalmModeChange(m) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = lbl,
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Stylus Pressure Response Curve
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Pressure Response Curve", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PressureCurve.values().forEach { curve ->
                            val isSel = (pressureCurve == curve)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) A2Theme.Accent else A2Theme.Ink850)
                                    .clickable { onPressureCurveChange(curve) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curve.name,
                                    color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Gestures Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Ink850)
                            .padding(8.dp)
                    ) {
                        Text(text = "2-Finger Tap: Undo", color = A2Theme.Accent, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Ink850)
                            .padding(8.dp)
                    ) {
                        Text(text = "3-Finger Tap: Redo", color = A2Theme.Amber, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                // Done Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(A2Theme.Accent)
                        .clickable { onClose() }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Done", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// =========================================================================
// 5. TEXT / FORMULA CALLOUT MODAL
// =========================================================================
@Composable
fun A2TextPromptModal(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textVal by remember { mutableStateOf("E = mc² · Kinetic Leap") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        GlassPill(
            modifier = Modifier
                .width(360.dp)
                .clickable(enabled = false) {},
            cornerRadius = 20.dp,
            elevation = 20.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Insert Animated Text / Formula", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = textVal,
                    onValueChange = { textVal = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = A2Theme.Accent,
                        unfocusedBorderColor = A2Theme.GlassBorder
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(A2Theme.Accent)
                            .clickable { onConfirm(textVal) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(text = "Add to Stage", color = A2Theme.Ink950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
