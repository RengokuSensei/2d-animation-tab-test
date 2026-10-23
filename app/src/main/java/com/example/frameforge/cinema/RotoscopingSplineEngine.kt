package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import kotlin.math.hypot
import kotlin.math.max

object RotoscopingSplineEngine {

    /**
     * Builds a smooth Android Path through a list of RotoVertices.
     */
    fun buildSplinePath(vertices: List<RotoVertex>, isClosed: Boolean, isFeather: Boolean = false): Path {
        val path = Path()
        if (vertices.isEmpty()) return path

        val n = vertices.size
        val startPt = if (isFeather) {
            Pair(vertices[0].featherAbsX, vertices[0].featherAbsY)
        } else {
            Pair(vertices[0].x, vertices[0].y)
        }
        path.moveTo(startPt.first, startPt.second)

        if (n == 1) return path

        val count = if (isClosed) n else n - 1
        for (i in 0 until count) {
            val curr = vertices[i]
            val next = vertices[(i + 1) % n]

            val p0X = if (isFeather) curr.featherAbsX else curr.x
            val p0Y = if (isFeather) curr.featherAbsY else curr.y

            val p3X = if (isFeather) next.featherAbsX else next.x
            val p3Y = if (isFeather) next.featherAbsY else next.y

            // Cubic Bezier control points
            val p1X = p0X + curr.outTangentX
            val p1Y = p0Y + curr.outTangentY

            val p2X = p3X + next.inTangentX
            val p2Y = p3Y + next.inTangentY

            path.cubicTo(p1X, p1Y, p2X, p2Y, p3X, p3Y)
        }

        if (isClosed) {
            path.close()
        }
        return path
    }

    /**
     * Renders interactive visual editor guides for the rotoscoping splines on the tablet screen.
     * Shows:
     * - Solid inner core line + anchor vertices
     * - Dashed outer feather line + feather handles
     * - Connecting whisker vectors between anchor and feather points
     */
    fun renderRotoGuides(
        canvas: Canvas,
        shape: RotoShape,
        vertices: List<RotoVertex>,
        selectedVertexIndex: Int?,
        isFeatherToolActive: Boolean
    ) {
        if (!shape.isVisible || vertices.isEmpty()) return

        val colorArgb = shape.overlayColorArgb

        // 1. Core Spline Path (Solid)
        val corePath = buildSplinePath(vertices, shape.isClosed, isFeather = false)
        val corePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = colorArgb
            alpha = 220
        }
        canvas.drawPath(corePath, corePaint)

        // 2. Feather Spline Path (Dashed)
        val featherPath = buildSplinePath(vertices, shape.isClosed, isFeather = true)
        val featherPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = colorArgb
            alpha = 150
            pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
        }
        canvas.drawPath(featherPath, featherPaint)

        // 3. Whisker lines connecting each Anchor point to its Feather handle
        val whiskerPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = colorArgb
            alpha = 110
        }
        for (v in vertices) {
            canvas.drawLine(v.x, v.y, v.featherAbsX, v.featherAbsY, whiskerPaint)
        }

        // 4. Vertex handles
        val anchorPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = colorArgb
        }
        val featherHandlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val selectedPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(255, 60, 60) // Bright red highlight for active vertex
        }

        for (i in vertices.indices) {
            val v = vertices[i]
            val isSelected = (i == selectedVertexIndex)

            // Anchor point circle (Radius 7px)
            val p = if (isSelected && !isFeatherToolActive) selectedPaint else anchorPaint
            canvas.drawCircle(v.x, v.y, 7f, p)

            // Feather point square (5px)
            val fp = if (isSelected && isFeatherToolActive) selectedPaint else featherHandlePaint
            canvas.drawRect(
                v.featherAbsX - 5f,
                v.featherAbsY - 5f,
                v.featherAbsX + 5f,
                v.featherAbsY + 5f,
                fp
            )
        }
    }

    /**
     * Generates a high-precision alpha matte Bitmap from the rotoscoping shapes
     * applying per-vertex feather interpolation.
     */
    fun generateRotoMatte(
        width: Int,
        height: Int,
        shapes: List<RotoShape>,
        frameIndex: Int
    ): Bitmap {
        val matteBitmap = Bitmap.createBitmap(
            width.coerceAtLeast(1),
            height.coerceAtLeast(1),
            Bitmap.Config.ALPHA_8
        )
        val canvas = Canvas(matteBitmap)

        for (shape in shapes) {
            if (!shape.isVisible) continue
            val vertices = shape.getVerticesForFrame(frameIndex)
            if (vertices.size < 3) continue

            // Compute average feather distance across vertices
            var totalFeather = 0f
            for (v in vertices) {
                totalFeather += hypot(v.featherOffsetX, v.featherOffsetY)
            }
            val avgFeather = (totalFeather / vertices.size).coerceIn(0.5f, 50f)

            val corePath = buildSplinePath(vertices, shape.isClosed, isFeather = false)

            val fillPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = Color.WHITE
                alpha = (shape.opacity * 255).toInt().coerceIn(0, 255)
                if (avgFeather > 1.5f) {
                    maskFilter = BlurMaskFilter(avgFeather, BlurMaskFilter.Blur.NORMAL)
                }
            }

            if (shape.isInverted) {
                // Inverted mask: draw full white, then clear out the shape
                canvas.drawColor(Color.WHITE)
                fillPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                canvas.drawPath(corePath, fillPaint)
            } else {
                canvas.drawPath(corePath, fillPaint)
            }
        }

        return matteBitmap
    }
}
