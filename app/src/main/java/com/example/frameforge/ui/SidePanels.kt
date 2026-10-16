package com.example.frameforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.ai.AiMotionAssistant
import com.example.frameforge.ai.ColorPaletteSuggestion
import com.example.frameforge.engine.ProceduralMotion
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.EasingCurve
import com.example.frameforge.viewmodel.SidePanelType
import java.io.File

import com.example.frameforge.model.BlendModeType
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.PressureCurve

@Composable
fun SidePanelDock(
    activePanel: SidePanelType?,
    project: AnimationProject,
    activeLayerId: String,
    brushColor: Color,
    brushSize: Float,
    brushOpacity: Float,
    brushSmoothing: Float,
    selectedBoneId: String?,
    isIkMode: Boolean,
    savedProjects: List<Pair<String, String>>,
    palmRejectionMode: PalmRejectionMode = PalmRejectionMode.AUTOMATIC,
    pressureCurve: PressureCurve = PressureCurve.LINEAR,
    recentColors: List<Color> = emptyList(),
    customPalette: List<Color> = emptyList(),
    onClose: () -> Unit,
    onSelectPanel: (SidePanelType) -> Unit,
    onSetActiveLayer: (String) -> Unit,
    onAddLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit = {},
    onMergeLayerDown: (String) -> Unit = {},
    onSetLayerBlendMode: (String, BlendModeType) -> Unit = { _, _ -> },
    onToggleLayerVisibility: (String) -> Unit,
    onToggleLayerLock: (String) -> Unit,
    onSetLayerOpacity: (String, Float) -> Unit,
    onSetColor: (Color) -> Unit,
    onAddToPalette: (Color) -> Unit = {},
    onRemoveFromPalette: (Color) -> Unit = {},
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onSetBrushSmoothing: (Float) -> Unit,
    onSetPalmRejection: (PalmRejectionMode) -> Unit = {},
    onSetPressureCurve: (PressureCurve) -> Unit = {},
    onSelectBone: (String?) -> Unit,
    onRotateBone: (Float) -> Unit,
    onToggleIk: () -> Unit,
    onApplyProcedural: (ProceduralMotion.MotionType) -> Unit,
    onApplyPromptMotion: (String) -> Unit,
    onApplyInBetween: (Int) -> Unit,
    onApplyPalette: (ColorPaletteSuggestion) -> Unit,
    onSaveProject: () -> Unit,
    onLoadProject: (String) -> Unit,
    onNewProject: (String) -> Unit,
    onExportGif: (Boolean, (File?) -> Unit) -> Unit,
    onExportPngSequence: ((List<File>) -> Unit) -> Unit,
    onExportMp4: ((File?) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (activePanel == null) return

    Surface(
        modifier = modifier
            .width(330.dp)
            .fillMaxHeight()
            .testTag("side_panel_dock"),
        color = Color(0xFF1E222B),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Panel Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = when (activePanel) {
                        SidePanelType.LAYERS -> "Layers & Blend Modes"
                        SidePanelType.COLORS -> "Advanced Color & Palette"
                        SidePanelType.SKELETON_RIG -> "Character Rig & IK"
                        SidePanelType.AI_ASSISTANT -> "AI Motion Assistant"
                        SidePanelType.EXPORT -> "Render & Export"
                        SidePanelType.PROJECTS -> "Projects"
                        SidePanelType.SETTINGS -> "Tablet & Stylus Settings"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("close_panel_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close panel", tint = Color.LightGray)
                }
            }

            Divider(color = Color(0xFF2E3440))

            // Body content according to active panel
            Box(modifier = Modifier.weight(1f).padding(12.dp)) {
                when (activePanel) {
                    SidePanelType.LAYERS -> LayersPanelContent(
                        project = project,
                        activeLayerId = activeLayerId,
                        onSetActiveLayer = onSetActiveLayer,
                        onAddLayer = onAddLayer,
                        onDeleteLayer = onDeleteLayer,
                        onDuplicateLayer = onDuplicateLayer,
                        onMergeLayerDown = onMergeLayerDown,
                        onSetBlendMode = onSetLayerBlendMode,
                        onToggleVisibility = onToggleLayerVisibility,
                        onToggleLock = onToggleLayerLock,
                        onSetOpacity = onSetLayerOpacity
                    )
                    SidePanelType.COLORS -> ColorsPanelContent(
                        brushColor = brushColor,
                        brushSize = brushSize,
                        brushOpacity = brushOpacity,
                        brushSmoothing = brushSmoothing,
                        recentColors = recentColors,
                        customPalette = customPalette,
                        onSetColor = onSetColor,
                        onAddToPalette = onAddToPalette,
                        onRemoveFromPalette = onRemoveFromPalette,
                        onSetSize = onSetBrushSize,
                        onSetOpacity = onSetBrushOpacity,
                        onSetSmoothing = onSetBrushSmoothing,
                        onApplyPalette = onApplyPalette
                    )
                    SidePanelType.SKELETON_RIG -> RigPanelContent(
                        project = project,
                        selectedBoneId = selectedBoneId,
                        isIkMode = isIkMode,
                        onSelectBone = onSelectBone,
                        onRotateBone = onRotateBone,
                        onToggleIk = onToggleIk,
                        onApplyMotion = onApplyProcedural
                    )
                    SidePanelType.AI_ASSISTANT -> AiAssistantPanelContent(
                        onApplyPromptMotion = onApplyPromptMotion,
                        onApplyInBetween = onApplyInBetween,
                        onApplyPalette = onApplyPalette
                    )
                    SidePanelType.EXPORT -> ExportPanelContent(
                        project = project,
                        onExportGif = onExportGif,
                        onExportPngSequence = onExportPngSequence,
                        onExportMp4 = onExportMp4
                    )
                    SidePanelType.PROJECTS -> ProjectsPanelContent(
                        savedProjects = savedProjects,
                        currentProjectName = project.name,
                        onSaveProject = onSaveProject,
                        onLoadProject = onLoadProject,
                        onNewProject = onNewProject
                    )
                    SidePanelType.SETTINGS -> SettingsPanelContent(
                        palmRejectionMode = palmRejectionMode,
                        pressureCurve = pressureCurve,
                        onSetPalmRejection = onSetPalmRejection,
                        onSetPressureCurve = onSetPressureCurve
                    )
                }
            }
        }
    }
}

