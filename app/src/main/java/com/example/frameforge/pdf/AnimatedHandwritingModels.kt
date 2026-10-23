package com.example.frameforge.pdf

import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.StrokePoint
import java.util.UUID

data class AnimatedStroke(
    val id: String = UUID.randomUUID().toString(),
    val pageIndex: Int,
    val startFrame: Int,
    val durationFrames: Int = 24, // Frames over which the handwriting unfolds
    val stroke: DrawingStroke,
    val isRevealAnimated: Boolean = true
) {
    /**
     * Computes the subset of stroke points visible at a specific frame timestamp
     */
    fun getVisiblePointsForFrame(currentFrame: Int): List<StrokePoint> {
        if (!isRevealAnimated) return stroke.points
        if (currentFrame < startFrame) return emptyList()
        if (currentFrame >= startFrame + durationFrames) return stroke.points

        val t = (currentFrame - startFrame).toFloat() / durationFrames.toFloat()
        val count = (stroke.points.size * t.coerceIn(0f, 1f)).toInt().coerceAtLeast(1)
        return stroke.points.take(count)
    }
}

data class PdfDocumentVideoTrack(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "UPSC Science & Tech Vol 1.pdf",
    val filePath: String? = null,
    val currentPageIndex: Int = 0,
    val totalPages: Int = 120,
    val opacity: Float = 1.0f,
    val scale: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    val animatedStrokes: List<AnimatedStroke> = emptyList()
)
