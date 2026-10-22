package com.example

import com.example.frameforge.ai.AiMotionAssistant
import com.example.frameforge.engine.DrawingEngine
import com.example.frameforge.engine.InverseKinematics
import com.example.frameforge.engine.ProceduralMotion
import com.example.frameforge.model.BonePose
import com.example.frameforge.model.SkeletonRig
import com.example.frameforge.model.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FrameForgeUnitTest {

    @Test
    fun testDefaultSkeletonHierarchy() {
        val rig = SkeletonRig.createDefaultBiped()
        assertTrue("Rig should contain bones", rig.bones.isNotEmpty())
        assertEquals("Root bone should be bone_hips", "bone_hips", rig.rootBoneId)

        val transforms = rig.computeWorldTransforms(null)
        assertTrue("Should calculate world transforms for all bones", transforms.size == rig.bones.size)
        assertNotNull(transforms["bone_head"])
        assertNotNull(transforms["bone_arm_r_low"])
    }

    @Test
    fun testProceduralWalkCycleGeneration() {
        val keyframes = ProceduralMotion.generateKeyframes(
            motionType = ProceduralMotion.MotionType.WALK,
            totalFrames = 16
        )
        assertEquals(16, keyframes.size)
        keyframes.forEach { kf ->
            assertTrue(kf.pose.boneAnglesDeg.containsKey("bone_leg_l_up"))
            assertTrue(kf.pose.boneAnglesDeg.containsKey("bone_arm_l_up"))
            assertFalse(kf.pose.rootY.isNaN())
        }
    }

    @Test
    fun testProceduralWaveGestureGeneration() {
        val keyframes = ProceduralMotion.generateKeyframes(
            motionType = ProceduralMotion.MotionType.WAVE,
            totalFrames = 12
        )
        assertEquals(12, keyframes.size)
        val firstArm = keyframes[0].pose.boneAnglesDeg["bone_arm_r_low"] ?: 0f
        val midArm = keyframes[6].pose.boneAnglesDeg["bone_arm_r_low"] ?: 0f
        assertTrue("Right arm should animate in wave gesture", firstArm != midArm)
    }

    @Test
    fun testTwoBoneInverseKinematics() {
        val rig = SkeletonRig.createDefaultBiped()
        val initialPose = BonePose()
        val updatedPose = InverseKinematics.solveTwoBoneIK(
            rig = rig,
            currentPose = initialPose,
            upperBoneId = "bone_arm_r_up",
            lowerBoneId = "bone_arm_r_low",
            targetX = 640f + 80f,
            targetY = 320f + 60f
        )

        val upperAngle = updatedPose.boneAnglesDeg["bone_arm_r_up"]
        val lowerAngle = updatedPose.boneAnglesDeg["bone_arm_r_low"]
        assertNotNull(upperAngle)
        assertNotNull(lowerAngle)
        assertFalse(upperAngle!!.isNaN())
        assertFalse(lowerAngle!!.isNaN())
    }

    @Test
    fun testAiTextToMotionPrompt() {
        val walkResult = AiMotionAssistant.generateMotionFromPrompt("make the character walk quickly", 16)
        assertEquals(16, walkResult.totalFrames)
        assertEquals(16, walkResult.keyframes.size)

        val waveResult = AiMotionAssistant.generateMotionFromPrompt("wave at camera", 14)
        assertEquals(14, waveResult.keyframes.size)
    }

    @Test
    fun testInBetweenBoneKeyframes() {
        val poseA = BonePose(boneAnglesDeg = mapOf("bone_head" to 0f))
        val poseB = BonePose(boneAnglesDeg = mapOf("bone_head" to 30f))

        val inBetweens = AiMotionAssistant.generateInBetweenBoneKeyframes(
            startFrame = 0,
            startPose = poseA,
            endFrame = 4,
            endPose = poseB
        )

        assertEquals("Should create 3 in-between frames between 0 and 4", 3, inBetweens.size)
        val middleHead = inBetweens[1].pose.boneAnglesDeg["bone_head"] ?: 0f
        assertTrue("Interpolated angle should be between 0 and 30", middleHead in 5f..25f)
    }

    @Test
    fun testStrokeSmoothing() {
        val raw = listOf(
            StrokePoint(0f, 0f),
            StrokePoint(10f, 20f),
            StrokePoint(20f, 15f),
            StrokePoint(30f, 35f)
        )
        val smoothed = DrawingEngine.smoothPoints(raw, 0.5f)
        assertEquals(raw.size, smoothed.size)
        assertEquals(raw[0].x, smoothed[0].x, 0.001f)
    }

    @Test
    fun testNodeGraphInitialization() {
        val graph = com.example.frameforge.model.AnimationNodeGraph()
        assertTrue("Node graph should contain default nodes", graph.nodes.isNotEmpty())
        assertTrue("Node graph should contain default connections", graph.connections.isNotEmpty())
        val hasLayerNode = graph.nodes.any { it.nodeType == com.example.frameforge.model.AnimationNodeType.LAYER_SOURCE }
        val hasAiNode = graph.nodes.any { it.nodeType == com.example.frameforge.model.AnimationNodeType.AI_MOTION }
        val hasOutputNode = graph.nodes.any { it.nodeType == com.example.frameforge.model.AnimationNodeType.COMPOSITE_OUTPUT }
        assertTrue(hasLayerNode && hasAiNode && hasOutputNode)
    }

    @Test
    fun testBlendModesAndCurves() {
        val blendModes = com.example.frameforge.model.BlendModeType.values()
        assertTrue(blendModes.contains(com.example.frameforge.model.BlendModeType.MULTIPLY))
        assertTrue(blendModes.contains(com.example.frameforge.model.BlendModeType.SCREEN))
        assertTrue(blendModes.contains(com.example.frameforge.model.BlendModeType.OVERLAY))

        val curves = com.example.frameforge.model.PressureCurve.values()
        assertTrue(curves.contains(com.example.frameforge.model.PressureCurve.LINEAR))
        assertTrue(curves.contains(com.example.frameforge.model.PressureCurve.SOFT))
        assertTrue(curves.contains(com.example.frameforge.model.PressureCurve.HARD))
        assertTrue(curves.contains(com.example.frameforge.model.PressureCurve.EXPONENTIAL))
    }

    @Test
    fun testProcreateBrushStudioPresets() {
        val brushes = com.example.frameforge.model.BrushDefinition.defaultBrushes()
        assertTrue("Should provide rich brush library", brushes.size >= 12)
        val hasSketching = brushes.any { it.category == com.example.frameforge.model.BrushCategory.SKETCHING }
        val hasInking = brushes.any { it.category == com.example.frameforge.model.BrushCategory.INKING }
        val hasPainting = brushes.any { it.category == com.example.frameforge.model.BrushCategory.PAINTING }
        val hasAirbrushing = brushes.any { it.category == com.example.frameforge.model.BrushCategory.AIRBRUSHING }
        assertTrue(hasSketching && hasInking && hasPainting && hasAirbrushing)
    }

    @Test
    fun testCanvasResolutionPresetsUpTo4K() {
        val presets = com.example.frameforge.model.CanvasResolutionPreset.values()
        val uhd4k = presets.find { it == com.example.frameforge.model.CanvasResolutionPreset.UHD_4K }
        assertNotNull(uhd4k)
        assertEquals(3840, uhd4k!!.width)
        assertEquals(2160, uhd4k.height)

        val fhd = presets.find { it == com.example.frameforge.model.CanvasResolutionPreset.FULL_HD_1080 }
        assertNotNull(fhd)
        assertEquals(1920, fhd!!.width)
        assertEquals(1080, fhd.height)
    }

    @Test
    fun testAudioTrackWaveformGeneration() {
        val waveform = com.example.frameforge.model.AudioTrack.generateSampleWaveform(60)
        assertEquals(60, waveform.size)
        waveform.forEach { peak ->
            assertTrue("Waveform peak should be normalized between 0.1 and 1.0", peak in 0.05f..1.0f)
        }
    }

    @Test
    fun testPdfPageRangeParserComprehensive() {
        // Standard range parsing
        val range1 = com.example.frameforge.pdf.PdfEngine.parsePageRange("1-3, 5, 8-10", 12)
        assertEquals(setOf(0, 1, 2, 4, 7, 8, 9), range1)

        // Single page
        val single = com.example.frameforge.pdf.PdfEngine.parsePageRange("4", 8)
        assertEquals(setOf(3), single)

        // Empty and blank strings
        assertTrue(com.example.frameforge.pdf.PdfEngine.parsePageRange("", 8).isEmpty())
        assertTrue(com.example.frameforge.pdf.PdfEngine.parsePageRange("   ", 8).isEmpty())

        // Out of bounds and clamping
        val clamped = com.example.frameforge.pdf.PdfEngine.parsePageRange("1-20", 5)
        assertEquals(setOf(0, 1, 2, 3, 4), clamped)

        // Inverted range order (e.g. 5-3)
        val inverted = com.example.frameforge.pdf.PdfEngine.parsePageRange("5-3", 10)
        assertEquals(setOf(2, 3, 4), inverted)

        // Invalid tokens gracefully ignored
        val invalidTokens = com.example.frameforge.pdf.PdfEngine.parsePageRange("1, abc, -5, 2-x, 3", 6)
        assertEquals(setOf(0, 2), invalidTokens)
    }

    @Test
    fun testWhiteToAlphaExtractionFilter() {
        // Create 2x2 test bitmap: TopLeft=White, TopRight=Black, BottomLeft=Red, BottomRight=LightGray
        val bmp = android.graphics.Bitmap.createBitmap(2, 2, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.setPixel(0, 0, android.graphics.Color.WHITE) // #FFFFFFFF
        bmp.setPixel(1, 0, android.graphics.Color.BLACK) // #FF000000
        bmp.setPixel(0, 1, android.graphics.Color.RED)   // #FFFF0000
        bmp.setPixel(1, 1, android.graphics.Color.rgb(250, 250, 250)) // Near-white paper

        val extracted = com.example.frameforge.pdf.PdfEngine.extractWhiteToAlpha(bmp, tolerance = 0.12f, invert = false)

        // Pure white should have alpha 0 (completely transparent)
        val whiteAlpha = android.graphics.Color.alpha(extracted.getPixel(0, 0))
        assertEquals("Pure white background must be 100% transparent", 0, whiteAlpha)

        // Pure black must remain fully opaque (alpha 255)
        val blackAlpha = android.graphics.Color.alpha(extracted.getPixel(1, 0))
        assertEquals("Black ink lines must remain fully opaque", 255, blackAlpha)

        // Near-white paper should have zero or near-zero alpha
        val lightAlpha = android.graphics.Color.alpha(extracted.getPixel(1, 1))
        assertTrue("Light paper should be transparent or faded", lightAlpha < 30)

        // Red drawing should remain visible
        val redAlpha = android.graphics.Color.alpha(extracted.getPixel(0, 1))
        assertTrue("Colored drawing should retain opacity", redAlpha > 200)
    }

    @Test
    fun testDarkModeInkInversion() {
        val bmp = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        bmp.setPixel(0, 0, android.graphics.Color.BLACK)

        val inverted = com.example.frameforge.pdf.PdfEngine.extractWhiteToAlpha(bmp, tolerance = 0.12f, invert = true)
        val pixel = inverted.getPixel(0, 0)
        assertEquals(255, android.graphics.Color.red(pixel))
        assertEquals(255, android.graphics.Color.green(pixel))
        assertEquals(255, android.graphics.Color.blue(pixel))
    }

    @Test
    fun testSmartHighlighterAndLaserPointerBrushes() {
        val brushes = com.example.frameforge.model.BrushDefinition.defaultBrushes()
        val highlighter = brushes.find { it.toolType == com.example.frameforge.model.ToolType.HIGHLIGHTER }
        assertNotNull("Should include Smart Highlighter brush", highlighter)

        val laserPointer = brushes.find { it.toolType == com.example.frameforge.model.ToolType.LASER_POINTER }
        assertNotNull("Should include Laser Pointer brush", laserPointer)

        val hasSpecialFx = brushes.any { it.category == com.example.frameforge.model.BrushCategory.SPECIAL_FX }
        assertTrue("Should include Special FX brush category", hasSpecialFx)
    }
}
