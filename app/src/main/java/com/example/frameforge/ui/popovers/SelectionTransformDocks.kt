package com.example.frameforge.ui.popovers

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.SelectionMode
import com.example.frameforge.model.SelectionState
import com.example.frameforge.model.TransformMode
import com.example.frameforge.model.TransformState

private val DockBg = Color(0xF218181B)
private val DockBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun SelectionDock(
    selectionState: SelectionState,
    onSetMode: (SelectionMode) -> Unit,
    onInvert: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, DockBorder, RoundedCornerShape(14.dp))
            .testTag("selection_dock"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DockBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SelectionModeButton("Freehand", Icons.Default.Gesture, selectionState.mode == SelectionMode.FREEHAND) {
                onSetMode(SelectionMode.FREEHAND)
            }
            SelectionModeButton("Automatic", Icons.Default.CropSquare, selectionState.mode == SelectionMode.AUTOMATIC) {
                onSetMode(SelectionMode.AUTOMATIC)
            }
            SelectionModeButton("Rectangle", Icons.Default.CropLandscape, selectionState.mode == SelectionMode.RECTANGLE) {
                onSetMode(SelectionMode.RECTANGLE)
            }
            SelectionModeButton("Ellipse", Icons.Default.CropSquare, selectionState.mode == SelectionMode.ELLIPSE) {
                onSetMode(SelectionMode.ELLIPSE)
            }

            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0x33FFFFFF)))

            DockActionButton("Invert", Icons.Default.InvertColors) { onInvert() }
            DockActionButton("Clear", Icons.Default.Clear) { onClear() }
        }
    }
}

@Composable
fun TransformDock(
    transformState: TransformState,
    onSetMode: (TransformMode) -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onRotate45: () -> Unit,
    onReset: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, DockBorder, RoundedCornerShape(14.dp))
            .testTag("transform_dock"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DockBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TransformMode.values().forEach { mode ->
                val isSel = mode == transformState.mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) ProcreateBlue else Color(0x22FFFFFF))
                        .clickable { onSetMode(mode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("transform_mode_${mode.name.lowercase()}")
                ) {
                    Text(
                        text = mode.label,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(modifier = Modifier.width(1.dp).height(20.dp).background(Color(0x33FFFFFF)))

            DockActionButton("Flip H", Icons.Default.Flip) { onFlipH() }
            DockActionButton("Flip V", Icons.Default.Flip) { onFlipV() }
            DockActionButton("45°", Icons.Default.Rotate90DegreesCcw) { onRotate45() }
            DockActionButton("Reset", Icons.Default.Refresh) { onReset() }

            IconButton(onClick = onDone, modifier = Modifier.size(28.dp).background(ProcreateBlue, RoundedCornerShape(6.dp))) {
                Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun SelectionModeButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) ProcreateBlue else Color(0x22FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DockActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0x22FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, color = Color.LightGray, fontSize = 10.sp)
    }
}
