package com.example.frameforge.ui.popovers

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import com.example.frameforge.cinema.ChromaKeyConfig
import com.example.frameforge.cinema.ChromaKeyType
import com.example.frameforge.cinema.DespillAlgorithm

@Composable
fun ChromaKeyDialog(
    config: ChromaKeyConfig,
    onConfigChange: (ChromaKeyConfig) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(600.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF14171F)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = "Chroma Key",
                            tint = Color(0xFF00FF66),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Industry-Proven Chroma Keying & Matte Tools",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "Hardware Despill, Softstep Edge Roll-off, and Contact Shadow Recovery",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = config.isEnabled,
                            onCheckedChange = { onConfigChange(config.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF66))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Color Presets (Green Screen / Blue Screen)
                Text("KEY COLOR SELECTION", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChromaKeyType.values().forEach { type ->
                        val isSelected = config.keyType == type
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E2330)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.clickable {
                                onConfigChange(
                                    config.copy(
                                        keyType = type,
                                        keyColorArgb = type.defaultColorArgb
                                    )
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(type.defaultColorArgb))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(type.label, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Matte Extraction Sliders
                Text("MATTE EXTRACTION & TOLERANCE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Similarity
                Text("Color Similarity: ${(config.similarity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = config.similarity,
                    onValueChange = { onConfigChange(config.copy(similarity = it)) },
                    valueRange = 0.05f..0.85f
                )

                // Smoothness
                Text("Edge Smoothness / Roll-off: ${(config.smoothness * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = config.smoothness,
                    onValueChange = { onConfigChange(config.copy(smoothness = it)) },
                    valueRange = 0.01f..0.50f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Despill Engine
                Text("EDGE DESPILL & SUPPRESSION", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Despill Intensity: ${(config.despillIntensity * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = config.despillIntensity,
                    onValueChange = { onConfigChange(config.copy(despillIntensity = it)) },
                    valueRange = 0.0f..1.0f
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DespillAlgorithm.values().forEach { algo ->
                        val isSelected = config.despillAlgorithm == algo
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF047857) else Color(0xFF1E2330)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                onConfigChange(config.copy(despillAlgorithm = algo))
                            }
                        ) {
                            Text(
                                algo.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Shadow Preservation & Diagnostic Matte View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shadow Preservation: ${(config.shadowPreservation * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
                        Slider(
                            value = config.shadowPreservation,
                            onValueChange = { onConfigChange(config.copy(shadowPreservation = it)) },
                            valueRange = 0.0f..1.0f
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (config.showMatteOnly) Color(0xFFE11D48) else Color(0xFF1E2330)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.clickable {
                            onConfigChange(config.copy(showMatteOnly = !config.showMatteOnly))
                        }
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = "Matte Inspection", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (config.showMatteOnly) "Diagnostic B&W Matte: ON" else "Inspect B&W Matte",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
