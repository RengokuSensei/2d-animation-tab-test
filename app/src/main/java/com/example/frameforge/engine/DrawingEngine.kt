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
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = stroke.size * 1.2f
                paint.alpha = (paint.alpha * 0.35f).toInt().coerceIn(1, 255)
                val path = buildSmoothPath(points)
                canvas.drawPath(path, paint)
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
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(1f, stroke.size * 0.75f)
                val path = buildSmoothPath(points)
                canvas.drawPath(path, paint)
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

        // Previous frames (tinted coral red)
        for (offset in 1..config.prevFramesCount) {
            val prevIdx = currentFrame - offset
            if (prevIdx < 0) continue
            val distRatio = (config.prevFramesCount - offset + 1).toFloat() / config.prevFramesCount
            val alpha = (config.opacity * distRatio * 255).toInt().coerceIn(10, 255)

            drawGhostFrame(canvas, width, height, prevIdx, activeLayers, cels, config.prevTintArgb, alpha, currentFrame, shiftAndTrace)
        }

        // Future frames (tinted green)
        for (offset in 1..config.nextFramesCount) {
            val nextIdx = currentFrame + offset
            if (nextIdx >= totalFrames) continue
            val distRatio = (config.nextFramesCount - offset + 1).toFloat() / config.nextFramesCount
            val alpha = (config.opacity * distRatio * 255).toInt().coerceIn(10, 255)

            drawGhostFrame(canvas, width, height, nextIdx, activeLayers, cels, config.nextTintArgb, alpha, currentFrame, shiftAndTrace)
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

        // Shift and Trace (Light Table / Out-of-Pegs) transformation
        val shouldShift = shiftAndTrace != null && shiftAndTrace.isActive && (frameIndex == currentFrame + shiftAndTrace.targetFrameOffset)
        if (shouldShift) {
            canvas.save()
            val cx = width / 2f
            val cy = height / 2f
            canvas.translate(cx + shiftAndTrace.offsetX, cy + shiftAndTrace.offsetY)
            canvas.rotate(shiftAndTrace.rotationDeg)
            canvas.scale(shiftAndTrace.scale, shiftAndTrace.scale)
            canvas.translate(-cx, -cy)
        }

        for (layer in layers) {
            val cel = frameCels.find { it.layerId == layer.id } ?: continue
            for (stroke in cel.strokes) {
                if (stroke.points.isEmpty()) continue
                ghostPaint.strokeWidth = stroke.size
                val path = buildSmoothPath(stroke.points)
                canvas.drawPath(path, ghostPaint)
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
}
