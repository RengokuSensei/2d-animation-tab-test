package com.example.frameforge.cinema

import com.example.frameforge.model.BlendModeType
import java.util.UUID

/**
 * Natron / Nuke / Silhouette FX VFX-Grade Rotoscoping Models with Per-Vertex Feathering.
 */

data class RotoVertex(
    val id: String = UUID.randomUUID().toString(),
    val x: Float,
    val y: Float,
    val inTangentX: Float = 0f,   // Relative offset from (x, y)
    val inTangentY: Float = 0f,
    val outTangentX: Float = 0f,  // Relative offset from (x, y)
    val outTangentY: Float = 0f,
    val featherOffsetX: Float = 0f, // Per-vertex feather offset vector
    val featherOffsetY: Float = 0f
) {
    // Outer feather boundary point in absolute coordinates
    val featherAbsX: Float get() = x + featherOffsetX
    val featherAbsY: Float get() = y + featherOffsetY
}

data class RotoShape(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Roto Mask 1",
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isInverted: Boolean = false,
    val overlayColorArgb: Int = 0xFFFFD700.toInt(), // Warm yellow spline guide
    val opacity: Float = 1.0f,
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val isClosed: Boolean = true,
    // Frame-by-frame animated vertex keyframes
    val keyframes: Map<Int, List<RotoVertex>> = mapOf(
        0 to listOf(
            RotoVertex(x = 400f, y = 300f, featherOffsetX = 15f, featherOffsetY = 15f),
            RotoVertex(x = 600f, y = 300f, featherOffsetX = 25f, featherOffsetY = 0f),
            RotoVertex(x = 650f, y = 500f, featherOffsetX = 35f, featherOffsetY = 20f),
            RotoVertex(x = 350f, y = 500f, featherOffsetX = 10f, featherOffsetY = 20f)
        )
    )
) {
    /**
     * Resolves the vertices for a given frame index.
     * Interpolates between keyframes if needed.
     */
    fun getVerticesForFrame(frameIndex: Int): List<RotoVertex> {
        val exact = keyframes[frameIndex]
        if (exact != null) return exact

        // Find surrounding keyframe frames
        val keys = keyframes.keys.sorted()
        if (keys.isEmpty()) return emptyList()
        if (frameIndex <= keys.first()) return keyframes[keys.first()] ?: emptyList()
        if (frameIndex >= keys.last()) return keyframes[keys.last()] ?: emptyList()

        var prevKey = keys.first()
        var nextKey = keys.last()
        for (k in keys) {
            if (k <= frameIndex) prevKey = k
            if (k >= frameIndex) {
                nextKey = k
                break
            }
        }

        val vPrev = keyframes[prevKey] ?: return emptyList()
        val vNext = keyframes[nextKey] ?: return emptyList()
        if (vPrev.size != vNext.size || prevKey == nextKey) return vPrev

        val t = (frameIndex - prevKey).toFloat() / (nextKey - prevKey).toFloat()
        // Hermite smoothstep
        val smoothT = t * t * (3f - 2f * t)

        return vPrev.indices.map { i ->
            val p0 = vPrev[i]
            val p1 = vNext[i]
            RotoVertex(
                id = p0.id,
                x = p0.x + (p1.x - p0.x) * smoothT,
                y = p0.y + (p1.y - p0.y) * smoothT,
                inTangentX = p0.inTangentX + (p1.inTangentX - p0.inTangentX) * smoothT,
                inTangentY = p0.inTangentY + (p1.inTangentY - p0.inTangentY) * smoothT,
                outTangentX = p0.outTangentX + (p1.outTangentX - p0.outTangentX) * smoothT,
                outTangentY = p0.outTangentY + (p1.outTangentY - p0.outTangentY) * smoothT,
                featherOffsetX = p0.featherOffsetX + (p1.featherOffsetX - p0.featherOffsetX) * smoothT,
                featherOffsetY = p0.featherOffsetY + (p1.featherOffsetY - p0.featherOffsetY) * smoothT
            )
        }
    }
}

data class RotoscopingState(
    val isEnabled: Boolean = false,
    val shapes: List<RotoShape> = listOf(RotoShape()),
    val activeShapeId: String? = null,
    val selectedVertexIndex: Int? = null,
    val isFeatherToolActive: Boolean = false, // When true, stylus adjusts feather handles; when false, adjusts core path
    val showGuideSplines: Boolean = true,
    val isTrackingAttached: Boolean = false,
    val attachedTrackerId: String? = null
)
