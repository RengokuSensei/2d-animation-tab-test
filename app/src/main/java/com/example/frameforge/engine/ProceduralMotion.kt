package com.example.frameforge.engine

import com.example.frameforge.model.BoneKeyframe
import com.example.frameforge.model.BonePose
import com.example.frameforge.model.EasingCurve
import kotlin.math.cos
import kotlin.math.sin

object ProceduralMotion {

    enum class MotionType(val title: String, val description: String, val suggestedFrames: Int) {
        WALK("Walk Cycle", "Natural 2-legged walking rhythm with arm swing and hip bob", 16),
        RUN("Run Cycle", "Energetic forward running motion with wider strides", 12),
        WAVE("Friendly Wave", "Right arm raised waving cheerfully side-to-side", 14),
        IDLE("Idle Breathing", "Subtle breathing chest expansion and head bob", 18),
        JUMP("Jump & Land", "Squash anticipation, explosive leap, and soft landing", 16),
        NOD("Nod Agree", "Head nodding in agreement with subtle shoulder gesture", 10)
    }

    /**
     * Generates a complete sequence of BoneKeyframes for the chosen procedural motion.
     */
    fun generateKeyframes(
        motionType: MotionType,
        totalFrames: Int = motionType.suggestedFrames,
        strideScale: Float = 1.0f,
        speedScale: Float = 1.0f
    ): List<BoneKeyframe> {
        val keyframes = mutableListOf<BoneKeyframe>()

        for (frame in 0 until totalFrames) {
            val progress = (frame.toFloat() / totalFrames) * (2.0 * Math.PI) * speedScale
            val p = progress.toFloat()

            val pose = when (motionType) {
                MotionType.WALK -> generateWalkPose(p, strideScale)
                MotionType.RUN -> generateRunPose(p, strideScale)
                MotionType.WAVE -> generateWavePose(p)
                MotionType.IDLE -> generateIdlePose(p)
                MotionType.JUMP -> generateJumpPose(frame, totalFrames)
                MotionType.NOD -> generateNodPose(p)
            }

            keyframes.add(
                BoneKeyframe(
                    frameIndex = frame,
                    pose = pose,
                    easing = EasingCurve.EASE_IN_OUT
                )
            )
        }

        return keyframes
    }

    private fun generateWalkPose(phase: Float, scale: Float): BonePose {
        val legSwing = sin(phase) * 28f * scale
        val armSwing = -sin(phase) * 32f * scale
        val hipBob = -kotlin.math.abs(cos(phase)) * 8f * scale

        val angles = mapOf(
            "bone_hips" to (sin(phase) * 3f),
            "bone_spine" to (-sin(phase) * 4f),
            "bone_head" to (cos(phase) * 3f),
            // Left leg
            "bone_leg_l_up" to legSwing,
            "bone_leg_l_low" to (if (legSwing < 0) -legSwing * 0.7f else 5f),
            // Right leg (opposite)
            "bone_leg_r_up" to -legSwing,
            "bone_leg_r_low" to (if (-legSwing < 0) legSwing * 0.7f else 5f),
            // Arms
            "bone_arm_l_up" to armSwing,
            "bone_arm_l_low" to (armSwing.coerceAtLeast(0f) * 0.5f),
            "bone_arm_r_up" to -armSwing,
            "bone_arm_r_low" to ((-armSwing).coerceAtLeast(0f) * 0.5f)
        )

        return BonePose(
            boneAnglesDeg = angles,
            rootX = 0f,
            rootY = hipBob
        )
    }

    private fun generateRunPose(phase: Float, scale: Float): BonePose {
        val legSwing = sin(phase) * 45f * scale
        val armSwing = -sin(phase) * 55f * scale
        val hipBob = -kotlin.math.abs(sin(phase)) * 16f * scale

        val angles = mapOf(
            "bone_hips" to 15f, // forward lean
            "bone_spine" to 10f,
            "bone_head" to -15f,
            "bone_leg_l_up" to legSwing,
            "bone_leg_l_low" to (if (legSwing < 0) -legSwing * 1.2f else 15f),
            "bone_leg_r_up" to -legSwing,
            "bone_leg_r_low" to (if (-legSwing < 0) legSwing * 1.2f else 15f),
            "bone_arm_l_up" to armSwing,
            "bone_arm_l_low" to 40f,
            "bone_arm_r_up" to -armSwing,
            "bone_arm_r_low" to 40f
        )

        return BonePose(
            boneAnglesDeg = angles,
            rootX = 0f,
            rootY = hipBob
        )
    }

