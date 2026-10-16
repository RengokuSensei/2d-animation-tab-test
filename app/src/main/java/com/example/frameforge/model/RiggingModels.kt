package com.example.frameforge.model

import kotlin.math.cos
import kotlin.math.sin

data class Bone(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val length: Float,
    val localAngleDeg: Float = 0f,
    val colorArgb: Int = 0xFFFF9800.toInt()
)

data class JointWorldTransform(
    val boneId: String,
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val worldAngleDeg: Float
)

data class BonePose(
    val boneAnglesDeg: Map<String, Float> = emptyMap(),
    val rootX: Float = 0f,
    val rootY: Float = 0f
)

data class BoneKeyframe(
    val frameIndex: Int,
    val pose: BonePose,
    val easing: EasingCurve = EasingCurve.EASE_IN_OUT
)

data class SkeletonRig(
    val id: String = java.util.UUID.randomUUID().toString(),
    val rootBoneId: String = "bone_hips",
    val bones: List<Bone> = emptyList(),
    val rootBaseX: Float = 640f,
    val rootBaseY: Float = 360f
) {
    companion object {
        fun createDefaultBiped(): SkeletonRig {
            return SkeletonRig(
                rootBoneId = "bone_hips",
                rootBaseX = 640f,
                rootBaseY = 320f,
                bones = listOf(
                    Bone(id = "bone_hips", name = "Hips", parentId = null, length = 40f, localAngleDeg = -90f, colorArgb = 0xFFFF9800.toInt()),
                    Bone(id = "bone_spine", name = "Spine", parentId = "bone_hips", length = 70f, localAngleDeg = 0f, colorArgb = 0xFFFFB74D.toInt()),
                    Bone(id = "bone_head", name = "Head", parentId = "bone_spine", length = 55f, localAngleDeg = 0f, colorArgb = 0xFFFFCC80.toInt()),
                    
                    // Left Arm (Screen Right/Viewer)
                    Bone(id = "bone_arm_l_up", name = "Arm L Up", parentId = "bone_spine", length = 60f, localAngleDeg = 110f, colorArgb = 0xFF42A5F5.toInt()),
                    Bone(id = "bone_arm_l_low", name = "Arm L Low", parentId = "bone_arm_l_up", length = 55f, localAngleDeg = 15f, colorArgb = 0xFF90CAF9.toInt()),
                    
                    // Right Arm
                    Bone(id = "bone_arm_r_up", name = "Arm R Up", parentId = "bone_spine", length = 60f, localAngleDeg = -110f, colorArgb = 0xFF26A69A.toInt()),
                    Bone(id = "bone_arm_r_low", name = "Arm R Low", parentId = "bone_arm_r_up", length = 55f, localAngleDeg = -15f, colorArgb = 0xFF80CBC4.toInt()),
                    
                    // Left Leg
                    Bone(id = "bone_leg_l_up", name = "Leg L Up", parentId = "bone_hips", length = 75f, localAngleDeg = 170f, colorArgb = 0xFFAB47BC.toInt()),
                    Bone(id = "bone_leg_l_low", name = "Leg L Low", parentId = "bone_leg_l_up", length = 70f, localAngleDeg = 10f, colorArgb = 0xFFCE93D8.toInt()),
                    
                    // Right Leg
                    Bone(id = "bone_leg_r_up", name = "Leg R Up", parentId = "bone_hips", length = 75f, localAngleDeg = 190f, colorArgb = 0xFFEC407A.toInt()),
                    Bone(id = "bone_leg_r_low", name = "Leg R Low", parentId = "bone_leg_r_up", length = 70f, localAngleDeg = -10f, colorArgb = 0xFFF48FB1.toInt())
                )
            )
        }
    }

    /**
     * Solves forward kinematics: calculates world space joint positions given current pose angles
     */
    fun computeWorldTransforms(pose: BonePose?): Map<String, JointWorldTransform> {
        val transforms = mutableMapOf<String, JointWorldTransform>()
        val boneMap = bones.associateBy { it.id }
        val rootX = rootBaseX + (pose?.rootX ?: 0f)
        val rootY = rootBaseY + (pose?.rootY ?: 0f)

        fun resolveBone(boneId: String) {
            if (transforms.containsKey(boneId)) return
            val bone = boneMap[boneId] ?: return

            val startX: Float
            val startY: Float
            val parentWorldAngle: Float

            if (bone.parentId == null) {
                startX = rootX
                startY = rootY
                parentWorldAngle = 0f
            } else {
                resolveBone(bone.parentId)
                val parentTransform = transforms[bone.parentId]
                if (parentTransform != null) {
                    startX = parentTransform.endX
                    startY = parentTransform.endY
                    parentWorldAngle = parentTransform.worldAngleDeg
                } else {
                    startX = rootX
                    startY = rootY
                    parentWorldAngle = 0f
                }
            }

            val poseAngle = pose?.boneAnglesDeg?.get(boneId) ?: 0f
            val worldAngle = parentWorldAngle + bone.localAngleDeg + poseAngle
            val rad = Math.toRadians(worldAngle.toDouble())
            val endX = startX + (bone.length * cos(rad)).toFloat()
            val endY = startY + (bone.length * sin(rad)).toFloat()

            transforms[boneId] = JointWorldTransform(
                boneId = boneId,
                startX = startX,
                startY = startY,
                endX = endX,
                endY = endY,
                worldAngleDeg = worldAngle
            )
        }

        bones.forEach { resolveBone(it.id) }
        return transforms
    }
}
