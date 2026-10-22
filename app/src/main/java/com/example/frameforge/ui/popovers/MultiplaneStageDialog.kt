package com.example.frameforge.ui.popovers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.cinema.MultiplaneLayer3D
import com.example.frameforge.cinema.MultiplaneStageState
import com.example.frameforge.cinema.VirtualCamera3D
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val DialogDarkBg = Color(0xF218181B)
private val DialogBorder = Color(0x33FFFFFF)
private val MultiplaneCyan = Color(0xFF00E5FF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun MultiplaneStageDialog(
    state: MultiplaneStageState,
    onUpdateCamera: (VirtualCamera3D) -> Unit,
    onUpdateLayer: (MultiplaneLayer3D) -> Unit,
    onToggleOrbit: () -> Unit,
    onSelectLayer: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var azimuth by remember { mutableStateOf(state.orbitAzimuthDeg) }
    var elevation by remember { mutableStateOf(state.orbitElevationDeg) }

    Card(
        modifier = modifier
            .width(460.dp)
            .heightIn(max = 660.dp)
            .border(1.dp, DialogBorder, RoundedCornerShape(16.dp))
            .testTag("multiplane_stage_dialog"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DialogDarkBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MultiplaneCyan, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("2.5D / 3D Multiplane Space Studio", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Multiplane Z-Depth, Parallax & 3D Virtual Camera", color = Color.Gray, fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D STAGE ORBIT INSPECTOR CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0E12))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            azimuth = (azimuth + dragAmount.x * 0.5f) % 360f
                            elevation = (elevation - dragAmount.y * 0.5f).coerceIn(-45f, 65f)
                        }
                    }
                    .testTag("multiplane_orbit_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    val radAz = azimuth * PI / 180.0
                    val radEl = elevation * PI / 180.0
                    val cosAz = cos(radAz).toFloat()
                    val sinAz = sin(radAz).toFloat()
                    val cosEl = cos(radEl).toFloat()
                    val sinEl = sin(radEl).toFloat()

                    // Draw 3D Ground Grid
                    val gridSpacing = 24f
                    val gridRange = 4
                    for (i in -gridRange..gridRange) {
                        val gx1 = cx + (i * gridSpacing) * cosAz - (-gridRange * gridSpacing) * sinAz * cosEl
                        val gy1 = cy + (i * gridSpacing) * sinAz * sinEl + (-gridRange * gridSpacing) * sinEl
                        val gx2 = cx + (i * gridSpacing) * cosAz - (gridRange * gridSpacing) * sinAz * cosEl
                        val gy2 = cy + (i * gridSpacing) * sinAz * sinEl + (gridRange * gridSpacing) * sinEl
                        drawLine(Color(0x18FFFFFF), Offset(gx1, gy1), Offset(gx2, gy2), strokeWidth = 1f)
                    }

                    // Draw 3D Camera Frustum Cone
                    val camX = cx - (state.camera.truckX * 0.1f)
                    val camY = cy + (state.camera.pedestalY * 0.1f)
                    drawCircle(MultiplaneCyan, radius = 5.dp.toPx(), center = Offset(camX, camY))

                    // Draw Frustum cone lines forward
                    val fLen = state.camera.focalLengthMm * 1.5f
                    val frustumSpread = 45f
                    drawLine(Color(0x4400E5FF), Offset(camX, camY), Offset(camX - frustumSpread, camY - fLen), strokeWidth = 1.5f)
                    drawLine(Color(0x4400E5FF), Offset(camX, camY), Offset(camX + frustumSpread, camY - fLen), strokeWidth = 1.5f)

                    // Draw Floating 2D Layer Planes in Z-depth
                    state.layerPlanes.forEachIndexed { idx, layer ->
                        val planeZ = layer.posZ * 0.04f
                        val planeY = cy - planeZ * cosEl - (layer.rotX * 0.5f)
                        val planeX = cx + planeZ * sinAz

                        val pWidth = 70f
                        val pHeight = 44f
                        val isSelected = layer.layerId == state.selectedLayerId

                        val pColor = if (isSelected) MultiplaneCyan else Color(0x8838BDF8)
                        val rectPath = Path().apply {
                            moveTo(planeX - pWidth / 2f, planeY - pHeight / 2f)
                            lineTo(planeX + pWidth / 2f, planeY - pHeight / 2f)
                            lineTo(planeX + pWidth / 2f, planeY + pHeight / 2f)
                            lineTo(planeX - pWidth / 2f, planeY + pHeight / 2f)
                            close()
                        }
                        drawPath(rectPath, if (isSelected) Color(0x3300E5FF) else Color(0x1838BDF8))
                        drawPath(rectPath, pColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isSelected) 2f else 1f))
                    }
                }

                Text(
                    "Drag with stylus to orbit 3D stage (Az: ${azimuth.toInt()}°, El: ${elevation.toInt()}°)",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3D VIRTUAL CAMERA CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Camera, contentDescription = null, tint = MultiplaneCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("3D Camera Dolly Z", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text("${state.camera.dollyZ.toInt()}px", color = MultiplaneCyan, fontSize = 11.sp)
            }
            Slider(
                value = state.camera.dollyZ,
                onValueChange = { onUpdateCamera(state.camera.copy(dollyZ = it)) },
                valueRange = -500f..2500f,
                colors = SliderDefaults.colors(thumbColor = MultiplaneCyan, activeTrackColor = MultiplaneCyan)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Focal Length: ${state.camera.focalLengthMm.toInt()}mm (${if (state.camera.focalLengthMm < 30f) "Wide" else if (state.camera.focalLengthMm > 70f) "Telephoto" else "Normal"})", color = Color.LightGray, fontSize = 10.sp)
                Text("Aperture: f/${state.camera.aperture}", color = MultiplaneCyan, fontSize = 10.sp)
            }
            Slider(
                value = state.camera.focalLengthMm,
                onValueChange = { onUpdateCamera(state.camera.copy(focalLengthMm = it)) },
                valueRange = 18f..135f,
                colors = SliderDefaults.colors(thumbColor = ProcreateBlue, activeTrackColor = ProcreateBlue)
            )

            HorizontalDivider(color = DialogBorder, modifier = Modifier.padding(vertical = 6.dp))

            // LAYER 3D DEPTH LIST
            Text("LAYER Z-DEPTH & PERSPECTIVE TILT", color = MultiplaneCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.layerPlanes) { layer ->
                    val isSel = layer.layerId == state.selectedLayerId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLayer(layer.layerId) }
                            .border(1.dp, if (isSel) MultiplaneCyan else Color.Transparent, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF162936) else Color(0x18FFFFFF))
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(layer.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Z: ${layer.posZ.toInt()}", color = MultiplaneCyan, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (layer.autoScaleCompensation) Color(0x3300E5FF) else Color(0x22FFFFFF),
                                        modifier = Modifier.clickable {
                                            onUpdateLayer(layer.copy(autoScaleCompensation = !layer.autoScaleCompensation))
                                        }
                                    ) {
                                        Text(
                                            "Auto-Scale Lock",
                                            color = if (layer.autoScaleCompensation) MultiplaneCyan else Color.Gray,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Slider(
                                value = layer.posZ,
                                onValueChange = { onUpdateLayer(layer.copy(posZ = it)) },
                                valueRange = -1000f..3000f,
                                colors = SliderDefaults.colors(thumbColor = MultiplaneCyan, activeTrackColor = MultiplaneCyan)
                            )
                        }
                    }
                }
            }
        }
    }
}
