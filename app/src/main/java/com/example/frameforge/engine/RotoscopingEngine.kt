package com.example.frameforge.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import com.example.frameforge.model.VideoRotoscopingTrack
import kotlin.math.cos
import kotlin.math.sin

/**
 * Engine for 4K / 30-min Video & Audio Rotoscoping reference frames.
 * Renders high-fidelity reference motion frames and NLE Video PiP Stage.
 */
object RotoscopingEngine {

    // Cache of synthesized reference motion video frames
    private val frameCache = mutableMapOf<Int, Bitmap>()

    /**
     * Renders the Video Stage PiP or Underlay matching A2_studio.html:
     * - Discovers active clip on Track V1 matching playhead percent
     * - Decodes real video frame if customVideo is set
     * - Otherwise renders cinematic documentary B-roll with telemetry HUD and timecode
     * - Supports both floating PiP box and full canvas underlay
     */
    fun renderVideoStagePiP(
        canvas: Canvas,
        docWidth: Int,
        docHeight: Int,
        frameIndex: Int,
        totalFrames: Int,
        fps: Int,
        track: VideoRotoscopingTrack,
        context: Context? = null
    ) {
        if (!track.isEnabled || !track.pipTransform.isVisible) return

        val playPct = if (totalFrames > 1) {
            (frameIndex.toFloat() / (totalFrames - 1)) * 100f
        } else 0f

        val activeClip = track.clips.find { playPct >= it.startPct && playPct <= it.endPct }
            ?: if (track.selectedClipId != null) track.clips.find { it.id == track.selectedClipId }
            else track.clips.firstOrNull() ?: return

        val isUnderlay = track.pipTransform.isUnderlayMode
        val dstRect = if (isUnderlay) {
            RectF(0f, 0f, docWidth.toFloat(), docHeight.toFloat())
        } else {
            RectF(
                track.pipTransform.x,
                track.pipTransform.y,
                track.pipTransform.x + track.pipTransform.width,
                track.pipTransform.y + track.pipTransform.height
            )
        }

        canvas.save()

        // Clip to rounded rectangle
        val clipPath = Path().apply {
            addRoundRect(dstRect, 14f, 14f, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        var renderedRealFrame = false
        if (activeClip.isCustomVideo && activeClip.videoUri != null && context != null) {
            val videoBmp = VideoFrameCache.getFrame(context, activeClip.videoUri, frameIndex, fps)
            if (videoBmp != null && !videoBmp.isRecycled) {
                val paint = Paint().apply {
                    alpha = (track.opacity * 255).toInt().coerceIn(10, 255)
                    isFilterBitmap = true
                }
                val srcRect = Rect(0, 0, videoBmp.width, videoBmp.height)
                canvas.drawBitmap(videoBmp, srcRect, dstRect, paint)
                renderedRealFrame = true
            }
        }

        if (!renderedRealFrame) {
            // Procedural Cinematic Documentary B-Roll Frame (A2_studio.html specification)
            val bgPaint = Paint().apply {
                shader = android.graphics.LinearGradient(
                    dstRect.left, dstRect.top, dstRect.right, dstRect.bottom,
                    Color.rgb(15, 23, 42), // #0F172A
                    Color.rgb(49, 16, 66), // #311042
                    android.graphics.Shader.TileMode.CLAMP
                )
                alpha = (track.opacity * 255).toInt().coerceIn(10, 255)
            }
            canvas.drawRect(dstRect, bgPaint)

            // Animated Orbit & Celestial Telemetry
            val cx = dstRect.centerX()
            val cy = dstRect.centerY()
            val t = frameIndex * activeClip.speed * 0.45f

            // Orbit Ring
            val orbitPaint = Paint().apply {
                color = Color.argb(90, 137, 139, 255)
                strokeWidth = 2f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawCircle(cx + sin(t) * 20f, cy + cos(t) * 10f, dstRect.height() * 0.32f, orbitPaint)

            // Revolving Star / Planet
            val orbPaint = Paint().apply {
                color = Color.rgb(255, 191, 105) // #FFBF69
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val orbX = cx + cos(t) * (dstRect.width() * 0.28f)
            val orbY = cy + sin(t) * (dstRect.height() * 0.22f)
            canvas.drawCircle(orbX, orbY, 10f, orbPaint)
        }

        // Film Timecode & Telemetry HUD Overlay
        val hudTextPaint = Paint().apply {
            color = Color.argb(230, 255, 255, 255)
            textSize = if (isUnderlay) 24f else 14f
            isAntiAlias = true
            typeface = android.graphics.Typeface.MONOSPACE
        }

        val badgeText = "REC ● ${activeClip.title} [${activeClip.speed}x]"
        canvas.drawText(badgeText, dstRect.left + 14f, dstRect.top + if (isUnderlay) 36f else 24f, hudTextPaint)

        val timecode = String.format("SMPTE %02d:%02d:%02d", frameIndex / fps.coerceAtLeast(1), frameIndex % fps.coerceAtLeast(1), (frameIndex * 4) % 60)
        canvas.drawText(timecode, dstRect.left + 14f, dstRect.bottom - if (isUnderlay) 24f else 14f, hudTextPaint)

        canvas.restore()

        // Subtle glowing border around PiP box
        if (!isUnderlay) {
            val borderPaint = Paint().apply {
                color = activeClip.colorHex.toInt()
                strokeWidth = 2.5f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawRoundRect(dstRect, 14f, 14f, borderPaint)
        }
    }

    /**
     * Renders a reference rotoscoping video frame onto the drawing canvas
     */
    fun renderRotoscopingFrame(
        canvas: Canvas,
        docWidth: Int,
        docHeight: Int,
        frameIndex: Int,
        track: VideoRotoscopingTrack
    ) {
        if (!track.isEnabled || track.opacity <= 0.01f) return

        val videoFrameBmp = getOrGenerateReferenceFrame(frameIndex, docWidth, docHeight)
        val paint = Paint().apply {
            alpha = (track.opacity * 255).toInt().coerceIn(1, 255)
            isFilterBitmap = true
            isAntiAlias = true
        }

        val dstRect = Rect(0, 0, docWidth, docHeight)
        canvas.drawBitmap(videoFrameBmp, null, dstRect, paint)
    }

    /**
     * Generates or retrieves a procedural reference motion frame (Athletic Action / Running / Jumping)
     */
    fun getOrGenerateReferenceFrame(frameIndex: Int, width: Int, height: Int): Bitmap {
        val cached = frameCache[frameIndex]
        if (cached != null && cached.width == width && cached.height == height) {
            return cached
        }

        val bmp = Bitmap.createBitmap(width.coerceAtLeast(320), height.coerceAtLeast(240), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Subtle studio grid & reference background
        val bgPaint = Paint().apply { color = Color.rgb(240, 244, 248) }
        c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Ground shadow & reference grid line
        val gridPaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val groundY = height * 0.78f
        c.drawLine(0f, groundY, width.toFloat(), groundY, gridPaint)

        // Procedural animated rotoscoping athlete silhouette & key joints
        // Cycle over 24 frames
        val cycle = (frameIndex % 24) / 24.0
        val t = cycle * 2.0 * Math.PI

        val centerX = width * 0.5f + (sin(t) * width * 0.15f).toFloat()
        val bounceY = (sin(t * 2) * height * 0.04f).toFloat()
        val pelvisY = height * 0.52f + bounceY

        val bodyPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val limbPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            style = Paint.Style.STROKE
            strokeWidth = (width * 0.016f).coerceAtLeast(8f)
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }
        val jointPaint = Paint().apply {
            color = Color.rgb(14, 165, 233) // Cyan joint tracker
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Head
        val headX = centerX + (cos(t) * 12f).toFloat()
        val headY = pelvisY - height * 0.22f
        val headRadius = width * 0.038f
        c.drawCircle(headX, headY, headRadius, bodyPaint)

        // Torso
        val torsoPath = Path().apply {
            moveTo(headX - headRadius * 0.8f, headY + headRadius * 0.8f)
            lineTo(headX + headRadius * 0.8f, headY + headRadius * 0.8f)
            lineTo(centerX + headRadius * 1.1f, pelvisY)
            lineTo(centerX - headRadius * 1.1f, pelvisY)
            close()
        }
        c.drawPath(torsoPath, bodyPaint)

        // Legs (Running / Jumping mechanics)
        val legLength = height * 0.16f
        val legPhase1 = t
        val legPhase2 = t + Math.PI

        // Left Leg
        val lKneeX = (centerX - sin(legPhase1) * 60f).toFloat()
        val lKneeY = (pelvisY + legLength * 0.9f + cos(legPhase1) * 30f).toFloat()
        val lFootX = (lKneeX - sin(legPhase1 + 0.5) * 50f).toFloat()
        val lFootY = (lKneeY + legLength * 0.8f).coerceAtMost(groundY)
        c.drawLine(centerX - 15f, pelvisY, lKneeX, lKneeY, limbPaint)
        c.drawLine(lKneeX, lKneeY, lFootX, lFootY, limbPaint)
        c.drawCircle(lKneeX, lKneeY, 6f, jointPaint)

        // Right Leg
        val rKneeX = (centerX - sin(legPhase2) * 60f).toFloat()
        val rKneeY = (pelvisY + legLength * 0.9f + cos(legPhase2) * 30f).toFloat()
        val rFootX = (rKneeX - sin(legPhase2 + 0.5) * 50f).toFloat()
        val rFootY = (rKneeY + legLength * 0.8f).coerceAtMost(groundY)
        c.drawLine(centerX + 15f, pelvisY, rKneeX, rKneeY, limbPaint)
        c.drawLine(rKneeX, rKneeY, rFootX, rFootY, limbPaint)
        c.drawCircle(rKneeX, rKneeY, 6f, jointPaint)

        // Arms (Counter-swing)
        val armLength = height * 0.13f
        val shoulderY = headY + headRadius * 1.2f

        // Left Arm
        val lElbowX = (centerX + sin(legPhase2) * 45f).toFloat()
        val lElbowY = (shoulderY + armLength * 0.8f).toFloat()
        val lHandX = (lElbowX + sin(legPhase2 * 1.2) * 40f).toFloat()
        val lHandY = (lElbowY + armLength * 0.7f).toFloat()
        c.drawLine(centerX - 25f, shoulderY, lElbowX, lElbowY, limbPaint)
        c.drawLine(lElbowX, lElbowY, lHandX, lHandY, limbPaint)

        // Right Arm
        val rElbowX = (centerX + sin(legPhase1) * 45f).toFloat()
        val rElbowY = (shoulderY + armLength * 0.8f).toFloat()
        val rHandX = (rElbowX + sin(legPhase1 * 1.2) * 40f).toFloat()
        val rHandY = (rElbowY + armLength * 0.7f).toFloat()
        c.drawLine(centerX + 25f, shoulderY, rElbowX, rElbowY, limbPaint)
        c.drawLine(rElbowX, rElbowY, rHandX, rHandY, limbPaint)

        // Reference HUD Watermark in top corner of rotoscoping video
        val textPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = (height * 0.024f).coerceAtLeast(16f)
            isAntiAlias = true
        }
        c.drawText("ROTOSCOPE VIDEO REF • FRAME ${frameIndex + 1}/24", 24f, 40f, textPaint)

        frameCache[frameIndex] = bmp
        return bmp
    }

    fun clearCache() {
        frameCache.values.forEach { if (!it.isRecycled) it.recycle() }
        frameCache.clear()
    }
}
