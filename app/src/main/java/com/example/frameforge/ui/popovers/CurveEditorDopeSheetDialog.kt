package com.example.frameforge.ui.popovers

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.frameforge.cinema.CurveEditorEngine
import com.example.frameforge.cinema.CurveEditorState
import com.example.frameforge.cinema.CurveEditorViewMode
import com.example.frameforge.cinema.TangentMode

@Composable
fun CurveEditorDopeSheetDialog(
    state: CurveEditorState,
    currentFrame: Float,
    totalFrames: Int,
    onStateChange: (CurveEditorState) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(650.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color(0xFF14171F)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Curve Editor",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Glaxnimate / Blender Graph Editor & Dope Sheet",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                "Sub-frame Bezier easing curves, tangent handle breaks, and multi-track timing blocks",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Mode Switcher (Dope Sheet vs Graph Curves)
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF1E2330), RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            val isDope = state.viewMode == CurveEditorViewMode.DOPE_SHEET
                            Text(
                                "Dope Sheet",
                                color = if (isDope) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isDope) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDope) Color(0xFF2563EB) else Color.Transparent)
                                    .clickable {
                                        onStateChange(state.copy(viewMode = CurveEditorViewMode.DOPE_SHEET))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            Text(
                                "Bezier Curves",
                                color = if (!isDope) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (!isDope) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (!isDope) Color(0xFF2563EB) else Color.Transparent)
                                    .clickable {
                                        onStateChange(state.copy(viewMode = CurveEditorViewMode.GRAPH_CURVE))
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Track Visibility Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.tracks) { track ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (track.isVisible) Color(0xFF1E293B) else Color(0xFF12141A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                val updated = state.tracks.map {
                                    if (it.id == track.id) it.copy(isVisible = !it.isVisible) else it
                                }
                                onStateChange(state.copy(tracks = updated))
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(track.colorArgb))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    track.name,
                                    color = if (track.isVisible) Color.White else Color(0xFF64748B),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // The Main Interactive Curve / Dope Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF2D3748), RoundedCornerShape(12.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Panning the timeline / curve view
                                    onStateChange(
                                        state.copy(
                                            panX = state.panX + dragAmount.x,
                                            panY = state.panY + dragAmount.y
                                        )
                                    )
                                }
                            }
                    ) {
                        drawContext.canvas.nativeCanvas.let { nativeCanvas ->
                            CurveEditorEngine.renderCurveCanvas(
                                nativeCanvas,
                                size.width,
                                size.height,
                                state,
                                currentFrame,
                                totalFrames
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tangent Handle Mode Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TANGENT HANDLE INTERPOLATION", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TangentMode.values().forEach { mode ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2330)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    mode.label.split(" ").first(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
