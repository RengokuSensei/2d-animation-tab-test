package com.example.frameforge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HudGlassBg = Color(0xD918181B)
private val HudBorder = Color(0x2EFFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun ProcreateSliderHud(
    brushSize: Float,
    brushOpacity: Float,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushOpacity: (Float) -> Unit,
    onQuickMenuClick: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(44.dp)
            .padding(vertical = 12.dp)
            .testTag("procreate_slider_hud"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Main glassmorphic slider pill
        Surface(
            modifier = Modifier
                .width(40.dp)
                .height(240.dp)
                .border(1.dp, HudBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = HudGlassBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Brush Size Vertical Slider
                VerticalBarSlider(
                    value = (brushSize / 100f).coerceIn(0.01f, 1f),
                    label = "${brushSize.toInt()}%",
                    onValueChange = { onSetBrushSize((it * 100f).coerceIn(1f, 120f)) },
                    modifier = Modifier.weight(1f)
                )

                // 2. Middle Eyedropper / QuickMenu Square button
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x33FFFFFF))
                        .clickable { onQuickMenuClick() }
                        .testTag("eyedropper_quickmenu_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Colorize,
                        contentDescription = "Eyedropper",
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // 3. Brush Opacity Vertical Slider
                VerticalBarSlider(
                    value = brushOpacity.coerceIn(0.05f, 1f),
                    label = "${(brushOpacity * 100).toInt()}%",
                    onValueChange = { onSetBrushOpacity(it.coerceIn(0.05f, 1f)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating Undo / Redo buttons underneath slider pill
        Surface(
            modifier = Modifier
                .width(40.dp)
                .border(1.dp, HudBorder, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            color = HudGlassBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(34.dp).testTag("floating_undo_button")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.LightGray, modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.height(2.dp))
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(34.dp).testTag("floating_redo_button")
                ) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.LightGray, modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}

@Composable
private fun VerticalBarSlider(
    value: Float,
    label: String,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(28.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x22FFFFFF))
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()
                    // Dragging up increases, down decreases
                    val delta = -dragAmount / 100f
                    onValueChange((value + delta).coerceIn(0.01f, 1f))
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Filled vertical bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(value.coerceIn(0.02f, 1f))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x55FFFFFF))
        )

        // Percentage indicator text
        Text(
            text = label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}
