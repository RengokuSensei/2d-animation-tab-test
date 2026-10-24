package com.example.frameforge.ui.a2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

enum class GridType(val label: String) {
    NO_GRID("No Grid"),
    DOT("Dot Grid"),
    SQUARE("Square"),
    ISOMETRIC("Isometric")
}

enum class GuideType(val label: String) {
    ARC("Arc"),
    LINE("Line"),
    ELLIPSE("Ellipse"),
    RECT("Rect")
}

@Composable
fun A2PrecisionStack(
    isGridEnabled: Boolean,
    onToggleGrid: (Boolean) -> Unit,
    gridType: GridType,
    onChangeGridType: (GridType) -> Unit,
    isSnapEnabled: Boolean,
    onToggleSnap: (Boolean) -> Unit,
    isMeasureEnabled: Boolean,
    onToggleMeasure: (Boolean) -> Unit,
    isGuideEnabled: Boolean,
    onToggleGuide: (Boolean) -> Unit,
    guideType: GuideType,
    onChangeGuideType: (GuideType) -> Unit,
    isRecognitionEnabled: Boolean,
    onToggleRecognition: (Boolean) -> Unit,
    onInsertText: () -> Unit,
    onImportPhotoRef: () -> Unit,
    onClearLayer: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    GlassPill(
        modifier = modifier.width(190.dp),
        cornerRadius = 16.dp,
        elevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Grip + "Precision" + toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded }
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Precision",
                        tint = A2Theme.Accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Precision",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = A2Theme.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Expanded Options Body
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // 1. Grid Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isGridEnabled,
                                onCheckedChange = onToggleGrid,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = A2Theme.Accent,
                                    uncheckedColor = A2Theme.TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Grid",
                                color = A2Theme.TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .clickable {
                                    val next = when (gridType) {
                                        GridType.NO_GRID -> GridType.DOT
                                        GridType.DOT -> GridType.SQUARE
                                        GridType.SQUARE -> GridType.ISOMETRIC
                                        GridType.ISOMETRIC -> GridType.NO_GRID
                                    }
                                    onChangeGridType(next)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = gridType.label,
                                color = A2Theme.TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 2. Snap Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isSnapEnabled,
                                onCheckedChange = onToggleSnap,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = A2Theme.Accent,
                                    uncheckedColor = A2Theme.TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Snap",
                                color = A2Theme.TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isSnapEnabled) "Active" else "Off",
                                color = if (isSnapEnabled) A2Theme.Accent else A2Theme.TextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // 3. Measure Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isMeasureEnabled,
                                onCheckedChange = onToggleMeasure,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = A2Theme.Accent,
                                    uncheckedColor = A2Theme.TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Measure",
                                color = A2Theme.TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "1:1 pts",
                            color = A2Theme.Amber,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 4. Guide Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isGuideEnabled,
                                onCheckedChange = onToggleGuide,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = A2Theme.Accent,
                                    uncheckedColor = A2Theme.TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Guide",
                                color = A2Theme.TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(A2Theme.Accent.copy(alpha = 0.2f))
                                .clickable {
                                    val next = when (guideType) {
                                        GuideType.ARC -> GuideType.LINE
                                        GuideType.LINE -> GuideType.ELLIPSE
                                        GuideType.ELLIPSE -> GuideType.RECT
                                        GuideType.RECT -> GuideType.ARC
                                    }
                                    onChangeGuideType(next)
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = guideType.label,
                                color = A2Theme.Accent,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 5. Shape Recognition Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(A2Theme.Ink850)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isRecognitionEnabled,
                                onCheckedChange = onToggleRecognition,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = A2Theme.Mint,
                                    uncheckedColor = A2Theme.TextSecondary
                                ),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Recognition",
                                color = A2Theme.TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = if (isRecognitionEnabled) "Auto-Snap" else "Off",
                            color = if (isRecognitionEnabled) A2Theme.Mint else A2Theme.TextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Quick Utility Row: Text, Ref Photo, Trash
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Text Tool Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Ink850)
                        .clickable { onInsertText() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TextFields,
                            contentDescription = "Text",
                            tint = A2Theme.Amber,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Text",
                            color = A2Theme.TextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Photo Reference Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Teal.copy(alpha = 0.15f))
                        .clickable { onImportPhotoRef() }
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Ref",
                            tint = A2Theme.Teal,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Ref",
                            color = A2Theme.Teal,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Clear Layer / Canvas Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(A2Theme.Rose.copy(alpha = 0.15f))
                        .clickable { onClearLayer() }
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear",
                        tint = A2Theme.Rose,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
