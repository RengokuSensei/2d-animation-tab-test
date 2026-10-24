package com.example.frameforge.ui.a2

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object A2Theme {
    // Deep Studio Cinema Palette matching A2_studio.html
    val Ink950 = Color(0xFF0F1115)
    val Ink900 = Color(0xFF161922)
    val Ink850 = Color(0xFF1F2330)
    val Ink800 = Color(0xFF282D3D)

    // Vibrant Luminous Accents
    val Accent = Color(0xFF898BFF)        // Periwinkle primary accent
    val AccentVariant = Color(0xFF6C63FF)
    val Amber = Color(0xFFFFBF69)         // Warm gold / timing readout
    val Mint = Color(0xFF4ECCA3)          // Emerald / recognition active
    val Rose = Color(0xFFEF6461)          // Coral / recording dot
    val Teal = Color(0xFF2DD4BF)          // Cyan / photo inspector
    val Purple = Color(0xFFA78BFA)        // Violet / node compositor

    // Translucent Glass Surfaces
    val GlassSurface = Color(0xEB161922)
    val GlassSurfaceSubtle = Color(0xC01F2330)
    val GlassBorder = Color(0x2EFFFFFF)
    val GlassBorderHighlight = Color(0x55898BFF)
    val TextPrimary = Color(0xFFF1F5F9)
    val TextSecondary = Color(0xFF94A3B8)
    val TextMuted = Color(0xFF64748B)

    // Stage Gradient
    val StageBackdropGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFBAD5D3),
            Color(0xFFD3DFD3),
            Color(0xFFE7D8C1)
        )
    )
}

@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 12.dp,
    borderColor: Color = A2Theme.GlassBorder,
    backgroundColor: Color = A2Theme.GlassSurface,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(elevation, shape)
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape),
        content = content
    )
}
