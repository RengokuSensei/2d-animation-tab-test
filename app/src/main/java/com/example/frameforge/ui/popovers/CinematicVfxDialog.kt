package com.example.frameforge.ui.popovers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.AtmosphericParticleType
import com.example.frameforge.cinema.CinemaAspectLetterbox
import com.example.frameforge.cinema.CinematicVfxConfig
import com.example.frameforge.cinema.LightSourceGizmo

private val DialogDarkBg = Color(0xF218181B)
private val DialogBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val GoldAccent = Color(0xFFFFD700)

@Composable
fun CinematicVfxDialog(
    config: CinematicVfxConfig,
    onUpdateConfig: (CinematicVfxConfig) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(380.dp)
            .heightIn(max = 620.dp)
            .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
            .testTag("cinematic_vfx_dialog"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DialogDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MovieFilter, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Cinematic VFX & Shaders", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("2D Rim-Lighting, Bloom, God Rays & Particles", color = Color.Gray, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 1: 2D Rim-Lighting & Cast Shadow Gizmo
            Text("DYNAMIC 2D RIM-LIGHT & SHADOW", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WbSunny, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Interactive Sun/Light Gizmo", color = Color.White, fontSize = 12.sp)
                }
                Switch(
                    checked = config.lightGizmo.isEnabled,
                    onCheckedChange = { onUpdateConfig(config.copy(lightGizmo = config.lightGizmo.copy(isEnabled = it))) },
                    colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent)
                )
            }

            if (config.lightGizmo.isEnabled) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Light Intensity", color = Color.LightGray, fontSize = 11.sp)
                    Text("${(config.lightGizmo.intensity * 100).toInt()}%", color = GoldAccent, fontSize = 11.sp)
                }
                Slider(
                    value = config.lightGizmo.intensity,
                    onValueChange = { onUpdateConfig(config.copy(lightGizmo = config.lightGizmo.copy(intensity = it))) },
                    colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Shadow Cast Length", color = Color.LightGray, fontSize = 11.sp)
                    Text("${config.lightGizmo.castShadowLength.toInt()}px", color = GoldAccent, fontSize = 11.sp)
                }
                Slider(
                    value = config.lightGizmo.castShadowLength,
                    onValueChange = { onUpdateConfig(config.copy(lightGizmo = config.lightGizmo.copy(castShadowLength = it))) },
                    valueRange = 10f..80f,
                    colors = SliderDefaults.colors(thumbColor = GoldAccent, activeTrackColor = GoldAccent)
                )
            }

            HorizontalDivider(color = DialogBorder, modifier = Modifier.padding(vertical = 10.dp))

            // SECTION 2: Anamorphic Bloom & God Rays
            Text("OPTICAL BLOOM & GOD RAYS", color = ProcreateBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("2.39:1 Anamorphic Bloom Flare", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = config.anamorphicBloom,
                    onCheckedChange = { onUpdateConfig(config.copy(anamorphicBloom = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Volumetric Light Rays (God Rays)", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = config.volumetricRays,
                    onCheckedChange = { onUpdateConfig(config.copy(volumetricRays = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ProcreateBlue)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Film Halation (Warm Edge Glow)", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = config.filmHalation,
                    onCheckedChange = { onUpdateConfig(config.copy(filmHalation = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF3B30))
                )
            }

            HorizontalDivider(color = DialogBorder, modifier = Modifier.padding(vertical = 10.dp))

            // SECTION 3: Atmospheric Particles
            Text("ATMOSPHERIC PARTICLE WEATHER", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            AtmosphericParticleType.values().forEach { pType ->
                val isSel = pType == config.particles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF1E3A2F) else Color(0x18FFFFFF))
                        .clickable { onUpdateConfig(config.copy(particles = pType)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(pType.label, color = if (isSel) Color(0xFF34D399) else Color.White, fontSize = 11.sp)
                    RadioButton(
                        selected = isSel,
                        onClick = { onUpdateConfig(config.copy(particles = pType)) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF34D399)),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }

            HorizontalDivider(color = DialogBorder, modifier = Modifier.padding(vertical = 10.dp))

            // SECTION 4: CinemaScope Letterbox
            Text("CINEMATIC LETTERBOX ASPECT", color = Color(0xFFA78BFA), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            CinemaAspectLetterbox.values().forEach { aspect ->
                val isSel = aspect == config.letterbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) Color(0xFF2E244D) else Color(0x18FFFFFF))
                        .clickable { onUpdateConfig(config.copy(letterbox = aspect)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(aspect.label, color = if (isSel) Color(0xFFA78BFA) else Color.White, fontSize = 11.sp)
                    RadioButton(
                        selected = isSel,
                        onClick = { onUpdateConfig(config.copy(letterbox = aspect)) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFA78BFA)),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
            }
        }
    }
}
