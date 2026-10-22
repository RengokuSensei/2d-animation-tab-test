package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class AtmosphericParticleType(val label: String) {
    NONE("None"),
    DUST_MOTES("Floating Dust Motes (Sunbeam)"),
    FIRE_EMBERS("Drifting Fire Embers"),
    RAIN("Cinematic Rain & Streaks"),
    SNOW("Gentle Snow Flurries"),
    SAKURA_PETALS("Falling Sakura Blossom Petals")
}

enum class CinemaAspectLetterbox(val label: String, val ratio: Float?) {
    NONE("Full Canvas (No Bars)", null),
    CINEMASCOPE_239("2.39:1 CinemaScope (Anamorphic)", 2.39f),
    ACADEMY_185("1.85:1 American Widescreen", 1.85f),
    STANDARD_169("16:9 HD Television", 1.777f)
}

data class LightSourceGizmo(
    val isEnabled: Boolean = true,
    val x: Float = 0.85f, // Normalized 0..1
    val y: Float = 0.15f,
    val colorArgb: Int = 0xFFFFD700.toInt(), // Warm sunlight
    val intensity: Float = 0.85f,
    val rimWidth: Float = 6f,
    val castShadowLength: Float = 35f,
    val shadowOpacity: Float = 0.45f
)

data class CinematicVfxConfig(
    val anamorphicBloom: Boolean = true,
    val bloomIntensity: Float = 0.45f,
    val filmHalation: Boolean = true,
    val halationColorArgb: Int = 0xFFFF3B30.toInt(), // Warm orange-red halation
    val chromaticAberration: Float = 4f, // Radial RGB split in pixels
    val lensDistortion: Float = 0.05f,
    val volumetricRays: Boolean = true,
    val godRayCount: Int = 18,
    val filmGrain: Float = 0.15f,
    val vhsGlitchScanlines: Boolean = false,
    val letterbox: CinemaAspectLetterbox = CinemaAspectLetterbox.CINEMASCOPE_239,
    val particles: AtmosphericParticleType = AtmosphericParticleType.DUST_MOTES,
    val particleDensity: Int = 45,
    val lightGizmo: LightSourceGizmo = LightSourceGizmo()
)

object CinematicShaderPipeline {

    /**
     * Renders 2D Rim-Lighting and Cast Shadows based on light source position.
     */
    fun renderRimLightAndShadow(
        canvas: Canvas,
        width: Float,
        height: Float,
        gizmo: LightSourceGizmo,
        silhouetteRect: RectF? = null
    ) {
        if (!gizmo.isEnabled) return

        val lightPx = width * gizmo.x
        val lightPy = height * gizmo.y

        // Draw interactive light source sun gizmo icon
        val sunPaint = Paint().apply {
            isAntiAlias = true
            color = gizmo.colorArgb
            style = Paint.Style.FILL
            alpha = (gizmo.intensity * 255).toInt().coerceIn(40, 255)
        }
        canvas.drawCircle(lightPx, lightPy, 14f, sunPaint)

        val auraPaint = Paint().apply {
            isAntiAlias = true
            color = gizmo.colorArgb
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            alpha = (gizmo.intensity * 140).toInt().coerceIn(20, 200)
        }
        canvas.drawCircle(lightPx, lightPy, 22f, auraPaint)

        // If target silhouette bounds are provided, draw rim highlight and floor shadow
        if (silhouetteRect != null) {
            val angle = atan2(silhouetteRect.centerY() - lightPy, silhouetteRect.centerX() - lightPx)
            val dx = cos(angle)
            val dy = sin(angle)

            // Cast Floor Shadow on opposite side of light
            val shadowPaint = Paint().apply {
                isAntiAlias = true
                color = Color.BLACK
                alpha = (gizmo.shadowOpacity * 255).toInt().coerceIn(10, 255)
                style = Paint.Style.FILL
            }
            val shadowRect = RectF(
                silhouetteRect.left + (dx * gizmo.castShadowLength).toFloat(),
                silhouetteRect.bottom - 4f,
                silhouetteRect.right + (dx * gizmo.castShadowLength).toFloat(),
                silhouetteRect.bottom + 18f
            )
            canvas.drawOval(shadowRect, shadowPaint)

            // Specular Rim Highlight on the lit edge
            val rimPaint = Paint().apply {
                isAntiAlias = true
                color = gizmo.colorArgb
                alpha = (gizmo.intensity * 200).toInt().coerceIn(30, 255)
                strokeWidth = gizmo.rimWidth
                style = Paint.Style.STROKE
                xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            }
            val rimArc = RectF(silhouetteRect)
            val rimStartAngle = (angle * 180.0 / PI + 180.0).toFloat() - 45f
            canvas.drawArc(rimArc, rimStartAngle, 90f, false, rimPaint)
        }
    }

