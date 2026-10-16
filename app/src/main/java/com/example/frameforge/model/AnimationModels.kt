package com.example.frameforge.model

enum class PlaybackMode(val label: String) {
    LOOP("Loop"),
    ONCE("Once"),
    PING_PONG("Ping-Pong")
}

data class OnionSkinConfig(
    val enabled: Boolean = true,
    val prevFramesCount: Int = 2,
    val nextFramesCount: Int = 2,
    val prevTintArgb: Int = 0xFFFF5252.toInt(), // Coral red
    val nextTintArgb: Int = 0xFF4CAF50.toInt(), // Fresh green
    val opacity: Float = 0.45f
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

// --- 4K / 30-MIN VIDEO ROTOSCOPING TRACK ---

data class VideoRotoscopingTrack(
    val isEnabled: Boolean = true,
    val videoTitle: String = "Athletic Run & Jump (Reference)",
    val opacity: Float = 0.45f,
    val isUnderlay: Boolean = true, // Rendered beneath sketch layers for tracing
    val totalVideoFrames: Int = 48,
    val frameOffset: Int = 0
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
    val objectTracks: List<ObjectTransformTrack> = listOf(
        ObjectTransformTrack("layer_sketch", "Rough Sketch Track"),
        ObjectTransformTrack("layer_ink", "Line Art Track")
    ),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
