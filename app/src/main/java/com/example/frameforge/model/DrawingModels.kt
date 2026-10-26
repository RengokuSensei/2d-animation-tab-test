package com.example.frameforge.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class ToolType(val label: String, val iconDescription: String) {
    PENCIL("Pencil", "Textured graphite pencil"),
    PEN("Studio Pen", "Crisp uniform technical pen"),
    INK("Fountain Ink", "Expressive calligraphic fountain pen"),
    BRUSH("Artist Brush", "Soft blended artist brush"),
    CHARCOAL("Charcoal / Chalk", "Grainy expressive charcoal"),
    WATERCOLOR("Watercolor / Wash", "Semi-transparent layered marker"),
    AIRBRUSH("Airbrush", "Soft feathered spray"),
    PIXEL("Pixel Brush", "Hard aliased pixel art brush"),
    SMUDGE("Smudge", "Blend and mix canvas strokes"),
    ERASER("Eraser", "Clean stroke eraser"),
    SOFT_ERASER("Soft Eraser", "Kneaded eraser with feathered edge"),
    FILL("ColorDrop", "Scanline flood fill with tolerance"),
    LASSO("Lasso Selection", "Freehand selection ribbon"),
    RECT_SELECT("Rectangle Selection", "Rectangular marquee"),
    ELLIPSE_SELECT("Ellipse Selection", "Elliptical marquee"),
    TRANSFORM("Transform", "Move, scale, rotate, distort"),
    BONE_RIG("Bones & IK", "Character skeleton and inverse kinematics"),
    EYEDROPPER("Eyedropper", "Sample canvas color"),
    HIGHLIGHTER("Smart Highlighter", "Smart text highlighter with Multiply blend"),
    LASER_POINTER("Laser Pointer", "Vibrant glowing laser pointer with Screen blend")
}

enum class PalmRejectionMode(val label: String) {
    AUTOMATIC("Smart Auto (Rejects large palm surface)"),
    STYLUS_ONLY("Stylus Only (Fingers pan/zoom exclusively)"),
    FINGER_AND_STYLUS("Touch & Stylus (Both finger & pen draw)")
}

enum class PressureCurve(val label: String) {
    LINEAR("Linear (Standard 1:1)"),
    SOFT("Soft (Bolder lines with light touch)"),
    HARD("Hard (Requires firmer pressure for heavy lines)"),
    EXPONENTIAL("S-Curve (High dynamic contrast)")
}

data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val tiltDeg: Float = 0f
)

data class DrawingStroke(
    val id: String = java.util.UUID.randomUUID().toString(),
    val points: List<StrokePoint>,
    val colorArgb: Int,
    val toolType: ToolType,
    val size: Float,
    val opacity: Float = 1.0f,
    val smoothing: Float = 0.5f,
    val isSmudge: Boolean = false
) {
    val color: Color get() = Color(colorArgb)
}

enum class BlendModeType(val label: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    ADD("Add"),
    COLOR_DODGE("Color Dodge"),
    DIFFERENCE("Difference")
}

data class AnimationLayer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isAlphaLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val blendMode: BlendModeType = BlendModeType.NORMAL,
    val onionSkinEnabled: Boolean = true,
    val posZ: Float = 0f,
    val rotX: Float = 0f,
    val rotY: Float = 0f,
    val autoScaleComp: Boolean = true,
    val onionSkinPinned: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f
)

data class DrawingCel(
    val id: String = java.util.UUID.randomUUID().toString(),
    val frameIndex: Int,
    val layerId: String,
    val strokes: List<DrawingStroke> = emptyList(),
    val holdDuration: Int = 1,
    val isKeyframe: Boolean = true
)

// --- PROCREATE BRUSH STUDIO MODELS ---

enum class BrushCategory(val title: String) {
    SKETCHING("Sketching"),
    INKING("Inking"),
    DRAWING("Drawing"),
    PAINTING("Painting"),
    ARTISTIC("Artistic"),
    AIRBRUSHING("Airbrushing"),
    CHARCOALS("Charcoals"),
    SPECIAL_FX("Special FX")
}

