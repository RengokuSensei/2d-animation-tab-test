package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve

enum class SettingsTab {
    WORKSPACE,
    INTERACTION
}

enum class CanvasPaperPreset(val label: String, val color: Color) {
    HEAVYWEIGHT("Heavyweight", Color(0xFFE8E8E8)),
    RIPPLED("Rippled", Color(0xFFF3F4F6)),
    BLUEPRINT("Blueprint", Color(0xFF1E3A8A)),
    BROWN_KRAFT("Brown Kraft", Color(0xFF92400E)),
    DARK_CINEMA("Dark Cinema", Color(0xFF0F1115))
}

@Composable
fun A2SettingsDrawer(
    paperPreset: CanvasPaperPreset,
    onSelectPaperPreset: (CanvasPaperPreset) -> Unit,
    palmMode: PalmRejectionMode,
    onPalmModeChange: (PalmRejectionMode) -> Unit,
    pressureCurve: PressureCurve,
    onPressureCurveChange: (PressureCurve) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(SettingsTab.WORKSPACE) }

    // Workspace settings state
    var selectedGrid by remember { mutableStateOf("No Grid") }
    var selectedArtboard by remember { mutableStateOf("1080p") }
    var selectedUnit by remember { mutableStateOf("Points") }
    var lowLatencyEnabled by remember { mutableStateOf(true) }

    // Interaction settings state
    var twoFingerZoom by remember { mutableStateOf(true) }
    var twoFingerRotate by remember { mutableStateOf(true) }
    var tapHoldTime by remember { mutableFloatStateOf(0.4f) }
    var shapeRecogHoldTime by remember { mutableFloatStateOf(0.6f) }
    var enablePressure by remember { mutableStateOf(true) }
    var enableTilt by remember { mutableStateOf(true) }

    GlassPill(
        modifier = modifier
            .width(360.dp)
            .height(580.dp),
        cornerRadius = 24.dp,
        elevation = 20.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drawer Top Bar: Close, Tabs (Workspace vs Interaction)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(A2Theme.Ink900)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Workspace vs Interaction Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(SettingsTab.WORKSPACE to "Workspace", SettingsTab.INTERACTION to "Interaction").forEach { (tab, lbl) ->
                        val isSel = (activeTab == tab)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color.White else Color.Transparent)
                                .clickable { activeTab = tab }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = lbl,
                                color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                Box(modifier = Modifier.size(28.dp)) // spacer
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == SettingsTab.WORKSPACE) {
                    // SECTION 1: CANVAS BACKGROUND
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Canvas Background", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Standard paper type or custom background color", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CanvasPaperPreset.values().forEach { preset ->
                                val isSel = (preset == paperPreset)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSelectPaperPreset(preset) }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(preset.color)
                                            .border(
                                                width = if (isSel) 2.5.dp else 1.dp,
                                                color = if (isSel) A2Theme.Accent else A2Theme.GlassBorder,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = if (preset == CanvasPaperPreset.HEAVYWEIGHT || preset == CanvasPaperPreset.RIPPLED) Color.Black else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = preset.label,
                                        color = if (isSel) Color.White else A2Theme.TextSecondary,
                                        fontSize = 8.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: GRID TYPE
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Grid Type", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("No Grid", "Dot Grid", "Graph Paper", "Isometric").forEach { g ->
                                val isSel = (selectedGrid == g)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) A2Theme.Accent.copy(alpha = 0.2f) else A2Theme.Ink850)
                                        .border(1.dp, if (isSel) A2Theme.Accent else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedGrid = g }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = g,
                                        color = if (isSel) A2Theme.Accent else A2Theme.TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 3: ARTBOARD SIZE
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Artboard Size & Reference Frame", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Infinite", "A4", "1080p", "4K").forEach { a ->
                                val isSel = (selectedArtboard == a)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) A2Theme.Amber.copy(alpha = 0.2f) else A2Theme.Ink850)
                                        .border(1.dp, if (isSel) A2Theme.Amber else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { selectedArtboard = a }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = a,
                                        color = if (isSel) A2Theme.Amber else A2Theme.TextSecondary,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 4: DOCUMENT UNITS
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Document Units", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Pixels", "Points", "Millimeters", "Inches").forEach { u ->
                                val isSel = (selectedUnit == u)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color.White else A2Theme.Ink850)
                                        .clickable { selectedUnit = u }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u,
                                        color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 5: PERFORMANCE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(A2Theme.Ink850)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "144Hz Low-Latency Mode", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Direct SIMD Rust spline pipeline", color = A2Theme.TextSecondary, fontSize = 9.sp)
                        }
                        Switch(
                            checked = lowLatencyEnabled,
                            onCheckedChange = { lowLatencyEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = A2Theme.Accent
                            )
                        )
                    }
                } else {
                    // INTERACTION TAB
                    // SECTION 1: FINGER ACTION
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Finger Action (Palm Rejection)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "While your stylus draws, you can set your finger to do something else", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                PalmRejectionMode.AUTOMATIC to "Do Nothing",
                                PalmRejectionMode.STYLUS_ONLY to "Pan Canvas",
                                PalmRejectionMode.FINGER_AND_STYLUS to "Draw"
                            ).forEach { (m, lbl) ->
                                val isSel = (palmMode == m)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) A2Theme.Amber.copy(alpha = 0.2f) else A2Theme.Ink850)
                                        .border(1.dp, if (isSel) A2Theme.Amber else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable { onPalmModeChange(m) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isSel) A2Theme.Amber else A2Theme.TextSecondary,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: TWO FINGERS GESTURES
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(A2Theme.Ink850)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Two Fingers Navigation", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Enable Canvas Zoom", color = A2Theme.TextSecondary, fontSize = 10.sp)
                            Switch(checked = twoFingerZoom, onCheckedChange = { twoFingerZoom = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = A2Theme.Accent))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Enable Canvas Rotation", color = A2Theme.TextSecondary, fontSize = 10.sp)
                            Switch(checked = twoFingerRotate, onCheckedChange = { twoFingerRotate = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = A2Theme.Accent))
                        }
                    }

                    // SECTION 3: DRAW & HOLD SHAPE RECOGNITION
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(A2Theme.Ink850)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Draw & Hold (Shape Recognition)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Hold still at stroke end to auto-snap vector geometry", color = A2Theme.TextSecondary, fontSize = 9.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Activation Time", color = A2Theme.TextSecondary, fontSize = 9.5.sp)
                            Text(text = "${String.format("%.1f", shapeRecogHoldTime)} s", color = A2Theme.Mint, fontSize = 9.5.sp, fontFamily = FontFamily.Monospace)
                        }
                        Slider(
                            value = shapeRecogHoldTime,
                            onValueChange = { shapeRecogHoldTime = it },
                            valueRange = 0.2f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = A2Theme.Mint, activeTrackColor = A2Theme.Mint, inactiveTrackColor = A2Theme.Ink950)
                        )
                    }

                    // SECTION 4: STYLUS PRESSURE RESPONSE
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Stylus Hardware Preferences", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { enablePressure = !enablePressure }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (enablePressure) "Pressure: ON" else "Pressure: OFF", color = if (enablePressure) A2Theme.Accent else A2Theme.TextSecondary, fontSize = 10.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(A2Theme.Ink850)
                                    .clickable { enableTilt = !enableTilt }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = if (enableTilt) "Tilt: ON" else "Tilt: OFF", color = if (enableTilt) A2Theme.Amber else A2Theme.TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
