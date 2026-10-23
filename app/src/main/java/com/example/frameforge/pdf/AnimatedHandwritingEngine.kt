package com.example.frameforge.pdf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.frameforge.engine.DrawingEngine
import com.example.frameforge.model.DrawingStroke

object AnimatedHandwritingEngine {

    /**
     * Renders an interactive PDF page document with synchronized handwriting reveals
     * directly into the documentary video frame.
     */
    fun renderDocumentWithAnimatedInk(
        canvas: Canvas,
        docWidth: Int,
        docHeight: Int,
        currentFrame: Int,
        track: PdfDocumentVideoTrack
    ) {
        if (track.opacity <= 0.01f) return

        canvas.save()
        canvas.translate(track.panOffsetX, track.panOffsetY)
        canvas.scale(track.scale, track.scale, docWidth / 2f, docHeight / 2f)

        // 1. Render clean textbook / research paper background sheet
        val paperPaint = Paint().apply {
            color = Color.rgb(250, 250, 252) // Rich off-white study paper
            isAntiAlias = true
        }
        val paperRect = RectF(docWidth * 0.10f, docHeight * 0.05f, docWidth * 0.90f, docHeight * 0.95f)
        canvas.drawRoundRect(paperRect, 12f, 12f, paperPaint)

        // Subtle document border & shadow line
        val borderPaint = Paint().apply {
            color = Color.rgb(220, 226, 235)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        canvas.drawRoundRect(paperRect, 12f, 12f, borderPaint)

        // 2. Render Page Header & Title
        val titlePaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = docHeight * 0.035f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText(
            "${track.title} — Page ${track.currentPageIndex + 1} / ${track.totalPages}",
            paperRect.left + 40f,
            paperRect.top + 60f,
            titlePaint
        )

        // 3. Render animated handwriting reveals for this page
        val pageStrokes = track.animatedStrokes.filter { it.pageIndex == track.currentPageIndex }
        for (animStroke in pageStrokes) {
            val visiblePoints = animStroke.getVisiblePointsForFrame(currentFrame)
            if (visiblePoints.isNotEmpty()) {
                val partialStroke = animStroke.stroke.copy(points = visiblePoints)
                DrawingEngine.renderStroke(canvas, partialStroke)
            }
        }

        canvas.restore()
    }
}
