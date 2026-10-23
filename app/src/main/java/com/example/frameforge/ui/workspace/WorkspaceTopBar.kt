package com.example.frameforge.ui.workspace

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.PalmRejectionMode
import com.example.frameforge.model.StudioWorkspace

private val GlassBg = Color(0xF0131418)
private val GlassBorder = Color(0x2EFFFFFF)
private val ActiveAccent = Color(0xFF0A84FF)

@Composable
fun WorkspaceTopBar(
    projectName: String,
    currentWorkspace: StudioWorkspace,
    onSelectWorkspace: (StudioWorkspace) -> Unit,
    onGalleryClick: () -> Unit,
    onUndoClick: () -> Unit = {},
    onRedoClick: () -> Unit = {},
    brushColor: Color = Color.Black,
    palmRejectionMode: PalmRejectionMode = PalmRejectionMode.AUTOMATIC,
    onTogglePalmRejectionMode: () -> Unit = {},
    onLayersClick: () -> Unit = {},
    onColorPickerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .testTag("workspace_top_bar"),
        shape = RoundedCornerShape(12.dp),
        color = GlassBg,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // --- LEFT: BRAND & PROJECT TITLE ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                        .clickable { onGalleryClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = "Projects",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Projects",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = projectName,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // --- CENTER: DAVINCI RESOLVE WORKSPACE SWITCHER ---
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E2128))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudioWorkspace.values().forEach { ws ->
                    val isSelected = ws == currentWorkspace
                    val tabBg by animateColorAsState(
                        targetValue = if (isSelected) ActiveAccent else Color.Transparent,
                        label = "tab_bg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color(0xFF94A3B8),
                        label = "tab_text"
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tabBg)
                            .clickable { onSelectWorkspace(ws) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("workspace_tab_${ws.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ws.badge,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ws.label.uppercase(),
                            color = textColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // --- RIGHT: CONTEXTUAL ACTIONS ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Quick Undo / Redo
                IconButton(
                    onClick = onUndoClick,
                    modifier = Modifier.size(32.dp).testTag("top_undo_button")
                ) {
                    Icon(
                        Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedoClick,
                    modifier = Modifier.size(32.dp).testTag("top_redo_button")
                ) {
                    Icon(
                        Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Palm Rejection Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (palmRejectionMode) {
                                PalmRejectionMode.STYLUS_ONLY -> Color(0x3310B981)
                                PalmRejectionMode.AUTOMATIC -> Color(0x22FFFFFF)
                                else -> Color(0x22EF4444)
                            }
                        )
                        .clickable { onTogglePalmRejectionMode() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (palmRejectionMode == PalmRejectionMode.STYLUS_ONLY) Icons.Outlined.PanTool else Icons.Default.Fingerprint,
                        contentDescription = "Palm Rejection",
                        tint = if (palmRejectionMode == PalmRejectionMode.STYLUS_ONLY) Color(0xFF10B981) else Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (palmRejectionMode) {
                            PalmRejectionMode.STYLUS_ONLY -> "Stylus Only"
                            PalmRejectionMode.AUTOMATIC -> "Palm Auto"
                            else -> "Touch+Pen"
                        },
                        color = if (palmRejectionMode == PalmRejectionMode.STYLUS_ONLY) Color(0xFF10B981) else Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Layers Toggle
                IconButton(
                    onClick = onLayersClick,
                    modifier = Modifier.size(32.dp).testTag("top_layers_button")
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Layers",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Active Color Swatch
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(brushColor)
                        .border(1.5.dp, Color.White, CircleShape)
                        .clickable { onColorPickerClick() }
                        .testTag("top_color_picker_button")
                )
            }
        }
    }
}
