package com.example.frameforge.ui.aether

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object AetherColors {
    val Ink950 = Color(0xFF0B0D12) // Ambient Stage Backdrop Deep Ink
    val Ink900 = Color(0xFF12151E) // Surface / Card Background
    val Ink850 = Color(0xFF181C27) // Popover / Elevated Card
    val Ink800 = Color(0xFF202534) // Interactive Elements
    val Ink700 = Color(0xFF2E3548) // Borders / Dividers
    val InkLine = Color(0x17FFFFFF) // Subtle 9% White Hairlines

    // Vibrant Studio Accents
    val Accent = Color(0xFF898BFF) // Lavender / Periwinkle (Active Pill, Pen)
    val Amber = Color(0xFFFFBF69)  // Warm Amber (Frame indicator, Velocity, Gain)
    val Mint = Color(0xFF5EEAD4)   // Mint Teal (Grade, Gamma)
    val Rose = Color(0xFFFB7185)   // Rose Pink (3D Stage, Next Onion)
    val Coral = Color(0xFFEF6461)  // Warm Coral (Default Swatch, Character)
    val Purple = Color(0xFFC084FC) // Purple (Compositor Nodes)
    val Sky = Color(0xFF38BDF8)    // Cyan Sky (Prev Onion Ghost)

    // Glassmorphic Shell
    val GlassBackground = Color(0xDE14171F) // 87% Dark Glass
    val GlassBorder = Color(0x1FFFFFFF)     // 12% White Border
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = AetherColors.GlassBackground,
    borderColor: Color = AetherColors.GlassBorder,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 8.dp,
        shadowElevation = 14.dp
    ) {
        content()
    }
}