    private fun generateWavePose(phase: Float): BonePose {
        val waveOscillation = sin(phase * 2f) * 22f
        val angles = mapOf(
            "bone_spine" to 3f,
            "bone_head" to -5f,
            // Right arm waving
            "bone_arm_r_up" to (80f + waveOscillation * 0.2f),
            "bone_arm_r_low" to (75f + waveOscillation),
            // Left arm relaxed at side
            "bone_arm_l_up" to 5f,
            "bone_arm_l_low" to 10f,
            "bone_leg_l_up" to 0f,
            "bone_leg_r_up" to 0f
        )
        return BonePose(boneAnglesDeg = angles, rootX = 0f, rootY = 0f)
    }

    private fun generateIdlePose(phase: Float): BonePose {
        val breath = sin(phase) * 4f
        val headBob = cos(phase) * 2f
        val angles = mapOf(
            "bone_spine" to breath,
            "bone_head" to headBob,
            "bone_arm_l_up" to -breath * 0.5f,
            "bone_arm_r_up" to -breath * 0.5f
        )
        return BonePose(boneAnglesDeg = angles, rootX = 0f, rootY = breath * 0.5f)
    }

    private fun generateJumpPose(frame: Int, total: Int): BonePose {
        val t = frame.toFloat() / total
        val (rootY, spineAngle, legAngle) = when {
            // Anticipation crouch (0.0 to 0.25)
            t < 0.25f -> {
                val subT = t / 0.25f
                Triple(subT * 25f, subT * 15f, -subT * 30f)
            }
            // Ascent (0.25 to 0.5)
            t < 0.5f -> {
                val subT = (t - 0.25f) / 0.25f
                Triple(25f - subT * 85f, -10f, subT * 20f)
            }
            // Apex and descent (0.5 to 0.8)
            t < 0.8f -> {
                val subT = (t - 0.5f) / 0.3f
                Triple(-60f + subT * 60f, 0f, 10f - subT * 20f)
            }
            // Landing impact and settle (0.8 to 1.0)
            else -> {
                val subT = (t - 0.8f) / 0.2f
                val settle = sin(subT * Math.PI.toFloat()) * 20f
                Triple(settle, settle * 0.5f, -settle * 0.8f)
            }
        }

        val armAngle = if (t in 0.25f..0.6f) 100f else 20f
        val angles = mapOf(
            "bone_spine" to spineAngle,
            "bone_leg_l_up" to legAngle,
            "bone_leg_l_low" to (-legAngle * 0.8f),
            "bone_leg_r_up" to legAngle,
            "bone_leg_r_low" to (-legAngle * 0.8f),
            "bone_arm_l_up" to armAngle,
            "bone_arm_r_up" to armAngle
        )
        return BonePose(boneAnglesDeg = angles, rootX = 0f, rootY = rootY)
    }

    private fun generateNodPose(phase: Float): BonePose {
        val nod = sin(phase * 2f) * 18f
        val angles = mapOf(
            "bone_head" to nod,
            "bone_spine" to (nod * 0.25f)
        )
        return BonePose(boneAnglesDeg = angles)
    }

    /**
     * Interpolates between two poses with shortest-path angular interpolation
     */
    fun interpolatePoses(poseA: BonePose, poseB: BonePose, ratio: Float): BonePose {
        val r = ratio.coerceIn(0f, 1f)
        val allKeys = poseA.boneAnglesDeg.keys + poseB.boneAnglesDeg.keys
        val interpolatedAngles = mutableMapOf<String, Float>()

        for (k in allKeys) {
            val a = poseA.boneAnglesDeg[k] ?: 0f
            val b = poseB.boneAnglesDeg[k] ?: 0f
            // Shortest angular difference
            var diff = (b - a) % 360f
            if (diff > 180f) diff -= 360f
            if (diff < -180f) diff += 360f
            interpolatedAngles[k] = a + diff * r
        }

        val interpRootX = poseA.rootX + (poseB.rootX - poseA.rootX) * r
        val interpRootY = poseA.rootY + (poseB.rootY - poseA.rootY) * r

        return BonePose(
            boneAnglesDeg = interpolatedAngles,
            rootX = interpRootX,
            rootY = interpRootY
        )
    }
}
