package com.example.frameforge.cinema

import com.example.frameforge.model.BlendModeType
import java.util.UUID

enum class StudioTrackKind(val label: String) {
    VIDEO("Video / Stock / 3D CGI"),
    DRAWING_ANIMATION("2D Animation / Drawing Layer"),
    PDF_DOCUMENT("PDF / EPUB Document Page"),
    ROTO_MATTE("Rotoscoping & VFX Matte"),
    AUDIO_VOICEOVER("Voiceover Narration (Clean A1)"),
    AUDIO_SOUND_EFFECTS("Music & Foley FX (A2/A3)")
}

data class StudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val kind: StudioTrackKind,
    val isVisible: Boolean = true,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val volume: Float = 1.0f, // For audio tracks
    val clips: List<TimelineClip> = emptyList()
)

data class UnifiedTimelineState(
    val tracks: List<StudioTrack> = listOf(
        StudioTrack(
            name = "V3: Drawing & Handwritten Reveal",
            kind = StudioTrackKind.DRAWING_ANIMATION,
            blendMode = BlendModeType.NORMAL
        ),
        StudioTrack(
            name = "V2: Hero 3D / VFX CGI",
            kind = StudioTrackKind.VIDEO,
            blendMode = BlendModeType.SCREEN // Black = 100% transparent for Blender space CGI!
        ),
        StudioTrack(
            name = "V1: PDF Document / Archive Bkg",
            kind = StudioTrackKind.PDF_DOCUMENT,
            blendMode = BlendModeType.NORMAL
        ),
        StudioTrack(
            name = "A1: Master User Narration",
            kind = StudioTrackKind.AUDIO_VOICEOVER,
            volume = 1.0f
        ),
        StudioTrack(
            name = "A2: Ambient Music & SFX",
            kind = StudioTrackKind.AUDIO_SOUND_EFFECTS,
            volume = 0.65f
        )
    ),
    val activeTrackId: String? = null,
    val selectedClipId: String? = null,
    val currentPlayheadFrame: Int = 0,
    val totalProjectFrames: Int = 240,
    val fps: Int = 24,
    val isPlaying: Boolean = false,
    val zoomLevel: Float = 1.0f
)
