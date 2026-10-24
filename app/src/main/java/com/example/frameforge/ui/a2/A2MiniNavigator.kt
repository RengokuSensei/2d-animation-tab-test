package com.example.frameforge.ui.a2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun A2MiniNavigator(
    onResetZoom: () -> Unit,
    onShare: () -> Unit,
    onToggleTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(96.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mini Radar Viewport Box
        GlassPill(
            modifier = Modifier
                .width(96.dp)
                .height(140.dp)
                .clickable { onResetZoom() },
            cornerRadius = 14.dp,
            elevation = 8.dp
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                val w = size.width
                val h = size.height

                // Artboard background preview
                drawRect(
                    color = Color.White.copy(alpha = 0.12f),
                    topLeft = Offset(4f, 4f),
                    size = Size(w - 8f, h - 8f)
                )

                // Simulated active strokes indicator
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                    radius = 16f,
                    center = Offset(w * 0.45f, h * 0.35f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(w * 0.5f, h * 0.55f),
                    end = Offset(w * 0.65f, h * 0.75f),
                    strokeWidth = 3.dp.toPx()
                )

                // Viewport Camera Frame Box (blue outline)
                drawRect(
                    color = Color(0xFF898BFF),
                    topLeft = Offset(8f, 12f),
                    size = Size(w * 0.75f, h * 0.65f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
        }

        // Quick Bottom Icon Buttons (Share, Pen, Fit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Share
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(A2Theme.Ink900)
                    .border(1.dp, A2Theme.GlassBorder, CircleShape)
                    .clickable { onShare() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = A2Theme.TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Quick Tool Toggle
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(A2Theme.Ink900)
                    .border(1.dp, A2Theme.GlassBorder, CircleShape)
                    .clickable { onToggleTools() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Tool",
                    tint = A2Theme.TextSecondary,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Fit Canvas / Reset Zoom
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(A2Theme.Ink900)
                    .border(1.dp, A2Theme.GlassBorder, CircleShape)
                    .clickable { onResetZoom() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Fit Viewport",
                    tint = A2Theme.Accent,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
