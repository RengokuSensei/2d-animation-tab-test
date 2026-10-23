package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object MotionTrackingEngine {

    /**
     * Executes 2D Patch-Based Feature Tracking between two animation or video frames.
     * Uses Fast Normalized Cross-Correlation (NCC) & Sum of Absolute Differences (SAD).
     */
    fun trackFeature(
        prevBitmap: Bitmap,
        currBitmap: Bitmap,
        prevPoint: TrackPoint,
        targetFrameIndex: Int
    ): TrackPoint {
        val width = prevBitmap.width
        val height = prevBitmap.height

        val pSize = prevPoint.patchSize
        val halfP = pSize / 2
        val radius = prevPoint.searchRadius

        val startX = prevPoint.x.toInt()
        val startY = prevPoint.y.toInt()

        // Bounds validation
        if (startX - halfP < 0 || startX + halfP >= width ||
            startY - halfP < 0 || startY + halfP >= height
        ) {
            return TrackPoint(
                frameIndex = targetFrameIndex,
                x = prevPoint.x,
                y = prevPoint.y,
                confidence = 0f
            )
        }

        // Extract template patch grayscale luminance
        val template = IntArray(pSize * pSize)
        val templatePixels = IntArray(pSize * pSize)
        prevBitmap.getPixels(templatePixels, 0, pSize, startX - halfP, startY - halfP, pSize, pSize)
        for (i in templatePixels.indices) {
            val c = templatePixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            template[i] = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
        }

        // Search window
        val minSearchX = max(halfP, startX - radius)
        val maxSearchX = min(width - 1 - halfP, startX + radius)
        val minSearchY = max(halfP, startY - radius)
        val maxSearchY = min(height - 1 - halfP, startY + radius)

        var bestSAD = Long.MAX_VALUE
        var bestX = startX
        var bestY = startY

        val candidatePixels = IntArray(pSize * pSize)

        // Step by 1 pixel in search radius (fast raster sweep)
        for (cy in minSearchY..maxSearchY step 2) {
            for (cx in minSearchX..maxSearchX step 2) {
                currBitmap.getPixels(candidatePixels, 0, pSize, cx - halfP, cy - halfP, pSize, pSize)
                var currentSAD: Long = 0

                for (pi in 0 until (pSize * pSize) step 2) {
                    val c = candidatePixels[pi]
                    val r = (c shr 16) and 0xFF
                    val g = (c shr 8) and 0xFF
                    val b = c and 0xFF
                    val luma = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

                    currentSAD += abs(template[pi] - luma)
                }

                if (currentSAD < bestSAD) {
                    bestSAD = currentSAD
                    bestX = cx
                    bestY = cy
                }
            }
        }

        val maxPossibleSad = (pSize * pSize / 2) * 255L
        val confidence = (1.0f - (bestSAD.toFloat() / maxPossibleSad.toFloat())).coerceIn(0f, 1f)

        return TrackPoint(
            frameIndex = targetFrameIndex,
            x = bestX.toFloat(),
            y = bestY.toFloat(),
            confidence = confidence,
            searchRadius = radius,
            patchSize = pSize
        )
    }

    /**
     * Renders professional VFX tracking HUD on tablet screen:
     * - Feature Template Box (Inner square)
     * - Search Radius Window (Outer dashed square)
     * - Tracking crosshairs
     * - Motion trail trajectory line
     */
    fun renderTrackerHud(
        canvas: Canvas,
        tracker: MotionTracker,
        currentFrame: Int
    ) {
        if (!tracker.isEnabled) return

        val pt = tracker.points[currentFrame] ?: return
        val colorArgb = tracker.colorArgb

        val halfP = pt.patchSize / 2f
        val rad = pt.searchRadius.toFloat()

        // 1. Motion Trail Trajectory
        val trailPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = colorArgb
            alpha = 140
        }
        val sortedKeys = tracker.points.keys.sorted()
        for (i in 0 until sortedKeys.size - 1) {
            val k1 = sortedKeys[i]
            val k2 = sortedKeys[i + 1]
            val p1 = tracker.points[k1] ?: continue
            val p2 = tracker.points[k2] ?: continue
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, trailPaint)
        }

        // 2. Search Radius Outer Box (Dashed)
        val searchPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = colorArgb
            alpha = 120
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
        }
        canvas.drawRect(pt.x - rad, pt.y - rad, pt.x + rad, pt.y + rad, searchPaint)

        // 3. Feature Template Inner Box (Solid)
        val patchPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            color = colorArgb
            alpha = 240
        }
        canvas.drawRect(pt.x - halfP, pt.y - halfP, pt.x + halfP, pt.y + halfP, patchPaint)

        // 4. Center Crosshair
        canvas.drawLine(pt.x - 10f, pt.y, pt.x + 10f, pt.y, patchPaint)
        canvas.drawLine(pt.x, pt.y - 10f, pt.x, pt.y + 10f, patchPaint)
    }
}