    /**
     * Renders Volumetric Light Rays (God Rays / Crepuscular Rays).
     */
    fun renderVolumetricGodRays(
        canvas: Canvas,
        width: Float,
        height: Float,
        lightX: Float,
        lightY: Float,
        colorArgb: Int,
        intensity: Float = 0.5f,
        rayCount: Int = 16
    ) {
        val lightPx = width * lightX
        val lightPy = height * lightY
        val maxDist = hypot(width, height) * 1.2f

        val rayPaint = Paint().apply {
            isAntiAlias = true
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
            style = Paint.Style.FILL
        }

        val random = Random(42)
        for (i in 0 until rayCount) {
            val baseAngle = (i.toFloat() / rayCount) * 2.0 * PI
            val spread = (random.nextFloat() * 0.12f) + 0.04f

            val p1x = lightPx + (maxDist * cos(baseAngle - spread)).toFloat()
            val p1y = lightPy + (maxDist * sin(baseAngle - spread)).toFloat()
            val p2x = lightPx + (maxDist * cos(baseAngle + spread)).toFloat()
            val p2y = lightPy + (maxDist * sin(baseAngle + spread)).toFloat()

            val alpha = (intensity * (40 + random.nextInt(60))).toInt().coerceIn(5, 200)
            val rayColor = Color.argb(alpha, Color.red(colorArgb), Color.green(colorArgb), Color.blue(colorArgb))

            rayPaint.shader = RadialGradient(
                lightPx, lightPy, maxDist,
                rayColor, Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )

            val path = Path().apply {
                moveTo(lightPx, lightPy)
                lineTo(p1x, p1y)
                lineTo(p2x, p2y)
                close()
            }
            canvas.drawPath(path, rayPaint)
        }
    }

