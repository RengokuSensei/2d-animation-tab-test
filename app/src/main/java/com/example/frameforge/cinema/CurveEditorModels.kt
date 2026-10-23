package com.example.frameforge.cinema

import java.util.UUID

enum class TangentMode(val label: String) {
    FREE("Free (Broken Handles)"),
    ALIGNED("Aligned (Smooth Colinear)"),
    VECTOR("Vector (Direct Tangent)"),
    AUTO_CLAMPED("Auto Clamped (Blender Non-Overshoot)")
}

enum class CurveEditorViewMode(val label: String) {
    DOPE_SHEET("Dope Sheet (Timing & Keyframe Blocks)"),
    GRAPH_CURVE("Graph Editor (Bezier Spline Curves)")
}

data class GraphKeyframe(
    val id: String = UUID.randomUUID().toString(),
    val frame: Float,
    val value: Float,
    val leftHandleFrameOffset: Float = -3f,
    val leftHandleValueOffset: Float = 0f,
    val rightHandleFrameOffset: Float = 3f,
    val rightHandleValueOffset: Float = 0f,
    val tangentMode: TangentMode = TangentMode.AUTO_CLAMPED,
    val isSelected: Boolean = false
) {
    val leftHandleFrame: Float get() = frame + leftHandleFrameOffset
    val leftHandleValue: Float get() = value + leftHandleValueOffset
    val rightHandleFrame: Float get() = frame + rightHandleFrameOffset
    val rightHandleValue: Float get() = value + rightHandleValueOffset
}

data class AnimatedPropertyTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String = "Transform",
    val colorArgb: Int = 0xFFFF3B30.toInt(), // Default Red
    val keyframes: List<GraphKeyframe> = emptyList(),
    val minValue: Float = 0f,
    val maxValue: Float = 1f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false
) {
    /**
     * Evaluates the Bezier curve value at any arbitrary frame timestamp
     */
    fun evaluate(atFrame: Float): Float {
        if (keyframes.isEmpty()) return minValue
        if (keyframes.size == 1 || atFrame <= keyframes.first().frame) return keyframes.first().value
        if (atFrame >= keyframes.last().frame) return keyframes.last().value

        // Find surrounding keyframes
        var k0 = keyframes.first()
        var k1 = keyframes.last()
        for (i in 0 until keyframes.size - 1) {
            if (atFrame >= keyframes[i].frame && atFrame <= keyframes[i + 1].frame) {
                k0 = keyframes[i]
                k1 = keyframes[i + 1]
                break
            }
        }

        val span = k1.frame - k0.frame
        if (span <= 0.001f) return k0.value

        val t = (atFrame - k0.frame) / span
        // Cubic Hermite / Bezier interpolation
        val p0 = k0.value
        val p1 = k0.rightHandleValue
        val p2 = k1.leftHandleValue
        val p3 = k1.value

        val u = 1f - t
        return (u * u * u * p0) + (3f * u * u * t * p1) + (3f * u * t * t * p2) + (t * t * t * p3)
    }
}

data class CurveEditorState(
    val isVisible: Boolean = false,
    val viewMode: CurveEditorViewMode = CurveEditorViewMode.GRAPH_CURVE,
    val tracks: List<AnimatedPropertyTrack> = listOf(
        AnimatedPropertyTrack(
            name = "Translation X",
            colorArgb = 0xFFFF453A.toInt(), // Red
            minValue = -500f,
            maxValue = 500f,
            keyframes = listOf(
                GraphKeyframe(frame = 0f, value = 0f),
                GraphKeyframe(frame = 12f, value = 240f, rightHandleFrameOffset = 4f, rightHandleValueOffset = 30f),
                GraphKeyframe(frame = 24f, value = 0f, leftHandleFrameOffset = -4f, leftHandleValueOffset = -30f)
            )
        ),
        AnimatedPropertyTrack(
            name = "Translation Y",
            colorArgb = 0xFF32D74B.toInt(), // Green
            minValue = -500f,
            maxValue = 500f,
            keyframes = listOf(
                GraphKeyframe(frame = 0f, value = 0f),
                GraphKeyframe(frame = 12f, value = -120f),
                GraphKeyframe(frame = 24f, value = 0f)
            )
        ),
        AnimatedPropertyTrack(
            name = "Scale Uniform",
            colorArgb = 0xFF0A84FF.toInt(), // Blue
            minValue = 0.1f,
            maxValue = 3.0f,
            keyframes = listOf(
                GraphKeyframe(frame = 0f, value = 1.0f),
                GraphKeyframe(frame = 12f, value = 1.35f),
                GraphKeyframe(frame = 24f, value = 1.0f)
            )
        ),
        AnimatedPropertyTrack(
            name = "Roto Feather Width",
            colorArgb = 0xFFFFD60A.toInt(), // Yellow
            minValue = 0f,
            maxValue = 50f,
            keyframes = listOf(
                GraphKeyframe(frame = 0f, value = 5f),
                GraphKeyframe(frame = 12f, value = 35f),
                GraphKeyframe(frame = 24f, value = 5f)
            )
        )
    ),
    val activeTrackId: String? = null,
    val selectedKeyframeId: String? = null,
    val zoomX: Float = 1.0f,
    val zoomY: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f
)
