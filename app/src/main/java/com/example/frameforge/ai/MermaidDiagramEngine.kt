package com.example.frameforge.ai

import com.example.frameforge.model.DrawingStroke

data class MermaidNode(
    val id: String,
    val label: String,
    val shape: ExcalidrawShapeType = ExcalidrawShapeType.RECTANGLE,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 160f,
    var height: Float = 70f
)

data class MermaidEdge(
    val fromId: String,
    val toId: String,
    val label: String? = null,
    val isDotted: Boolean = false,
    val isThick: Boolean = false
)

data class MermaidDiagram(
    val title: String,
    val direction: String = "TD", // "TD" (Top-Down) or "LR" (Left-Right)
    val nodes: Map<String, MermaidNode>,
    val edges: List<MermaidEdge>
)

object MermaidDiagramEngine {

    /**
     * Parses a Mermaid flowchart/graph definition into nodes and edges.
     */
    fun parseMermaid(code: String): MermaidDiagram {
        val lines = code.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("%%") }
        var direction = "TD"
        val nodesMap = mutableMapOf<String, MermaidNode>()
        val edgesList = mutableListOf<MermaidEdge>()

        for (line in lines) {
            val lower = line.lowercase()
            if (lower.startsWith("graph") || lower.startsWith("flowchart")) {
                if (lower.contains("lr")) direction = "LR"
                else if (lower.contains("td") || lower.contains("tb")) direction = "TD"
                else if (lower.contains("rl")) direction = "RL"
                else if (lower.contains("bt")) direction = "BT"
                continue
            }

            // 1. Extract all node definitions present on the line
            extractNodesFromLine(line, nodesMap)

            // 2. Parse edges, e.g. A --> B or A -->|text| B or A -- text --> B
            val edgeRegex = Regex("""([a-zA-Z0-9_]+)\s*(?:\[[^]]*]|\([^)]*\)|\{[^}]*}|\[\([^)]*\)]|\(\([^)]*\)\)|\(\[[^]]*]\))?\s*(-->|-.->|==>|--\s*[^->]*\s*-->|-->\|[^|]*\|)\s*([a-zA-Z0-9_]+)""")
            val match = edgeRegex.find(line)

            if (match != null) {
                val fromRaw = match.groupValues[1]
                val arrowRaw = match.groupValues[2]
                val toRaw = match.groupValues[3]

                var edgeLabel: String? = null
                if (arrowRaw.contains("|")) {
                    edgeLabel = arrowRaw.substringAfter("|").substringBefore("|").trim()
                } else if (arrowRaw.startsWith("--") && arrowRaw.endsWith("-->")) {
                    edgeLabel = arrowRaw.removePrefix("--").removeSuffix("-->").trim()
                }

                val isDotted = arrowRaw.contains("-.-")
                val isThick = arrowRaw.contains("==")

                if (!nodesMap.containsKey(fromRaw)) {
                    nodesMap[fromRaw] = MermaidNode(id = fromRaw, label = fromRaw, shape = ExcalidrawShapeType.RECTANGLE)
                }
                if (!nodesMap.containsKey(toRaw)) {
                    nodesMap[toRaw] = MermaidNode(id = toRaw, label = toRaw, shape = ExcalidrawShapeType.RECTANGLE)
                }

                edgesList.add(MermaidEdge(fromRaw, toRaw, edgeLabel, isDotted, isThick))
            }
        }

