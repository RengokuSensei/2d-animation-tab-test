package com.example.frameforge.ai

import com.example.frameforge.engine.ProceduralMotion
import com.example.frameforge.model.BoneKeyframe
import com.example.frameforge.model.BonePose
import com.example.frameforge.model.DrawingCel
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.EasingCurve
import com.example.frameforge.model.StrokePoint
import kotlin.math.cos
import kotlin.math.sin

data class MotionPromptResult(
    val title: String,
    val description: String,
    val keyframes: List<BoneKeyframe>,
    val totalFrames: Int
)

data class TrackedVideoPoint(
    val frameIndex: Int,
    val pointName: String,
    val x: Float, // Normalized 0..1
    val y: Float  // Normalized 0..1
)

data class MotionTrackResult(
    val pointTrajectory: Map<String, List<TrackedVideoPoint>>,
    val suggestedKeyframes: List<BoneKeyframe>
)

data class ColorPaletteSuggestion(
    val themeName: String,
    val colorsArgb: List<Int>,
    val description: String
)

data class StoryboardBeat(
    val beatNumber: Int,
    val title: String,
    val action: String,
    val cameraAngle: String,
    val frameStart: Int,
    val frameEnd: Int
)

object AiMotionAssistant {

    /**
     * Interprets a text motion prompt and synthesizes a keyframe sequence.
     * Offline deterministic procedural kinematics ensures it works without API,
     * while giving professional structured output.
     */
    fun generateMotionFromPrompt(prompt: String, durationFrames: Int = 16): MotionPromptResult {
        val lower = prompt.lowercase()

        val motionType = when {
            lower.contains("run") || lower.contains("sprint") || lower.contains("chase") ->
                ProceduralMotion.MotionType.RUN
            lower.contains("wave") || lower.contains("greet") || lower.contains("hello") || lower.contains("bye") ->
                ProceduralMotion.MotionType.WAVE
            lower.contains("jump") || lower.contains("hop") || lower.contains("leap") || lower.contains("bounce") ->
                ProceduralMotion.MotionType.JUMP
            lower.contains("nod") || lower.contains("yes") || lower.contains("agree") ->
                ProceduralMotion.MotionType.NOD
            lower.contains("idle") || lower.contains("breathe") || lower.contains("wait") || lower.contains("stand") ->
                ProceduralMotion.MotionType.IDLE
            else ->
                ProceduralMotion.MotionType.WALK
        }

        val speed = if (lower.contains("fast") || lower.contains("quick")) 1.4f
                    else if (lower.contains("slow") || lower.contains("careful")) 0.7f
                    else 1.0f

        val stride = if (lower.contains("wide") || lower.contains("big")) 1.3f
                     else if (lower.contains("small") || lower.contains("tiny")) 0.6f
                     else 1.0f

        val keyframes = ProceduralMotion.generateKeyframes(
            motionType = motionType,
            totalFrames = durationFrames,
            strideScale = stride,
            speedScale = speed
        )

        return MotionPromptResult(
            title = motionType.title,
            description = "Synthesized motion for: \"$prompt\"",
            keyframes = keyframes,
            totalFrames = durationFrames
        )
    }

    /**
     * Intermediate-frame generation (In-betweening) between two bone poses or frames
     */
    fun generateInBetweenBoneKeyframes(
        startFrame: Int,
        startPose: BonePose,
        endFrame: Int,
        endPose: BonePose,
        easing: EasingCurve = EasingCurve.EASE_IN_OUT
    ): List<BoneKeyframe> {
        val diff = endFrame - startFrame
        if (diff <= 1) return emptyList()

        val result = mutableListOf<BoneKeyframe>()

        for (i in 1 until diff) {
            val linearT = i.toFloat() / diff
            val easedT = applyEasing(linearT, easing)

            val interpPose = ProceduralMotion.interpolatePoses(startPose, endPose, easedT)
            result.add(
                BoneKeyframe(
                    frameIndex = startFrame + i,
                    pose = interpPose,
                    easing = easing
                )
            )
        }

        return result
    }

