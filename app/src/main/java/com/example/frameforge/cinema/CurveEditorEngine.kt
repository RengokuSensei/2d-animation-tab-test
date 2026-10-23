package com.example.frameforge.cinema

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.max
import kotlin.math.min

object CurveEditorEngine {

    /**
     * Renders either Dope Sheet or Bezier Graph Editor onto the given canvas.
     */
    fun renderCurveCanvas(
        canvas: Canvas,
        width: Float,
        height: Float,
        state: CurveEditorState,
        currentFrame: Float,
        totalFrames: Int
    ) {
        if (!state.isVisible) return

        // 1. Dark Studio Grid Background
        val bgPaint = Paint().apply { color = Color.rgb(18, 20, 26) }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        when (state.viewMode) {
            CurveEditorViewMode.DOPE_SHEET -> {
                renderDopeSheet(canvas, width, height, state, currentFrame, totalFrames)
            }
            CurveEditorViewMode.GRAPH_CURVE -> {
                renderGraphCurves(canvas, width, height, state, currentFrame, totalFrames)
            }
        }

        // Playhead indicator (Bright Red vertical bar)
        val playheadX = (currentFrame / totalFrames.toFloat()) * width
        val playheadPaint = Paint().apply {
            color = Color.rgb(255, 69, 58)
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        canvas.drawLine(playheadX, 0f, playheadX, height, playheadPaint)
    }

    private fun renderDopeSheet(
        canvas: Canvas,
        width: Float,
        height: Float,
        state: CurveEditorState,
        currentFrame: Float,
        totalFrames: Int
    ) {
        val tracks = state.tracks
        if (tracks.isEmpty()) return

        val rowHeight = (height - 30f) / tracks.size.coerceAtLeast(1)
        val textPaint = Paint().apply {
            color = Color.rgb(200, 205, 215)
            textSize = 24f
            isAntiAlias = true
        }
        val gridLinePaint = Paint().apply {
            color = Color.rgb(36, 40, 50)
            strokeWidth = 1.5f
        }
        val diamondPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in tracks.indices) {
            val track = tracks[i]
            val rowY = 30f + i * rowHeight

            // Row background divider
            canvas.drawLine(0f, rowY, width, rowY, gridLinePaint)
            canvas.drawText(track.name, 16f, rowY + rowHeight * 0.65f, textPaint)

            diamondPaint.color = track.colorArgb

            // Draw keyframe diamonds
            for (kf in track.keyframes) {
                val kfX = (kf.frame / totalFrames.toFloat()) * width
                val kfY = rowY + rowHeight * 0.5f
                val dSize = 10f

                val diamondPath = Path().apply {
                    moveTo(kfX, kfY - dSize)
                    lineTo(kfX + dSize, kfY)
                    lineTo(kfX, kfY + dSize)
                    lineTo(kfX - dSize, kfY)
                    close()
                }
                canvas.drawPath(diamondPath, diamondPaint)
            }
        }
    }

    private fun renderGraphCurves(
        canvas: Canvas,
        width: Float,
        height: Float,
        state: CurveEditorState,
        currentFrame: Float,
        totalFrames: Int
    ) {
        val gridPaint = Paint().apply {
            color = Color.rgb(32, 36, 46)
            strokeWidth = 1f
        }

        // Draw horizontal grid lines (values)
        for (step in 1..4) {
            val y = (height / 5f) * step
            canvas.drawLine(0f, y, width, y, gridPaint)
        }

        val curvePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
        }
        val handleLinePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.rgb(180, 190, 205)
        }
        val handlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(255, 214, 10)
        }
        val kfPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.WHITE
        }

        for (track in state.tracks) {
            if (!track.isVisible || track.keyframes.size < 2) continue

            curvePaint.color = track.colorArgb
            val minV = track.minValue
            val maxV = track.maxValue
            val spanV = (maxV - minV).coerceAtLeast(0.001f)

            fun frameToX(f: Float): Float = (f / totalFrames.toFloat()) * width
            fun valToY(v: Float): Float = height - ((v - minV) / spanV) * height

            val curvePath = Path()
            val kfs = track.keyframes

            val startX = frameToX(kfs[0].frame)
            val startY = valToY(kfs[0].value)
            curvePath.moveTo(startX, startY)

            for (i in 0 until kfs.size - 1) {
                val k0 = kfs[i]
                val k1 = kfs[i + 1]

                val p0X = frameToX(k0.frame)
                val p0Y = valToY(k0.value)

                val p1X = frameToX(k0.rightHandleFrame)
                val p1Y = valToY(k0.rightHandleValue)

                val p2X = frameToX(k1.leftHandleFrame)
                val p2Y = valToY(k1.leftHandleValue)

                val p3X = frameToX(k1.frame)
                val p3Y = valToY(k1.value)

                curvePath.cubicTo(p1X, p1Y, p2X, p2Y, p3X, p3Y)
            }
            canvas.drawPath(curvePath, curvePaint)

            // Draw keyframe nodes & tangent handles
            for (kf in kfs) {
                val kfX = frameToX(kf.frame)
                val kfY = valToY(kf.value)

                val lhX = frameToX(kf.leftHandleFrame)
                val lhY = valToY(kf.leftHandleValue)
                val rhX = frameToX(kf.rightHandleFrame)
                val rhY = valToY(kf.rightHandleValue)

                // Tangent whisker lines
                canvas.drawLine(kfX, kfY, lhX, lhY, handleLinePaint)
                canvas.drawLine(kfX, kfY, rhX, rhY, handleLinePaint)

                // Handle circular endpoints
                canvas.drawCircle(lhX, lhY, 5f, handlePaint)
                canvas.drawCircle(rhX, rhY, 5f, handlePaint)

                // Keyframe vertex node
                canvas.drawCircle(kfX, kfY, 7f, kfPaint)
            }
        }
    }
}
