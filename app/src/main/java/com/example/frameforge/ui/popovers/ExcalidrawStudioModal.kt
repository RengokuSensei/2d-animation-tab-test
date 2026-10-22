package com.example.frameforge.ui.popovers

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.frameforge.ai.*
import com.example.frameforge.model.DrawingStroke
import kotlinx.coroutines.launch

private val ModalDarkBg = Color(0xF2121316)
private val CardInnerBg = Color(0xFF1E2026)
private val ProcreateBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)
private val ProcreateGreen = Color(0xFF10B981)

enum class ExcalidrawTab(val title: String) {
    TEXT_TO_DIAGRAM("Text to Diagram (AI)"),
    MERMAID("Mermaid Editor"),
    WIREFRAME_TO_CODE("Wireframe to Code"),
    SMART_SHAPES("Rough Shapes Dock")
}

@Composable
fun ExcalidrawStudioModal(
    onDismiss: () -> Unit,
    onStampStrokes: (List<DrawingStroke>, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(ExcalidrawTab.TEXT_TO_DIAGRAM) }

    // Text to Diagram state
    var textPrompt by remember { mutableStateOf("OAuth 2.0 authentication flow with mobile client, auth server and user database") }
    var isGeneratingDiagram by remember { mutableStateOf(false) }
    var lastDiagramResult by remember { mutableStateOf<TextToDiagramResult?>(null) }

    // Mermaid state
    var mermaidCode by remember {
        mutableStateOf(
            """
            flowchart TD
                Client[Mobile App] -->|1. Submit Request| Gateway[API Gateway]
                Gateway --> Auth[Auth Service]
                Gateway --> Orders[Order Service]
                Orders --> DB[(Cloud Database)]
                DB --> Cache[(Redis Cache)]
            """.trimIndent()
        )
    }
    var mermaidDirection by remember { mutableStateOf("TD") }

    // Wireframe to Code state
    var selectedPreset by remember { mutableStateOf(WireframePreset.LOGIN_AUTH) }
    var wireframePrompt by remember { mutableStateOf("Mobile login screen with email, password, and social buttons") }
    var isGeneratingCode by remember { mutableStateOf(false) }
    var lastCodeResult by remember { mutableStateOf<WireframeToCodeResult?>(null) }
    var copyNotice by remember { mutableStateOf<String?>(null) }

    // Smart Shapes state
    var selectedShape by remember { mutableStateOf(ExcalidrawShapeType.RECTANGLE) }
    var selectedFillStyle by remember { mutableStateOf(RoughFillStyle.HACHURE) }
    var shapeRoughness by remember { mutableStateOf(1.2f) }
    var shapeStrokeColor by remember { mutableStateOf(0xFFFFFFFF.toInt()) }
    var shapeFillColor by remember { mutableStateOf(0xFF38BDF8.toInt()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, ProcreateBorder, RoundedCornerShape(20.dp))
                .testTag("excalidraw_studio_modal"),
            color = ModalDarkBg,
            tonalElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // --- TOP MODAL HEADER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181A20))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF6965DB)), // Excalidraw iconic violet
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Excalidraw Smart Studio",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x336965DB)
                                ) {
                                    Text(
                                        "AI & Diagrams",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Text-to-Diagram, Mermaid, Wireframe-to-Code, & Hand-Drawn Rough Shapes",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Navigation Tabs Pill Bar
                    Row(
                        modifier = Modifier
                            .background(Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(3.dp)
                    ) {
                        ExcalidrawTab.values().forEach { tab ->
                            val isSel = tab == activeTab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(if (isSel) Color(0xFF6965DB) else Color.Transparent)
                                    .clickable { activeTab = tab }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("tab_${tab.name.lowercase()}")
                            ) {
                                Text(
                                    tab.title,
                                    color = if (isSel) Color.White else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_excalidraw_modal")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
                    }
                }

                HorizontalDivider(color = ProcreateBorder, thickness = 1.dp)

                // --- TAB CONTENT ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp)
                ) {
                    when (activeTab) {
                        ExcalidrawTab.TEXT_TO_DIAGRAM -> {
                            TextToDiagramTab(
                                prompt = textPrompt,
                                onPromptChange = { textPrompt = it },
                                isGenerating = isGeneratingDiagram,
                                lastResult = lastDiagramResult,
                                onGenerate = {
                                    isGeneratingDiagram = true
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).run {
                                        // Trigger async generation
                                    }
                                },
                                onSetResult = { res ->
                                    lastDiagramResult = res
                                    isGeneratingDiagram = false
                                },
                                onStamp = { strokes ->
                                    onStampStrokes(strokes, "Stamped AI Diagram (${strokes.size} strokes)")
                                    onDismiss()
                                }
                            )
                        }

                        ExcalidrawTab.MERMAID -> {
                            MermaidEditorTab(
                                code = mermaidCode,
                                onCodeChange = { mermaidCode = it },
                                direction = mermaidDirection,
                                onDirectionChange = { mermaidDirection = it },
                                onStamp = { strokes ->
                                    onStampStrokes(strokes, "Stamped Mermaid Diagram (${strokes.size} strokes)")
                                    onDismiss()
                                }
                            )
                        }

                        ExcalidrawTab.WIREFRAME_TO_CODE -> {
                            WireframeToCodeTab(
                                prompt = wireframePrompt,
                                onPromptChange = { wireframePrompt = it },
                                selectedPreset = selectedPreset,
                                onSelectPreset = { selectedPreset = it },
                                isGenerating = isGeneratingCode,
                                lastResult = lastCodeResult,
                                onSetResult = { res ->
                                    lastCodeResult = res
                                    isGeneratingCode = false
                                },
                                onStartGenerate = { isGeneratingCode = true },
                                onCopyCode = { code ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Jetpack Compose Code", code))
                                    copyNotice = "Copied Jetpack Compose code to clipboard!"
                                },
                                onStampWireframe = { strokes ->
                                    onStampStrokes(strokes, "Stamped Wireframe Mockup (${strokes.size} strokes)")
                                    onDismiss()
                                },
                                copyNotice = copyNotice
                            )
                        }

                        ExcalidrawTab.SMART_SHAPES -> {
                            SmartShapesDockTab(
                                selectedShape = selectedShape,
                                onSelectShape = { selectedShape = it },
                                selectedFillStyle = selectedFillStyle,
                                onSelectFillStyle = { selectedFillStyle = it },
                                roughness = shapeRoughness,
                                onRoughnessChange = { shapeRoughness = it },
                                strokeColor = shapeStrokeColor,
                                onStrokeColorChange = { shapeStrokeColor = it },
                                fillColor = shapeFillColor,
                                onFillColorChange = { shapeFillColor = it },
                                onStampShape = { strokes, desc ->
                                    onStampStrokes(strokes, desc)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 1: TEXT TO DIAGRAM (AI) ----------------

@Composable
private fun TextToDiagramTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    isGenerating: Boolean,
    lastResult: TextToDiagramResult?,
    onGenerate: () -> Unit,
    onSetResult: (TextToDiagramResult) -> Unit,
    onStamp: (List<DrawingStroke>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val quickIdeas = listOf(
        "OAuth 2.0 Auth Flow",
        "Kubernetes Pod Cluster",
        "E-Commerce Checkout Funnel",
        "Video Ingest & CDN Pipeline",
        "User Onboarding Lifecycle",
        "Microservices Event Stream"
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Prompt Input & Ideas
        Card(
            modifier = Modifier.weight(0.48f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Describe Your Diagram / Architecture", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("AI synthesizes connected sketched nodes, arrows, and databases.", color = Color.Gray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth().height(140.dp).testTag("diagram_prompt_input"),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("e.g. Microservices architecture with API Gateway, Redis cache and PostgreSQL...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6965DB),
                        unfocusedBorderColor = ProcreateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Quick Architecture Ideas:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickIdeas.forEach { idea ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x226965DB),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x446965DB)),
                            modifier = Modifier.clickable { onPromptChange(idea) }
                        ) {
                            Text(idea, color = Color(0xFFA5B4FC), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onGenerate()
                        coroutineScope.launch {
                            val res = TextToDiagramEngine.generateDiagram(prompt)
                            onSetResult(res)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_diagram_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6965DB))
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesizing Diagram...", fontSize = 13.sp)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Diagram with AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Right Column: Synthesized Result & Canvas Stamping
        Card(
            modifier = Modifier.weight(0.52f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Synthesized Mermaid Diagram", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (lastResult != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (lastResult.isFromAi) Color(0x3310B981) else Color(0x3338BDF8)
                        ) {
                            Text(
                                if (lastResult.isFromAi) "Gemini 3.5 Flash" else "Smart Synthesizer",
                                color = if (lastResult.isFromAi) Color(0xFF34D399) else Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1115))
                        .padding(12.dp)
                ) {
                    val code = lastResult?.mermaidCode ?: """
                        flowchart TD
                            User([User]) --> App[FrameForge Tablet Studio]
                            App --> Engine[Rough Shape Engine]
                            Engine --> Canvas[Animation Frame Cel]
                    """.trimIndent()

                    Text(
                        text = code,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val res = lastResult
                        if (res != null) {
                            onStamp(res.strokes)
                        } else {
                            // Synthesize on the fly
                            val defCode = TextToDiagramEngine.generateOfflineMermaid(prompt)
                            val parsed = MermaidDiagramEngine.parseMermaid(defCode)
                            val st = MermaidDiagramEngine.renderDiagramToStrokes(parsed)
                            onStamp(st)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stamp_diagram_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateGreen)
                ) {
                    Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stamp Diagram onto Active Layer", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- TAB 2: MERMAID EDITOR ----------------

@Composable
private fun MermaidEditorTab(
    code: String,
    onCodeChange: (String) -> Unit,
    direction: String,
    onDirectionChange: (String) -> Unit,
    onStamp: (List<DrawingStroke>) -> Unit
) {
    val templates = remember { MermaidDiagramEngine.getTemplates() }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Template Selector & Syntax Editor
        Card(
            modifier = Modifier.weight(0.55f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mermaid Flowchart Syntax", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Write or paste Mermaid graph syntax. Node types: [Rect], (Round), {Diamond}, [(Database)], ((Circle)).", color = Color.Gray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // Presets row
                Text("Diagram Presets:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    templates.forEach { (title, templateCode) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x2238BDF8),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8)),
                            modifier = Modifier.clickable { onCodeChange(templateCode) }
                        ) {
                            Text(title, color = Color(0xFF7DD3FC), fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.fillMaxWidth().weight(1f).testTag("mermaid_code_editor"),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF6965DB),
                        unfocusedBorderColor = ProcreateBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }

        // Right Column: Live Layout Specs & Action
        Card(
            modifier = Modifier.weight(0.45f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            val parsed = remember(code) { MermaidDiagramEngine.parseMermaid(code) }

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Diagram Analysis & Structure", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Direction", parsed.direction, Modifier.weight(1f))
                    StatCard("Nodes", "${parsed.nodes.size}", Modifier.weight(1f))
                    StatCard("Connections", "${parsed.edges.size}", Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Detected Node Elements:", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(parsed.nodes.values.toList().size) { idx ->
                        val node = parsed.nodes.values.toList()[idx]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF13151A))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(node.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text(node.shape.label, color = Color(0xFF38BDF8), fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val strokes = MermaidDiagramEngine.renderDiagramToStrokes(parsed)
                        onStamp(strokes)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("render_mermaid_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6965DB))
                ) {
                    Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Render Hand-Drawn Diagram to Canvas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- TAB 3: WIREFRAME TO CODE (MAKE REAL) ----------------

@Composable
private fun WireframeToCodeTab(
    prompt: String,
    onPromptChange: (String) -> Unit,
    selectedPreset: WireframePreset,
    onSelectPreset: (WireframePreset) -> Unit,
    isGenerating: Boolean,
    lastResult: WireframeToCodeResult?,
    onSetResult: (WireframeToCodeResult) -> Unit,
    onStartGenerate: () -> Unit,
    onCopyCode: (String) -> Unit,
    onStampWireframe: (List<DrawingStroke>) -> Unit,
    copyNotice: String?
) {
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Wireframe Selector & Generate Controls
        Card(
            modifier = Modifier.weight(0.42f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Excalidraw 'Make Real' (Wireframe to Code)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Transforms hand-drawn UI sketches into production Jetpack Compose code with Material 3.", color = Color.Gray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select UI Wireframe Model:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                WireframePreset.values().forEach { preset ->
                    val isSel = preset == selectedPreset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFF2E2C5E) else Color(0x18FFFFFF))
                            .border(1.dp, if (isSel) Color(0xFF6965DB) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectPreset(preset)
                                onPromptChange(preset.description)
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSel,
                            onClick = {
                                onSelectPreset(preset)
                                onPromptChange(preset.description)
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF6965DB)),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(preset.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(preset.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        onStartGenerate()
                        coroutineScope.launch {
                            val res = WireframeToCodeEngine.convertWireframeToCompose(prompt, selectedPreset)
                            onSetResult(res)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("generate_compose_code_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6965DB))
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Synthesizing Compose Code...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Jetpack Compose Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val currentResult = lastResult
                        if (currentResult != null) {
                            onStampWireframe(currentResult.wireframeStrokes)
                        } else {
                            coroutineScope.launch {
                                val res = WireframeToCodeEngine.convertWireframeToCompose(prompt, selectedPreset)
                                onSetResult(res)
                                onStampWireframe(res.wireframeStrokes)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("draw_wireframe_on_canvas_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ProcreateBlue)
                ) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Draw Wireframe Mockup onto Canvas", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Right Column: Compose Code Inspector & Clipboard Copy
        Card(
            modifier = Modifier.weight(0.58f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            val code = lastResult?.composeCode ?: WireframeToCodeEngine.generateWireframeSketchedStrokes(selectedPreset, 0f, 0f).let {
                // Default code
                "// Tap 'Generate Jetpack Compose Code' to compile production code from your wireframe."
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Jetpack Compose Code Inspector", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (lastResult != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(6.dp), color = Color(0x3310B981)) {
                                Text(if (lastResult.isFromAi) "Gemini 3.5 Flash" else "Verified M3", color = Color(0xFF34D399), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    Button(
                        onClick = { onCopyCode(lastResult?.composeCode ?: code) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                        modifier = Modifier.height(34.dp).testTag("copy_code_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Code", color = Color.White, fontSize = 11.sp)
                    }
                }

                if (copyNotice != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(copyNotice, color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1115))
                        .padding(12.dp)
                ) {
                    Text(
                        text = code,
                        color = Color(0xFF93C5FD),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

// ---------------- TAB 4: SMART HAND-DRAWN SHAPES DOCK ----------------

@Composable
private fun SmartShapesDockTab(
    selectedShape: ExcalidrawShapeType,
    onSelectShape: (ExcalidrawShapeType) -> Unit,
    selectedFillStyle: RoughFillStyle,
    onSelectFillStyle: (RoughFillStyle) -> Unit,
    roughness: Float,
    onRoughnessChange: (Float) -> Unit,
    strokeColor: Int,
    onStrokeColorChange: (Int) -> Unit,
    fillColor: Int,
    onFillColorChange: (Int) -> Unit,
    onStampShape: (List<DrawingStroke>, String) -> Unit
) {
    val colorPalette = listOf(
        0xFFFFFFFF.toInt(), // White
        0xFF38BDF8.toInt(), // Sky blue
        0xFF34D399.toInt(), // Mint green
        0xFFFBBF24.toInt(), // Amber
        0xFFF472B6.toInt(), // Rose
        0xFFA78BFA.toInt(), // Violet
        0xFFF87171.toInt()  // Coral red
    )

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Shape Grid Selector
        Card(
            modifier = Modifier.weight(0.55f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Hand-Drawn Excalidraw Shape", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Stamps organic wobbly dual-stroke vector shapes onto the active animation layer.", color = Color.Gray, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(14.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(ExcalidrawShapeType.values()) { shape ->
                        val isSel = shape == selectedShape
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clickable { onSelectShape(shape) }
                                .border(1.5.dp, if (isSel) Color(0xFF6965DB) else Color.Transparent, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF2E2C5E) else Color(0x18FFFFFF))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    when (shape) {
                                        ExcalidrawShapeType.RECTANGLE -> Icons.Default.CropSquare
                                        ExcalidrawShapeType.ROUNDED_RECT -> Icons.Default.RoundedCorner
                                        ExcalidrawShapeType.DIAMOND -> Icons.Default.Diamond
                                        ExcalidrawShapeType.ELLIPSE -> Icons.Default.RadioButtonUnchecked
                                        ExcalidrawShapeType.CYLINDER -> Icons.Default.Storage
                                        ExcalidrawShapeType.CLOUD -> Icons.Default.Cloud
                                        ExcalidrawShapeType.ARROW -> Icons.Default.ArrowForward
                                        ExcalidrawShapeType.STICKY_NOTE -> Icons.Default.StickyNote2
                                        ExcalidrawShapeType.ACTOR -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = if (isSel) Color.White else Color.LightGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(shape.label, color = if (isSel) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Right Column: Style Options & Stamp Button
        Card(
            modifier = Modifier.weight(0.45f).fillMaxHeight(),
            colors = CardDefaults.cardColors(containerColor = CardInnerBg),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Shape Style & Roughness", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(14.dp))

                // Fill style pills
                Text("Fill Style:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoughFillStyle.values().forEach { fill ->
                        val isSel = fill == selectedFillStyle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) Color(0xFF6965DB) else Color(0x22FFFFFF),
                            modifier = Modifier.clickable { onSelectFillStyle(fill) }
                        ) {
                            Text(fill.label, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Roughness slider
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Organic Roughness", color = Color.LightGray, fontSize = 11.sp)
                    Text("%.1f".format(roughness), color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = roughness,
                    onValueChange = onRoughnessChange,
                    valueRange = 0.5f..2.5f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF6965DB), activeTrackColor = Color(0xFF6965DB))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stroke Color Picker
                Text("Stroke Color:", color = Color.LightGray, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorPalette.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(2.dp, if (c == strokeColor) Color.White else Color.Transparent, CircleShape)
                                .clickable { onStrokeColorChange(c) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val centerX = 640f
                        val centerY = 360f
                        val strokes = when (selectedShape) {
                            ExcalidrawShapeType.RECTANGLE -> RoughShapeGenerator.createRoughRectangle(centerX - 100f, centerY - 60f, 200f, 120f, roughness, selectedFillStyle, strokeColor, fillColor)
                            ExcalidrawShapeType.ROUNDED_RECT -> RoughShapeGenerator.createRoughRectangle(centerX - 100f, centerY - 60f, 200f, 120f, roughness, selectedFillStyle, strokeColor, fillColor)
                            ExcalidrawShapeType.DIAMOND -> RoughShapeGenerator.createRoughDiamond(centerX, centerY, 180f, 140f, roughness, selectedFillStyle, strokeColor, fillColor)
                            ExcalidrawShapeType.ELLIPSE -> RoughShapeGenerator.createRoughEllipse(centerX, centerY, 90f, 70f, roughness, selectedFillStyle, strokeColor, fillColor)
                            ExcalidrawShapeType.CYLINDER -> RoughShapeGenerator.createRoughCylinder(centerX - 80f, centerY - 60f, 160f, 120f, roughness, strokeColor)
                            ExcalidrawShapeType.CLOUD -> RoughShapeGenerator.createRoughCloud(centerX - 100f, centerY - 60f, 200f, 120f, roughness, strokeColor)
                            ExcalidrawShapeType.ARROW -> RoughShapeGenerator.createRoughArrow(centerX - 120f, centerY, centerX + 120f, centerY, true, "Flow", roughness, strokeColor)
                            ExcalidrawShapeType.STICKY_NOTE -> RoughShapeGenerator.createRoughStickyNote(centerX - 80f, centerY - 80f, 160f, 160f, "Idea", 0xFFFDE047.toInt(), strokeColor)
                            ExcalidrawShapeType.ACTOR -> RoughShapeGenerator.createRoughActor(centerX, centerY - 60f, 120f, strokeColor)
                        }
                        onStampShape(strokes, "Stamped ${selectedShape.label}")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stamp_shape_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProcreateGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stamp ${selectedShape.label} onto Canvas", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13151A))
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.Gray, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color(0xFF38BDF8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