@Composable
private fun LayersPanelContent(
    project: AnimationProject,
    activeLayerId: String,
    onSetActiveLayer: (String) -> Unit,
    onAddLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onMergeLayerDown: (String) -> Unit,
    onSetBlendMode: (String, BlendModeType) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onSetOpacity: (String, Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Layer Stack", color = Color(0xFF94A3B8), fontSize = 12.sp)
            FilledTonalButton(
                onClick = { onAddLayer("Layer ${project.layers.size + 1}") },
                modifier = Modifier.height(32.dp).testTag("add_layer_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Layer", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Layer", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(project.layers) { layer ->
                val isActive = layer.id == activeLayerId
                var showBlendMenu by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSetActiveLayer(layer.id) }
                        .testTag("layer_card_${layer.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) Color(0xFF263238) else Color(0xFF14171D)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onToggleVisibility(layer.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility",
                                        tint = if (layer.isVisible) Color(0xFF00E5FF) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    layer.name,
                                    color = Color.White,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }

                            Row {
                                IconButton(
                                    onClick = { onDuplicateLayer(layer.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.ContentCopy,
                                        contentDescription = "Duplicate layer",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onMergeLayerDown(layer.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        androidx.compose.material.icons.Icons.Default.Layers,
                                        contentDescription = "Merge down",
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onToggleLock(layer.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Lock",
                                        tint = if (layer.isLocked) Color(0xFFFF9800) else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (project.layers.size > 1) {
                                    IconButton(
                                        onClick = { onDeleteLayer(layer.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete layer", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Opacity slider and Blend Mode for active layer
                        if (isActive) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Opacity: ${(layer.opacity * 100).toInt()}%", color = Color.Gray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Slider(
                                    value = layer.opacity,
                                    onValueChange = { onSetOpacity(layer.id, it) },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                            ) {
                                Text("Blend Mode:", color = Color.Gray, fontSize = 10.sp)
                                Box {
                                    FilledTonalButton(
                                        onClick = { showBlendMenu = true },
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(layer.blendMode.label, fontSize = 10.sp)
                                    }
                                    androidx.compose.material3.DropdownMenu(
                                        expanded = showBlendMenu,
                                        onDismissRequest = { showBlendMenu = false }
                                    ) {
                                        BlendModeType.values().forEach { mode ->
                                            androidx.compose.material3.DropdownMenuItem(
                                                text = { Text(mode.label) },
                                                onClick = {
                                                    onSetBlendMode(layer.id, mode)
                                                    showBlendMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorsPanelContent(
    brushColor: Color,
    brushSize: Float,
    brushOpacity: Float,
    brushSmoothing: Float,
    recentColors: List<Color>,
    customPalette: List<Color>,
    onSetColor: (Color) -> Unit,
    onAddToPalette: (Color) -> Unit,
    onRemoveFromPalette: (Color) -> Unit,
    onSetSize: (Float) -> Unit,
    onSetOpacity: (Float) -> Unit,
    onSetSmoothing: (Float) -> Unit,
    onApplyPalette: (ColorPaletteSuggestion) -> Unit
) {
    var modeTab by remember { mutableStateOf(0) } // 0 = HSV/RGB/Hex, 1 = Palettes, 2 = Brush Settings
    var hexText by remember(brushColor) {
        val argb = brushColor.toArgb()
        mutableStateOf(String.format("#%06X", 0xFFFFFF and argb))
    }

    // Convert current Color to HSV
    val hsv = remember(brushColor) {
        val hsvArr = FloatArray(3)
        android.graphics.Color.colorToHSV(brushColor.toArgb(), hsvArr)
        hsvArr
    }

    Column(modifier = Modifier.fillMaxHeight().padding(2.dp)) {
        // Tab switcher
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color(0xFF14171D)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Color Picker", "Palettes", "Brush FX").forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { modeTab = index }
                        .background(if (modeTab == index) Color(0xFF263238) else Color.Transparent)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        title,
                        color = if (modeTab == index) Color(0xFF00E5FF) else Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (modeTab) {
            0 -> {
                // --- ADVANCED COLOR PICKER (HSV / RGB / HEX) ---
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        // Current Color Preview Banner + Hex input
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(brushColor)
                                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hex Color Code", color = Color.Gray, fontSize = 9.sp)
                                OutlinedTextField(
                                    value = hexText,
                                    onValueChange = { input ->
                                        hexText = input
                                        try {
                                            if (input.startsWith("#") && (input.length == 7 || input.length == 9)) {
                                                val parsed = android.graphics.Color.parseColor(input)
                                                onSetColor(Color(parsed))
                                            }
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF00E5FF)
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    item {
                        // Recent Colors History
                        Text("Recent Colors", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            recentColors.take(8).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(c)
                                        .border(1.dp, Color.White, CircleShape)
                                        .clickable { onSetColor(c) }
                                )
                            }
                        }
                    }

                    item {
                        // HSV Sliders
                        Text("HSV Spectrum", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)

                        // Hue (0..360)
                        Text("Hue: ${hsv[0].toInt()}°", color = Color.White, fontSize = 10.sp)
                        Slider(
                            value = hsv[0],
                            onValueChange = { newH ->
                                val updated = android.graphics.Color.HSVToColor(floatArrayOf(newH, hsv[1], hsv[2]))
                                onSetColor(Color(updated))
                            },
                            valueRange = 0f..360f
                        )

                        // Saturation (0..1)
                        Text("Saturation: ${(hsv[1] * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
                        Slider(
                            value = hsv[1],
                            onValueChange = { newS ->
                                val updated = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], newS, hsv[2]))
                                onSetColor(Color(updated))
                            },
                            valueRange = 0f..1f
                        )

                        // Value / Brightness (0..1)
                        Text("Brightness: ${(hsv[2] * 100).toInt()}%", color = Color.White, fontSize = 10.sp)
                        Slider(
                            value = hsv[2],
                            onValueChange = { newV ->
                                val updated = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], newV))
                                onSetColor(Color(updated))
                            },
                            valueRange = 0f..1f
                        )
                    }

                    item {
                        // Custom Palette Row with Add button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Custom Swatches", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            FilledTonalButton(
                                onClick = { onAddToPalette(brushColor) },
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("+ Add Current", fontSize = 9.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            customPalette.forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(c)
                                        .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                                        .clickable { onSetColor(c) }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // --- PRESET PALETTES ---
                val palettes = remember { AiMotionAssistant.getSuggestedPalettes() }
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(palettes) { pal ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplyPalette(pal) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF14171D))
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(pal.themeName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    pal.colorsArgb.forEach { argb ->
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(argb))
                                                .border(1.dp, Color.White, CircleShape)
                                                .clickable { onSetColor(Color(argb)) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // --- BRUSH SETTINGS ---
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Brush Size: ${brushSize.toInt()} px", color = Color.White, fontSize = 11.sp)
                    Slider(
                        value = brushSize,
                        onValueChange = onSetSize,
                        valueRange = 1f..120f
                    )

                    Text("Flow Opacity: ${(brushOpacity * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                    Slider(
                        value = brushOpacity,
                        onValueChange = onSetOpacity,
                        valueRange = 0.05f..1f
                    )

                    Text("Line Smoothing (Stabilizer): ${(brushSmoothing * 100).toInt()}%", color = Color.White, fontSize = 11.sp)
                    Slider(
                        value = brushSmoothing,
                        onValueChange = onSetSmoothing,
                        valueRange = 0f..0.95f
                    )
                }
            }
        }
    }
}

@Composable
private fun RigPanelContent(
    project: AnimationProject,
    selectedBoneId: String?,
    isIkMode: Boolean,
    onSelectBone: (String?) -> Unit,
    onRotateBone: (Float) -> Unit,
    onToggleIk: () -> Unit,
    onApplyMotion: (ProceduralMotion.MotionType) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Kinematics Mode", color = Color.White, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isIkMode) "2-Bone IK" else "FK (Direct)", color = Color(0xFF00E5FF), fontSize = 11.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(checked = isIkMode, onCheckedChange = { onToggleIk() })
            }
        }

        Divider(color = Color(0xFF2E3440), modifier = Modifier.padding(vertical = 8.dp))

        Text("Bones Hierarchy", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        LazyColumn(
            modifier = Modifier.weight(0.5f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(project.boneRig.bones) { bone ->
                val isSelected = bone.id == selectedBoneId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) Color(0xFF263238) else Color(0xFF14171D))
                        .clickable { onSelectBone(bone.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(bone.colorArgb))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(bone.name, color = if (isSelected) Color(0xFF00E5FF) else Color.White, fontSize = 11.sp)
                }
            }
        }

        // Rotate selected bone control
        if (selectedBoneId != null) {
            val currBone = project.boneRig.bones.find { it.id == selectedBoneId }
            Text("Rotate ${currBone?.name ?: ""}", color = Color.White, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = { onRotateBone(-10f) },
                    modifier = Modifier.height(30.dp)
                ) { Text("-10°", fontSize = 10.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = { onRotateBone(10f) },
                    modifier = Modifier.height(30.dp)
                ) { Text("+10°", fontSize = 10.sp) }
            }
        }

        Divider(color = Color(0xFF2E3440), modifier = Modifier.padding(vertical = 8.dp))

        Text("Procedural Motion Presets", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = { onApplyMotion(ProceduralMotion.MotionType.WALK) },
                    modifier = Modifier.weight(1f).height(32.dp).testTag("preset_walk")
                ) { Text("Walk", fontSize = 10.sp) }

                FilledTonalButton(
                    onClick = { onApplyMotion(ProceduralMotion.MotionType.RUN) },
                    modifier = Modifier.weight(1f).height(32.dp).testTag("preset_run")
                ) { Text("Run", fontSize = 10.sp) }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(
                    onClick = { onApplyMotion(ProceduralMotion.MotionType.WAVE) },
                    modifier = Modifier.weight(1f).height(32.dp).testTag("preset_wave")
                ) { Text("Wave", fontSize = 10.sp) }

                FilledTonalButton(
                    onClick = { onApplyMotion(ProceduralMotion.MotionType.IDLE) },
                    modifier = Modifier.weight(1f).height(32.dp).testTag("preset_idle")
                ) { Text("Idle", fontSize = 10.sp) }
            }

            FilledTonalButton(
                onClick = { onApplyMotion(ProceduralMotion.MotionType.JUMP) },
                modifier = Modifier.fillMaxWidth().height(32.dp).testTag("preset_jump")
            ) { Text("Jump & Land", fontSize = 10.sp) }
        }
    }
}

@Composable
private fun AiAssistantPanelContent(
    onApplyPromptMotion: (String) -> Unit,
    onApplyInBetween: (Int) -> Unit,
    onApplyPalette: (ColorPaletteSuggestion) -> Unit
) {
    var promptText by remember { mutableStateOf("walk slowly and wave") }
    var inBetweenCount by remember { mutableIntStateOf(2) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section A: Text-to-Motion
        Text("AI Text-to-Motion", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Generates realistic keyframes from physical kinematic descriptions", color = Color.Gray, fontSize = 10.sp)

        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("e.g. walk slowly, jump high, fast run", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth().testTag("prompt_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF00E5FF),
                unfocusedBorderColor = Color(0xFF475569)
            ),
            singleLine = true
        )

        Button(
            onClick = { onApplyPromptMotion(promptText) },
            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("generate_motion_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate Motion Keyframes", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color(0xFF2E3440))

        // Section B: Intermediate Frame Generation (In-betweening)
        Text("In-Between Generator", color = Color(0xFFFF9800), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Interpolates drawings & poses between keyframes", color = Color.Gray, fontSize = 10.sp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("In-between frames: $inBetweenCount", color = Color.White, fontSize = 11.sp)
            Row {
                listOf(1, 2, 3, 5).forEach { count ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (inBetweenCount == count) Color(0xFFFF9800) else Color(0xFF2A2E39))
                            .clickable { inBetweenCount = count },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$count", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        FilledTonalButton(
            onClick = { onApplyInBetween(inBetweenCount) },
            modifier = Modifier.fillMaxWidth().height(36.dp).testTag("in_between_button")
        ) {
            Text("Generate $inBetweenCount In-Between Frames", fontSize = 11.sp)
        }

        Divider(color = Color(0xFF2E3440))

        // Section C: Optical Motion / Video Reference
        Text("Optical Reference Tracker", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Tracks motion trajectories (Head, Wrist, Ankle) to map to character rig", color = Color.Gray, fontSize = 10.sp)

        OutlinedButton(
            onClick = {
                val tracked = AiMotionAssistant.trackReferencePoints()
                onApplyPromptMotion("pendulum swing")
            },
            modifier = Modifier.fillMaxWidth().height(34.dp).testTag("track_video_button")
        ) {
            Icon(Icons.Default.Movie, contentDescription = "Track", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Optical Point Tracker", fontSize = 10.sp)
        }
    }
}

@Composable
private fun ExportPanelContent(
    project: AnimationProject,
    onExportGif: (Boolean, (File?) -> Unit) -> Unit,
    onExportPngSequence: ((List<File>) -> Unit) -> Unit,
    onExportMp4: ((File?) -> Unit) -> Unit
) {
    var withBg by remember { mutableStateOf(true) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Project Specs", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14171D))
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text("Canvas: ${project.canvasWidth} x ${project.canvasHeight}", color = Color.White, fontSize = 11.sp)
                Text("Frames: ${project.totalFrames} @ ${project.fps} FPS", color = Color.White, fontSize = 11.sp)
                Text("Layers: ${project.layers.size} layers", color = Color.White, fontSize = 11.sp)
                Text("Duration: ${"%.2f".format(project.totalFrames.toFloat() / project.fps)} seconds", color = Color(0xFF00E5FF), fontSize = 11.sp)
            }
        }

        // --- MP4 Video Export (H.264) ---
        Button(
            onClick = {
                isExporting = true
                onExportMp4 { file ->
                    isExporting = false
                    exportResult = if (file != null) "MP4 Exported: ${file.name} (${file.length() / 1024} KB)" else "MP4 Export failed"
                }
            },
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("export_mp4_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Movie, contentDescription = "Export MP4")
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isExporting) "Encoding H.264..." else "Export MP4 Video (H.264)", fontWeight = FontWeight.Bold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("White Background (for GIF):", color = Color.White, fontSize = 11.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = withBg, onCheckedChange = { withBg = it })
        }

        // --- Animated GIF Export ---
        Button(
            onClick = {
                isExporting = true
                onExportGif(withBg) { file ->
                    isExporting = false
                    exportResult = if (file != null) "GIF Exported: ${file.name} (${file.length() / 1024} KB)" else "Export failed"
                }
            },
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("export_gif_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = "Export")
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isExporting) "Encoding GIF..." else "Export Animated GIF", fontWeight = FontWeight.Bold)
        }

        // --- PNG Sequence Export ---
        OutlinedButton(
            onClick = {
                isExporting = true
                onExportPngSequence { files ->
                    isExporting = false
                    exportResult = "Exported ${files.size} transparent PNG frames"
                }
            },
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth().height(42.dp).testTag("export_png_seq_button")
        ) {
            Icon(Icons.Default.Layers, contentDescription = "PNG Sequence")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Export PNG Sequence (Transparent)", fontSize = 11.sp)
        }

        if (exportResult != null) {
            Text(exportResult!!, color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SettingsPanelContent(
    palmRejectionMode: PalmRejectionMode,
    pressureCurve: PressureCurve,
    onSetPalmRejection: (PalmRejectionMode) -> Unit,
    onSetPressureCurve: (PressureCurve) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tablet Palm Rejection", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Optimized for tablet drawing touch screens and active styluses (S-Pen, Precision Pen)", color = Color.Gray, fontSize = 10.sp)

        PalmRejectionMode.values().forEach { mode ->
            val isSelected = mode == palmRejectionMode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetPalmRejection(mode) }
                    .border(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF263238) else Color(0xFF14171D))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = isSelected,
                        onClick = { onSetPalmRejection(mode) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(mode.label, color = Color.White, fontSize = 11.sp)
                }
            }
        }

        Divider(color = Color(0xFF2E3440))

        Text("Stylus Pressure Sensitivity", color = Color(0xFFFF9800), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Maps stylus hardware pressure to line weight and flow opacity", color = Color.Gray, fontSize = 10.sp)

        PressureCurve.values().forEach { curve ->
            val isSelected = curve == pressureCurve
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetPressureCurve(curve) }
                    .border(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) Color(0xFFFF9800) else Color(0xFF334155), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF332A1E) else Color(0xFF14171D))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.RadioButton(
                        selected = isSelected,
                        onClick = { onSetPressureCurve(curve) }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(curve.label, color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ProjectsPanelContent(
    savedProjects: List<Pair<String, String>>,
    currentProjectName: String,
    onSaveProject: () -> Unit,
    onLoadProject: (String) -> Unit,
    onNewProject: (String) -> Unit
) {
    var newProjName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onSaveProject,
            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("save_project_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF), contentColor = Color.Black)
        ) {
            Icon(Icons.Default.Save, contentDescription = "Save")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Current Project", fontWeight = FontWeight.Bold)
        }

        Divider(color = Color(0xFF2E3440))

        Text("Create New Project", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = newProjName,
            onValueChange = { newProjName = it },
            placeholder = { Text("Project title", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        FilledTonalButton(
            onClick = {
                if (newProjName.isNotBlank()) {
                    onNewProject(newProjName)
                    newProjName = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(34.dp).testTag("create_project_button")
        ) {
            Text("Create Project")
        }

        Divider(color = Color(0xFF2E3440))

        Text("Saved Projects (${savedProjects.size})", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(savedProjects) { (id, name) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLoadProject(id) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14171D))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Folder", tint = Color(0xFFFF9800), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
