package com.example.frameforge.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object PdfEngine {

    /**
     * Parses human-readable page ranges (e.g. "1-3, 5, 8-10") into 0-based page indices,
     * clamped strictly within 0 until totalPages.
     */
    fun parsePageRange(rangeStr: String, totalPages: Int): Set<Int> {
        if (totalPages <= 0 || rangeStr.isBlank()) return emptySet()

        val resultSet = mutableSetOf<Int>()
        val parts = rangeStr.split(",")

        for (rawPart in parts) {
            val part = rawPart.trim()
            if (part.isEmpty()) continue

            if (part.contains("-")) {
                val subParts = part.split("-").map { it.trim() }
                if (subParts.size == 2) {
                    val start = subParts[0].toIntOrNull()
                    val end = subParts[1].toIntOrNull()
                    if (start != null && end != null) {
                        val minVal = min(start, end)
                        val maxVal = max(start, end)
                        for (p in minVal..maxVal) {
                            val zeroBased = p - 1
                            if (zeroBased in 0 until totalPages) {
                                resultSet.add(zeroBased)
                            }
                        }
                    }
                }
            } else {
                val page = part.toIntOrNull()
                if (page != null) {
                    val zeroBased = page - 1
                    if (zeroBased in 0 until totalPages) {
                        resultSet.add(zeroBased)
                    }
                }
            }
        }

        return resultSet.toSortedSet()
    }

    /**
     * Extracts pure white/light paper background into transparent alpha.
     * Keeps dark ink and text lines sharp and anti-aliased.
     * If invert is true, transforms dark ink on white into crisp white ink on transparent background.
     */
    fun extractWhiteToAlpha(
        source: Bitmap,
        tolerance: Float = 0.12f,
        invert: Boolean = false
    ): Bitmap {
        val width = source.width
        val height = source.height
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val thresholdLuma = 255f * (1.0f - tolerance)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            if (a == 0) continue

            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            // Standard perceptual luminance
            val luma = 0.299f * r + 0.587f * g + 0.114f * b

            if (luma >= thresholdLuma) {
                // Smooth quadratic transition to transparent for near-white paper
                val fade = ((255f - luma) / (255f - thresholdLuma)).coerceIn(0f, 1f)
                val newAlpha = (a * fade * fade).toInt()
                if (invert) {
                    // Invert ink colors (dark becomes bright white/chalkboard)
                    pixels[i] = Color.argb(newAlpha, 255 - r, 255 - g, 255 - b)
                } else {
                    pixels[i] = Color.argb(newAlpha, r, g, b)
                }
            } else {
                // Dark ink / colored drawing pixel: preserve or invert
                if (invert) {
                    pixels[i] = Color.argb(a, 255 - r, 255 - g, 255 - b)
                }
            }
        }

        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    /**
     * Stitches multiple rendered page bitmaps into a continuous vertical strip.
     */
    fun stitchPagesVertically(bitmaps: List<Bitmap>, maxWidth: Int = 1920): Bitmap {
        if (bitmaps.isEmpty()) {
            return Bitmap.createBitmap(maxWidth, 1080, Bitmap.Config.ARGB_8888)
        }
        if (bitmaps.size == 1) {
            return bitmaps[0]
        }

        var totalHeight = 0
        val targetWidth = maxWidth
        val scaledHeights = mutableListOf<Int>()

        for (bmp in bitmaps) {
            val scale = targetWidth.toFloat() / bmp.width.coerceAtLeast(1)
            val h = (bmp.height * scale).toInt().coerceAtLeast(1)
            scaledHeights.add(h)
            totalHeight += h
        }

        // Limit maximum height to avoid excessive bitmap size (cap around 8192px)
        val cappedHeight = totalHeight.coerceAtMost(8192)
        val result = Bitmap.createBitmap(targetWidth, cappedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        val heightRatio = if (totalHeight > 8192) 8192f / totalHeight else 1.0f
        var currentY = 0f

        for (i in bitmaps.indices) {
            val bmp = bitmaps[i]
            val h = scaledHeights[i] * heightRatio
            val destRect = Rect(0, currentY.toInt(), targetWidth, (currentY + h).toInt())
            canvas.drawBitmap(bmp, null, destRect, paint)

            // Subtle divider line between pages
            if (i < bitmaps.size - 1) {
                val linePaint = Paint().apply {
                    color = Color.parseColor("#330A84FF")
                    strokeWidth = 3f
                }
                canvas.drawLine(0f, currentY + h, targetWidth.toFloat(), currentY + h, linePaint)
            }
            currentY += h
        }

        return result
    }

    /**
     * Generates a rich 8-page sample PDF file for instant testing on tablets and emulators.
     */
    fun generateSamplePdfFile(context: Context): File {
        val outputFile = File(context.cacheDir, "sample_storyboard_doc.pdf")
        if (outputFile.exists() && outputFile.length() > 5000) {
            return outputFile
        }

        val document = PdfDocument()
        val pageW = 1080
        val pageH = 1440

        // Common paints
        val titlePaint = Paint().apply {
            color = Color.rgb(20, 30, 50)
            textSize = 48f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(80, 95, 120)
            textSize = 24f
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 50, 70)
            textSize = 20f
            isAntiAlias = true
        }
        val strokePaint = Paint().apply {
            color = Color.rgb(10, 132, 255) // Procreate Blue
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        val gridPaint = Paint().apply {
            color = Color.rgb(225, 230, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        // --- PAGE 1: STORYBOARD CONCEPT ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 1).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            // Header Banner
            val bannerPaint = Paint().apply { color = Color.rgb(240, 245, 255) }
            c.drawRect(0f, 0f, pageW.toFloat(), 200f, bannerPaint)
            c.drawText("FRAMEFORGE STUDIO STORYBOARD", 60f, 90f, titlePaint)
            c.drawText("Episode 1: The Quantum Leap — Production Animatic Script", 60f, 140f, subPaint)

            // Storyboard Panels
            drawStoryboardPanel(c, 60f, 240f, 440f, 320f, "PANEL 1A: Establishing Shot", "Camera pans over neon futuristic skyline")
            drawStoryboardPanel(c, 560f, 240f, 440f, 320f, "PANEL 1B: Character Reveal", "Hero steps into rain-soaked alley")
            drawStoryboardPanel(c, 60f, 600f, 440f, 320f, "PANEL 1C: Dynamic Action", "Stylus power surge and transformation")
            drawStoryboardPanel(c, 560f, 600f, 440f, 320f, "PANEL 1D: Cliffhanger Look", "Hero turns toward viewer with determined focus")

            // Director Notes
            val boxPaint = Paint().apply {
                color = Color.rgb(248, 250, 252)
                style = Paint.Style.FILL
            }
            c.drawRoundRect(60f, 960f, 1020f, 1360f, 16f, 16f, boxPaint)
            c.drawText("DIRECTOR PRODUCTION NOTES & TIMING", 90f, 1020f, titlePaint.apply { textSize = 28f })
            c.drawText("• Target Frame Rate: 24 FPS with 3s hold per keyframe sequence.", 90f, 1070f, bodyPaint)
            c.drawText("• Onion skinning recommended with 2 previous and 2 next frames.", 90f, 1110f, bodyPaint)
            c.drawText("• Use Smart Highlighter brush to annotate motion arcs directly on ink layer.", 90f, 1150f, bodyPaint)
            c.drawText("• Camera Move: Continuous vertical scroll tracking hero's ascent in Act II.", 90f, 1190f, bodyPaint)

            document.finishPage(page)
        }

        // --- PAGE 2: CHARACTER MODEL SHEET ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 2).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("CHARACTER MODEL SHEET & PROPORTIONS", 60f, 90f, titlePaint)
            c.drawText("Hero Protagonist 'Nova' — 8-Head Proportions & Turnaround", 60f, 140f, subPaint)

            // Draw turnaround height guidelines
            for (h in 0..8) {
                val y = 240f + h * 100f
                c.drawLine(60f, y, 1020f, y, gridPaint)
                c.drawText("Head $h", 70f, y - 8f, subPaint.apply { textSize = 16f })
            }

            // Draw stylized character silhouette turnaround
            drawTurnaroundFigure(c, 240f, 240f, "Front View")
            drawTurnaroundFigure(c, 540f, 240f, "3/4 View")
            drawTurnaroundFigure(c, 840f, 240f, "Side Profile")

            c.drawText("Rigging Specs: 14 IK Joints with root bone at pelvis.", 60f, 1120f, bodyPaint)
            c.drawText("Keyframe deform: Use Transform Cage Warp 3x3 for dynamic cape physics.", 60f, 1160f, bodyPaint)
            document.finishPage(page)
        }

        // --- PAGE 3: ARCHITECTURAL BLUEPRINT ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 3).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("ISOMETRIC CITY BLUEPRINT & CAMERA PATH", 60f, 90f, titlePaint)
            c.drawText("Environment Track 01 — Continuous Camera Scroll Layout", 60f, 140f, subPaint)

            // Blueprint grid
            for (x in 60..1020 step 40) {
                c.drawLine(x.toFloat(), 200f, x.toFloat(), 1300f, gridPaint)
            }
            for (y in 200..1300 step 40) {
                c.drawLine(60f, y.toFloat(), 1020f, y.toFloat(), gridPaint)
            }

            // Isometric buildings & motion arrow
            val bldPaint = Paint().apply {
                color = Color.rgb(20, 80, 160)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            c.drawRect(180f, 400f, 420f, 800f, bldPaint)
            c.drawRect(540f, 320f, 880f, 720f, bldPaint)
            c.drawRect(300f, 880f, 700f, 1200f, bldPaint)

            // Flight path trajectory in cyan
            val pathPaint = Paint().apply {
                color = Color.rgb(0, 229, 255)
                style = Paint.Style.STROKE
                strokeWidth = 5f
                isAntiAlias = true
            }
            c.drawLine(240f, 1150f, 480f, 650f, pathPaint)
            c.drawLine(480f, 650f, 800f, 400f, pathPaint)
            c.drawCircle(800f, 400f, 12f, pathPaint.apply { style = Paint.Style.FILL })
            c.drawText("Hero Camera Target [Keyframe 48]", 600f, 380f, subPaint.apply { textSize = 18f })

            document.finishPage(page)
        }

        // --- PAGE 4: ACTION STORYBOARD SEQUENCES ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 4).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("ACTION SEQUENCE 02: THE ROOF CHASE", 60f, 90f, titlePaint)
            c.drawText("Keyframe Beats: Contact, Squash, Stretch & Apex Jump", 60f, 140f, subPaint)

            drawActionFrame(c, 60f, 220f, "Frame 1: Anticipation (Hold 6f)")
            drawActionFrame(c, 60f, 480f, "Frame 7: Launch & Kinetic Blur")
            drawActionFrame(c, 60f, 740f, "Frame 14: Mid-Air Peak Pose")
            drawActionFrame(c, 60f, 1000f, "Frame 21: Heavy Landing & Recovery")

            document.finishPage(page)
        }

        // --- PAGE 5: TYPOGRAPHY & DIALOGUE SCRIPT ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 5).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("CINEMATIC SCRIPT & DIALOGUE CALLOUTS", 60f, 90f, titlePaint)
            c.drawText("Scene 14 — Audio Track Voiceover Alignment", 60f, 140f, subPaint)

            drawScriptBox(c, 60f, 220f, "NOVA (V.O.)", "If we don't breach the mainframe before sunrise, the whole system locks down forever.", "Timecode 00:01:12")
            drawScriptBox(c, 60f, 460f, "KAI (OVER COMMS)", "I see the security drones incoming on sector four! Nova, take the rooftop!", "Timecode 00:03:05")
            drawScriptBox(c, 60f, 700f, "SFX: ENERGY BLADE ACTIVATION", "[High-frequency electrical hum swelling into an impact resonance]", "Audio Layer 2")
            drawScriptBox(c, 60f, 940f, "NOVA", "Locked and loaded. Initiating jump sequence in three... two... one!", "Timecode 00:06:20")

            document.finishPage(page)
        }

        // --- PAGE 6: MECHA RIG & SCHEMATIC ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 6).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("MECHA MECHANICAL RIG & GEAR JOINTS", 60f, 90f, titlePaint)
            c.drawText("Sub-Assembly Diagram for Rigging & IK Constraints", 60f, 140f, subPaint)

            // Gear schematics
            val mechPaint = Paint().apply {
                color = Color.rgb(40, 60, 90)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            c.drawCircle(540f, 500f, 180f, mechPaint)
            c.drawCircle(540f, 500f, 120f, mechPaint)
            c.drawCircle(540f, 500f, 50f, mechPaint)
            for (ang in 0..360 step 30) {
                val rad = Math.toRadians(ang.toDouble())
                val x1 = (540f + 160f * Math.cos(rad)).toFloat()
                val y1 = (500f + 160f * Math.sin(rad)).toFloat()
                val x2 = (540f + 200f * Math.cos(rad)).toFloat()
                val y2 = (500f + 200f * Math.sin(rad)).toFloat()
                c.drawLine(x1, y1, x2, y2, mechPaint)
            }

            c.drawText("Rotational Limit: -45° to +120° IK dampening", 280f, 740f, bodyPaint)
            c.drawText("Piston Actuator Stroke: 320mm pneumatic return", 280f, 780f, bodyPaint)

            document.finishPage(page)
        }

        // --- PAGE 7: COLOR SCRIPT & MOODBOARD ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 7).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("LIGHTING COLOR SCRIPT & MOODBOARD", 60f, 90f, titlePaint)
            c.drawText("Color Palette Progression Across 3 Story Acts", 60f, 140f, subPaint)

            // 3 Color Palette Swatches
            drawPaletteRow(c, 60f, 240f, "ACT I: Cyber Blue Night", listOf(0xFF0F172A.toInt(), 0xFF1E293B.toInt(), 0xFF0284C7.toInt(), 0xFF38BDF8.toInt()))
            drawPaletteRow(c, 60f, 540f, "ACT II: Neon Infiltration Amber", listOf(0xFF451A03.toInt(), 0xFFB45309.toInt(), 0xFFF59E0B.toInt(), 0xFFFDE68A.toInt()))
            drawPaletteRow(c, 60f, 840f, "ACT III: Dawn Victory Crimson & Gold", listOf(0xFF881337.toInt(), 0xFFE11D48.toInt(), 0xFFFB7185.toInt(), 0xFFFEF08A.toInt()))

            document.finishPage(page)
        }

        // --- PAGE 8: CREDITS & PRODUCTION NOTES ---
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageW, pageH, 8).create()
            val page = document.startPage(pageInfo)
            val c = page.canvas
            c.drawColor(Color.WHITE)

            c.drawText("FRAMEFORGE STUDIO — PRODUCTION METRICS", 60f, 90f, titlePaint)
            c.drawText("Project Wrap & Animation Studio Export Configuration", 60f, 140f, subPaint)

            val statCardPaint = Paint().apply {
                color = Color.rgb(245, 248, 255)
                style = Paint.Style.FILL
            }
            c.drawRoundRect(60f, 220f, 1020f, 1200f, 20f, 20f, statCardPaint)

            c.drawText("PRODUCTION PIPELINE CHECKLIST", 100f, 290f, titlePaint.apply { textSize = 32f })
            val checkmark = "✓ "
            val checklist = listOf(
                "Multi-Page PDF Import Studio with PdfRenderer hardware acceleration",
                "Sequential Timeline Cels with Hold Duration & Scrubbing",
                "Independent Scene Layers with Procreate Transform Cage",
                "Continuous Stitched Document Strip with smooth camera scroll keyframes",
                "White Paper Background removal (Luminance to Alpha)",
                "Dark Mode / Chalkboard color inversion pass",
                "Linked Animation Track for drawing/highlighting over PDF pages",
                "Hardware-accelerated 4K MP4 / GIF video compositing"
            )

            var yPos = 360f
            for (item in checklist) {
                c.drawText(checkmark + item, 100f, yPos, bodyPaint.apply { textSize = 22f })
                yPos += 75f
            }

            document.finishPage(page)
        }

        try {
            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
        } finally {
            document.close()
        }

        return outputFile
    }

    private fun drawStoryboardPanel(canvas: Canvas, x: Float, y: Float, w: Float, h: Float, title: String, subtitle: String) {
        val borderPaint = Paint().apply {
            color = Color.rgb(180, 195, 215)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        val bgPaint = Paint().apply {
            color = Color.rgb(250, 252, 255)
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + w, y + h, bgPaint)
        canvas.drawRect(x, y, x + w, y + h, borderPaint)

        // Draw simple camera focus crosshairs
        val cx = x + w / 2f
        val cy = y + h / 2f
        val chPaint = Paint().apply {
            color = Color.rgb(200, 215, 235)
            strokeWidth = 1f
        }
        canvas.drawLine(cx - 20f, cy, cx + 20f, cy, chPaint)
        canvas.drawLine(cx, cy - 20f, cx, cy + 20f, chPaint)

        val tPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val sPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 14f
            isAntiAlias = true
        }
        canvas.drawText(title, x + 14f, y + h - 36f, tPaint)
        canvas.drawText(subtitle, x + 14f, y + h - 14f, sPaint)
    }

    private fun drawTurnaroundFigure(canvas: Canvas, cx: Float, topY: Float, label: String) {
        val paint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        // Head
        canvas.drawCircle(cx, topY + 50f, 35f, paint)
        // Torso
        canvas.drawLine(cx, topY + 85f, cx, topY + 350f, paint)
        // Arms
        canvas.drawLine(cx - 60f, topY + 160f, cx + 60f, topY + 160f, paint)
        // Legs
        canvas.drawLine(cx, topY + 350f, cx - 40f, topY + 680f, paint)
        canvas.drawLine(cx, topY + 350f, cx + 40f, topY + 680f, paint)

        val textPaint = Paint().apply {
            color = Color.rgb(10, 132, 255)
            textSize = 20f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(label, cx, topY + 740f, textPaint)
    }

    private fun drawActionFrame(canvas: Canvas, x: Float, y: Float, title: String) {
        val rectPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(x, y, x + 960f, y + 210f, 12f, 12f, rectPaint)
        canvas.drawRoundRect(x, y, x + 960f, y + 210f, 12f, 12f, borderPaint)

        val tPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title, x + 24f, y + 42f, tPaint)

        // Draw horizontal motion speedlines
        val linePaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            strokeWidth = 2f
        }
        for (i in 0..4) {
            val ly = y + 80f + i * 25f
            canvas.drawLine(x + 24f, ly, x + 300f + i * 120f, ly, linePaint)
        }
    }

    private fun drawScriptBox(canvas: Canvas, x: Float, y: Float, character: String, dialogue: String, timing: String) {
        val bgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(x, y, x + 960f, y + 190f, 12f, 12f, bgPaint)
        canvas.drawRoundRect(x, y, x + 960f, y + 190f, 12f, 12f, borderPaint)

        val cPaint = Paint().apply {
            color = Color.rgb(10, 132, 255)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val dPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 18f
            isAntiAlias = true
        }
        val tPaint = Paint().apply {
            color = Color.rgb(148, 163, 184)
            textSize = 14f
            isAntiAlias = true
        }
        canvas.drawText(character, x + 24f, y + 44f, cPaint)
        canvas.drawText(timing, x + 800f, y + 44f, tPaint)
        canvas.drawText(dialogue, x + 24f, y + 90f, dPaint)
    }

    private fun drawPaletteRow(canvas: Canvas, x: Float, y: Float, title: String, colors: List<Int>) {
        val tPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title, x, y, tPaint)

        var cx = x
        for (colorInt in colors) {
            val cPaint = Paint().apply {
                color = colorInt
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cx, y + 20f, cx + 220f, y + 180f, 14f, 14f, cPaint)
            cx += 240f
        }
    }

    /**
     * Memory cache for rendered PDF page bitmaps to prevent OutOfMemory errors on large PDFs.
     */
    object BitmapCache {
        private const val MAX_CACHE_SIZE_BYTES = 64 * 1024 * 1024 // 64 MB
        private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_SIZE_BYTES) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount
            }
        }

        fun get(key: String): Bitmap? = cache.get(key)

        fun put(key: String, bitmap: Bitmap) {
            cache.put(key, bitmap)
        }

        fun clear() {
            cache.evictAll()
        }
    }
}