data class BrushDefinition(
    val id: String,
    val name: String,
    val category: BrushCategory,
    val toolType: ToolType,
    val defaultSize: Float = 16f,
    val defaultOpacity: Float = 1.0f,
    val spacing: Float = 0.1f, // 0.05 to 1.0
    val streamline: Float = 0.5f, // stroke stabilization
    val jitter: Float = 0f,
    val pressureSizeFactor: Float = 0.8f,
    val pressureOpacityFactor: Float = 0.5f,
    val wetMix: Float = 0.3f
) {
    companion object {
        fun defaultBrushes(): List<BrushDefinition> {
            return listOf(
                BrushDefinition("brush_peppermint", "Peppermint", BrushCategory.SKETCHING, ToolType.PENCIL, defaultSize = 8f, defaultOpacity = 0.7f, spacing = 0.08f, streamline = 0.3f),
                BrushDefinition("brush_derwent", "Derwent Pencil", BrushCategory.SKETCHING, ToolType.PENCIL, defaultSize = 12f, defaultOpacity = 0.85f, spacing = 0.1f, streamline = 0.4f),
                BrushDefinition("brush_6b", "6B Pencil", BrushCategory.SKETCHING, ToolType.PENCIL, defaultSize = 16f, defaultOpacity = 0.9f, spacing = 0.12f, streamline = 0.2f),
                BrushDefinition("brush_smart_highlighter", "Smart Highlighter", BrushCategory.INKING, ToolType.HIGHLIGHTER, defaultSize = 28f, defaultOpacity = 0.85f, spacing = 0.05f, streamline = 0.7f),
                BrushDefinition("brush_studio_pen", "Studio Pen", BrushCategory.INKING, ToolType.PEN, defaultSize = 10f, defaultOpacity = 1.0f, spacing = 0.05f, streamline = 0.75f),
                BrushDefinition("brush_technical_pen", "Technical Pen", BrushCategory.INKING, ToolType.PEN, defaultSize = 6f, defaultOpacity = 1.0f, spacing = 0.04f, streamline = 0.8f),
                BrushDefinition("brush_syrup", "Syrup Ink", BrushCategory.INKING, ToolType.INK, defaultSize = 14f, defaultOpacity = 1.0f, spacing = 0.06f, streamline = 0.6f),
                BrushDefinition("brush_gel_pen", "Gel Pen", BrushCategory.INKING, ToolType.PEN, defaultSize = 8f, defaultOpacity = 1.0f, spacing = 0.05f, streamline = 0.65f),
                BrushDefinition("brush_round", "Round Brush", BrushCategory.PAINTING, ToolType.BRUSH, defaultSize = 24f, defaultOpacity = 0.85f, spacing = 0.08f, streamline = 0.5f, wetMix = 0.4f),
                BrushDefinition("brush_wet_acrylic", "Wet Acrylic", BrushCategory.PAINTING, ToolType.BRUSH, defaultSize = 32f, defaultOpacity = 0.75f, spacing = 0.1f, streamline = 0.45f, wetMix = 0.7f),
                BrushDefinition("brush_gouache", "Gouache", BrushCategory.PAINTING, ToolType.WATERCOLOR, defaultSize = 28f, defaultOpacity = 0.9f, spacing = 0.09f, streamline = 0.4f),
                BrushDefinition("brush_tarraleah", "Tarraleah", BrushCategory.ARTISTIC, ToolType.CHARCOAL, defaultSize = 36f, defaultOpacity = 0.8f, spacing = 0.15f, streamline = 0.3f),
                BrushDefinition("brush_hartz", "Hartz Canvas", BrushCategory.ARTISTIC, ToolType.CHARCOAL, defaultSize = 40f, defaultOpacity = 0.7f, spacing = 0.18f, streamline = 0.25f),
                BrushDefinition("brush_soft_airbrush", "Soft Airbrush", BrushCategory.AIRBRUSHING, ToolType.AIRBRUSH, defaultSize = 48f, defaultOpacity = 0.4f, spacing = 0.05f, streamline = 0.5f),
                BrushDefinition("brush_medium_nozzle", "Medium Nozzle", BrushCategory.AIRBRUSHING, ToolType.AIRBRUSH, defaultSize = 32f, defaultOpacity = 0.6f, spacing = 0.06f, streamline = 0.55f),
                BrushDefinition("brush_hard_airbrush", "Hard Airbrush", BrushCategory.AIRBRUSHING, ToolType.AIRBRUSH, defaultSize = 20f, defaultOpacity = 0.85f, spacing = 0.05f, streamline = 0.6f),
                BrushDefinition("brush_willow_charcoal", "Willow Charcoal", BrushCategory.CHARCOALS, ToolType.CHARCOAL, defaultSize = 26f, defaultOpacity = 0.75f, spacing = 0.14f, streamline = 0.3f),
                BrushDefinition("brush_vine_charcoal", "Vine Charcoal", BrushCategory.CHARCOALS, ToolType.CHARCOAL, defaultSize = 34f, defaultOpacity = 0.65f, spacing = 0.16f, streamline = 0.2f),
                BrushDefinition("brush_neon_highlighter", "Neon Fluorescent Marker", BrushCategory.SPECIAL_FX, ToolType.HIGHLIGHTER, defaultSize = 34f, defaultOpacity = 0.8f, spacing = 0.05f, streamline = 0.65f),
                BrushDefinition("brush_laser_pointer", "Laser Pointer & Reveal", BrushCategory.SPECIAL_FX, ToolType.LASER_POINTER, defaultSize = 16f, defaultOpacity = 1.0f, spacing = 0.04f, streamline = 0.85f)
            )
        }
    }
}