    /**
     * Intermediate-frame morph interpolation between two drawing cels
     */
    fun generateInBetweenDrawingCels(
        celA: DrawingCel,
        celB: DrawingCel,
        inBetweenCount: Int,
        startFrame: Int,
        layerId: String
    ): List<DrawingCel> {
        val result = mutableListOf<DrawingCel>()
        val strokesA = celA.strokes
        val strokesB = celB.strokes

        for (step in 1..inBetweenCount) {
            val ratio = step.toFloat() / (inBetweenCount + 1)
            val blendedStrokes = mutableListOf<DrawingStroke>()

            val maxCount = maxOf(strokesA.size, strokesB.size)
            for (idx in 0 until maxCount) {
                val sA = strokesA.getOrNull(idx) ?: strokesB.getOrNull(idx) ?: continue
                val sB = strokesB.getOrNull(idx) ?: sA

                val ptsA = sA.points
                val ptsB = sB.points

                val ptCount = maxOf(ptsA.size, ptsB.size)
                val blendedPts = mutableListOf<StrokePoint>()

                for (pIdx in 0 until ptCount) {
                    val pA = ptsA.getOrNull(pIdx) ?: ptsA.lastOrNull() ?: continue
                    val pB = ptsB.getOrNull(pIdx) ?: ptsB.lastOrNull() ?: pA

                    blendedPts.add(
                        StrokePoint(
                            x = pA.x + (pB.x - pA.x) * ratio,
                            y = pA.y + (pB.y - pA.y) * ratio,
                            pressure = pA.pressure + (pB.pressure - pA.pressure) * ratio
                        )
                    )
                }

                blendedStrokes.add(
                    sA.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        points = blendedPts
                    )
                )
            }

            result.add(
                DrawingCel(
                    id = java.util.UUID.randomUUID().toString(),
                    frameIndex = startFrame + step,
                    layerId = layerId,
                    strokes = blendedStrokes,
                    isKeyframe = false
                )
            )
        }

