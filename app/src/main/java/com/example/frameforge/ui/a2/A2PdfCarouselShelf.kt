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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PdfRefCard(
    val pageNum: Int,
    val title: String,
    val formula: String,
    val note: String
)

@Composable
fun A2PdfCarouselShelf(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    isShowOnStage: Boolean,
    onToggleShowOnStage: (Boolean) -> Unit,
    onImportPdf: () -> Unit,
    onExtractDiagramAsCel: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catalog = remember {
        listOf(
            PdfRefCard(12, "Orbital Kinematics", "v(t) = v₀ + ∫ a(t) dt", "Fig 12.1 · Parabolic Trajectory"),
            PdfRefCard(13, "Wave Harmonics", "Ψ(x,t) = A·sin(kx - ωt)", "Fig 13.4 · Phase Envelope"),
            PdfRefCard(14, "Optics & Prism Split", "n₁·sin(θ₁) = n₂·sin(θ₂)", "Fig 14.2 · Refractive Curve"),
            PdfRefCard(15, "Cellular Mitosis", "ΔG = ΔH - TΔS < 0", "Fig 15.8 · Spindle Fiber Tension"),
            PdfRefCard(16, "Architectural Truss", "ΣF_x = 0, ΣF_y = 0", "Fig 16.3 · Cantilever Vectors")
        )
    }

    GlassPill(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        cornerRadius = 20.dp,
        elevation = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Bar: Title, Stepper, Stage BG Checkbox, Import, Extract Diagram, Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Title & Page Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "PDF Ref",
                        tint = A2Theme.Amber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "PDF & Reference Deck",
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Stepper: ◀ Page 12 / 48 ▶
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .border(1.dp, A2Theme.GlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) onPageChange(currentPage - 1) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Prev",
                                tint = A2Theme.Amber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "Page $currentPage / $totalPages",
                            color = A2Theme.Amber,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { if (currentPage < totalPages) onPageChange(currentPage + 1) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next",
                                tint = A2Theme.Amber,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Actions: Stage BG, Import, Extract Cel, Close
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stage BG Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleShowOnStage(!isShowOnStage) }
                    ) {
                        Checkbox(
                            checked = isShowOnStage,
                            onCheckedChange = onToggleShowOnStage,
                            colors = CheckboxDefaults.colors(
                                checkedColor = A2Theme.Amber,
                                uncheckedColor = A2Theme.TextSecondary
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Stage BG",
                            color = A2Theme.TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    // Import PDF / Img
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onImportPdf() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Import PDF",
                            color = A2Theme.TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Extract Diagram as Cel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Amber)
                            .clickable { onExtractDiagramAsCel() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Extract as Cel",
                            color = A2Theme.Ink950,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = A2Theme.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Horizontal Preview Cards
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(catalog) { item ->
                    val isSel = (item.pageNum == currentPage)
                    Column(
                        modifier = Modifier
                            .width(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) A2Theme.Amber.copy(alpha = 0.15f) else A2Theme.Ink850)
                            .border(
                                1.dp,
                                if (isSel) A2Theme.Amber else A2Theme.GlassBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onPageChange(item.pageNum) }
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Page ${item.pageNum}",
                                color = if (isSel) A2Theme.Amber else A2Theme.TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.title,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = item.formula,
                            color = A2Theme.Accent,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = item.note,
                            color = A2Theme.TextMuted,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
