package com.example.frameforge.ui.workspace

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.AnimationNodeGraph
import com.example.frameforge.model.AnimationNodeType
import com.example.frameforge.model.GraphNode

private val WorkspaceDarkBg = Color(0xFF0F1115)
private val PanelBg = Color(0xFF16181F)
private val PanelBorder = Color(0x2EFFFFFF)
private val WireColor = Color(0xFF00E5FF)

@Composable
fun NodesWorkspace(
    graph: AnimationNodeGraph,
    onUpdateNodePosition: (String, Float, Float) -> Unit,
    onAddNode: (AnimationNodeType) -> Unit,
    onDeleteNode: (String) -> Unit,
    onUpdateParameter: (String, String, String) -> Unit,
    onAutoArrange: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNodeId by remember { mutableStateOf<String?>(graph.nodes.firstOrNull()?.id) }
    var panX by remember { mutableFloatStateOf(60f) }
    var panY by remember { mutableFloatStateOf(60f) }
    var zoom by remember { mutableFloatStateOf(1.0f) }

    val density = LocalDensity.current

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(WorkspaceDarkBg)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- LEFT (70%): INTERACTIVE PAN/ZOOM NODE DAG CANVAS ---
        Card(
            modifier = Modifier
                .weight(0.70f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("nodes_dag_canvas")
            ) {
                val nodeMap = remember(graph.nodes) { graph.nodes.associateBy { it.id } }

                // 1. Interactive Canvas: Background Grid & Glowing Bezier Wires
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                panX += pan.x
                                panY += pan.y
                                zoom = (zoom * gestureZoom).coerceIn(0.5f, 2.0f)
                            }
                        }
                ) {
                    // Dot Grid
                    val dotSpacing = 32f * zoom
                    val startX = (panX % dotSpacing)
                    val startY = (panY % dotSpacing)
                    var gx = startX
                    while (gx < size.width) {
                        var gy = startY
                        while (gy < size.height) {
                            drawCircle(Color(0x33475569), radius = 1.5f, center = Offset(gx, gy))
                            gy += dotSpacing
                        }
                        gx += dotSpacing
                    }

                    // Bezier Wires connecting sockets
                    graph.connections.forEach { conn ->
                        val fromNode = nodeMap[conn.fromNodeId]
                        val toNode = nodeMap[conn.toNodeId]
                        if (fromNode != null && toNode != null) {
                            // Calculate socket connection points with zoom & pan
                            val cardWidthPx = 220.dp.toPx() * zoom
                            val p1 = Offset(
                                fromNode.x * zoom + panX + cardWidthPx,
                                fromNode.y * zoom + panY + (40.dp.toPx() * zoom)
                            )
                            val p2 = Offset(
                                toNode.x * zoom + panX,
                                toNode.y * zoom + panY + (40.dp.toPx() * zoom)
                            )

                            val controlOffset = (p2.x - p1.x).coerceAtLeast(60f) * 0.5f
                            val path = Path().apply {
                                moveTo(p1.x, p1.y)
                                cubicTo(
                                    p1.x + controlOffset, p1.y,
                                    p2.x - controlOffset, p2.y,
                                    p2.x, p2.y
                                )
                            }

                            // Glowing halo
                            drawPath(path, WireColor.copy(alpha = 0.3f), style = Stroke(width = 6f * zoom, cap = StrokeCap.Round))
                            // Crisp wire
                            drawPath(path, WireColor, style = Stroke(width = 2.5f * zoom, cap = StrokeCap.Round))
                        }
                    }
                }

                // 2. Draggable Node Cards (Spaced with density conversion)
                graph.nodes.forEach { node ->
                    val isSelected = node.id == selectedNodeId
                    val nodeScreenX = (node.x * zoom + panX).toInt()
                    val nodeScreenY = (node.y * zoom + panY).toInt()

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(nodeScreenX, nodeScreenY) }
                            .width(220.dp)
                            .pointerInput(node.id) {
                                detectDragGestures(
                                    onDragStart = { selectedNodeId = node.id },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onUpdateNodePosition(
                                            node.id,
                                            node.x + dragAmount.x / zoom,
                                            node.y + dragAmount.y / zoom
                                        )
                                    }
                                )
                            }
                            .testTag("node_card_${node.id}")
                    ) {
                        ModernNodeCard(
                            node = node,
                            isSelected = isSelected,
                            onDelete = { onDeleteNode(node.id) },
                            onSelect = { selectedNodeId = node.id }
                        )
                    }
                }

                // 3. Floating Action Bar: Auto-Arrange & Add Nodes
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .border(1.dp, PanelBorder, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xF0181A20),
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { onAddNode(AnimationNodeType.LAYER_SOURCE) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ Source", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onAddNode(AnimationNodeType.TRANSFORM) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ Transform", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onAddNode(AnimationNodeType.COLOR_FX) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ Color FX", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onAddNode(AnimationNodeType.AI_MOTION) },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("+ Motion", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        // Auto Arrange button (realigns cleanly)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x3300E5FF))
                                .clickable { onAutoArrange() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Auto Arrange", tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto Arrange", color = Color(0xFF00E5FF), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- RIGHT (30%): LIVE NODE INSPECTOR PANEL ---
        Card(
            modifier = Modifier
                .weight(0.30f)
                .fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = PanelBg),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PanelBorder)
        ) {
            val activeNode = graph.nodes.find { it.id == selectedNodeId }

            if (activeNode != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Node Inspector",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = activeNode.nodeType.category,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    // Node Info Capsule
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E2128)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(activeNode.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("ID: ${activeNode.id}", color = Color.Gray, fontSize = 9.sp)
                        }
                    }

                    // Live Parameters Editor
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (activeNode.nodeType) {
                            AnimationNodeType.TRANSFORM -> {
                                val scale = activeNode.parameters["scale"]?.toFloatOrNull() ?: 1.0f
                                Text("Scale: ${"%.2f".format(scale)}x", color = Color.White, fontSize = 11.sp)
                                Slider(
                                    value = scale,
                                    onValueChange = { onUpdateParameter(activeNode.id, "scale", it.toString()) },
                                    valueRange = 0.2f..3.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
                                )

                                val rotation = activeNode.parameters["rotation"]?.toFloatOrNull() ?: 0f
                                Text("Rotation: ${rotation.toInt()}°", color = Color.White, fontSize = 11.sp)
                                Slider(
                                    value = rotation,
                                    onValueChange = { onUpdateParameter(activeNode.id, "rotation", it.toString()) },
                                    valueRange = -180f..180f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
                                )
                            }
                            AnimationNodeType.COLOR_FX -> {
                                val glow = activeNode.parameters["glow"]?.toFloatOrNull() ?: 0.5f
                                Text("FX Intensity: ${(glow * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                                Slider(
                                    value = glow,
                                    onValueChange = { onUpdateParameter(activeNode.id, "glow", it.toString()) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFFA855F7), activeTrackColor = Color(0xFFA855F7))
                                )
                            }
                            AnimationNodeType.AI_MOTION -> {
                                val prompt = activeNode.parameters["prompt"] ?: "Natural breathing motion"
                                Text("Motion Prompt", color = Color.White, fontSize = 11.sp)
                                OutlinedTextField(
                                    value = prompt,
                                    onValueChange = { onUpdateParameter(activeNode.id, "prompt", it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                                )
                            }
                            AnimationNodeType.LAYER_SOURCE -> {
                                val opacity = activeNode.parameters["opacity"]?.toFloatOrNull() ?: 1.0f
                                Text("Layer Opacity: ${(opacity * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                                Slider(
                                    value = opacity,
                                    onValueChange = { onUpdateParameter(activeNode.id, "opacity", it.toString()) },
                                    valueRange = 0f..1f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF00E5FF), activeTrackColor = Color(0xFF00E5FF))
                                )
                                Text("Source: ${activeNode.parameters["layerName"] ?: "Drawing Cel Layer"}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                            AnimationNodeType.COMPOSITE_OUTPUT -> {
                                Text("Output Resolution: ${activeNode.parameters["quality"] ?: "1080p Cinema"}", color = Color.White, fontSize = 11.sp)
                                Text("Frame Rate: ${activeNode.parameters["fps"] ?: "24"} FPS", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("Master Blend: Porter-Duff Over", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            else -> {
                                Text("No configurable properties for this node.", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a node on the canvas to inspect its parameters.", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernNodeCard(
    node: GraphNode,
    isSelected: Boolean,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    val headerColor = when (node.nodeType) {
        AnimationNodeType.LAYER_SOURCE -> Color(0xFF0284C7)
        AnimationNodeType.TRANSFORM -> Color(0xFFD97706)
        AnimationNodeType.COLOR_FX -> Color(0xFF7C3AED)
        AnimationNodeType.AI_MOTION -> Color(0xFF059669)
        AnimationNodeType.COMPOSITE_OUTPUT -> Color(0xFF0D9488)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color(0x33FFFFFF),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2128)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = node.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                if (node.nodeType != AnimationNodeType.COMPOSITE_OUTPUT) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Sockets Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inputs
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    node.inputs.forEach { sock ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(WireColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(sock.name, color = Color(0xFFCBD5E1), fontSize = 9.sp)
                        }
                    }
                }

                // Outputs
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    node.outputs.forEach { sock ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(sock.name, color = Color(0xFFCBD5E1), fontSize = 9.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800))
                            )
                        }
                    }
                }
            }
        }
    }
}
