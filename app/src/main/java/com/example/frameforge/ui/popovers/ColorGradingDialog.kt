package com.example.frameforge.ui.popovers

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.frameforge.cinema.ColorGradingEngine
import com.example.frameforge.cinema.ColorGradingPreset
import com.example.frameforge.cinema.ColorGradingState
import com.example.frameforge.cinema.ColorWheelValues

@Composable
fun ColorGradingDialog(
    state: ColorGradingState,
    onStateChange: (ColorGradingState) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(640.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF14171F)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = "Color Grading",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "DaVinci Resolve Pro Color Wheels & ASC CDL",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Hardware-accelerated Lift/Gamma/Gain, 3200K-6500K White Balance, and Film Emulations",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = state.isEnabled,
                            onCheckedChange = { onStateChange(state.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD700))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Film Emulation Presets Strip
                Text("FILM EMULATION PRESETS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ColorGradingPreset.values()) { preset ->
                        val isSelected = state.preset == preset
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF2563EB) else Color(0xFF1E2330)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                onStateChange(ColorGradingEngine.applyPreset(preset))
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(preset.label, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text(preset.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Three-Way Color Wheels
                Text("THREE-WAY COLOR WHEELS (LIFT / GAMMA / GAIN)", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // LIFT (Shadows)
                    ColorWheelCard(
                        title = "LIFT (Shadows)",
                        wheel = state.liftGammaGain.lift,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(lift = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // GAMMA (Midtones)
                    ColorWheelCard(
                        title = "GAMMA (Midtones)",
                        wheel = state.liftGammaGain.gamma,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(gamma = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // GAIN (Highlights)
                    ColorWheelCard(
                        title = "GAIN (Highlights)",
                        wheel = state.liftGammaGain.gain,
                        onWheelChange = { newW ->
                            onStateChange(state.copy(liftGammaGain = state.liftGammaGain.copy(gain = newW)))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Color Temperature & Tint (Kelvin)
                Text("WHITE BALANCE & CHROMATIC ADAPTATION", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Temperature: ${state.whiteBalance.kelvin.toInt()}K",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = state.whiteBalance.kelvin,
                            onValueChange = { k ->
                                onStateChange(state.copy(whiteBalance = state.whiteBalance.copy(kelvin = k)))
                            },
                            valueRange = 2500f..9500f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Tint: ${state.whiteBalance.tint.toInt()}",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Slider(
                            value = state.whiteBalance.tint,
                            onValueChange = { t ->
                                onStateChange(state.copy(whiteBalance = state.whiteBalance.copy(tint = t)))
                            },
                            valueRange = -50f..50f
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Saturation & Contrast
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ASC CDL Saturation: ${(state.cdl.saturation * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = state.cdl.saturation,
                            onValueChange = { s -> onStateChange(state.copy(cdl = state.cdl.copy(saturation = s))) },
                            valueRange = 0f..2.5f
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Global Contrast: ${(state.contrast * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = state.contrast,
                            onValueChange = { c -> onStateChange(state.copy(contrast = c)) },
                            valueRange = 0.5f..2.0f
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorWheelCard(
    title: String,
    wheel: ColorWheelValues,
    onWheelChange: (ColorWheelValues) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2330)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            // Master luminance slider
            Text("Luminance: ${(wheel.master * 100).toInt()}%", color = Color(0xFF94A3B8), fontSize = 10.sp)
            Slider(
                value = wheel.master,
                onValueChange = { onWheelChange(wheel.copy(master = it)) },
                valueRange = -0.5f..0.5f
            )

            // Red balance
            Text("Red: ${(wheel.red * 100).toInt()}%", color = Color(0xFFFF5252), fontSize = 10.sp)
            Slider(
                value = wheel.red,
                onValueChange = { onWheelChange(wheel.copy(red = it)) },
                valueRange = -0.4f..0.4f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF5252), activeTrackColor = Color(0xFFFF5252))
            )

            // Blue balance
            Text("Blue: ${(wheel.blue * 100).toInt()}%", color = Color(0xFF448AFF), fontSize = 10.sp)
            Slider(
                value = wheel.blue,
                onValueChange = { onWheelChange(wheel.copy(blue = it)) },
                valueRange = -0.4f..0.4f,
                colors = SliderDefaults.colors(thumbColor = Color(0xFF448AFF), activeTrackColor = Color(0xFF448AFF))
            )
        }
    }
}
