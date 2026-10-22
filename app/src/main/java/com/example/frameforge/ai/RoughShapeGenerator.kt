package com.example.frameforge.ai

import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.StrokePoint
import com.example.frameforge.model.ToolType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class RoughFillStyle(val label: String) {
    NONE("Transparent / Outline"),
    HACHURE("Sketchy Hatching"),
    CROSS_HATCH("Cross-Hatch"),
    SOLID("Solid Tint")
}

enum class ExcalidrawShapeType(val label: String) {
    RECTANGLE("Rectangle"),
    ROUNDED_RECT("Rounded Container"),
    DIAMOND("Decision Diamond"),
    ELLIPSE("Circle / Ellipse"),
    CYLINDER("Database Storage"),
    CLOUD("Cloud Service"),
    ARROW("Connecting Arrow"),
    STICKY_NOTE("Sticky Note"),
    ACTOR("Actor / User")
}

/**
 * Generates organic, hand-drawn vector strokes matching the iconic Excalidraw aesthetic:
 * wobbly double-lines, overlapping corners, sketchy hatching, and curved connectors.
 */
object RoughShapeGenerator {

    /**
     * Creates a sketchy double-pass line between two points with subtle organic jitter.
     */
    fun createRoughLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        roughness: Float = 1.2f,
        colorArgb: Int = 0xFFFFFFFF.toInt(),
        strokeWidth: Float = 3.5f,
        seed: Long = (x1 * 31 + y1 * 17).toLong()
    ): List<DrawingStroke> {
        val random = Random(seed)
        val strokes = mutableListOf<DrawingStroke>()
        val passes = 2

        val dx = x2 - x1
        val dy = y2 - y1
        val len = kotlin.math.hypot(dx, dy)
        if (len < 1f) return emptyList()

        val steps = (len / 18f).toInt().coerceIn(3, 30)
        val normalX = -dy / len
        val normalY = dx / len

        for (pass in 0 until passes) {
            val points = mutableListOf<StrokePoint>()
            val passOffset = if (pass == 0) -0.5f else 0.5f

            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val midBow = sin(t * PI).toFloat() * roughness * (passOffset * 1.5f + (random.nextFloat() - 0.5f) * 2f)
                val jitter = (random.nextFloat() - 0.5f) * roughness * 2.2f

                val px = x1 + dx * t + normalX * (midBow + jitter)
                val py = y1 + dy * t + normalY * (midBow + jitter)
                val pressure = 0.8f + (random.nextFloat() * 0.4f)

                points.add(StrokePoint(px, py, pressure))
            }

            strokes.add(
                DrawingStroke(
                    id = java.util.UUID.randomUUID().toString(),
                    points = points,
                    colorArgb = colorArgb,
                    toolType = ToolType.PEN,
                    size = strokeWidth * (0.85f + pass * 0.2f),
                    opacity = 0.92f,
                    smoothing = 0.4f
                )
            )
        }

        return strokes
    }

    /**
     * Generates a hand-drawn Excalidraw rectangle with overlapping sketched edges and optional hatching.
     */
    fun createRoughRectangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        roughness: Float = 1.2f,
        fillStyle: RoughFillStyle = RoughFillStyle.NONE,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        fillColorArgb: Int = 0x3338BDF8.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()

        // 1. Fill hatching if requested
        if (fillStyle == RoughFillStyle.HACHURE || fillStyle == RoughFillStyle.CROSS_HATCH) {
            strokes.addAll(createHatchingStrokes(x, y, width, height, fillColorArgb, strokeWidth * 0.7f, 18f, 45f))
            if (fillStyle == RoughFillStyle.CROSS_HATCH) {
                strokes.addAll(createHatchingStrokes(x, y, width, height, fillColorArgb, strokeWidth * 0.7f, 18f, -45f))
            }
        }

        // Slight overhang for hand-drawn character
        val overhang = 4f * roughness
        val top = y
        val bottom = y + height
        val left = x
        val right = x + width

        // Top line
        strokes.addAll(createRoughLine(left - overhang, top, right + overhang, top, roughness, strokeColorArgb, strokeWidth))
        // Right line
        strokes.addAll(createRoughLine(right, top - overhang, right, bottom + overhang, roughness, strokeColorArgb, strokeWidth))
        // Bottom line
        strokes.addAll(createRoughLine(right + overhang, bottom, left - overhang, bottom, roughness, strokeColorArgb, strokeWidth))
        // Left line
        strokes.addAll(createRoughLine(left, bottom + overhang, left, top - overhang, roughness, strokeColorArgb, strokeWidth))

        return strokes
    }

    /**
     * Generates a hand-drawn decision diamond.
     */
    fun createRoughDiamond(
        centerX: Float,
        centerY: Float,
        width: Float,
        height: Float,
        roughness: Float = 1.2f,
        fillStyle: RoughFillStyle = RoughFillStyle.NONE,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        fillColorArgb: Int = 0x33F59E0B.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val halfW = width / 2f
        val halfH = height / 2f

        val topX = centerX
        val topY = centerY - halfH
        val rightX = centerX + halfW
        val rightY = centerY
        val bottomX = centerX
        val bottomY = centerY + halfH
        val leftX = centerX - halfW
        val leftY = centerY

        if (fillStyle == RoughFillStyle.HACHURE || fillStyle == RoughFillStyle.CROSS_HATCH) {
            strokes.addAll(createHatchingStrokes(centerX - halfW * 0.7f, centerY - halfH * 0.7f, width * 0.7f, height * 0.7f, fillColorArgb, strokeWidth * 0.7f, 16f, 45f))
        }

        val overhang = 3.5f * roughness
        strokes.addAll(createRoughLine(topX - overhang, topY - overhang, rightX + overhang, rightY - overhang, roughness, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(rightX + overhang, rightY - overhang, bottomX + overhang, bottomY + overhang, roughness, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(bottomX + overhang, bottomY + overhang, leftX - overhang, leftY + overhang, roughness, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(leftX - overhang, leftY + overhang, topX - overhang, topY - overhang, roughness, strokeColorArgb, strokeWidth))

        return strokes
    }

    /**
     * Generates a hand-drawn ellipse/circle with overlapping loops.
     */
    fun createRoughEllipse(
        centerX: Float,
        centerY: Float,
        radiusX: Float,
        radiusY: Float,
        roughness: Float = 1.2f,
        fillStyle: RoughFillStyle = RoughFillStyle.NONE,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        fillColorArgb: Int = 0x3310B981.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()

        if (fillStyle == RoughFillStyle.HACHURE || fillStyle == RoughFillStyle.CROSS_HATCH) {
            strokes.addAll(createHatchingStrokes(centerX - radiusX * 0.7f, centerY - radiusY * 0.7f, radiusX * 1.4f, radiusY * 1.4f, fillColorArgb, strokeWidth * 0.7f, 16f, 45f))
        }

        val random = Random(centerX.toLong() * 37 + centerY.toLong())
        val steps = 36
        val totalLoops = 2

        for (loop in 0 until totalLoops) {
            val points = mutableListOf<StrokePoint>()
            val loopOffset = if (loop == 0) -1.2f else 1.2f

            for (i in 0..steps) {
                val angle = (i.toFloat() / steps) * 2.0 * PI
                val radJitter = (random.nextFloat() - 0.5f) * roughness * 3f + loopOffset
                val rx = radiusX + radJitter
                val ry = radiusY + radJitter

                val px = centerX + (rx * cos(angle)).toFloat()
                val py = centerY + (ry * sin(angle)).toFloat()
                points.add(StrokePoint(px, py, 0.9f))
            }

            strokes.add(
                DrawingStroke(
                    id = java.util.UUID.randomUUID().toString(),
                    points = points,
                    colorArgb = strokeColorArgb,
                    toolType = ToolType.PEN,
                    size = strokeWidth,
                    opacity = 0.92f,
                    smoothing = 0.5f
                )
            )
        }

        return strokes
    }

    /**
     * Generates a database cylinder shape.
     */
    fun createRoughCylinder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        roughness: Float = 1.2f,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val topRadiusY = height * 0.15f
        val centerX = x + width / 2f
        val radiusX = width / 2f

        // Top cap ellipse
        strokes.addAll(createRoughEllipse(centerX, y + topRadiusY, radiusX, topRadiusY, roughness, RoughFillStyle.NONE, strokeColorArgb, 0, strokeWidth))

        // Left and Right walls
        strokes.addAll(createRoughLine(x, y + topRadiusY, x, y + height - topRadiusY, roughness, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(x + width, y + topRadiusY, x + width, y + height - topRadiusY, roughness, strokeColorArgb, strokeWidth))

        // Bottom curved arc
        val bottomArcPoints = mutableListOf<StrokePoint>()
        val bottomY = y + height - topRadiusY
        val steps = 18
        for (i in 0..steps) {
            val angle = (i.toFloat() / steps) * PI
            val px = centerX + (radiusX * cos(angle)).toFloat()
            val py = bottomY + (topRadiusY * sin(angle)).toFloat()
            bottomArcPoints.add(StrokePoint(px, py, 0.9f))
        }
        strokes.add(
            DrawingStroke(
                id = java.util.UUID.randomUUID().toString(),
                points = bottomArcPoints,
                colorArgb = strokeColorArgb,
                toolType = ToolType.PEN,
                size = strokeWidth
            )
        )

        return strokes
    }

    /**
     * Generates a cloud service node shape.
     */
    fun createRoughCloud(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        roughness: Float = 1.2f,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val centerX = x + width / 2f
        val centerY = y + height / 2f

        // Interlocking puffy circles
        val puffs = listOf(
            Triple(centerX - width * 0.28f, centerY, width * 0.22f),
            Triple(centerX - width * 0.10f, centerY - height * 0.22f, width * 0.26f),
            Triple(centerX + width * 0.15f, centerY - height * 0.18f, width * 0.24f),
            Triple(centerX + width * 0.28f, centerY + height * 0.05f, width * 0.20f),
            Triple(centerX, centerY + height * 0.22f, width * 0.24f)
        )

        puffs.forEach { (px, py, r) ->
            strokes.addAll(createRoughEllipse(px, py, r, r * 0.85f, roughness, RoughFillStyle.NONE, strokeColorArgb, 0, strokeWidth))
        }

        return strokes
    }

    /**
     * Generates a connecting arrow with hand-drawn curvature and arrowhead.
     */
    fun createRoughArrow(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        curved: Boolean = true,
        label: String? = null,
        roughness: Float = 1.2f,
        strokeColorArgb: Int = 0xFF38BDF8.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()

        val dx = x2 - x1
        val dy = y2 - y1
        val dist = kotlin.math.hypot(dx, dy)
        if (dist < 4f) return emptyList()

        if (curved) {
            // Subtle quadratic bezier curve
            val midX = (x1 + x2) / 2f
            val midY = (y1 + y2) / 2f
            val normalX = -dy / dist
            val normalY = dx / dist
            val curveOffset = (dist * 0.15f).coerceIn(12f, 40f)
            val ctrlX = midX + normalX * curveOffset
            val ctrlY = midY + normalY * curveOffset

            val steps = 24
            for (pass in 0..1) {
                val pts = mutableListOf<StrokePoint>()
                val passJitter = if (pass == 0) -0.5f else 0.5f
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val oneMinusT = 1f - t
                    val px = oneMinusT * oneMinusT * x1 + 2 * oneMinusT * t * ctrlX + t * t * x2 + passJitter
                    val py = oneMinusT * oneMinusT * y1 + 2 * oneMinusT * t * ctrlY + t * t * y2 + passJitter
                    pts.add(StrokePoint(px, py, 0.9f))
                }
                strokes.add(
                    DrawingStroke(
                        id = java.util.UUID.randomUUID().toString(),
                        points = pts,
                        colorArgb = strokeColorArgb,
                        toolType = ToolType.PEN,
                        size = strokeWidth
                    )
                )
            }
        } else {
            strokes.addAll(createRoughLine(x1, y1, x2, y2, roughness, strokeColorArgb, strokeWidth))
        }

        // Arrow head at (x2, y2)
        val angle = atan2((y2 - (if (curved) (y1 + y2)/2f else y1)), (x2 - (if (curved) (x1 + x2)/2f else x1)))
        val headLen = 18f * (strokeWidth / 3f)
        val arrowAngle = PI / 6.0 // 30 degrees

        val leftWingX = (x2 - headLen * cos(angle - arrowAngle)).toFloat()
        val leftWingY = (y2 - headLen * sin(angle - arrowAngle)).toFloat()
        val rightWingX = (x2 - headLen * cos(angle + arrowAngle)).toFloat()
        val rightWingY = (y2 - headLen * sin(angle + arrowAngle)).toFloat()

        strokes.addAll(createRoughLine(x2, y2, leftWingX, leftWingY, roughness, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(x2, y2, rightWingX, rightWingY, roughness, strokeColorArgb, strokeWidth))

        // If label exists, draw a small pill label near the midpoint
        if (!label.isNullOrBlank()) {
            val labelMidX = (x1 + x2) / 2f
            val labelMidY = (y1 + y2) / 2f - 14f
            strokes.addAll(createSketchedLabel(label, labelMidX, labelMidY, strokeColorArgb))
        }

        return strokes
    }

    /**
     * Generates a sticky note with folded bottom-right corner.
     */
    fun createRoughStickyNote(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        text: String = "",
        noteColorArgb: Int = 0xFFFDE047.toInt(), // Warm yellow post-it
        strokeColorArgb: Int = 0xFF854D0E.toInt()
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val foldSize = 22f

        // Outline rectangle with fold notch
        strokes.addAll(createRoughLine(x, y, x + width, y, 1.0f, strokeColorArgb, 3f))
        strokes.addAll(createRoughLine(x + width, y, x + width, y + height - foldSize, 1.0f, strokeColorArgb, 3f))
        strokes.addAll(createRoughLine(x + width, y + height - foldSize, x + width - foldSize, y + height, 1.0f, strokeColorArgb, 3f))
        strokes.addAll(createRoughLine(x + width - foldSize, y + height, x, y + height, 1.0f, strokeColorArgb, 3f))
        strokes.addAll(createRoughLine(x, y + height, x, y, 1.0f, strokeColorArgb, 3f))

        // Corner fold triangle
        strokes.addAll(createRoughLine(x + width - foldSize, y + height - foldSize, x + width, y + height - foldSize, 1.0f, strokeColorArgb, 2f))
        strokes.addAll(createRoughLine(x + width - foldSize, y + height - foldSize, x + width - foldSize, y + height, 1.0f, strokeColorArgb, 2f))

        // Sticky note background wash
        strokes.addAll(createHatchingStrokes(x, y, width, height, noteColorArgb, 4f, 14f, 25f))

        if (text.isNotBlank()) {
            strokes.addAll(createSketchedLabel(text, x + width / 2f, y + height / 2f, strokeColorArgb))
        }

        return strokes
    }

    /**
     * Creates hand-drawn stick figure actor.
     */
    fun createRoughActor(
        centerX: Float,
        topY: Float,
        height: Float = 100f,
        strokeColorArgb: Int = 0xFFFFFFFF.toInt(),
        strokeWidth: Float = 3.5f
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val headRadius = height * 0.16f
        val headCenterY = topY + headRadius
        val spineTop = headCenterY + headRadius
        val spineBottom = topY + height * 0.65f

        // Head
        strokes.addAll(createRoughEllipse(centerX, headCenterY, headRadius, headRadius, 1.0f, RoughFillStyle.NONE, strokeColorArgb, 0, strokeWidth))
        // Spine
        strokes.addAll(createRoughLine(centerX, spineTop, centerX, spineBottom, 1.0f, strokeColorArgb, strokeWidth))
        // Arms
        val armsY = spineTop + (spineBottom - spineTop) * 0.35f
        strokes.addAll(createRoughLine(centerX - height * 0.25f, armsY + 5f, centerX + height * 0.25f, armsY + 5f, 1.0f, strokeColorArgb, strokeWidth))
        // Legs
        strokes.addAll(createRoughLine(centerX, spineBottom, centerX - height * 0.22f, topY + height, 1.0f, strokeColorArgb, strokeWidth))
        strokes.addAll(createRoughLine(centerX, spineBottom, centerX + height * 0.22f, topY + height, 1.0f, strokeColorArgb, strokeWidth))

        return strokes
    }

    /**
     * Generates sketchy hatching lines inside an area.
     */
    private fun createHatchingStrokes(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        colorArgb: Int,
        strokeWidth: Float,
        gap: Float,
        angleDeg: Float
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val rad = angleDeg * PI / 180.0
        val sinA = sin(rad).toFloat()
        val cosA = cos(rad).toFloat()

        val count = ((w + h) / gap).toInt().coerceIn(3, 40)
        for (i in 0..count) {
            val offset = (i - count / 2) * gap
            val startX = (x + w / 2f + offset * cosA - (h / 2f) * sinA).coerceIn(x, x + w)
            val startY = (y + h / 2f + offset * sinA + (w / 2f) * cosA).coerceIn(y, y + h)
            val endX = (x + w / 2f + offset * cosA + (h / 2f) * sinA).coerceIn(x, x + w)
            val endY = (y + h / 2f + offset * sinA - (w / 2f) * cosA).coerceIn(y, y + h)

            if (kotlin.math.hypot(endX - startX, endY - startY) > 8f) {
                strokes.addAll(createRoughLine(startX, startY, endX, endY, 1.0f, colorArgb, strokeWidth))
            }
        }
        return strokes
    }

    /**
     * Draws an elegant sketched pill label or text banner at a given position.
     */
    fun createSketchedLabel(
        text: String,
        centerX: Float,
        centerY: Float,
        colorArgb: Int = 0xFFFFFFFF.toInt()
    ): List<DrawingStroke> {
        val strokes = mutableListOf<DrawingStroke>()
        val charWidth = 9f
        val pillWidth = (text.length * charWidth + 24f).coerceAtLeast(40f)
        val pillHeight = 26f
        val left = centerX - pillWidth / 2f
        val top = centerY - pillHeight / 2f

        // Pill background & border
        strokes.addAll(createRoughRectangle(left, top, pillWidth, pillHeight, 0.8f, RoughFillStyle.NONE, colorArgb, 0, 2f))

        // Center line text indicator (represents clean sketched label)
        strokes.addAll(createRoughLine(left + 8f, centerY, left + pillWidth - 8f, centerY, 0.5f, colorArgb, 1.8f))

        return strokes
    }
}
