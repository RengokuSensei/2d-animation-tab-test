package com.example.frameforge.ui.a2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class A2StudioMode(val label: String, val color: Color) {
    DRAW("Draw", Color(0xFF898BFF)),
    NODES("Nodes", Color(0xFFA78BFA)),
    VELOCITY("Velocity", Color(0xFFFFBF69)),
    GRADE("Grade", Color(0xFF2DD4BF)),
    STAGE_3D("3D Stage", Color(0xFFEF6461))
}

@Composable
fun A2TopTrayBar(
    projectTitle: String,
    currentMode: A2StudioMode,
    onSelectMode: (A2StudioMode) -> Unit,
    currentPage: Int,
    totalPages: Int,
    onOpenPagesPopover: () -> Unit,
    isPdfShelfOpen: Boolean,
    onTogglePdfShelf: () -> Unit,
    isPhotoInspectorOpen: Boolean,
    onTogglePhotoInspector: () -> Unit,
    canUndo: Boolean,
    onUndo: () -> Unit,
    canRedo: Boolean,
    onRedo: () -> Unit,
    isOnionActive: Boolean,
    onToggleOnionPopover: () -> Unit,
    layerCount: Int,
    onToggleLayersPopover: () -> Unit,
    currentZoom: Float,
    onChangeZoom: (Float) -> Unit,
    rotationAngle: Float,
    onRotateCanvas: () -> Unit,
    onOpenStylusSettings: () -> Unit,
    onOpenWorkspaceSettings: () -> Unit,
    onOpenObjectLibrary: () -> Unit,
    onOpenExportModal: () -> Unit,
    onToggleZenMode: () -> Unit,
    onExportMp4: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT PILL: Project Title, Pages, PDF Shelf, Photo Inspector
        GlassPill(
            cornerRadius = 16.dp,
            elevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    text = projectTitle,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                // Pages Stepper Pill (P1/3)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onOpenPagesPopover() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "P$currentPage/$totalPages",
                        color = A2Theme.TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // PDF Shelf Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPdfShelfOpen) A2Theme.Amber.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (isPdfShelfOpen) A2Theme.Amber.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onTogglePdfShelf() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF Ref",
                            tint = A2Theme.Amber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "PDF",
                            color = A2Theme.Amber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Photo Inspector Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPhotoInspectorOpen) A2Theme.Teal.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            if (isPhotoInspectorOpen) A2Theme.Teal.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onTogglePhotoInspector() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Photo Ref",
                            tint = A2Theme.Teal,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Ref",
                            color = A2Theme.Teal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // CENTER PILL: Mode Switcher (Draw, Nodes, Velocity, Grade, 3D Stage)
        GlassPill(
            cornerRadius = 16.dp,
            elevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                A2StudioMode.values().forEach { mode ->
                    val isSel = (mode == currentMode)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) mode.color else Color.Transparent)
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = mode.label,
                            color = if (isSel) A2Theme.Ink950 else A2Theme.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // RIGHT PILL: Actions & Settings (Undo, Redo, Onion, Layers, Zoom, Stylus, Zen, MP4 Export)
        GlassPill(
            cornerRadius = 16.dp,
            elevation = 10.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else A2Theme.TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else A2Theme.TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Onion Popover Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOnionActive) A2Theme.Accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                        .clickable { onToggleOnionPopover() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Onion",
                            tint = if (isOnionActive) A2Theme.Accent else A2Theme.TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Onion",
                            color = if (isOnionActive) A2Theme.Accent else A2Theme.TextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Layers Popover Toggle (e.g. 3L)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onToggleLayersPopover() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers",
                            tint = A2Theme.Amber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${layerCount}L",
                            color = A2Theme.TextPrimary,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Canvas Rotation Angle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onRotateCanvas() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${rotationAngle.toInt()}°",
                        color = A2Theme.TextSecondary,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Stylus Settings Button
                IconButton(
                    onClick = onOpenStylusSettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Stylus & Palm Rejection",
                        tint = A2Theme.Amber,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Object Library Button
                IconButton(
                    onClick = onOpenObjectLibrary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Object Library",
                        tint = A2Theme.Purple,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Workspace & Interaction Settings Button
                IconButton(
                    onClick = onOpenWorkspaceSettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Workspace Settings",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Fullscreen Zen Mode Button
                IconButton(
                    onClick = onToggleZenMode,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Zen Mode",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Concepts Multi-Format Export Dialog Button
                IconButton(
                    onClick = onOpenExportModal,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Formats",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // DIRECT EXPORT MP4 BUTTON
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Accent)
                        .clickable { onExportMp4() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Export MP4",
                            tint = A2Theme.Ink950,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "MP4",
                            color = A2Theme.Ink950,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
