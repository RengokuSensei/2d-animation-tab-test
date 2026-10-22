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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Transform
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
import com.example.frameforge.cinema.PsychovisualTransitionConfig
import com.example.frameforge.cinema.PsychovisualTransitionType
import com.example.frameforge.cinema.TransitionDirection

private val DialogDarkBg = Color(0xF218181B)
private val DialogBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val ElectricPurple = Color(0xFFA855F7)

@Composable
fun SceneTransitionsDialog(
    config: PsychovisualTransitionConfig,
    onUpdateConfig: (PsychovisualTransitionConfig) -> Unit,
    onApplyTransition: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(400.dp)
            .heightIn(max = 620.dp)
            .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
            .testTag("scene_transitions_dialog"),
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
                    Icon(Icons.Default.Transform, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Psychovisual Scene Transitions", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Saccadic Masking, Retinal Flash & Smear Frames", color = Color.Gray, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transition Type Cards
            Text("TRANSITION PSYCHOVISUAL MECHANISM", color = ElectricPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            PsychovisualTransitionType.values().forEach { tType ->
                val isSel = tType == config.type
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onUpdateConfig(config.copy(type = tType)) }
                        .border(1.dp, if (isSel) ElectricPurple else Color.Transparent, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF281C3D) else Color(0x18FFFFFF))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tType.title, color = if (isSel) Color(0xFFD8B4FE) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            RadioButton(
                                selected = isSel,
                                onClick = { onUpdateConfig(config.copy(type = tType)) },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(tType.description, color = Color.Gray, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            HorizontalDivider(color = DialogBorder, modifier = Modifier.padding(vertical = 10.dp))

            // Duration Slider (3 to 8 frames)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Transition Cut Duration", color = Color.LightGray, fontSize = 11.sp)
                Text("${config.durationFrames} Frames (${(config.durationFrames * 1000 / 24)}ms)", color = ElectricPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = config.durationFrames.toFloat(),
                onValueChange = { onUpdateConfig(config.copy(durationFrames = it.toInt())) },
                valueRange = 3f..8f,
                steps = 4,
                colors = SliderDefaults.colors(thumbColor = ElectricPurple, activeTrackColor = ElectricPurple)
            )

            // Direction selector (for Whip-Pan)
            if (config.type == PsychovisualTransitionType.SACCADIC_WHIP_PAN) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Whip-Pan Direction:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TransitionDirection.values().forEach { dir ->
                        val isSel = dir == config.direction
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) ElectricPurple else Color(0x22FFFFFF),
                            modifier = Modifier.clickable { onUpdateConfig(config.copy(direction = dir)) }
                        ) {
                            Text(dir.label, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // Impact frame controls
            if (config.type == PsychovisualTransitionType.SHONEN_IMPACT_FRAME) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Inverted Negative Flash", color = Color.White, fontSize = 12.sp)
                    Switch(
                        checked = config.invertNegativeFlash,
                        onCheckedChange = { onUpdateConfig(config.copy(invertNegativeFlash = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ElectricPurple)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onApplyTransition()
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(46.dp).testTag("apply_transition_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Transition to Playhead Junction", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
