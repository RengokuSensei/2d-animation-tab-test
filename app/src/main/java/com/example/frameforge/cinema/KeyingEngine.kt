package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object KeyingEngine {

    /**
     * Executes industry-standard Chroma Keying, Matte Extraction, Despill, and Edge Refinement.
     * Generates a clean alpha-keyed Bitmap.
     */
    fun processChromaKey(
        sourceBitmap: Bitmap,
        config: ChromaKeyConfig
    ): Bitmap {
        if (!config.isEnabled) return sourceBitmap

        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        sourceBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val keyR = Color.red(config.keyColorArgb)
        val keyG = Color.green(config.keyColorArgb)
        val keyB = Color.blue(config.keyColorArgb)

        val sim = config.similarity.coerceIn(0.01f, 1.0f)
        val smooth = config.smoothness.coerceIn(0.001f, 1.0f)
        val despill = config.despillIntensity.coerceIn(0.0f, 1.0f)

        // 1. Compute per-pixel alpha matte & despill
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            val a = (color shr 24) and 0xFF

            if (a == 0) continue

            // Normalized Euclidean distance in RGB color cube (max distance ~ 441.67)
            val dR = (r - keyR).toFloat()
            val dG = (g - keyG).toFloat()
            val dB = (b - keyB).toFloat()
            val dist = sqrt(dR * dR + dG * dG + dB * dB) / 441.67f

            // Smoothstep alpha calculation
            val rawAlpha: Float = when {
                dist < sim -> 0f
                dist >= sim + smooth -> 1f
                else -> {
                    val t = (dist - sim) / smooth
                    t * t * (3f - 2f * t) // Hermite smoothstep
                }
            }

            // Shadow preservation: retain subtle contact shadows under feet/props
            val luma = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f
            var finalAlpha = rawAlpha
            if (config.shadowPreservation > 0f && rawAlpha < 0.9f) {
                val shadowAlpha = (1f - luma) * config.shadowPreservation
                finalAlpha = max(rawAlpha, shadowAlpha.coerceIn(0f, 1f))
            }

            // Despill suppression (replaces green/blue tint on hair/edges with neutral adjacent tone)
            var cleanR = r
            var cleanG = g
            var cleanB = b

            if (config.keyType == ChromaKeyType.GREEN_SCREEN || (keyG > keyR && keyG > keyB)) {
                val limit = when (config.despillAlgorithm) {
                    DespillAlgorithm.AVERAGE -> (r + b) / 2
                    DespillAlgorithm.MAX_COLOR -> max(r, b)
                    DespillAlgorithm.WEIGHTED -> ((0.3f * r + 0.7f * b)).toInt()
                }
                if (g > limit) {
                    cleanG = (g - despill * (g - limit)).toInt().coerceIn(0, 255)
                }
            } else if (config.keyType == ChromaKeyType.BLUE_SCREEN || (keyB > keyR && keyB > keyG)) {
                val limit = when (config.despillAlgorithm) {
                    DespillAlgorithm.AVERAGE -> (r + g) / 2
                    DespillAlgorithm.MAX_COLOR -> max(r, g)
                    DespillAlgorithm.WEIGHTED -> ((0.7f * r + 0.3f * g)).toInt()
                }
                if (b > limit) {
                    cleanB = (b - despill * (b - limit)).toInt().coerceIn(0, 255)
                }
            }

            val outAlphaInt = (finalAlpha * a).toInt().coerceIn(0, 255)

            // Diagnostic mode: Show Matte Only (B&W Alpha inspection view)
            if (config.showMatteOnly) {
                pixels[i] = (0xFF shl 24) or (outAlphaInt shl 16) or (outAlphaInt shl 8) or outAlphaInt
            } else {
                pixels[i] = (outAlphaInt shl 24) or (cleanR shl 16) or (cleanG shl 8) or cleanB
            }
        }

        outputBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return outputBitmap
    }
}
