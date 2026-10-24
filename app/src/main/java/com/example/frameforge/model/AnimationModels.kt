package com.example.frameforge.model

enum class PlaybackMode(val label: String) {
    LOOP("Loop"),
    ONCE("Once"),
    PING_PONG("Ping-Pong")
}

enum class OnionSkinTintMode(val label: String) {
    CLASSIC_RED_GREEN("Classic Red/Green (Industry Cel Standard)"),
    KRITA_RAINBOW("Krita Stepped Palette (Orange/Red vs Green/Blue)"),
    MONOCHROME_INK("Monochrome Ink Wash (Graphite Gray)"),
    BLUE_ORANGE("Blue / Orange High-Contrast (Photographic)")
}

enum class OnionSkinFadeMode(val label: String) {
    LINEAR("Linear Dropoff"),
    EXPONENTIAL("Exponential Curve (Heavy In-Betweens)"),
    FLAT("Constant Opacity")
}

data class OnionSkinConfig(
    val enabled: Boolean = true,
    val prevFramesCount: Int = 3,
    val nextFramesCount: Int = 3,
    val prevTintArgb: Int = 0xFFFF5252.toInt(), // Coral red
    val nextTintArgb: Int = 0xFF4CAF50.toInt(), // Fresh green
    val opacity: Float = 0.50f,
    val tintMode: OnionSkinTintMode = OnionSkinTintMode.KRITA_RAINBOW,
    val fadeMode: OnionSkinFadeMode = OnionSkinFadeMode.LINEAR,
    val isolatePinnedLayersOnly: Boolean = false,
    val prevPalette: List<Int> = listOf(
        0xFFFF5252.toInt(), // Step 1: Bright Coral Red
        0xFFFF9100.toInt(), // Step 2: Luminous Amber
        0xFFFFEA00.toInt()  // Step 3: Golden Yellow
    ),
    val nextPalette: List<Int> = listOf(
        0xFF00E676.toInt(), // Step 1: Bright Emerald Green
        0xFF00B0FF.toInt(), // Step 2: Sky Azure Blue
        0xFF651FFF.toInt()  // Step 3: Royal Purple
    )
)

enum class EasingCurve(val label: String) {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out"),
    BOUNCE("Bounce"),
    ANTICIPATION("Anticipation")
}

data class TransformKeyframe(
    val frameIndex: Int,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val rotationDeg: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val opacity: Float = 1f,
    val easing: EasingCurve = EasingCurve.EASE_IN_OUT
)

// --- INDEPENDENT MULTI-OBJECT SCENE GRAPH KEYFRAME TRACK ---

data class ObjectTransformTrack(
    val objectId: String,
    val objectName: String,
    val keyframes: List<TransformKeyframe> = listOf(
        TransformKeyframe(frameIndex = 0),
        TransformKeyframe(frameIndex = 12, translationX = 60f, rotationDeg = 15f),
        TransformKeyframe(frameIndex = 23, translationX = 0f, rotationDeg = 0f)
    )
)

// --- 4K / 30-MIN VIDEO ROTOSCOPING & NLE B-ROLL TRACK ---

data class A2VideoClip(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "docu_cut_1.mp4",
    val startPct: Float = 0f,
    val endPct: Float = 45f,
    val speed: Float = 1.0f,
    val colorHex: Long = 0xFF0EA5E9,
    val isCustomVideo: Boolean = false,
    val videoUri: String? = null
)

data class VideoPipTransform(
    val x: Float = 36f,
    val y: Float = 36f,
    val width: Float = 380f,
    val height: Float = 214f,
    val rotation: Float = 0f,
    val isVisible: Boolean = true,
    val isUnderlayMode: Boolean = false // If true, renders full canvas background underlay
)

data class VideoRotoscopingTrack(
    val isEnabled: Boolean = true,
    val videoTitle: String = "B-Roll Reference Track",
    val opacity: Float = 0.85f,
    val isUnderlay: Boolean = false, // Rendered beneath sketch layers for tracing
    val totalVideoFrames: Int = 48,
    val frameOffset: Int = 0,
    val clips: List<A2VideoClip> = listOf(
        A2VideoClip(id = "vclip-1", title = "cosmic_nebula_broll.mp4", startPct = 0f, endPct = 45f, speed = 1.0f, colorHex = 0xFF3B82F6),
        A2VideoClip(id = "vclip-2", title = "sun_core_cross_section.mp4", startPct = 50f, endPct = 100f, speed = 1.0f, colorHex = 0xFFEC4899)
    ),
    val selectedClipId: String? = "vclip-1",
    val pipTransform: VideoPipTransform = VideoPipTransform()
)

// --- AUDIO TRACK & WAVEFORM ENGINE ---

data class AudioTrack(
    val isEnabled: Boolean = true,
    val title: String = "Cinematic Beat & Foley.wav",
    val durationSeconds: Float = 12.0f,
    val volume: Float = 0.85f,
    val isMuted: Boolean = false,
    val waveformPeaks: List<Float> = generateSampleWaveform()
) {
    companion object {
        fun generateSampleWaveform(count: Int = 80): List<Float> {
            val list = mutableListOf<Float>()
            for (i in 0 until count) {
                // Procedural musical waveform profile with rhythmic drum beats
                val beat = if (i % 8 == 0) 0.95f else if (i % 4 == 0) 0.7f else 0.3f
                val noise = (kotlin.math.sin(i * 0.45) * 0.3f + kotlin.math.cos(i * 1.2) * 0.2f).toFloat()
                list.add((beat + noise).coerceIn(0.1f, 1.0f))
            }
            return list
        }
    }
}

// --- CANVAS RESOLUTION PRESETS (UP TO 4K) ---

enum class CanvasResolutionPreset(val label: String, val width: Int, val height: Int) {
    HD_720("720p HD (1280 × 720)", 1280, 720),
    FULL_HD_1080("1080p Full HD (1920 × 1080)", 1920, 1080),
    UHD_4K("4K Ultra HD (3840 × 2160)", 3840, 2160),
    SQUARE_2K("Square 2K (2048 × 2048)", 2048, 2048),
    INSTAGRAM_PORTRAIT("Vertical Story (1080 × 1920)", 1080, 1920)
}

data class AnimationProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "Untitled Animation",
    val canvasWidth: Int = 1920, // Default to Full HD 1080p
    val canvasHeight: Int = 1080,
    val fps: Int = 24,
    val totalFrames: Int = 24,
    val layers: List<AnimationLayer> = listOf(
        AnimationLayer(id = "layer_sketch", name = "Rough Sketch", opacity = 0.7f),
        AnimationLayer(id = "layer_ink", name = "Line Art"),
        AnimationLayer(id = "layer_color", name = "Colors")
    ),
    val cels: List<DrawingCel> = emptyList(),
    val boneRig: SkeletonRig = SkeletonRig.createDefaultBiped(),
    val boneKeyframes: List<BoneKeyframe> = emptyList(),
    val videoTrack: VideoRotoscopingTrack = VideoRotoscopingTrack(),
    val audioTrack: AudioTrack = AudioTrack(),
    val pdfTrack: com.example.frameforge.pdf.PdfDocumentTrack = com.example.frameforge.pdf.PdfDocumentTrack(),
    val objectTracks: List<ObjectTransformTrack> = listOf(
        ObjectTransformTrack("layer_sketch", "Rough Sketch Track"),
        ObjectTransformTrack("layer_ink", "Line Art Track")
    ),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