// --- PROCREATE ADJUSTMENTS / FILTERS MODELS ---

enum class AdjustmentType(val label: String) {
    NONE("None"),
    GAUSSIAN_BLUR("Gaussian Blur"),
    MOTION_BLUR("Motion Blur"),
    BLOOM("Bloom / Neon Glow"),
    HSB("Hue, Saturation, Brightness"),
    COLOR_BALANCE("Color Balance"),
    CHROMATIC_ABERRATION("Chromatic Aberration"),
    HALFTONE("Halftone"),
    LIQUIFY("Liquify Studio")
}

data class AdjustmentState(
    val activeType: AdjustmentType = AdjustmentType.NONE,
    val intensity: Float = 0.35f, // 0..1 slider
    val secondaryParam: Float = 0.5f
)

// --- SELECTION & TRANSFORM MODELS ---

enum class SelectionMode(val label: String) {
    NONE("None"),
    FREEHAND("Freehand"),
    AUTOMATIC("Automatic"),
    RECTANGLE("Rectangle"),
    ELLIPSE("Ellipse")
}

data class SelectionState(
    val mode: SelectionMode = SelectionMode.NONE,
    val isActive: Boolean = false,
    val points: List<Offset> = emptyList(),
    val feather: Float = 0f,
    val isInverted: Boolean = false
)

enum class TransformMode(val label: String) {
    FREEFORM("Freeform"),
    UNIFORM("Uniform"),
    DISTORT("Distort"),
    WARP("Warp")
}

data class TransformState(
    val isActive: Boolean = false,
    val mode: TransformMode = TransformMode.FREEFORM,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationDeg: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    val snapping: Boolean = true
)


enum class ActionsTab(val title: String) {
    ADD("Add"),
    CANVAS("Canvas"),
    SHARE("Share"),
    PREFS("Prefs")
}

enum class ColorPickerTab(val title: String) {
    DISC("Disc"),
    CLASSIC("Classic"),
    HARMONY("Harmony"),
    VALUE("Value"),
    PALETTES("Palettes")
}

