package com.example.frameforge.ui.a2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class PhotoKeyMode(val label: String) {
    OFF("Off"),
    CHROMA_GREEN("Chroma Green"),
    PAPER_LUMA("White Paper Luma")
}

@Composable
fun A2PhotoInspector(
    opacity: Float,
    onOpacityChange: (Float) -> Unit,
    onFlipH: () -> Unit,
    onFlipV: () -> Unit,
    onCrop: () -> Unit,
    keyMode: PhotoKeyMode,
    onKeyModeChange: (PhotoKeyMode) -> Unit,
    keyTolerance: Float,
    onKeyToleranceChange: (Float) -> Unit,
    onUploadPhoto: () -> Unit,
    onClearPhoto: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassPill(
        modifier = modifier.width(260.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Photo Inspector",
                        tint = A2Theme.Teal,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Photo & Ref Inspector",
                        color = A2Theme.Teal,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = A2Theme.TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Upload Local Photo Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(A2Theme.Teal.copy(alpha = 0.18f))
                        .border(1.dp, A2Theme.Teal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .clickable { onUploadPhoto() }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+ Photo Ref",
                        color = A2Theme.Teal,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(A2Theme.Ink850)
                        .clickable { onClearPhoto() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Clear", color = A2Theme.TextSecondary, fontSize = 10.sp)
                }
            }

            // Tracing Opacity Slider
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tracing Opacity",
                        color = A2Theme.TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${(opacity * 100).toInt()}%",
                        color = A2Theme.Teal,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = opacity,
                    onValueChange = onOpacityChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = A2Theme.Teal,
                        activeTrackColor = A2Theme.Teal,
                        inactiveTrackColor = A2Theme.Ink800
                    )
                )
            }

            // Transform Actions: Flip H, Flip V, Crop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Ink850)
                        .clickable { onFlipH() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Flip H", color = A2Theme.TextPrimary, fontSize = 9.5.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Ink850)
                        .clickable { onFlipV() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Flip V", color = A2Theme.TextPrimary, fontSize = 9.5.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Ink850)
                        .clickable { onCrop() }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Crop", color = A2Theme.TextPrimary, fontSize = 9.5.sp)
                }
            }

            // Background Removal / Chroma Key Mode Selector
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BG Removal / Key",
                        color = A2Theme.TextSecondary,
                        fontSize = 10.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(A2Theme.Ink850)
                            .clickable {
                                val next = when (keyMode) {
                                    PhotoKeyMode.OFF -> PhotoKeyMode.CHROMA_GREEN
                                    PhotoKeyMode.CHROMA_GREEN -> PhotoKeyMode.PAPER_LUMA
                                    PhotoKeyMode.PAPER_LUMA -> PhotoKeyMode.OFF
                                }
                                onKeyModeChange(next)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = keyMode.label,
                            color = A2Theme.Teal,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (keyMode != PhotoKeyMode.OFF) {
                    Slider(
                        value = keyTolerance,
                        onValueChange = onKeyToleranceChange,
                        valueRange = 0.1f..0.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = A2Theme.Teal,
                            activeTrackColor = A2Theme.Teal,
                            inactiveTrackColor = A2Theme.Ink800
                        )
                    )
                }
            }
        }
    }
}
