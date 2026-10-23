package com.example.frameforge.cinema

import java.util.UUID

enum class TrackerStatus(val label: String) {
    UNINITIALIZED("Ready to Track"),
    TRACKING("Tracking in Progress..."),
    COMPLETE("Tracked Successfully"),
    LOST("Feature Lost / Search Window Exceeded")
}

data class TrackPoint(
    val frameIndex: Int,
    val x: Float,
    val y: Float,
    val confidence: Float = 1.0f,
    val searchRadius: Int = 30, // Search window radius in pixels
    val patchSize: Int = 24     // Feature patch size in pixels
)

data class MotionTracker(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Tracker 1",
    val isEnabled: Boolean = true,
    val colorArgb: Int = 0xFF00E5FF.toInt(), // Cyan tracking crosshair
    val points: Map<Int, TrackPoint> = emptyMap(),
    val status: TrackerStatus = TrackerStatus.UNINITIALIZED,
    // Attached entities that follow this track:
    val attachedLayerId: String? = null,
    val attachedTextLabel: String? = null,
    val attachedRotoShapeId: String? = null,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
) {
    fun getPositionForFrame(frameIndex: Int): Pair<Float, Float>? {
        val pt = points[frameIndex] ?: return null
        return Pair(pt.x + offsetX, pt.y + offsetY)
    }
}

data class MotionTrackingState(
    val isEnabled: Boolean = false,
    val trackers: List<MotionTracker> = listOf(
        MotionTracker(name = "Hero Celestial Core Tracker")
    ),
    val activeTrackerId: String? = null,
    val isTrackingInProgress: Boolean = false
)
