package com.example.frameforge.cinema

enum class TimelineClipType(val label: String) {
    DRAWING_CEL("Drawing Cel"),
    VIDEO("Video Rotoscoping"),
    AUDIO("Audio Track"),
    PDF("PDF Document"),
    LOOP_GROUP("Looping Group")
}

data class TimelineClip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val trackId: String,
    val name: String,
    val startFrame: Int,
    val durationFrames: Int,
    val sourceInFrame: Int = 0,
    val sourceDuration: Int = durationFrames,
    val speedMultiplier: Float = 1.0f, // 0.25f (slow-mo) to 4.0f (fast-forward)
    val clipType: TimelineClipType = TimelineClipType.DRAWING_CEL,
    val colorArgb: Int = 0xFF0A84FF.toInt(),
    val isSelected: Boolean = false,
    val isLocked: Boolean = false
) {
    val endFrame: Int get() = startFrame + durationFrames
}

enum class ClipEditMode(val label: String) {
    STANDARD("Standard Trim"),
    RIPPLE("Ripple Edit (Shift Subsequent)"),
    SLIP("Slip Mode (Shift Media Window)"),
    SLIDE("Slide Mode (Move Between Adjacent)"),
    TIME_STRETCH("Time-Stretch / Speed Ramping")
}

data class TrimResult(
    val trimmedClip: TimelineClip,
    val shiftedClips: List<TimelineClip> = emptyList(),
    val deltaFrames: Int = 0
)

data class SnapGuide(
    val frameIndex: Int,
    val label: String,
    val colorArgb: Int = 0xFFFFD700.toInt() // Golden guide
)

object ClipEditorEngine {

    /**
     * Splits a clip into two independent clips at the playhead position.
     */
    fun splitClipAt(clip: TimelineClip, playheadFrame: Int): Pair<TimelineClip, TimelineClip>? {
        if (playheadFrame <= clip.startFrame || playheadFrame >= clip.endFrame) {
            return null
        }

        val firstDuration = playheadFrame - clip.startFrame
        val secondDuration = clip.durationFrames - firstDuration

        val firstClip = clip.copy(
            id = java.util.UUID.randomUUID().toString(),
            durationFrames = firstDuration,
            sourceDuration = (firstDuration * clip.speedMultiplier).toInt().coerceAtLeast(1)
        )

        val secondClip = clip.copy(
            id = java.util.UUID.randomUUID().toString(),
            startFrame = playheadFrame,
            durationFrames = secondDuration,
            sourceInFrame = clip.sourceInFrame + (firstDuration * clip.speedMultiplier).toInt(),
            sourceDuration = (secondDuration * clip.speedMultiplier).toInt().coerceAtLeast(1)
        )

        return Pair(firstClip, secondClip)
    }

    /**
     * Trims the In-Point (left handle) of a clip.
     */
    fun trimInPoint(
        clip: TimelineClip,
        newStartFrame: Int,
        isRipple: Boolean,
        subsequentClips: List<TimelineClip> = emptyList()
    ): TrimResult {
        val clampedStart = newStartFrame.coerceAtMost(clip.endFrame - 1).coerceAtLeast(0)
        val delta = clampedStart - clip.startFrame
        val newDuration = (clip.durationFrames - delta).coerceAtLeast(1)
        val newSourceIn = (clip.sourceInFrame + (delta * clip.speedMultiplier).toInt()).coerceAtLeast(0)

        val trimmed = clip.copy(
            startFrame = clampedStart,
            durationFrames = newDuration,
            sourceInFrame = newSourceIn
        )

        val shifted = if (isRipple && delta != 0) {
            subsequentClips.map { it.copy(startFrame = (it.startFrame - delta).coerceAtLeast(0)) }
        } else {
            subsequentClips
        }

        return TrimResult(trimmed, shifted, delta)
    }

    /**
     * Trims the Out-Point (right handle) of a clip.
     */
    fun trimOutPoint(
        clip: TimelineClip,
        newEndFrame: Int,
        isRipple: Boolean,
        subsequentClips: List<TimelineClip> = emptyList()
    ): TrimResult {
        val clampedEnd = newEndFrame.coerceAtLeast(clip.startFrame + 1)
        val newDuration = clampedEnd - clip.startFrame
        val delta = newDuration - clip.durationFrames

        val trimmed = clip.copy(
            durationFrames = newDuration,
            sourceDuration = (newDuration * clip.speedMultiplier).toInt().coerceAtLeast(1)
        )

        val shifted = if (isRipple && delta != 0) {
            subsequentClips.map { it.copy(startFrame = (it.startFrame + delta).coerceAtLeast(0)) }
        } else {
            subsequentClips
        }

        return TrimResult(trimmed, shifted, delta)
    }

    /**
     * Slip Edit: shifts internal source media in/out without altering timeline placement.
     */
    fun slipClip(clip: TimelineClip, deltaFrames: Int): TimelineClip {
        val newSourceIn = (clip.sourceInFrame + deltaFrames).coerceAtLeast(0)
        return clip.copy(sourceInFrame = newSourceIn)
    }

    /**
     * Time-stretch / speed ramping: dragging clip edge changes playback speed.
     */
    fun timeStretchClip(clip: TimelineClip, targetDuration: Int): TimelineClip {
        val safeDuration = targetDuration.coerceAtLeast(1)
        val speed = (clip.sourceDuration.toFloat() / safeDuration.toFloat()).coerceIn(0.25f, 4.0f)
        return clip.copy(
            durationFrames = safeDuration,
            speedMultiplier = speed
        )
    }

    /**
     * Magnetic snapping: snaps frame position to adjacent clip boundaries or beat markers.
     */
    fun findMagneticSnap(
        targetFrame: Int,
        clipBoundaries: List<Int>,
        markers: List<Int> = emptyList(),
        audioBeatPeaks: List<Int> = emptyList(),
        tolerance: Int = 2
    ): SnapGuide? {
        // 1. Check clip boundaries
        for (boundary in clipBoundaries) {
            if (kotlin.math.abs(targetFrame - boundary) <= tolerance) {
                return SnapGuide(boundary, "Clip Boundary")
            }
        }
        // 2. Check scene markers
        for (marker in markers) {
            if (kotlin.math.abs(targetFrame - marker) <= tolerance) {
                return SnapGuide(marker, "Scene Marker", 0xFFFF5252.toInt())
            }
        }
        // 3. Check audio beats
        for (beat in audioBeatPeaks) {
            if (kotlin.math.abs(targetFrame - beat) <= tolerance) {
                return SnapGuide(beat, "Beat Peak", 0xFF00E5FF.toInt())
            }
        }
        return null
    }
}
