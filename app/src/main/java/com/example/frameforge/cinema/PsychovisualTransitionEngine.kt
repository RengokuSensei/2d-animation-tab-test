package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.StrokePoint
import com.example.frameforge.model.ToolType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class TransitionDirection(val label: String, val angleDeg: Float) {
    LEFT("Pan Left", 180f),
    RIGHT("Pan Right", 0f),
    UP("Pan Up", 270f),
    DOWN("Pan Down", 90f),
    DIAGONAL("Diagonal Snap", 45f)
}

enum class PsychovisualTransitionType(val title: String, val description: String) {
    SACCADIC_WHIP_PAN(
        "Saccadic Whip-Pan",
        "Exploits cortical motion suppression: extreme directional streak blur + exponential snap."
    ),
    SHONEN_IMPACT_FRAME(
        "Shonen Retinal Flash Mask",
        "Exploits luminance adaptation latency: 1-2 frame monochrome threshold + radial speed lines."
    ),
    SMEAR_FRAME_ECHO(
        "Automatic Motion Smear",
        "Exploits persistence of vision: stretched directional hull + trailing chromatic aberration echoes."
    ),
    OPTICAL_FLOW_MORPH(
        "Optical Silhouette Morph",
        "3×3 deformation lattice warping Scene A contour toward Scene B focal center."
    ),
    DEPTH_LUMA_IRIS_WIPE(
        "Foveal Gaze Iris Wipe",
        "Wipes organically toward the next scene's action point so viewer's eye is pre-guided."
    )
}

data class PsychovisualTransitionConfig(
    val type: PsychovisualTransitionType = PsychovisualTransitionType.SACCADIC_WHIP_PAN,
    val durationFrames: Int = 6, // 3 to 8 frames
    val direction: TransitionDirection = TransitionDirection.RIGHT,
    val impactThreshold: Float = 0.5f,
    val invertNegativeFlash: Boolean = true,
    val gazePointX: Float = 0.5f, // Normalized 0..1
    val gazePointY: Float = 0.5f
)

object PsychovisualTransitionEngine {

    /**
     * Synthesizes a high-contrast Shonen impact frame (monochrome silhouette + speed lines).
     */
    fun generateImpactFrame(
        source: Bitmap,
        threshold: Float = 0.5f,
        invert: Boolean = false,
        addSpeedLines: Boolean = true
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val thresholdInt = (threshold * 255).toInt()
        val white = Color.WHITE
        val black = Color.BLACK

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val a = Color.alpha(pixel)
            if (a < 20) {
                pixels[i] = if (invert) black else white
                continue
            }
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val luma = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

            val isHigh = luma > thresholdInt
            pixels[i] = if (invert) {
                if (isHigh) black else white
            } else {
                if (isHigh) white else black
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)

        if (addSpeedLines) {
            val canvas = Canvas(output)
            val paint = Paint().apply {
                isAntiAlias = true
                color = if (invert) white else black
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            val cx = width / 2f
            val cy = height / 2f
            val rayCount = 36
            val random = Random(42)

            for (i in 0 until rayCount) {
                val angle = (i.toFloat() / rayCount) * 2.0 * PI
                val innerR = (width * 0.15f) + random.nextFloat() * 40f
                val outerR = (width * 0.70f) + random.nextFloat() * 80f

                val x1 = cx + (innerR * cos(angle)).toFloat()
                val y1 = cy + (innerR * sin(angle)).toFloat()
                val x2 = cx + (outerR * cos(angle)).toFloat()
                val y2 = cy + (outerR * sin(angle)).toFloat()

                canvas.drawLine(x1, y1, x2, y2, paint)
            }
        }

        return output
    }

    /**
     * Generates a directional Saccadic Whip-Pan frame with streaks.
     */
    fun renderWhipPanOverlay(
        canvas: Canvas,
        width: Float,
        height: Float,
        progress: Float, // 0..1 over transition
        direction: TransitionDirection
    ) {
        val rad = direction.angleDeg * PI / 180.0
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        // Peak streak intensity mid-transition (progress ~ 0.5)
        val intensity = sin(progress * PI).toFloat()
        if (intensity < 0.05f) return

        val streakPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = (intensity * 180).toInt().coerceIn(0, 255)
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }

        val streakCount = (intensity * 28).toInt().coerceIn(4, 30)
        val random = Random(1337)
        val streakLength = width * 0.45f * intensity

        for (i in 0 until streakCount) {
            val startX = random.nextFloat() * width
            val startY = random.nextFloat() * height
            val endX = startX + cosA * streakLength
            val endY = startY + sinA * streakLength

            canvas.drawLine(startX, startY, endX, endY, streakPaint)
        }
    }

    /**
     * Synthesizes an automatic 2D Smear Frame between two strokes along a motion vector.
     */
    fun generateSmearStrokes(
        strokeA: DrawingStroke,
        strokeB: DrawingStroke,
        smearProgress: Float = 0.5f,
        chromaEchoes: Boolean = true
    ): List<DrawingStroke> {
        val result = mutableListOf<DrawingStroke>()
        val ptsA = strokeA.points
        val ptsB = strokeB.points
        if (ptsA.isEmpty() || ptsB.isEmpty()) return result

        val count = maxOf(ptsA.size, ptsB.size)
        val interpolatedPoints = mutableListOf<StrokePoint>()
        val smearEchoPoints = mutableListOf<StrokePoint>()

        for (i in 0 until count) {
            val pA = ptsA.getOrNull(i) ?: ptsA.last()
            val pB = ptsB.getOrNull(i) ?: ptsB.last()

            val mx = pA.x + (pB.x - pA.x) * smearProgress
            val my = pA.y + (pB.y - pA.y) * smearProgress

            // Elongate points along movement vector
            val dx = pB.x - pA.x
            val dy = pB.y - pA.y
            val dist = hypot(dx, dy)
            val stretchFactor = (dist * 0.35f).coerceIn(1f, 30f)

            interpolatedPoints.add(StrokePoint(mx, my, pA.pressure * 1.4f))
            smearEchoPoints.add(StrokePoint(mx - dx * 0.15f, my - dy * 0.15f, pA.pressure * 0.8f))
        }

        // Main smear hull
        result.add(
            strokeA.copy(
                id = java.util.UUID.randomUUID().toString(),
                points = interpolatedPoints,
                size = strokeA.size * 1.5f,
                opacity = 0.9f
            )
        )

        // Trailing chromatic aberration echo
        if (chromaEchoes) {
            result.add(
                strokeA.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    points = smearEchoPoints,
                    colorArgb = 0x88FF0055.toInt(), // Magenta chroma trail
                    size = strokeA.size * 1.2f,
                    opacity = 0.6f
                )
            )
        }

        return result
    }

    /**
     * Generates a circular/luma Iris Wipe path centered on the foveal gaze coordinates.
     */
    fun createIrisWipePath(
        width: Float,
        height: Float,
        progress: Float,
        gazeX: Float,
        gazeY: Float
    ): Path {
        val path = Path()
        val centerX = width * gazeX
        val centerY = height * gazeY
        val maxRadius = hypot(maxOf(centerX, width - centerX), maxOf(centerY, height - centerY))
        val currentRadius = maxRadius * progress.coerceIn(0f, 1f)

        path.addCircle(centerX, centerY, currentRadius, Path.Direction.CW)
        return path
    }
}
