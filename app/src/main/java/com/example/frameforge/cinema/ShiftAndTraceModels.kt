package com.example.frameforge.cinema

import androidx.compose.ui.geometry.Offset

/**
 * Interactive "Shift and Trace" (Light Table / Out-of-Pegs) state.
 * Allows animators to temporarily pan, zoom, or rotate an Onion Skin ghost
 * directly on canvas to trace in-betweens, and click "Reset Pegs" to snap back.
 */
data class ShiftAndTraceState(
    val isActive: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val rotationDeg: Float = 0f,
    val scale: Float = 1.0f,
    val isWorldSpace: Boolean = true, // World-space trajectory vs Local object-space
    val targetFrameOffset: Int = -1   // -1 = prev frame, +1 = next frame
) {
    fun isModified(): Boolean =
        offsetX != 0f || offsetY != 0f || rotationDeg != 0f || scale != 1.0f
}
