package com.example.frameforge.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import com.example.frameforge.model.AdjustmentState
import com.example.frameforge.model.AdjustmentType
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.DrawingCel
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.OnionSkinConfig
import com.example.frameforge.model.SelectionState
import com.example.frameforge.model.StrokePoint
import com.example.frameforge.model.ToolType
import com.example.frameforge.model.TransformState
import com.example.frameforge.ui.a2.CanvasPaperPreset
import com.example.frameforge.ui.a2.GridType
import com.example.frameforge.ui.a2.GuideType
import java.util.LinkedList
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object DrawingEngine {

    /**
     * Smooths an incoming point stream using exponential moving average
     */
    fun smoothPoints(rawPoints: List<StrokePoint>, smoothingWeight: Float): List<StrokePoint> {
        if (rawPoints.size < 3 || smoothingWeight <= 0.05f) return rawPoints
        val result = mutableListOf<StrokePoint>()
        result.add(rawPoints[0])

        var prevX = rawPoints[0].x
        var prevY = rawPoints[0].y
        val alpha = (1.0f - smoothingWeight).coerceIn(0.1f, 0.9f)

        for (i in 1 until rawPoints.size) {
            val curr = rawPoints[i]
            val smoothedX = prevX + alpha * (curr.x - prevX)
            val smoothedY = prevY + alpha * (curr.y - prevY)
            result.add(curr.copy(x = smoothedX, y = smoothedY))
            prevX = smoothedX
            prevY = smoothedY
        }
        return result
    }

    /**
     * Renders a single stroke onto a Canvas
     */
    fun renderStroke(canvas: Canvas, stroke: DrawingStroke) {
        val points = stroke.points
        if (points.isEmpty()) return

        val paint = Paint().apply {
            isAntiAlias = true
            isDither = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        if (stroke.toolType == ToolType.ERASER) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke.size
        } else if (stroke.toolType == ToolType.SOFT_ERASER) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke.size
            paint.alpha = (stroke.opacity * 128).toInt().coerceIn(1, 255)
        } else if (stroke.toolType == ToolType.HIGHLIGHTER) {
            // Smart highlighter: Multiply blend preserves dark text/ink on PDF pages
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = stroke.size * 1.35f
            paint.strokeCap = Paint.Cap.SQUARE
            paint.color = stroke.colorArgb
            paint.alpha = (Color.alpha(stroke.colorArgb) * stroke.opacity * 0.85f).toInt().coerceIn(1, 255)
            val path = buildSmoothPath(points)
            canvas.drawPath(path, paint)
            return
        } else if (stroke.toolType == ToolType.LASER_POINTER) {
            // Glowing neon laser pointer with soft aura
            val outerPaint = Paint(paint).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                style = Paint.Style.STROKE
                strokeWidth = stroke.size * 2.2f
                color = stroke.colorArgb
                alpha = (stroke.opacity * 90).toInt().coerceIn(1, 255)
            }
            val corePaint = Paint(paint).apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
                style = Paint.Style.STROKE
                strokeWidth = max(2f, stroke.size * 0.5f)
                color = Color.WHITE
                alpha = (stroke.opacity * 240).toInt().coerceIn(1, 255)
            }
            val path = buildSmoothPath(points)
            canvas.drawPath(path, outerPaint)
            canvas.drawPath(path, corePaint)
            return
        } else if (stroke.toolType == ToolType.SMUDGE || stroke.isSmudge) {
            // Smudge blend mode: pulls and dilutes colors
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            paint.style = Paint.Style.FILL
            paint.color = stroke.colorArgb
            paint.alpha = (stroke.opacity * 70).toInt().coerceIn(1, 255)
            for (p in points) {
                val r = (stroke.size * (0.6f + 0.4f * p.pressure)) / 2f
                canvas.drawCircle(p.x, p.y, r, paint)
            }
            return
        } else {
            paint.xfermode = null
            paint.color = stroke.colorArgb
            paint.alpha = (Color.alpha(stroke.colorArgb) * stroke.opacity).toInt().coerceIn(0, 255)
        }

        when (stroke.toolType) {
            ToolType.AIRBRUSH -> {
                paint.style = Paint.Style.FILL
                val radius = stroke.size / 2f
                paint.alpha = (paint.alpha * 0.22f).toInt().coerceIn(1, 255)
                for (p in points) {
                    val r = radius * (0.5f + 0.5f * p.pressure)
                    canvas.drawCircle(p.x, p.y, r, paint)
                }
            }
            ToolType.WATERCOLOR -> {
                // 1. Soft wet pigment wash underlayer with edge pooling
                val washPaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = stroke.size * 1.15f
                    alpha = (paint.alpha * 0.28f).toInt().coerceIn(1, 255)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                val washPath = buildSmoothPath(points)
                canvas.drawPath(washPath, washPaint)

                // 2. Concentrated edge pooling contour (darker pigment boundary fringe)
                val edgePaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = stroke.size * 0.92f
                    alpha = (paint.alpha * 0.22f).toInt().coerceIn(1, 255)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                canvas.drawPath(washPath, edgePaint)

                // 3. Stippled pigment grain settling into paper tooth (matching Concepts tablet stroke)
                val grainPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                }
                val radius = stroke.size / 2f
                val step = max(1, points.size / 50)
                for (idx in 0 until points.size step step) {
                    val p = points[idx]
                    val pRad = radius * (0.6f + 0.4f * p.pressure)
                    for (k in 0..4) {
                        val pseudoSeed = (p.x * 12.9898f + p.y * 78.233f + k * 37.719f)
                        val rOff = ((sin(pseudoSeed.toDouble()) * 0.85).toFloat()) * pRad
                        val ang = (cos((pseudoSeed * 1.7).toDouble()) * Math.PI).toFloat()
                        val dabX = p.x + rOff * cos(ang)
                        val dabY = p.y + rOff * sin(ang)
                        val dabRadius = max(1f, pRad * 0.22f * (0.8f + 0.4f * sin((pseudoSeed * 3.1).toDouble()).toFloat()))
                        grainPaint.alpha = (paint.alpha * 0.18f).toInt().coerceIn(1, 255)
                        canvas.drawCircle(dabX, dabY, dabRadius, grainPaint)
                    }
                }
            }
            ToolType.CHARCOAL -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = stroke.size
                paint.alpha = (paint.alpha * 0.75f).toInt().coerceIn(1, 255)
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val jitter = (sin((p1.x + p1.y).toDouble()) * 2f).toFloat()
                    canvas.drawLine(p1.x + jitter, p1.y - jitter, p2.x, p2.y, paint)
                }
            }
            ToolType.PIXEL -> {
                paint.isAntiAlias = false
                paint.style = Paint.Style.FILL
                val half = (stroke.size / 2f).coerceAtLeast(1f)
                for (p in points) {
                    canvas.drawRect(p.x - half, p.y - half, p.x + half, p.y + half, paint)
                }
            }
            ToolType.INK -> {
                if (points.size == 1) {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(points[0].x, points[0].y, (stroke.size * points[0].pressure) / 2f, paint)
                } else {
                    paint.style = Paint.Style.STROKE
                    paint.strokeCap = Paint.Cap.ROUND
                    paint.strokeJoin = Paint.Join.ROUND
                    
                    var pPrev = points[0]
                    for (i in 1 until points.size) {
                        val pCurr = points[i]
                        val midX = (pPrev.x + pCurr.x) / 2f
                        val midY = (pPrev.y + pCurr.y) / 2f
                        val avgPressure = (pPrev.pressure + pCurr.pressure) / 2f
                        paint.strokeWidth = max(1.5f, stroke.size * avgPressure)
                        
                        val path = Path().apply {
                            moveTo(pPrev.x, pPrev.y)
                            quadTo(pPrev.x, pPrev.y, midX, midY)
                        }
                        canvas.drawPath(path, paint)
                        pPrev = StrokePoint(midX, midY, avgPressure)
                    }
                    val last = points.last()
                    paint.strokeWidth = max(1.5f, stroke.size * last.pressure)
                    canvas.drawLine(pPrev.x, pPrev.y, last.x, last.y, paint)
                }
            }
            ToolType.PENCIL -> {
                // Core graphite stroke
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, stroke.size * 0.75f)
                paint.alpha = (paint.alpha * 0.75f).toInt().coerceIn(1, 255)
                val path = buildSmoothPath(points)
                canvas.drawPath(path, paint)

                // Grainy graphite tooth speckle
                val grainPaint = Paint(paint).apply {
                    style = Paint.Style.FILL
                    alpha = (paint.alpha * 0.35f).toInt().coerceIn(1, 255)
                }
                val step = max(1, points.size / 60)
                for (idx in 0 until points.size step step) {
                    val p = points[idx]
                    val seed = (p.x * 31.415f + p.y * 47.123f)
                    val jitterX = (sin(seed.toDouble()) * stroke.size * 0.35f).toFloat()
                    val jitterY = (cos(seed.toDouble()) * stroke.size * 0.35f).toFloat()
                    canvas.drawCircle(p.x + jitterX, p.y + jitterY, max(0.75f, stroke.size * 0.15f), grainPaint)
                }
            }
            else -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = stroke.size
                if (points.size == 1) {
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(points[0].x, points[0].y, stroke.size / 2f, paint)
                } else {
                    val path = buildSmoothPath(points)
                    canvas.drawPath(path, paint)
                }
            }
        }
    }

    private fun buildSmoothPath(points: List<StrokePoint>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        path.moveTo(points[0].x, points[0].y)

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val curr = points[i]
            val midX = (prev.x + curr.x) / 2f
            val midY = (prev.y + curr.y) / 2f
            path.quadTo(prev.x, prev.y, midX, midY)
        }
        path.lineTo(points.last().x, points.last().y)
        return path
    }

    /**
     * Renders an imported PDF document track frame (page or scroll strip)
     */
    fun renderPdfTrackFrame(
        canvas: Canvas,
        docWidth: Int,
        docHeight: Int,
        frameIndex: Int,
        totalFrames: Int,
        track: com.example.frameforge.pdf.PdfDocumentTrack
    ) {
        if (!track.isEnabled || track.pageIndices.isEmpty()) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (track.opacity * 255).toInt().coerceIn(0, 255)
        }

        when (track.importMode) {
            com.example.frameforge.pdf.PdfImportMode.STITCHED_SCROLL -> {
                val stitchedKey = "${track.cacheSessionId}_stitched"
                val bmp = com.example.frameforge.pdf.PdfEngine.BitmapCache.get(stitchedKey)
                if (bmp != null) {
                    val scale = docWidth.toFloat() / bmp.width.coerceAtLeast(1)
                    val totalH = bmp.height * scale
                    val scrollRange = max(0f, totalH - docHeight)
                    val progress = if (totalFrames > 1) frameIndex.toFloat() / (totalFrames - 1) else 0f
                    val scrollY = -scrollRange * progress.coerceIn(0f, 1f)

                    val destRect = RectF(0f, scrollY, docWidth.toFloat(), scrollY + totalH)
                    canvas.drawBitmap(bmp, null, destRect, paint)
                }
            }
            else -> {
                // Sequential cels or separate layers
                val hold = track.holdFramesPerPage.coerceAtLeast(1)
                val pageIndexInList = (frameIndex / hold).coerceIn(0, track.pageIndices.lastIndex)
                val targetPageIndex = track.pageIndices[pageIndexInList]
                val cacheKey = "${track.cacheSessionId}_page_$targetPageIndex"
                val bmp = com.example.frameforge.pdf.PdfEngine.BitmapCache.get(cacheKey)
                if (bmp != null) {
                    val scale = min(docWidth.toFloat() / bmp.width, docHeight.toFloat() / bmp.height)
                    val w = bmp.width * scale
                    val h = bmp.height * scale
                    val left = (docWidth - w) / 2f
                    val top = (docHeight - h) / 2f
                    val destRect = RectF(left, top, left + w, top + h)
                    canvas.drawBitmap(bmp, null, destRect, paint)
                }
            }
        }
    }

    /**
     * Composites all active layers for a given frame index into a target Bitmap
     */
    fun compositeFrame(
        width: Int,
        height: Int,
        frameIndex: Int,
        layers: List<AnimationLayer>,
        cels: List<DrawingCel>,
        backgroundColor: Int? = null,
        targetBitmap: Bitmap? = null,
        pdfTrack: com.example.frameforge.pdf.PdfDocumentTrack? = null,
        totalFrames: Int = 24
    ): Bitmap {
        val bmp = targetBitmap ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        if (backgroundColor != null) {
            canvas.drawColor(backgroundColor)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        // Draw PDF Track Underlay if enabled
        if (pdfTrack != null && pdfTrack.isEnabled && pdfTrack.isUnderlay) {
            renderPdfTrackFrame(canvas, width, height, frameIndex, totalFrames, pdfTrack)
        }

        val celsByLayer = cels.filter { it.frameIndex == frameIndex }.associateBy { it.layerId }

        for (layer in layers) {
            if (!layer.isVisible) continue
            val cel = celsByLayer[layer.id] ?: continue

            val mode = when (layer.blendMode) {
                com.example.frameforge.model.BlendModeType.MULTIPLY -> PorterDuff.Mode.MULTIPLY
                com.example.frameforge.model.BlendModeType.SCREEN -> PorterDuff.Mode.SCREEN
                com.example.frameforge.model.BlendModeType.ADD -> PorterDuff.Mode.ADD
                com.example.frameforge.model.BlendModeType.DARKEN -> PorterDuff.Mode.DARKEN
                com.example.frameforge.model.BlendModeType.LIGHTEN -> PorterDuff.Mode.LIGHTEN
                com.example.frameforge.model.BlendModeType.OVERLAY -> PorterDuff.Mode.OVERLAY
                else -> null
            }

            val layerPaint = Paint().apply {
                alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
                isAntiAlias = true
                if (mode != null) {
                    xfermode = PorterDuffXfermode(mode)
                }
            }

            if (layer.opacity < 0.999f || mode != null) {
                val tempBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val tempCanvas = Canvas(tempBmp)
                for (s in cel.strokes) {
                    renderStroke(tempCanvas, s)
                }
                canvas.drawBitmap(tempBmp, 0f, 0f, layerPaint)
                tempBmp.recycle()
            } else {
                for (s in cel.strokes) {
                    renderStroke(canvas, s)
                }
            }
        }

        // Draw PDF Track Overlay if enabled
        if (pdfTrack != null && pdfTrack.isEnabled && !pdfTrack.isUnderlay) {
            renderPdfTrackFrame(canvas, width, height, frameIndex, totalFrames, pdfTrack)
        }

        return bmp
    }

    /**
     * Generates Onion Skin overlay for previous and future frames
     */
    /**
     * Generates Onion Skin overlay for previous and future frames
     */
    fun renderOnionSkins(
        canvas: Canvas,
        width: Int,
        height: Int,
        currentFrame: Int,
        totalFrames: Int,
        layers: List<AnimationLayer>,
        cels: List<DrawingCel>,
        config: OnionSkinConfig,
        shiftAndTrace: com.example.frameforge.cinema.ShiftAndTraceState? = null
    ) {
        if (!config.enabled) return

        // Per-layer Onion Skin Pinning: if any layers are pinned, only show pinned layers
        val pinnedLayers = layers.filter { it.isVisible && it.onionSkinPinned }
        val activeLayers = if (pinnedLayers.isNotEmpty()) pinnedLayers else layers.filter { it.isVisible && it.onionSkinEnabled }
        if (activeLayers.isEmpty()) return

        // Previous frames (Krita stepped palette / Coral red)
        for (offset in 1..config.prevFramesCount) {
            val prevIdx = currentFrame - offset
            if (prevIdx < 0) continue

            val distRatio = when (config.fadeMode) {
                com.example.frameforge.model.OnionSkinFadeMode.LINEAR -> (config.prevFramesCount - offset + 1).toFloat() / config.prevFramesCount
                com.example.frameforge.model.OnionSkinFadeMode.EXPONENTIAL -> Math.pow(0.5, offset.toDouble()).toFloat()
                com.example.frameforge.model.OnionSkinFadeMode.FLAT -> 1.0f
            }
            val alpha = (config.opacity * distRatio * 255).toInt().coerceIn(10, 255)

            // Select color from palette or fall back to default
            val tintColor = if (config.prevPalette.isNotEmpty()) {
                config.prevPalette[(offset - 1) % config.prevPalette.size]
            } else {
                config.prevTintArgb
            }

            drawGhostFrame(canvas, width, height, prevIdx, activeLayers, cels, tintColor, alpha, currentFrame, shiftAndTrace)
        }

        // Future frames (Krita stepped palette / Fresh green)
        for (offset in 1..config.nextFramesCount) {
            val nextIdx = currentFrame + offset
            if (nextIdx >= totalFrames) continue

            val distRatio = when (config.fadeMode) {
                com.example.frameforge.model.OnionSkinFadeMode.LINEAR -> (config.nextFramesCount - offset + 1).toFloat() / config.nextFramesCount
                com.example.frameforge.model.OnionSkinFadeMode.EXPONENTIAL -> Math.pow(0.5, offset.toDouble()).toFloat()
                com.example.frameforge.model.OnionSkinFadeMode.FLAT -> 1.0f
            }
            val alpha = (config.opacity * distRatio * 255).toInt().coerceIn(10, 255)

            val tintColor = if (config.nextPalette.isNotEmpty()) {
                config.nextPalette[(offset - 1) % config.nextPalette.size]
            } else {
                config.nextTintArgb
            }

            drawGhostFrame(canvas, width, height, nextIdx, activeLayers, cels, tintColor, alpha, currentFrame, shiftAndTrace)
        }
    }

    private fun drawGhostFrame(
        canvas: Canvas,
        width: Int,
        height: Int,
        frameIndex: Int,
        layers: List<AnimationLayer>,
        cels: List<DrawingCel>,
        tintColorArgb: Int,
        alpha: Int,
        currentFrame: Int = 0,
        shiftAndTrace: com.example.frameforge.cinema.ShiftAndTraceState? = null
    ) {
        val frameCels = cels.filter { it.frameIndex == frameIndex }
        if (frameCels.isEmpty()) return

        val ghostPaint = Paint().apply {
            isAntiAlias = true
            color = tintColorArgb
            this.alpha = alpha
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }

        // Shift and Trace (Light Table / Out-of-Pegs) transformation & Local vs World Space
        val isLocalObjectSpace = shiftAndTrace != null && !shiftAndTrace.isWorldSpace
        val shouldShift = (shiftAndTrace != null && shiftAndTrace.isActive && (frameIndex == currentFrame + shiftAndTrace.targetFrameOffset)) || isLocalObjectSpace

        if (shouldShift) {
            canvas.save()
            val cx = width / 2f
            val cy = height / 2f

            if (isLocalObjectSpace) {
                var sumX = 0f
                var sumY = 0f
                var ptCount = 0
                for (l in layers) {
                    val c = frameCels.find { it.layerId == l.id } ?: continue
                    for (s in c.strokes) {
                        for (pt in s.points) {
                            sumX += pt.x
                            sumY += pt.y
                            ptCount++
                        }
                    }
                }
                if (ptCount > 0) {
                    val centroidX = sumX / ptCount
                    val centroidY = sumY / ptCount
                    canvas.translate(cx - centroidX, cy - centroidY)
                }
            }

            if (shiftAndTrace != null && shiftAndTrace.isActive) {
                canvas.translate(cx + shiftAndTrace.offsetX, cy + shiftAndTrace.offsetY)
                canvas.rotate(shiftAndTrace.rotationDeg)
                canvas.scale(shiftAndTrace.scale, shiftAndTrace.scale)
                canvas.translate(-cx, -cy)
            }
        }

        for (layer in layers) {
            val cel = frameCels.find { it.layerId == layer.id } ?: continue

            // 3D Perspective scaling if layer has non-zero Z-Depth
            val has3DDepth = layer.posZ != 0f
            if (has3DDepth) {
                canvas.save()
                val cx = width / 2f
                val cy = height / 2f
                val f = 1050f // 35mm equivalent focal distance
                val scaleFactor = (f / (f + layer.posZ).coerceAtLeast(10f))
                canvas.translate(cx, cy)
                canvas.scale(scaleFactor, scaleFactor)
                canvas.translate(-cx, -cy)
            }

            for (stroke in cel.strokes) {
                if (stroke.points.isEmpty()) continue
                ghostPaint.strokeWidth = stroke.size
                val path = buildSmoothPath(stroke.points)
                canvas.drawPath(path, ghostPaint)
            }

            if (has3DDepth) {
                canvas.restore()
            }
        }

        if (shouldShift) {
            canvas.restore()
        }
    }

    /**
     * Scanline flood fill algorithm
     */
    fun floodFill(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        fillColor: Int,
        tolerance: Int = 32
    ) {
        val w = bitmap.width
        val h = bitmap.height
        if (startX !in 0 until w || startY !in 0 until h) return

        val targetColor = bitmap.getPixel(startX, startY)
        if (colorMatch(targetColor, fillColor, 5)) return

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        val queue = LinkedList<Int>()
        queue.add(startX or (startY shl 16))

        val visited = BooleanArray(w * h)
        visited[startY * w + startX] = true

        while (!queue.isEmpty()) {
            val p = queue.poll() ?: break
            val x = p and 0xFFFF
            val y = p ushr 16

            pixels[y * w + x] = fillColor

            if (x > 0 && !visited[y * w + (x - 1)] && colorMatch(pixels[y * w + (x - 1)], targetColor, tolerance)) {
                visited[y * w + (x - 1)] = true
                queue.add((x - 1) or (y shl 16))
            }
            if (x < w - 1 && !visited[y * w + (x + 1)] && colorMatch(pixels[y * w + (x + 1)], targetColor, tolerance)) {
                visited[y * w + (x + 1)] = true
                queue.add((x + 1) or (y shl 16))
            }
            if (y > 0 && !visited[(y - 1) * w + x] && colorMatch(pixels[(y - 1) * w + x], targetColor, tolerance)) {
                visited[(y - 1) * w + x] = true
                queue.add(x or ((y - 1) shl 16))
            }
            if (y < h - 1 && !visited[(y + 1) * w + x] && colorMatch(pixels[(y + 1) * w + x], targetColor, tolerance)) {
                visited[(y + 1) * w + x] = true
                queue.add(x or ((y + 1) shl 16))
            }
        }

        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
    }

    private fun colorMatch(c1: Int, c2: Int, tolerance: Int): Boolean {
        if (c1 == c2) return true
        val rDiff = Math.abs(Color.red(c1) - Color.red(c2))
        val gDiff = Math.abs(Color.green(c1) - Color.green(c2))
        val bDiff = Math.abs(Color.blue(c1) - Color.blue(c2))
        val aDiff = Math.abs(Color.alpha(c1) - Color.alpha(c2))
        return (rDiff + gDiff + bDiff + aDiff) / 4 <= tolerance
    }

    // --- PROCREATE ADJUSTMENTS ENGINE ---

    /**
     * Applies real-time adjustment filter (Gaussian blur, Bloom, HSB, Chromatic Aberration) to canvas
     */
    fun applyAdjustment(
        source: Bitmap,
        state: AdjustmentState
    ): Bitmap {
        if (state.activeType == AdjustmentType.NONE) return source

        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        when (state.activeType) {
            AdjustmentType.GAUSSIAN_BLUR, AdjustmentType.MOTION_BLUR -> {
                // Approximate fast box/gaussian blur through downscaling/upscaling or iterative convolution
                val radius = (state.intensity * 24f).coerceAtLeast(1f)
                val paint = Paint().apply {
                    isFilterBitmap = true
                    alpha = 255
                }
                // Render with multi-sample offset for smooth blur effect
                val step = radius / 4f
                val offsets = listOf(
                    0f to 0f, -step to 0f, step to 0f, 0f to -step, 0f to step,
                    -step to -step, step to -step, -step to step, step to step
                )
                val alphaSample = (255f / offsets.size).toInt()
                paint.alpha = alphaSample
                for ((ox, oy) in offsets) {
                    canvas.drawBitmap(source, ox, oy, paint)
                }
            }
            AdjustmentType.HSB -> {
                // Hue (-180..180), Saturation, Brightness matrix
                val cm = ColorMatrix()
                val hueDegrees = (state.intensity - 0.5f) * 360f
                val sat = (state.secondaryParam * 2f).coerceIn(0f, 3f)
                cm.setRotate(0, hueDegrees)
                cm.setRotate(1, hueDegrees)
                cm.setRotate(2, hueDegrees)
                val satMatrix = ColorMatrix().apply { setSaturation(sat) }
                cm.postConcat(satMatrix)

                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(cm)
                    isAntiAlias = true
                }
                canvas.drawBitmap(source, 0f, 0f, paint)
            }
            AdjustmentType.CHROMATIC_ABERRATION -> {
                // Separation of Red and Blue channels with displacement
                val shift = (state.intensity * 30f)
                val rPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
                    alpha = 220
                }
                canvas.drawBitmap(source, -shift, 0f, rPaint)
                val bPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
                    alpha = 220
                }
                canvas.drawBitmap(source, shift, 0f, bPaint)
            }
            AdjustmentType.BLOOM -> {
                // Draw base then glowing boosted highlight layer
                canvas.drawBitmap(source, 0f, 0f, null)
                val glowPaint = Paint().apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
                    alpha = (state.intensity * 200).toInt().coerceIn(0, 255)
                    isFilterBitmap = true
                }
                canvas.drawBitmap(source, 0f, 0f, glowPaint)
            }
            else -> {
                canvas.drawBitmap(source, 0f, 0f, null)
            }
        }

        return output
    }

    // --- SELECTION & TRANSFORM OVERLAYS ---

    /**
     * Renders marching-ants / dashed border around the current active selection
     */
    fun renderSelectionOverlay(
        canvas: Canvas,
        selectionState: SelectionState,
        scale: Float
    ) {
        if (!selectionState.isActive || selectionState.points.size < 2) return

        val path = Path()
        val pts = selectionState.points
        path.moveTo(pts[0].x, pts[0].y)
        for (i in 1 until pts.size) {
            path.lineTo(pts[i].x, pts[i].y)
        }
        if (selectionState.mode != com.example.frameforge.model.SelectionMode.FREEHAND) {
            path.close()
        }

        val strokePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = (2.5f / scale).coerceAtLeast(1.5f)
            color = Color.rgb(10, 132, 255) // Procreate Blue
            pathEffect = DashPathEffect(floatArrayOf(12f / scale, 8f / scale), 0f)
            isAntiAlias = true
        }

        canvas.drawPath(path, strokePaint)
    }

    /**
     * Renders Procreate interactive Transform cage (bounding box + corner handles + rotation nodule)
     */
    fun renderTransformCage(
        canvas: Canvas,
        docWidth: Int,
        docHeight: Int,
        state: TransformState,
        scale: Float
    ) {
        if (!state.isActive) return

        val boxWidth = docWidth * state.scaleX * 0.7f
        val boxHeight = docHeight * state.scaleY * 0.7f
        val cx = docWidth / 2f + state.translationX
        val cy = docHeight / 2f + state.translationY

        canvas.save()
        canvas.translate(cx, cy)
        canvas.rotate(state.rotationDeg)

        val halfW = boxWidth / 2f
        val halfH = boxHeight / 2f

        // Bounding Rectangle
        val cagePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = (2f / scale).coerceAtLeast(1.5f)
            color = Color.rgb(10, 132, 255) // Procreate Blue
            isAntiAlias = true
        }
        canvas.drawRect(-halfW, -halfH, halfW, halfH, cagePaint)

        // Corner nodes & midpoint handles
        val handlePaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.WHITE
            isAntiAlias = true
        }
        val handleBorderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = (2f / scale).coerceAtLeast(1.5f)
            color = Color.rgb(10, 132, 255)
            isAntiAlias = true
        }
        val handleRadius = (8f / scale).coerceAtLeast(5f)

        val handlePoints = listOf(
            -halfW to -halfH,
            halfW to -halfH,
            -halfW to halfH,
            halfW to halfH,
            0f to -halfH,
            0f to halfH,
            -halfW to 0f,
            halfW to 0f
        )

        for ((hx, hy) in handlePoints) {
            canvas.drawCircle(hx, hy, handleRadius, handlePaint)
            canvas.drawCircle(hx, hy, handleRadius, handleBorderPaint)
        }

        // Rotation stalk & handle at top
        val stalkLen = (30f / scale).coerceAtLeast(18f)
        canvas.drawLine(0f, -halfH, 0f, -halfH - stalkLen, cagePaint)
        val rotHandlePaint = Paint().apply {
            style = Paint.Style.FILL
            color = Color.rgb(16, 185, 129) // Emerald green rotation handle
            isAntiAlias = true
        }
        canvas.drawCircle(0f, -halfH - stalkLen, handleRadius * 1.1f, rotHandlePaint)

        canvas.restore()
    }

    /**
     * Concepts "Draw and Hold" Shape Recognition
     * Automatically classifies freehand strokes into:
     * - Line (straight line from start to end)
     * - Circle / Ellipse (regular closed curve)
     * - Rectangle (4-corner closed polygon)
     * - Arc (smooth curved segment)
     */
    fun recognizeShape(points: List<StrokePoint>): List<StrokePoint>? {
        if (points.size < 12) return null

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE

        var arcLength = 0f
        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            minX = min(minX, p0.x)
            maxX = max(maxX, p0.x)
            minY = min(minY, p0.y)
            maxY = max(maxY, p0.y)
            arcLength += Math.hypot((p1.x - p0.x).toDouble(), (p1.y - p0.y).toDouble()).toFloat()
        }
        val last = points.last()
        minX = min(minX, last.x)
        maxX = max(maxX, last.x)
        minY = min(minY, last.y)
        maxY = max(maxY, last.y)

        val first = points.first()
        val directDist = Math.hypot((last.x - first.x).toDouble(), (last.y - first.y).toDouble()).toFloat()
        val avgPressure = points.map { it.pressure }.average().toFloat().coerceIn(0.2f, 1.0f)
        val avgTilt = points.first().tiltDeg

        // 1. Straight Line Check: direct distance / arc length > 0.92
        if (directDist / arcLength > 0.92f && arcLength > 30f) {
            val numSamples = 24
            val res = mutableListOf<StrokePoint>()
            for (i in 0..numSamples) {
                val t = i / numSamples.toFloat()
                val x = first.x + t * (last.x - first.x)
                val y = first.y + t * (last.y - first.y)
                res.add(StrokePoint(x, y, avgPressure, tiltDeg = avgTilt))
            }
            return res
        }

        // 2. Closed Shape Check: endpoints meet within 22% of arc length or 45px
        val isClosed = (directDist / arcLength < 0.22f) || (directDist < 45f)
        if (isClosed && arcLength > 60f) {
            val centerX = (minX + maxX) / 2f
            val centerY = (minY + maxY) / 2f
            val halfW = (maxX - minX) / 2f
            val halfH = (maxY - minY) / 2f

            // Calculate radial standard deviation from centroid
            var sumDist = 0f
            for (p in points) {
                sumDist += Math.hypot((p.x - centerX).toDouble(), (p.y - centerY).toDouble()).toFloat()
            }
            val avgRad = sumDist / points.size
            var sumVariance = 0f
            for (p in points) {
                val d = Math.hypot((p.x - centerX).toDouble(), (p.y - centerY).toDouble()).toFloat()
                sumVariance += (d - avgRad) * (d - avgRad)
            }
            val radStdDev = kotlin.math.sqrt(sumVariance / points.size)

            // Circle / Ellipse: low radial variance (< 22%)
            if (radStdDev / avgRad < 0.22f) {
                val numSamples = 48
                val res = mutableListOf<StrokePoint>()
                for (i in 0..numSamples) {
                    val angle = (i / numSamples.toFloat()) * (2f * Math.PI.toFloat())
                    val x = centerX + halfW * cos(angle.toDouble()).toFloat()
                    val y = centerY + halfH * sin(angle.toDouble()).toFloat()
                    res.add(StrokePoint(x, y, avgPressure, tiltDeg = avgTilt))
                }
                return res
            } else {
                // Rectangle: snap to 4 corners of bounding box
                val numSamplesPerSide = 12
                val res = mutableListOf<StrokePoint>()
                // Top side (minX, minY) -> (maxX, minY)
                for (i in 0..numSamplesPerSide) {
                    val t = i / numSamplesPerSide.toFloat()
                    res.add(StrokePoint(minX + t * (maxX - minX), minY, avgPressure, tiltDeg = avgTilt))
                }
                // Right side (maxX, minY) -> (maxX, maxY)
                for (i in 1..numSamplesPerSide) {
                    val t = i / numSamplesPerSide.toFloat()
                    res.add(StrokePoint(maxX, minY + t * (maxY - minY), avgPressure, tiltDeg = avgTilt))
                }
                // Bottom side (maxX, maxY) -> (minX, maxY)
                for (i in 1..numSamplesPerSide) {
                    val t = i / numSamplesPerSide.toFloat()
                    res.add(StrokePoint(maxX - t * (maxX - minX), maxY, avgPressure, tiltDeg = avgTilt))
                }
                // Left side (minX, maxY) -> (minX, minY)
                for (i in 1..numSamplesPerSide) {
                    val t = i / numSamplesPerSide.toFloat()
                    res.add(StrokePoint(minX, maxY - t * (maxY - minY), avgPressure, tiltDeg = avgTilt))
                }
                return res
            }
        }

        // 3. Smooth Curved Arc: fit a 3-point quadratic bezier through start, mid, end
        if (directDist / arcLength in 0.65f..0.92f && points.size >= 16) {
            val mid = points[points.size / 2]
            val ctrlX = 2f * mid.x - 0.5f * (first.x + last.x)
            val ctrlY = 2f * mid.y - 0.5f * (first.y + last.y)

            val numSamples = 32
            val res = mutableListOf<StrokePoint>()
            for (i in 0..numSamples) {
                val t = i / numSamples.toFloat()
                val oneMinusT = 1f - t
                val x = oneMinusT * oneMinusT * first.x + 2f * oneMinusT * t * ctrlX + t * t * last.x
                val y = oneMinusT * oneMinusT * first.y + 2f * oneMinusT * t * ctrlY + t * t * last.y
                res.add(StrokePoint(x, y, avgPressure, tiltDeg = avgTilt))
            }
            return res
        }

        return null
    }

    /**
     * Concepts Measure (1:1 pts): Renders live dimension tag above vector strokes
     */
    fun renderMeasureTag(canvas: Canvas, stroke: DrawingStroke) {
        val points = stroke.points
        if (points.size < 2) return

        var totalLen = 0f
        for (i in 0 until points.size - 1) {
            val dx = points[i + 1].x - points[i].x
            val dy = points[i + 1].y - points[i].y
            totalLen += Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
        }
        val midPt = points[points.size / 2]
        val text = String.format("%.1f pts", totalLen)

        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = 22f
            typeface = android.graphics.Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
        }
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = Color.argb(220, 15, 17, 21) // Ink900 dark glass
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            isAntiAlias = true
            color = Color.argb(180, 56, 189, 248) // A2 Sky Blue
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val padX = 12f
        val padY = 6f
        val rect = RectF(
            midPt.x - textBounds.width() / 2f - padX,
            midPt.y - textBounds.height() - padY * 2 - 10f,
            midPt.x + textBounds.width() / 2f + padX,
            midPt.y - 10f
        )
        canvas.drawRoundRect(rect, 8f, 8f, bgPaint)
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)
        canvas.drawText(text, midPt.x, rect.bottom - padY - 2f, textPaint)
    }

    /**
     * Concepts Precision Grids (Dot, Square, Isometric)
     */
    fun renderGrid(
        canvas: Canvas,
        docWidth: Float,
        docHeight: Float,
        gridType: GridType,
        paperPreset: CanvasPaperPreset
    ) {
        if (gridType == GridType.NO_GRID) return

        val isDark = paperPreset == CanvasPaperPreset.DARK_CINEMA || paperPreset == CanvasPaperPreset.BLUEPRINT
        val gridColor = if (paperPreset == CanvasPaperPreset.BLUEPRINT) {
            Color.argb(80, 255, 255, 255)
        } else if (isDark) {
            Color.argb(50, 255, 255, 255)
        } else {
            Color.argb(40, 0, 0, 0)
        }

        val step = 48f
        when (gridType) {
            GridType.DOT -> {
                val dotPaint = Paint().apply {
                    isAntiAlias = true
                    color = gridColor
                    style = Paint.Style.FILL
                }
                var y = step
                while (y < docHeight) {
                    var x = step
                    while (x < docWidth) {
                        canvas.drawCircle(x, y, 2.0f, dotPaint)
                        x += step
                    }
                    y += step
                }
            }
            GridType.SQUARE -> {
                val linePaint = Paint().apply {
                    isAntiAlias = true
                    color = gridColor
                    style = Paint.Style.STROKE
                    strokeWidth = 1.0f
                }
                var x = step
                while (x < docWidth) {
                    canvas.drawLine(x, 0f, x, docHeight, linePaint)
                    x += step
                }
                var y = step
                while (y < docHeight) {
                    canvas.drawLine(0f, y, docWidth, y, linePaint)
                    y += step
                }
            }
            GridType.ISOMETRIC -> {
                val isoPaint = Paint().apply {
                    isAntiAlias = true
                    color = gridColor
                    style = Paint.Style.STROKE
                    strokeWidth = 1.0f
                }
                var x = 0f
                while (x < docWidth) {
                    canvas.drawLine(x, 0f, x, docHeight, isoPaint)
                    x += step
                }
                val diagCount = ((docWidth + docHeight) / step).toInt()
                for (k in -diagCount..diagCount) {
                    val startX = k * step
                    canvas.drawLine(startX, 0f, startX + docHeight * 0.577f, docHeight, isoPaint)
                    canvas.drawLine(startX, 0f, startX - docHeight * 0.577f, docHeight, isoPaint)
                }
            }
            else -> {}
        }
    }

    /**
     * Concepts Interactive Guide Overlay (Line, Arc, Ellipse, Rectangle)
     */
    fun renderGuideOverlay(
        canvas: Canvas,
        docWidth: Float,
        docHeight: Float,
        guideType: GuideType
    ) {
        val guidePaint = Paint().apply {
            isAntiAlias = true
            color = Color.argb(190, 56, 189, 248) // Cyan guide line
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            pathEffect = DashPathEffect(floatArrayOf(14f, 8f), 0f)
        }
        val handlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.argb(255, 245, 158, 11) // Amber handle
            style = Paint.Style.FILL
        }
        val handleRing = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val cx = docWidth / 2f
        val cy = docHeight / 2f

        when (guideType) {
            GuideType.LINE -> {
                canvas.drawLine(cx - 300f, cy, cx + 300f, cy, guidePaint)
                canvas.drawCircle(cx - 300f, cy, 9f, handlePaint)
                canvas.drawCircle(cx - 300f, cy, 9f, handleRing)
                canvas.drawCircle(cx + 300f, cy, 9f, handlePaint)
                canvas.drawCircle(cx + 300f, cy, 9f, handleRing)
                canvas.drawCircle(cx, cy, 7f, handlePaint)
            }
            GuideType.ARC -> {
                val rect = RectF(cx - 240f, cy - 240f, cx + 240f, cy + 240f)
                canvas.drawArc(rect, 180f, 90f, false, guidePaint)
                canvas.drawCircle(cx - 240f, cy, 9f, handlePaint)
                canvas.drawCircle(cx - 240f, cy, 9f, handleRing)
                canvas.drawCircle(cx, cy - 240f, 9f, handlePaint)
                canvas.drawCircle(cx, cy - 240f, 9f, handleRing)
            }
            GuideType.ELLIPSE -> {
                val rect = RectF(cx - 280f, cy - 180f, cx + 280f, cy + 180f)
                canvas.drawOval(rect, guidePaint)
                canvas.drawCircle(cx - 280f, cy, 8f, handlePaint)
                canvas.drawCircle(cx + 280f, cy, 8f, handlePaint)
                canvas.drawCircle(cx, cy - 180f, 8f, handlePaint)
                canvas.drawCircle(cx, cy + 180f, 8f, handlePaint)
            }
            GuideType.RECT -> {
                val rect = RectF(cx - 250f, cy - 180f, cx + 250f, cy + 180f)
                canvas.drawRect(rect, guidePaint)
                canvas.drawCircle(rect.left, rect.top, 8f, handlePaint)
                canvas.drawCircle(rect.right, rect.top, 8f, handlePaint)
                canvas.drawCircle(rect.right, rect.bottom, 8f, handlePaint)
                canvas.drawCircle(rect.left, rect.bottom, 8f, handlePaint)
            }
        }
    }
}
