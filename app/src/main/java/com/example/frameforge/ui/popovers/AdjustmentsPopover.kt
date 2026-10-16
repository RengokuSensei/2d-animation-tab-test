package com.example.frameforge.ui.popovers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Waves
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frameforge.model.AdjustmentType

private val PopoverBg = Color(0xF218181B)
private val PopoverBorder = Color(0x33FFFFFF)
private val ProcreateBlue = Color(0xFF0A84FF)

@Composable
fun AdjustmentsPopover(
    onSelectAdjustment: (AdjustmentType) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(320.dp)
            .border(1.dp, PopoverBorder, RoundedCornerShape(14.dp))
            .testTag("adjustments_popover"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PopoverBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Adjustments & Filters",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AdjustmentRow(AdjustmentType.GAUSSIAN_BLUR, "Gaussian Blur", Icons.Default.BlurOn) {
                    onSelectAdjustment(AdjustmentType.GAUSSIAN_BLUR)
                }
                AdjustmentRow(AdjustmentType.MOTION_BLUR, "Motion Blur", Icons.Default.Waves) {
                    onSelectAdjustment(AdjustmentType.MOTION_BLUR)
                }
                AdjustmentRow(AdjustmentType.BLOOM, "Bloom / Neon Glow", Icons.Default.Flare) {
                    onSelectAdjustment(AdjustmentType.BLOOM)
                }
                AdjustmentRow(AdjustmentType.HSB, "Hue, Saturation, Brightness (HSB)", Icons.Default.ColorLens) {
                    onSelectAdjustment(AdjustmentType.HSB)
                }
                AdjustmentRow(AdjustmentType.COLOR_BALANCE, "Color Balance", Icons.Default.InvertColors) {
                    onSelectAdjustment(AdjustmentType.COLOR_BALANCE)
                }
                AdjustmentRow(AdjustmentType.CHROMATIC_ABERRATION, "Chromatic Aberration", Icons.Default.FilterDrama) {
                    onSelectAdjustment(AdjustmentType.CHROMATIC_ABERRATION)
                }
                AdjustmentRow(AdjustmentType.HALFTONE, "Halftone Dot Screen", Icons.Default.Grain) {
                    onSelectAdjustment(AdjustmentType.HALFTONE)
                }
                AdjustmentRow(AdjustmentType.LIQUIFY, "Liquify Studio (Push, Twirl, Pinch)", Icons.Default.Waves) {
                    onSelectAdjustment(AdjustmentType.LIQUIFY)
                }
            }
        }
    }
}

@Composable
private fun AdjustmentRow(
    type: AdjustmentType,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x18FFFFFF))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("adjustment_${type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = ProcreateBlue, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