        return result
    }

    /**
     * Simulates or extracts motion landmarks from reference video / optical tracking
     */
    fun trackReferencePoints(totalFrames: Int = 16, motionPattern: String = "pendulum"): MotionTrackResult {
        val headTrajectory = mutableListOf<TrackedVideoPoint>()
        val wristTrajectory = mutableListOf<TrackedVideoPoint>()
        val ankleTrajectory = mutableListOf<TrackedVideoPoint>()

        for (f in 0 until totalFrames) {
            val angle = (f.toFloat() / totalFrames) * 2.0 * Math.PI
            val headY = (0.25 + 0.03 * sin(angle)).toFloat()
            val wristX = (0.55 + 0.15 * cos(angle)).toFloat()
            val wristY = (0.45 + 0.12 * sin(angle)).toFloat()
            val ankleX = (0.50 + 0.10 * sin(angle)).toFloat()
            val ankleY = (0.80 - 0.05 * kotlin.math.abs(cos(angle))).toFloat()

            headTrajectory.add(TrackedVideoPoint(f, "Head", 0.5f, headY))
            wristTrajectory.add(TrackedVideoPoint(f, "Right Wrist", wristX, wristY))
            ankleTrajectory.add(TrackedVideoPoint(f, "Right Ankle", ankleX, ankleY))
        }

        // Map tracked coordinates to bone keyframes
        val keyframes = mutableListOf<BoneKeyframe>()
        for (f in 0 until totalFrames) {
            val w = wristTrajectory[f]
            val a = ankleTrajectory[f]
            val armAngle = ((w.x - 0.5f) * 120f)
            val legAngle = ((a.x - 0.5f) * 80f)

            keyframes.add(
                BoneKeyframe(
                    frameIndex = f,
                    pose = BonePose(
                        boneAnglesDeg = mapOf(
                            "bone_arm_r_up" to armAngle,
                            "bone_leg_r_up" to legAngle
                        )
                    )
                )
            )
        }

        return MotionTrackResult(
            pointTrajectory = mapOf(
                "Head" to headTrajectory,
                "Right Wrist" to wristTrajectory,
                "Right Ankle" to ankleTrajectory
            ),
            suggestedKeyframes = keyframes
        )
    }

    /**
     * AI Color Palette generation
     */
    fun getSuggestedPalettes(): List<ColorPaletteSuggestion> {
        return listOf(
            ColorPaletteSuggestion(
                themeName = "Ghibli Meadow",
                colorsArgb = listOf(
                    0xFF2E7D32.toInt(), // Foliage green
                    0xFF81C784.toInt(), // Soft grass
                    0xFFFFF176.toInt(), // Sun yellow
                    0xFF81D4FA.toInt(), // Sky blue
                    0xFFE0E0E0.toInt(), // Cloud white
                    0xFF8D6E63.toInt()  // Earth brown
                ),
                description = "Lush pastoral anime palette with vivid greens and soft sky tones."
            ),
            ColorPaletteSuggestion(
                themeName = "Cyberpunk Neon",
                colorsArgb = listOf(
                    0xFFFF007F.toInt(), // Hot magenta
                    0xFF00F0FF.toInt(), // Electric cyan
                    0xFFFFE600.toInt(), // Acid yellow
                    0xFF7928CA.toInt(), // Deep ultraviolet
                    0xFF12121A.toInt(), // Void black
                    0xFF00FF66.toInt()  // Neon green
                ),
                description = "High-energy futuristic palette for cyberpunk character animation."
            ),
            ColorPaletteSuggestion(
                themeName = "Golden Hour Sunset",
                colorsArgb = listOf(
                    0xFFFF6F00.toInt(), // Burnt amber
                    0xFFFFB300.toInt(), // Warm golden
                    0xFFFF5252.toInt(), // Coral red
                    0xFF7B1FA2.toInt(), // Twilight purple
                    0xFF311B92.toInt(), // Deep dusk
                    0xFFFFE082.toInt()  // Flare highlight
                ),
                description = "Dramatic sunset lighting with rich ambers and dusk purples."
            ),
            ColorPaletteSuggestion(
                themeName = "Dark Fantasy Ink",
                colorsArgb = listOf(
                    0xFF1C1917.toInt(), // Deep slate ink
                    0xFF78716C.toInt(), // Stone gray
                    0xFFDC2626.toInt(), // Crimson blood
                    0xFFD97706.toInt(), // Torch flame
                    0xFF0284C7.toInt(), // Arcane azure
                    0xFFFEF3C7.toInt()  // Aged parchment
                ),
                description = "Moody graphic novel tones with parchment paper accents."
            )
        )
    }

    /**
     * Scene Storyboard beats generator
     */
    fun generateStoryboardBeats(sceneIdea: String): List<StoryboardBeat> {
        return listOf(
            StoryboardBeat(1, "Establishing Shot", "Wide view setting the stage for $sceneIdea", "Wide / High Angle", 0, 12),
            StoryboardBeat(2, "Character Entry", "Protagonist walks into frame with anticipation", "Medium Full", 12, 24),
            StoryboardBeat(3, "Key Action Apex", "Dramatic focal gesture / climax of motion", "Close-Up Dynamic", 24, 36),
            StoryboardBeat(4, "Reaction & Settle", "Subtle breathing, turn head towards viewer, hold", "Medium Shot", 36, 48)
        )
    }

    private fun applyEasing(t: Float, easing: EasingCurve): Float {
        return when (easing) {
            EasingCurve.LINEAR -> t
            EasingCurve.EASE_IN -> t * t
            EasingCurve.EASE_OUT -> t * (2f - t)
            EasingCurve.EASE_IN_OUT -> if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t
            EasingCurve.BOUNCE -> {
                val p = t
                if (p < 0.3636f) 7.5625f * p * p
                else if (p < 0.7272f) {
                    val p2 = p - 0.5454f
                    7.5625f * p2 * p2 + 0.75f
                } else if (p < 0.9090f) {
                    val p3 = p - 0.8181f
                    7.5625f * p3 * p3 + 0.9375f
                } else {
                    val p4 = p - 0.9545f
                    7.5625f * p4 * p4 + 0.984375f
                }
            }
            EasingCurve.ANTICIPATION -> {
                val s = 1.70158f
                t * t * ((s + 1f) * t - s)
            }
        }
    }
}