        return MermaidDiagram(
            title = "Mermaid Flowchart",
            direction = direction,
            nodes = nodesMap,
            edges = edgesList
        )
    }

    private fun extractNodesFromLine(line: String, nodesMap: MutableMap<String, MermaidNode>) {
        val patterns = listOf(
            Regex("""([a-zA-Z0-9_]+)\s*\[\((.*?)\)]""") to ExcalidrawShapeType.CYLINDER,
            Regex("""([a-zA-Z0-9_]+)\s*\(\((.*?)\)\)""") to ExcalidrawShapeType.ELLIPSE,
            Regex("""([a-zA-Z0-9_]+)\s*\{(.*?)}""") to ExcalidrawShapeType.DIAMOND,
            Regex("""([a-zA-Z0-9_]+)\s*\(\[(.*?)]\)""") to ExcalidrawShapeType.ROUNDED_RECT,
            Regex("""([a-zA-Z0-9_]+)\s*\((.*?)\)""") to ExcalidrawShapeType.ROUNDED_RECT,
            Regex("""([a-zA-Z0-9_]+)\s*\[(.*?)]""") to ExcalidrawShapeType.RECTANGLE
        )

        val parsedIds = mutableSetOf<String>()
        for ((regex, shape) in patterns) {
            val matches = regex.findAll(line)
            for (m in matches) {
                val id = m.groupValues[1]
                if (parsedIds.contains(id)) continue
                val label = m.groupValues[2].trim()
                nodesMap[id] = MermaidNode(id = id, label = if (label.isNotBlank()) label else id, shape = shape)
                parsedIds.add(id)
            }
        }
    }

    /**
     * Lays out the diagram into coordinate space and renders sketchy Excalidraw strokes.
     */
    fun renderDiagramToStrokes(
        diagram: MermaidDiagram,
        originX: Float = 140f,
        originY: Float = 120f,
        colorArgb: Int = 0xFFFFFFFF.toInt(),
        accentColorArgb: Int = 0xFF38BDF8.toInt()
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        if (diagram.nodes.isEmpty()) return strokes

        // 1. Compute levels / ranks for each node (topological sort / BFS)
        val inDegree = mutableMapOf<String, Int>()
        val adjList = mutableMapOf<String, MutableList<String>>()

        diagram.nodes.keys.forEach {
            inDegree[it] = 0
            adjList[it] = mutableListOf()
        }

        diagram.edges.forEach { edge ->
            if (diagram.nodes.containsKey(edge.fromId) && diagram.nodes.containsKey(edge.toId)) {
                adjList[edge.fromId]?.add(edge.toId)
                inDegree[edge.toId] = (inDegree[edge.toId] ?: 0) + 1
            }
        }

        val levels = mutableMapOf<String, Int>()
        val queue = ArrayDeque<String>()

        // Nodes with inDegree 0 start at level 0
        inDegree.filter { it.value == 0 }.keys.forEach {
            queue.add(it)
            levels[it] = 0
        }

        // Fallback if there is a cycle: pick first node
        if (queue.isEmpty() && diagram.nodes.isNotEmpty()) {
            val first = diagram.nodes.keys.first()
            queue.add(first)
            levels[first] = 0
        }

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            val currLevel = levels[curr] ?: 0

            adjList[curr]?.forEach { neighbor ->
                val nextLevel = currLevel + 1
                if (!levels.containsKey(neighbor) || levels[neighbor]!! < nextLevel) {
                    levels[neighbor] = nextLevel
                    queue.add(neighbor)
                }
            }
        }

        // Any orphan nodes
        diagram.nodes.keys.forEach {
            if (!levels.containsKey(it)) levels[it] = 0
        }

        // Group nodes by level
        val levelGroups = mutableMapOf<Int, MutableList<MermaidNode>>()
        diagram.nodes.values.forEach { node ->
            val lvl = levels[node.id] ?: 0
            levelGroups.getOrPut(lvl) { mutableListOf() }.add(node)
        }

        val isTopDown = diagram.direction == "TD" || diagram.direction == "TB"
        val nodeWidth = 170f
        val nodeHeight = 75f
        val hSpacing = 70f
        val vSpacing = 90f

        // Assign positions
        val sortedLevels = levelGroups.keys.sorted()
        sortedLevels.forEach { lvl ->
            val group = levelGroups[lvl] ?: return@forEach
            val totalInGroup = group.size

            group.forEachIndexed { index, node ->
                node.width = nodeWidth
                node.height = nodeHeight

                if (isTopDown) {
                    val rowWidth = totalInGroup * nodeWidth + (totalInGroup - 1) * hSpacing
                    val startX = originX + (1280f - originX * 2f - rowWidth) / 2f
                    node.x = (startX + index * (nodeWidth + hSpacing)).coerceAtLeast(originX)
                    node.y = originY + lvl * (nodeHeight + vSpacing)
                } else {
                    // Left to Right
                    val colHeight = totalInGroup * nodeHeight + (totalInGroup - 1) * vSpacing
                    val startY = originY + (720f - originY * 2f - colHeight) / 2f
                    node.x = originX + lvl * (nodeWidth + hSpacing)
                    node.y = (startY + index * (nodeHeight + vSpacing)).coerceAtLeast(originY)
                }
            }
        }

        // 2. Render Hand-Drawn Shapes for Each Node
        val shapeColors = listOf(
            0xFF38BDF8.toInt(), // Sky blue
            0xFF34D399.toInt(), // Mint green
            0xFFFBBF24.toInt(), // Amber
            0xFFA78BFA.toInt(), // Purple
            0xFFF472B6.toInt()  // Rose
        )

        diagram.nodes.values.forEachIndexed { idx, node ->
            val nodeColor = shapeColors[idx % shapeColors.size]
            val centerX = node.x + node.width / 2f
            val centerY = node.y + node.height / 2f

            when (node.shape) {
                ExcalidrawShapeType.DIAMOND -> {
                    strokes.addAll(
                        RoughShapeGenerator.createRoughDiamond(
                            centerX = centerX,
                            centerY = centerY,
                            width = node.width * 1.05f,
                            height = node.height * 1.15f,
                            roughness = 1.1f,
                            fillStyle = RoughFillStyle.HACHURE,
                            strokeColorArgb = colorArgb,
                            fillColorArgb = nodeColor
                        )
                    )
                }
                ExcalidrawShapeType.ROUNDED_RECT -> {
                    strokes.addAll(
                        RoughShapeGenerator.createRoughRectangle(
                            x = node.x,
                            y = node.y,
                            width = node.width,
                            height = node.height,
                            roughness = 1.1f,
                            fillStyle = RoughFillStyle.HACHURE,
                            strokeColorArgb = colorArgb,
                            fillColorArgb = nodeColor
                        )
                    )
                }
                ExcalidrawShapeType.CYLINDER -> {
                    strokes.addAll(
                        RoughShapeGenerator.createRoughCylinder(
                            x = node.x,
                            y = node.y,
                            width = node.width,
                            height = node.height,
                            roughness = 1.0f,
                            strokeColorArgb = colorArgb
                        )
                    )
                }
                ExcalidrawShapeType.ELLIPSE -> {
                    strokes.addAll(
                        RoughShapeGenerator.createRoughEllipse(
                            centerX = centerX,
                            centerY = centerY,
                            radiusX = node.width / 2f,
                            radiusY = node.height / 2f,
                            roughness = 1.1f,
                            fillStyle = RoughFillStyle.HACHURE,
                            strokeColorArgb = colorArgb,
                            fillColorArgb = nodeColor
                        )
                    )
                }
                else -> {
                    // Default RECTANGLE
                    strokes.addAll(
                        RoughShapeGenerator.createRoughRectangle(
                            x = node.x,
                            y = node.y,
                            width = node.width,
                            height = node.height,
                            roughness = 1.1f,
                            fillStyle = RoughFillStyle.HACHURE,
                            strokeColorArgb = colorArgb,
                            fillColorArgb = nodeColor
                        )
                    )
                }
            }

            // Draw sketched label inside the node
            strokes.addAll(
                RoughShapeGenerator.createSketchedLabel(
                    text = node.label,
                    centerX = centerX,
                    centerY = centerY,
                    colorArgb = 0xFFFFFFFF.toInt()
                )
            )
        }

        // 3. Render Hand-Drawn Connecting Arrows for Each Edge
        diagram.edges.forEach { edge ->
            val fromNode = diagram.nodes[edge.fromId] ?: return@forEach
            val toNode = diagram.nodes[edge.toId] ?: return@forEach

            val fromCenterX = fromNode.x + fromNode.width / 2f
            val fromCenterY = fromNode.y + fromNode.height / 2f
            val toCenterX = toNode.x + toNode.width / 2f
            val toCenterY = toNode.y + toNode.height / 2f

            // Calculate start and end connection points on bounding box edges
            val startX: Float
            val startY: Float
            val endX: Float
            val endY: Float

            if (isTopDown) {
                if (toCenterY > fromCenterY) {
                    startX = fromCenterX
                    startY = fromNode.y + fromNode.height
                    endX = toCenterX
                    endY = toNode.y
                } else {
                    startX = fromNode.x + fromNode.width
                    startY = fromCenterY
                    endX = toNode.x
                    endY = toCenterY
                }
            } else {
                startX = fromNode.x + fromNode.width
                startY = fromCenterY
                endX = toNode.x
                endY = toCenterY
            }

            strokes.addAll(
                RoughShapeGenerator.createRoughArrow(
                    x1 = startX,
                    y1 = startY,
                    x2 = endX,
                    y2 = endY,
                    curved = true,
                    label = edge.label,
                    roughness = 1.0f,
                    strokeColorArgb = accentColorArgb,
                    strokeWidth = if (edge.isThick) 4.5f else 3.0f
                )
            )
        }

        return strokes
    }

    /**
     * Preset Mermaid templates
     */
    fun getTemplates(): List<Pair<String, String>> {
        return listOf(
            "OAuth 2.0 Auth Flow" to """
                flowchart TD
                    Client[Mobile Client App] -->|1. Request Token| Auth[Auth Server]
                    Auth -->|2. Verify Credentials| DB[(User Credentials DB)]
                    DB -->|3. Validated| Auth
                    Auth -->|4. Return JWT| Client
                    Client -->|5. Authorized API Call| API[Resource API]
            """.trimIndent(),

            "E-Commerce Checkout Pipeline" to """
                flowchart LR
                    Cart[Shopping Cart] --> Checkout[Checkout Page]
                    Checkout --> Payment{Process Payment?}
                    Payment -->|Approved| Order[(Order DB)]
                    Payment -->|Declined| Retry[Retry Prompt]
                    Order --> Shipping[Warehouse Dispatch]
            """.trimIndent(),

            "Microservices Architecture" to """
                flowchart TD
                    Gateway[API Gateway] --> Auth[Auth Service]
                    Gateway --> Catalog[Product Catalog]
                    Gateway --> Orders[Order Processing]
                    Orders --> EventBus((Kafka Event Stream))
                    EventBus --> Analytics[BI Analytics]
                    EventBus --> Notify[Push Notifications]
            """.trimIndent(),

            "CI/CD Build & Deploy" to """
                flowchart LR
                    Git[Git Push Main] --> Test{Run Unit Tests}
                    Test -->|Pass| Build[Assemble APK / Docker]
                    Test -->|Fail| Alert[Notify Slack Channel]
                    Build --> Deploy[Cloud Deploy Staging]
                    Deploy --> Smoke{Smoke Test}
                    Smoke -->|Green| Prod[Deploy Production]
            """.trimIndent(),

            "Order State Machine" to """
                flowchart TD
                    Created([Order Created]) --> Paid{Payment Captured}
                    Paid -->|Yes| Processing[Packaging Items]
                    Paid -->|No| Cancelled([Order Cancelled])
                    Processing --> Shipped[Carrier Transit]
                    Shipped --> Delivered([Delivered to Customer])
            """.trimIndent()
        )
    }
}
