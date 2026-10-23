package com.example.frameforge.model

enum class AnimationNodeType(val title: String, val category: String) {
    LAYER_SOURCE("Layer Source", "Input"),
    TRANSFORM("Transform Motion", "Motion"),
    COLOR_FX("Color & FX Filter", "Effects"),
    AI_MOTION("AI Prompt Motion", "AI & Procedural"),
    COMPOSITE_OUTPUT("Master Output", "Output")
}

data class NodeSocket(
    val id: String,
    val name: String,
    val isOutput: Boolean
)

data class GraphNode(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val nodeType: AnimationNodeType,
    var x: Float,
    var y: Float,
    val inputs: List<NodeSocket> = emptyList(),
    val outputs: List<NodeSocket> = emptyList(),
    val parameters: Map<String, String> = emptyMap()
)

data class NodeConnection(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fromNodeId: String,
    val fromSocketId: String,
    val toNodeId: String,
    val toSocketId: String
)

data class AnimationNodeGraph(
    val nodes: List<GraphNode> = defaultNodes(),
    val connections: List<NodeConnection> = defaultConnections()
) {
    companion object {
        fun defaultNodes(): List<GraphNode> {
            return listOf(
                GraphNode(
                    id = "node_layer_src",
                    title = "Line Art Layer",
                    nodeType = AnimationNodeType.LAYER_SOURCE,
                    x = 60f,
                    y = 60f,
                    outputs = listOf(NodeSocket("out_image", "Image", true)),
                    parameters = mapOf("layerName" to "Line Art", "opacity" to "1.0")
                ),
                GraphNode(
                    id = "node_ai_motion",
                    title = "AI Motion Driver",
                    nodeType = AnimationNodeType.AI_MOTION,
                    x = 60f,
                    y = 380f,
                    outputs = listOf(NodeSocket("out_motion", "Motion Curves", true)),
                    parameters = mapOf("prompt" to "Subtle breathing pulse", "intensity" to "0.8")
                ),
                GraphNode(
                    id = "node_fx",
                    title = "Glow & Tone FX",
                    nodeType = AnimationNodeType.COLOR_FX,
                    x = 720f,
                    y = 60f,
                    inputs = listOf(NodeSocket("in_image", "Input Image", false)),
                    outputs = listOf(NodeSocket("out_image", "Image", true)),
                    parameters = mapOf("effect" to "Warm Studio Bloom", "glow" to "0.5")
                ),
                GraphNode(
                    id = "node_transform",
                    title = "Kinematic Transform",
                    nodeType = AnimationNodeType.TRANSFORM,
                    x = 720f,
                    y = 380f,
                    inputs = listOf(
                        NodeSocket("in_image", "Input Image", false),
                        NodeSocket("in_motion", "Motion Data", false)
                    ),
                    outputs = listOf(NodeSocket("out_image", "Image", true)),
                    parameters = mapOf("scale" to "1.0", "rotation" to "0.0")
                ),
                GraphNode(
                    id = "node_master_out",
                    title = "Master Composite",
                    nodeType = AnimationNodeType.COMPOSITE_OUTPUT,
                    x = 1380f,
                    y = 220f,
                    inputs = listOf(NodeSocket("in_composite", "Final In", false)),
                    parameters = mapOf("quality" to "High 1080p", "fps" to "24")
                )
            )
        }

        fun defaultConnections(): List<NodeConnection> {
            return listOf(
                NodeConnection(
                    fromNodeId = "node_layer_src",
                    fromSocketId = "out_image",
                    toNodeId = "node_fx",
                    toSocketId = "in_image"
                ),
                NodeConnection(
                    fromNodeId = "node_ai_motion",
                    fromSocketId = "out_motion",
                    toNodeId = "node_transform",
                    toSocketId = "in_motion"
                ),
                NodeConnection(
                    fromNodeId = "node_fx",
                    fromSocketId = "out_image",
                    toNodeId = "node_transform",
                    toSocketId = "in_image"
                ),
                NodeConnection(
                    fromNodeId = "node_transform",
                    fromSocketId = "out_image",
                    toNodeId = "node_master_out",
                    toSocketId = "in_composite"
                )
            )
        }
    }
}
