package com.example.frameforge.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.AnimationNodeGraph
import com.example.frameforge.model.AnimationNodeType
import com.example.frameforge.model.GraphNode
import com.example.frameforge.model.NodeConnection
import com.example.frameforge.model.NodeSocket

@Composable
fun NodeCanvas(
    graph: AnimationNodeGraph,
    onUpdateNodePosition: (String, Float, Float) -> Unit,
    onAddNode: (AnimationNodeType) -> Unit,
    onDeleteNode: (String) -> Unit,
    onUpdateParameter: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    panX += pan.x
                    panY += pan.y
                    zoom = (zoom * gestureZoom).coerceIn(0.4f, 2.5f)
                }
            }
            .testTag("node_canvas_workspace")
    ) {
        val nodeMap = remember(graph.nodes) { graph.nodes.associateBy { it.id } }

        // 1. Draw Background Grid and Bezier Connection Wires
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw grid dots
            val dotSpacing = 30f * zoom
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

            // Draw Connection Wires
            graph.connections.forEach { conn ->
                val fromNode = nodeMap[conn.fromNodeId]
                val toNode = nodeMap[conn.toNodeId]
                if (fromNode != null && toNode != null) {
                    val p1 = Offset(
                        (fromNode.x + 220f) * zoom + panX,
                        (fromNode.y + 40f) * zoom + panY
                    )
                    val p2 = Offset(
                        toNode.x * zoom + panX,
                        (toNode.y + 40f) * zoom + panY
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

                    // Glow line
                    drawPath(path, Color(0x6600E5FF), style = Stroke(width = 6f * zoom, cap = StrokeCap.Round))
                    // Sharp wire line
                    drawPath(path, Color(0xFF00E5FF), style = Stroke(width = 2.5f * zoom, cap = StrokeCap.Round))
                }
            }
        }

        // 2. Render Node Cards
        graph.nodes.forEach { node ->
            val nodeScreenX = (node.x * zoom + panX).toInt()
            val nodeScreenY = (node.y * zoom + panY).toInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(nodeScreenX, nodeScreenY) }
                    .width(220.dp)
                    .pointerInput(node.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onUpdateNodePosition(
                                node.id,
                                node.x + dragAmount.x / zoom,
                                node.y + dragAmount.y / zoom
                            )
                        }
                    }
                    .testTag("node_${node.id}")
            ) {
                NodeCard(
                    node = node,
                    onDelete = { onDeleteNode(node.id) },
                    onUpdateParameter = onUpdateParameter
                )
            }
        }

        // 3. Top Floating Action Bar: Add Node Buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { onAddNode(AnimationNodeType.LAYER_SOURCE) },
                modifier = Modifier.height(34.dp).testTag("add_layer_node")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Layer", fontSize = 11.sp)
            }

            FilledTonalButton(
                onClick = { onAddNode(AnimationNodeType.TRANSFORM) },
                modifier = Modifier.height(34.dp).testTag("add_transform_node")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Transform", fontSize = 11.sp)
            }

            FilledTonalButton(
                onClick = { onAddNode(AnimationNodeType.COLOR_FX) },
                modifier = Modifier.height(34.dp).testTag("add_fx_node")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ FX Filter", fontSize = 11.sp)
            }

            FilledTonalButton(
                onClick = { onAddNode(AnimationNodeType.AI_MOTION) },
                modifier = Modifier.height(34.dp).testTag("add_ai_node")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ AI Motion", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun NodeCard(
    node: GraphNode,
    onDelete: () -> Unit,
    onUpdateParameter: (String, String, String) -> Unit
) {
    val headerColor = when (node.nodeType) {
        AnimationNodeType.LAYER_SOURCE -> Color(0xFF1E88E5)
        AnimationNodeType.TRANSFORM -> Color(0xFF43A047)
        AnimationNodeType.COLOR_FX -> Color(0xFF8E24AA)
        AnimationNodeType.AI_MOTION -> Color(0xFFFF8F00)
        AnimationNodeType.COMPOSITE_OUTPUT -> Color(0xFF00ACC1)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D24)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    node.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (node.nodeType != AnimationNodeType.COMPOSITE_OUTPUT) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete node", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Body Parameters
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (node.nodeType) {
                    AnimationNodeType.AI_MOTION -> {
                        val prompt = node.parameters["prompt"] ?: "Walk cycle"
                        var textVal by remember(prompt) { mutableStateOf(prompt) }
                        Text("Motion Prompt:", color = Color.Gray, fontSize = 9.sp)
                        OutlinedTextField(
                            value = textVal,
                            onValueChange = {
                                textVal = it
                                onUpdateParameter(node.id, "prompt", it)
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }
                    AnimationNodeType.COLOR_FX -> {
                        val glow = node.parameters["glow"]?.toFloatOrNull() ?: 0.5f
                        Text("FX Intensity: ${(glow * 100).toInt()}%", color = Color.Gray, fontSize = 9.sp)
                        Slider(
                            value = glow,
                            onValueChange = { onUpdateParameter(node.id, "glow", it.toString()) },
                            valueRange = 0f..1f
                        )
                    }
                    AnimationNodeType.TRANSFORM -> {
                        val scale = node.parameters["scale"]?.toFloatOrNull() ?: 1.0f
                        Text("Scale Factor: ${"%.2f".format(scale)}x", color = Color.Gray, fontSize = 9.sp)
                        Slider(
                            value = scale,
                            onValueChange = { onUpdateParameter(node.id, "scale", it.toString()) },
                            valueRange = 0.2f..3.0f
                        )
                    }
                    else -> {
                        Text(node.nodeType.category, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                }

                // Sockets display row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Inputs
                    Column {
                        node.inputs.forEach { sock ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E5FF))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(sock.name, color = Color(0xFFCBD5E1), fontSize = 8.sp)
                            }
                        }
                    }

                    // Outputs
                    Column(horizontalAlignment = Alignment.End) {
                        node.outputs.forEach { sock ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(sock.name, color = Color(0xFFCBD5E1), fontSize = 8.sp)
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
}
