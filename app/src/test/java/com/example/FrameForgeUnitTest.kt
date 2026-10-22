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

    @Test
    fun testRoughShapeGenerator() {
        val rect = com.example.frameforge.ai.RoughShapeGenerator.createRoughRectangle(
            x = 100f, y = 100f, width = 200f, height = 150f,
            roughness = 1.2f, fillStyle = com.example.frameforge.ai.RoughFillStyle.HACHURE
        )
        assertTrue("Rough rectangle should generate multiple strokes", rect.isNotEmpty())
        rect.forEach { stroke ->
            assertTrue("Stroke should contain points", stroke.points.isNotEmpty())
        }

        val diamond = com.example.frameforge.ai.RoughShapeGenerator.createRoughDiamond(
            centerX = 200f, centerY = 200f, width = 160f, height = 120f
        )
        assertTrue("Rough diamond should generate strokes", diamond.isNotEmpty())

        val ellipse = com.example.frameforge.ai.RoughShapeGenerator.createRoughEllipse(
            centerX = 200f, centerY = 200f, radiusX = 80f, radiusY = 60f
        )
        assertTrue("Rough ellipse should generate strokes", ellipse.isNotEmpty())

        val arrow = com.example.frameforge.ai.RoughShapeGenerator.createRoughArrow(
            x1 = 50f, y1 = 50f, x2 = 250f, y2 = 150f, curved = true, label = "Flow"
        )
        assertTrue("Rough arrow should generate stem and head strokes", arrow.size >= 3)

        val note = com.example.frameforge.ai.RoughShapeGenerator.createRoughStickyNote(
            x = 100f, y = 100f, width = 150f, height = 150f, text = "Sprint Plan"
        )
        assertTrue("Rough sticky note should generate border, fold, and fill", note.isNotEmpty())
    }

    @Test
    fun testMermaidDiagramParserAndLayout() {
        val mermaidCode = """
            flowchart TD
                Client[Mobile Client App] --> Auth{Authenticate}
                Auth -->|Success| DB[(User Database)]
                Auth -->|Failure| Error([Error Page])
        """.trimIndent()

        val parsed = com.example.frameforge.ai.MermaidDiagramEngine.parseMermaid(mermaidCode)
        assertEquals("TD", parsed.direction)
        assertEquals(4, parsed.nodes.size)
        assertEquals(3, parsed.edges.size)

        assertEquals(com.example.frameforge.ai.ExcalidrawShapeType.RECTANGLE, parsed.nodes["Client"]?.shape)
        assertEquals(com.example.frameforge.ai.ExcalidrawShapeType.DIAMOND, parsed.nodes["Auth"]?.shape)
        assertEquals(com.example.frameforge.ai.ExcalidrawShapeType.CYLINDER, parsed.nodes["DB"]?.shape)

        val strokes = com.example.frameforge.ai.MermaidDiagramEngine.renderDiagramToStrokes(parsed)
        assertTrue("Rendering diagram should produce vector strokes", strokes.size > 10)
    }

    @Test
    fun testWireframeToCodeEnginePresets() {
        val loginResult = kotlinx.coroutines.runBlocking {
            com.example.frameforge.ai.WireframeToCodeEngine.convertWireframeToCompose(
                "Login with email and password",
                com.example.frameforge.ai.WireframePreset.LOGIN_AUTH
            )
        }
        assertNotNull(loginResult.composeCode)
        assertTrue("Generated code should contain @Composable", loginResult.composeCode.contains("@Composable"))
        assertTrue("Generated code should contain OutlinedTextField", loginResult.composeCode.contains("OutlinedTextField"))
        assertTrue("Wireframe mockup should generate sketched strokes", loginResult.wireframeStrokes.isNotEmpty())

        val feedResult = kotlinx.coroutines.runBlocking {
            com.example.frameforge.ai.WireframeToCodeEngine.convertWireframeToCompose(
                "Social media feed card",
                com.example.frameforge.ai.WireframePreset.SOCIAL_FEED
            )
        }
        assertTrue("Generated code should contain SocialFeedCard", feedResult.composeCode.contains("SocialFeedCard"))
        assertTrue("Feed card should include testTag", feedResult.composeCode.contains("testTag"))
    }

    @Test
    fun testTextToDiagramOfflineFallback() {
        val authMermaid = com.example.frameforge.ai.TextToDiagramEngine.generateOfflineMermaid("User login and authentication token flow")
        assertTrue(authMermaid.contains("flowchart"))
        assertTrue(authMermaid.contains("AuthServer") || authMermaid.contains("User"))

        val k8sMermaid = com.example.frameforge.ai.TextToDiagramEngine.generateOfflineMermaid("Kubernetes pod container cluster deployment")
        assertTrue(k8sMermaid.contains("Ingress") || k8sMermaid.contains("Pod"))
    }

    // --- MASTER CINEMA SYSTEM TESTS ---

    @Test
    fun testClipEditorEngineSplit() {
        val originalClip = com.example.frameforge.cinema.TimelineClip(
            id = "clip_1",
            trackId = "track_main",
            name = "Drawing Cel",
            startFrame = 0,
            durationFrames = 20,
            sourceInFrame = 0,
            speedMultiplier = 1.0f
        )
        val split = com.example.frameforge.cinema.ClipEditorEngine.splitClipAt(originalClip, 8)
        assertNotNull("Splitting at valid playhead should succeed", split)
        val (first, second) = split!!

        assertEquals("First clip starts at 0", 0, first.startFrame)
        assertEquals("First clip duration is 8", 8, first.durationFrames)
        assertEquals("Second clip starts at playhead 8", 8, second.startFrame)
        assertEquals("Second clip duration is 12", 12, second.durationFrames)
        assertEquals("Second clip source in offset is 8", 8, second.sourceInFrame)
    }

    @Test
    fun testClipEditorEngineRippleTrim() {
        val clipA = com.example.frameforge.cinema.TimelineClip(
            id = "clip_a",
            trackId = "track_1",
            name = "Clip A",
            startFrame = 0,
            durationFrames = 10
        )
        val clipB = com.example.frameforge.cinema.TimelineClip(
            id = "clip_b",
            trackId = "track_1",
            name = "Clip B",
            startFrame = 10,
            durationFrames = 15
        )

        // Trim Out-point of Clip A from 10 to 6 with Ripple ON
        val trimResult = com.example.frameforge.cinema.ClipEditorEngine.trimOutPoint(
            clip = clipA,
            newEndFrame = 6,
            isRipple = true,
            subsequentClips = listOf(clipB)
        )

        assertEquals("Trimmed duration is 6", 6, trimResult.trimmedClip.durationFrames)
        assertEquals("Subsequent clip shifted forward by 4 frames", 1, trimResult.shiftedClips.size)
        assertEquals("Clip B shifted start is 6", 6, trimResult.shiftedClips[0].startFrame)
    }

    @Test
    fun testClipEditorEngineTimeStretch() {
        val clip = com.example.frameforge.cinema.TimelineClip(
            id = "stretch_clip",
            trackId = "track_1",
            name = "Stretch Test",
            startFrame = 0,
            durationFrames = 10,
            sourceDuration = 10,
            speedMultiplier = 1.0f
        )

        // 200% Fast-forward (target duration = 5 frames -> speed 2.0x)
        val fastResult = com.example.frameforge.cinema.ClipEditorEngine.timeStretchClip(clip, 5)
        assertEquals(5, fastResult.durationFrames)
        assertEquals(2.0f, fastResult.speedMultiplier, 0.001f)

        // 50% Slow-Mo (target duration = 20 frames -> speed 0.5x)
        val slowResult = com.example.frameforge.cinema.ClipEditorEngine.timeStretchClip(clip, 20)
        assertEquals(20, slowResult.durationFrames)
        assertEquals(0.5f, slowResult.speedMultiplier, 0.001f)
    }

    @Test
    fun testCubicBezierNewtonRaphsonAndVelocity() {
        val curve = com.example.frameforge.cinema.BezierPresets.CINEMA_SNAP

        // At x = 0, y should be 0; at x = 1, y should be 1
        val y0 = curve.evaluate(0f)
        val y1 = curve.evaluate(1f)
        assertEquals(0f, y0, 0.01f)
        assertEquals(1f, y1, 0.01f)

        // Monotonic velocity derivative check
        val velMid = curve.evaluateVelocity(0.5f)
        assertFalse("Velocity should not be NaN", velMid.isNaN())
        assertTrue("Velocity should be positive in forward curve", velMid >= 0f)

        // Elastic overshoot should exceed 1.0 at peak
        val elastic = com.example.frameforge.cinema.BezierPresets.ELASTIC_OVERSHOOT
        var maxVal = 0f
        for (i in 0..100) {
            val v = elastic.evaluate(i / 100f)
            if (v > maxVal) maxVal = v
        }
        assertTrue("Elastic overshoot should exceed 1.0", maxVal > 1.0f)
    }

    @Test
    fun testMultiplaneVirtualCamera3DPerspectiveAndDoF() {
        val camera = com.example.frameforge.cinema.VirtualCamera3D(
            focalLengthMm = 50f,
            focusDistanceZ = 0f,
            aperture = 2.8f,
            dofEnabled = true
        )

        // Foreground layer (Z < 0) should be magnified (scale > 1.0)
        val fgScale = camera.computePerspectiveScale(layerZ = -500f, autoScaleComp = false)
        assertTrue("Foreground layer should have perspective scale > 1.0", fgScale > 1.0f)

        // Background layer (Z > 0) should be demagnified (scale < 1.0)
        val bgScale = camera.computePerspectiveScale(layerZ = 1000f, autoScaleComp = false)
        assertTrue("Background layer should have perspective scale < 1.0", bgScale < 1.0f)

        // Focal plane (Z = 0) has 0 blur; deep layer (Z = 2000) has substantial blur
        val focusBlur = camera.computeDoFBlurRadius(0f)
        val deepBlur = camera.computeDoFBlurRadius(2000f)
        assertEquals("Focal plane has 0 blur radius", 0f, focusBlur, 0.01f)
        assertTrue("Out-of-focus plane has bokeh blur radius", deepBlur > 10f)

        // 2D Projection point
        val (px, py) = camera.projectPoint(x = 960f, y = 540f, z = 0f, canvasWidth = 1920f, canvasHeight = 1080f)
        assertEquals("Center point projects to center on focal plane", 960f, px, 0.1f)
        assertEquals("Center point projects to center on focal plane", 540f, py, 0.1f)
    }

    @Test
    fun testPsychovisualTransitionGeneration() {
        val bmp = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val impactBitmap = com.example.frameforge.cinema.PsychovisualTransitionEngine.generateImpactFrame(
            source = bmp,
            threshold = 0.5f,
            invert = true,
            addSpeedLines = true
        )
        assertNotNull("Impact frame generates bitmap", impactBitmap)

        val strokeA = com.example.frameforge.model.DrawingStroke(
            points = listOf(com.example.frameforge.model.StrokePoint(100f, 100f), com.example.frameforge.model.StrokePoint(150f, 150f)),
            colorArgb = 0xFF000000.toInt(),
            toolType = com.example.frameforge.model.ToolType.PEN,
            size = 10f
        )
        val strokeB = com.example.frameforge.model.DrawingStroke(
            points = listOf(com.example.frameforge.model.StrokePoint(200f, 200f), com.example.frameforge.model.StrokePoint(250f, 250f)),
            colorArgb = 0xFF000000.toInt(),
            toolType = com.example.frameforge.model.ToolType.PEN,
            size = 10f
        )
        val smearEchoes = com.example.frameforge.cinema.PsychovisualTransitionEngine.generateSmearStrokes(
            strokeA = strokeA,
            strokeB = strokeB,
            smearProgress = 0.5f,
            chromaEchoes = true
        )
        assertTrue("Smear echoes generate trailing passes", smearEchoes.isNotEmpty())
    }

    @Test
    fun testAtmosphericParticlesAndLetterbox() {
        val letterbox239 = com.example.frameforge.cinema.CinemaAspectLetterbox.CINEMASCOPE_239
        assertNotNull(letterbox239.ratio)
        assertEquals(2.39f, letterbox239.ratio!!, 0.01f)

        val lightGizmo = com.example.frameforge.cinema.LightSourceGizmo(x = 0.8f, y = 0.2f)
        assertTrue("Light gizmo is enabled by default", lightGizmo.isEnabled)
    }

    @Test
    fun testShiftAndTraceState() {
        val state = com.example.frameforge.cinema.ShiftAndTraceState(
            isActive = true,
            offsetX = 40f,
            offsetY = -20f,
            rotationDeg = 15f,
            scale = 1.2f
        )
        assertTrue("Modified light table pegs should return isModified = true", state.isModified())

        val canonical = com.example.frameforge.cinema.ShiftAndTraceState()
        assertFalse("Default state is not modified", canonical.isModified())
    }
}

