package com.example.frameforge.engine

import com.example.frameforge.model.BonePose
import com.example.frameforge.model.JointWorldTransform
import com.example.frameforge.model.SkeletonRig
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

object InverseKinematics {

    /**
     * Solves analytical 2-bone IK for a limb:
     * upperBone -> lowerBone reaching towards target (targetX, targetY).
     * Returns updated local angle deltas for upper and lower bones.
     */
    fun solveTwoBoneIK(
        rig: SkeletonRig,
        currentPose: BonePose,
        upperBoneId: String,
        lowerBoneId: String,
        targetX: Float,
        targetY: Float,
        flipBend: Boolean = false
    ): BonePose {
        val transforms = rig.computeWorldTransforms(currentPose)
        val upperTrans = transforms[upperBoneId] ?: return currentPose
        val upperBone = rig.bones.find { it.id == upperBoneId } ?: return currentPose
        val lowerBone = rig.bones.find { it.id == lowerBoneId } ?: return currentPose

        val rootX = upperTrans.startX
        val rootY = upperTrans.startY

        val dx = targetX - rootX
        val dy = targetY - rootY
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)

        val l1 = upperBone.length
        val l2 = lowerBone.length

        // Clamp target distance so triangle is solvable
        val clampedDist = dist.coerceIn(0.01f, (l1 + l2) * 0.999f)

        // Law of Cosines for angle at upper joint and angle at lower joint
        val cosAngle1 = ((l1 * l1 + clampedDist * clampedDist - l2 * l2) / (2f * l1 * clampedDist)).coerceIn(-1f, 1f)
        val alpha1 = acos(cosAngle1.toDouble())

        val cosAngle2 = ((l1 * l1 + l2 * l2 - clampedDist * clampedDist) / (2f * l1 * l2)).coerceIn(-1f, 1f)
        val alpha2 = acos(cosAngle2.toDouble())

        val baseTargetAngle = atan2(dy.toDouble(), dx.toDouble())

        val bendSign = if (flipBend) -1.0 else 1.0
        val targetUpperWorldAngleRad = baseTargetAngle + bendSign * alpha1
        val targetLowerWorldAngleRad = targetUpperWorldAngleRad + Math.PI - (bendSign * alpha2)

        val targetUpperWorldDeg = Math.toDegrees(targetUpperWorldAngleRad).toFloat()
        val targetLowerWorldDeg = Math.toDegrees(targetLowerWorldAngleRad).toFloat()

        // Find parent world angle for upper bone
        val parentWorldAngle = if (upperBone.parentId != null) {
            transforms[upperBone.parentId]?.worldAngleDeg ?: 0f
        } else {
            0f
        }

        val newUpperPoseAngle = (targetUpperWorldDeg - parentWorldAngle - upperBone.localAngleDeg)
        val newLowerPoseAngle = (targetLowerWorldDeg - targetUpperWorldDeg - lowerBone.localAngleDeg)

        val newAngles = currentPose.boneAnglesDeg.toMutableMap()
        newAngles[upperBoneId] = normalizeAngle(newUpperPoseAngle)
        newAngles[lowerBoneId] = normalizeAngle(newLowerPoseAngle)

        return currentPose.copy(boneAnglesDeg = newAngles)
    }

    private fun normalizeAngle(angle: Float): Float {
        var a = angle % 360f
        if (a > 180f) a -= 360f
        if (a < -180f) a += 360f
        return a
    }
}