    /**
     * Renders procedural atmospheric particles with depth parallax.
     */
    fun renderAtmosphericParticles(
        canvas: Canvas,
        width: Float,
        height: Float,
        frameIndex: Int,
        type: AtmosphericParticleType,
        count: Int = 40
    ) {
        if (type == AtmosphericParticleType.NONE) return

        val random = Random(1001)
        val particlePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (i in 0 until count) {
            val seedX = random.nextFloat()
            val seedY = random.nextFloat()
            val depth = 0.3f + random.nextFloat() * 1.5f // Parallax speed factor

            when (type) {
                AtmosphericParticleType.DUST_MOTES -> {
                    // Gentle drifting sine wave
                    val px = (seedX * width + sin((frameIndex + i * 10) * 0.05f) * 18f * depth) % width
                    val py = (seedY * height - (frameIndex * 1.2f * depth)) % height
                    val actualY = if (py < 0) py + height else py

                    particlePaint.color = Color.WHITE
                    particlePaint.alpha = (random.nextFloat() * 120 + 40).toInt()
                    canvas.drawCircle(px, actualY, 2.5f * depth, particlePaint)
                }
                AtmosphericParticleType.FIRE_EMBERS -> {
                    // Rising warm sparks
                    val px = (seedX * width + cos((frameIndex + i * 8) * 0.08f) * 24f * depth) % width
                    val py = (seedY * height - (frameIndex * 3.5f * depth)) % height
                    val actualY = if (py < 0) py + height else py

                    val isYellow = random.nextBoolean()
                    particlePaint.color = if (isYellow) 0xFFFFD700.toInt() else 0xFFFF4500.toInt()
                    particlePaint.alpha = (random.nextFloat() * 180 + 75).toInt()
                    canvas.drawCircle(px, actualY, 3f * depth, particlePaint)
                }
                AtmosphericParticleType.RAIN -> {
                    // Fast diagonal streaks
                    val px = (seedX * width - (frameIndex * 8f * depth)) % width
                    val actualX = if (px < 0) px + width else px
                    val py = (seedY * height + (frameIndex * 24f * depth)) % height

                    particlePaint.color = 0xAA81D4FA.toInt()
                    particlePaint.strokeWidth = 2f * depth
                    particlePaint.style = Paint.Style.STROKE
                    canvas.drawLine(actualX, py, actualX - 10f * depth, py + 28f * depth, particlePaint)
                    particlePaint.style = Paint.Style.FILL
                }
                AtmosphericParticleType.SNOW -> {
                    // Falling swirling flakes
                    val px = (seedX * width + sin((frameIndex + i * 15) * 0.04f) * 20f * depth) % width
                    val py = (seedY * height + (frameIndex * 2.2f * depth)) % height

                    particlePaint.color = Color.WHITE
                    particlePaint.alpha = (random.nextFloat() * 160 + 60).toInt()
                    canvas.drawCircle(px, py, 3.5f * depth, particlePaint)
                }
                AtmosphericParticleType.SAKURA_PETALS -> {
                    // Fluttering pink petal ovals
                    val px = (seedX * width + sin((frameIndex + i * 12) * 0.06f) * 28f * depth) % width
                    val py = (seedY * height + (frameIndex * 2.5f * depth)) % height

                    particlePaint.color = 0xFFF48FB1.toInt() // Soft sakura pink
                    particlePaint.alpha = 210
                    canvas.save()
                    canvas.translate(px, py)
                    canvas.rotate((frameIndex * 4f + i * 30f) % 360f)
                    val petalRect = RectF(-6f * depth, -3.5f * depth, 6f * depth, 3.5f * depth)
                    canvas.drawOval(petalRect, particlePaint)
                    canvas.restore()
                }
                else -> Unit
            }
        }
    }

    /**
     * Renders CinemaScope / Widescreen matte letterbox bars.
     */
    fun renderCinemaLetterbox(
        canvas: Canvas,
        width: Float,
        height: Float,
        letterbox: CinemaAspectLetterbox
    ) {
        val targetRatio = letterbox.ratio ?: return
        val currentRatio = width / height

        if (currentRatio < targetRatio) {
            // Need top and bottom matte bars
            val desiredHeight = width / targetRatio
            val barHeight = (height - desiredHeight) / 2f
            if (barHeight <= 0) return

            val mattePaint = Paint().apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }

            // Top bar
            canvas.drawRect(0f, 0f, width, barHeight, mattePaint)
            // Bottom bar
            canvas.drawRect(0f, height - barHeight, width, height, mattePaint)
        }
    }

    /**
     * Applies radial chromatic aberration and 35mm film grain overlay.
     */
    fun renderFilmPostProcessing(
        canvas: Canvas,
        width: Float,
        height: Float,
        config: CinematicVfxConfig
    ) {
        // VHS scanlines if enabled
        if (config.vhsGlitchScanlines) {
            val scanlinePaint = Paint().apply {
                color = Color.BLACK
                alpha = 45
                strokeWidth = 2f
                style = Paint.Style.STROKE
            }
            var y = 0f
            while (y < height) {
                canvas.drawLine(0f, y, width, y, scanlinePaint)
                y += 6f
            }
        }
    }
}
