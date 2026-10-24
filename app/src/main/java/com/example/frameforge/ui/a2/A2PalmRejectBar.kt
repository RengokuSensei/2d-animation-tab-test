package com.example.frameforge.ui.a2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class LayerScope(val label: String) {
    ACTIVE("Active"),
    ALL("All")
}

enum class FingerTouchMode(val label: String) {
    IGNORE("Ignore"),
    PAN("Pan"),
    DRAW("Draw")
}

@Composable
fun A2PalmRejectBar(
    isCanvasLocked: Boolean,
    onToggleCanvasLock: () -> Unit,
    layerScope: LayerScope,
    onToggleLayerScope: () -> Unit,
    fingerTouchMode: FingerTouchMode,
    onToggleFingerMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier,
        cornerRadius = 16.dp,
        elevation = 12.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Canvas Lock / Ignore Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleCanvasLock() }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isCanvasLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Canvas Lock",
                    tint = if (isCanvasLocked) A2Theme.Amber else A2Theme.TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isCanvasLocked) "Locked" else "Ignore",
                    color = A2Theme.TextPrimary,
                    fontSize = 10.sp
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(1.dp))
            )

            // 2. Layer Scope Button (Active vs All Layers for Selection/Nudge/Slice)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleLayerScope() }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "Layer Scope",
                    tint = A2Theme.Accent,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = layerScope.label,
                    color = A2Theme.Accent,
                    fontSize = 10.sp
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(1.dp))
            )

            // 3. Finger Action (Palm Rejection) Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleFingerMode() }
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = "Finger Action",
                    tint = A2Theme.Amber,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = fingerTouchMode.label,
                    color = A2Theme.Amber,
                    fontSize = 10.sp
                )
            }
        }
    }
}
