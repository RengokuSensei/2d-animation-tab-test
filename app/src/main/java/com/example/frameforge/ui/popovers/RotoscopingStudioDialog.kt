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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.frameforge.cinema.RotoShape
import com.example.frameforge.cinema.RotoVertex
import com.example.frameforge.cinema.RotoscopingState

@Composable
fun RotoscopingStudioDialog(
    state: RotoscopingState,
    onStateChange: (RotoscopingState) -> Unit,
    currentFrame: Int,
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
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = "Rotoscoping",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Multi-Layered Rotoscoping & Per-Vertex Feathering",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "Natron / Silhouette FX standard: inner core splines + outer variable feather boundaries",
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
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector: Core Spline Mode vs Per-Vertex Feather Mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onStateChange(state.copy(isFeatherToolActive = false)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!state.isFeatherToolActive) Color(0xFF2563EB) else Color(0xFF1E2330)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Core Path Mode (Solid Inner)")
                    }

                    Button(
                        onClick = { onStateChange(state.copy(isFeatherToolActive = true)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isFeatherToolActive) Color(0xFFD97706) else Color(0xFF1E2330)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Grain, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Per-Vertex Feather Mode (Dashed Outer)")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shape List
                Text("ROTO SHAPES & MATTES", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.shapes) { shape ->
                        val isSelected = shape.id == state.activeShapeId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF181C26)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable {
                                onStateChange(state.copy(activeShapeId = shape.id))
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(Color(shape.overlayColorArgb))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(shape.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text(
                                            "Vertices: ${shape.getVerticesForFrame(currentFrame).size} | Invert: ${if (shape.isInverted) "YES" else "NO"}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Invert Mask Button
                                    IconButton(
                                        onClick = {
                                            val updated = state.shapes.map {
                                                if (it.id == shape.id) it.copy(isInverted = !it.isInverted) else it
                                            }
                                            onStateChange(state.copy(shapes = updated))
                                        }
                                    ) {
                                        Icon(Icons.Default.Flip, contentDescription = "Invert", tint = Color(0xFF38BDF8))
                                    }

                                    // Delete shape
                                    if (state.shapes.size > 1) {
                                        IconButton(
                                            onClick = {
                                                val remaining = state.shapes.filter { it.id != shape.id }
                                                onStateChange(state.copy(shapes = remaining, activeShapeId = remaining.firstOrNull()?.id))
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add New Shape Button
                Button(
                    onClick = {
                        val newShape = RotoShape(
                            name = "Roto Mask ${state.shapes.size + 1}",
                            overlayColorArgb = Color(0xFF38BDF8).hashCode()
                        )
                        onStateChange(state.copy(shapes = state.shapes + newShape, activeShapeId = newShape.id))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Roto Spline")
                }
            }
        }
    }
}
