package com.example.frameforge.ui.workspace

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.ColorGradingEngine
import com.example.frameforge.cinema.ColorGradingPreset
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.model.AnimationProject

private val WorkspaceDarkBg = Color(0xFF0F1115)
private val PanelBg = Color(0xFF16181F)
private val PanelBorder = Color(0x2EFFFFFF)
private val ResolveGold = Color(0xFFFFD700)

@Composable
fun ColorWorkspace(
    project: AnimationProject,
    currentFrame: Int,
    state: ColorGradingState,
    onStateChange: (ColorGradingState) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSplitCompareActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WorkspaceDarkBg)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- TOP HALF: SPLIT-SCREEN PREVIEW (BEFORE VS AFTER GRADE) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Monitor: Original Log/Rec709
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Original Source (Frame ${currentFrame + 1})", color = Color(0xFF64748B), fontSize = 11.sp)
                    }

                    // Right Monitor: Live Graded Composite
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0A0C10))
                            .border(1.5.dp, if (state.isEnabled) ResolveGold else Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Graded Output", color = if (state.isEnabled) ResolveGold else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                if (state.isEnabled) "Color Grade: ACTIVE (${state.whiteBalance.kelvin.toInt()}K)" else "Color Grade: BYPASSED",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Top Floating Toolbar: Grade Bypass & Reset
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(18.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC181A20)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (state.isEnabled) "Grade ON" else "Bypass",
                            color = if (state.isEnabled) Color(0xFF10B981) else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = { onStateChange(state.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                        )

                        IconButton(
                            onClick = { onStateChange(ColorGradingState()) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // --- BOTTOM HALF: DAVINCI 3-WAY WHEELS & PRESETS ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Presets Bar
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ColorGradingPreset.values()) { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF222734))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { onStateChange(ColorGradingEngine.applyPreset(preset)) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(preset.label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // 3-Way Wheels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    com.example.frameforge.ui.popovers.ColorWheelCard(
                        title = "LIFT (Shadows)",
                        wheel = state.liftGammaGain.lift,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(lift = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    com.example.frameforge.ui.popovers.ColorWheelCard(
                        title = "GAMMA (Midtones)",
                        wheel = state.liftGammaGain.gamma,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(gamma = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    com.example.frameforge.ui.popovers.ColorWheelCard(
                        title = "GAIN (Highlights)",
                        wheel = state.liftGammaGain.gain,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(gain = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sliders: Temperature, Tint, Contrast, Saturation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Temperature: ${state.whiteBalance.kelvin.toInt()}K", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = state.whiteBalance.kelvin,
                            onValueChange = { k ->
                                onStateChange(state.copy(whiteBalance = state.whiteBalance.copy(kelvin = k)))
                            },
                            valueRange = 2500f..9500f
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contrast: ${"%.2f".format(state.toneCurve.contrast)}", color = Color.White, fontSize = 11.sp)
                        Slider(
                            value = state.toneCurve.contrast,
                            onValueChange = { c ->
                                onStateChange(state.copy(toneCurve = state.toneCurve.copy(contrast = c)))
                            },
                            valueRange = 0.5f..2.0f
                        )
                    }
                }
            }
        }
    }
}
