package com.example.frameforge.cinema

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Professional Color Grading Models inspired by DaVinci Resolve, ASC CDL, and Film Emulations.
 */

data class ColorWheelValues(
    val red: Float = 0f,    // -1.0 .. +1.0
    val green: Float = 0f,  // -1.0 .. +1.0
    val blue: Float = 0f,   // -1.0 .. +1.0
    val master: Float = 0f  // -1.0 .. +1.0 (Master wheel luminance)
)

data class LiftGammaGain(
    val lift: ColorWheelValues = ColorWheelValues(),    // Shadows
    val gamma: ColorWheelValues = ColorWheelValues(),   // Midtones (1.0 neutral)
    val gain: ColorWheelValues = ColorWheelValues(),    // Highlights (1.0 neutral)
    val offset: ColorWheelValues = ColorWheelValues()   // Master Global Offset
)

/**
 * ASC CDL (American Society of Cinematographers Color Decision List)
 * Formula: Out = (In * Slope + Offset) ^ Power, followed by Saturation
 */
data class AscCdlConfig(
    val slopeR: Float = 1.0f,
    val slopeG: Float = 1.0f,
    val slopeB: Float = 1.0f,
    val offsetR: Float = 0.0f,
    val offsetG: Float = 0.0f,
    val offsetB: Float = 0.0f,
    val powerR: Float = 1.0f,
    val powerG: Float = 1.0f,
    val powerB: Float = 1.0f,
    val saturation: Float = 1.0f
)

data class ColorTemperatureTint(
    val kelvin: Float = 6500f,   // 2000K (candle) .. 10000K (cool sky)
    val tint: Float = 0f         // -100 (magenta) .. +100 (green)
)

data class SplineControlPoint(
    val x: Float, // 0.0 .. 1.0
    val y: Float  // 0.0 .. 1.0
)

data class ColorCurves(
    val masterCurve: List<SplineControlPoint> = listOf(SplineControlPoint(0f, 0f), SplineControlPoint(1f, 1f)),
    val redCurve: List<SplineControlPoint> = listOf(SplineControlPoint(0f, 0f), SplineControlPoint(1f, 1f)),
    val greenCurve: List<SplineControlPoint> = listOf(SplineControlPoint(0f, 0f), SplineControlPoint(1f, 1f)),
    val blueCurve: List<SplineControlPoint> = listOf(SplineControlPoint(0f, 0f), SplineControlPoint(1f, 1f))
)

enum class ColorGradingPreset(val label: String, val description: String) {
    NONE("Neutral / Rec.709", "Standard natural sRGB pass-through"),
    TEAL_AND_ORANGE("Teal & Orange Blockbuster", "Warm skin tones with deep teal/cyan shadows"),
    KODAK_VISION3("Kodak Vision3 500T", "Rich organic film emulsion with golden highlight roll-off"),
    FUJI_ETERNA("Fuji Eterna Cinema", "Soft pastel tones, low saturation cinematic softness"),
    BLEACH_BYPASS("Bleach Bypass Silver", "Desaturated, gritty high-contrast silver halide look"),
    MONOCHROME_NOIR("High-Contrast Film Noir", "Deep velvety blacks and punchy photographic whites"),
    ANIME_VIBRANT("Makoto Shinkai Skies", "Hyper-saturated azure skies and glowing sunset gradients"),
    UPSC_STUDY_WARM("Warm Tungsten Study", "Warm 3200K desk lamp glow with crisp high-contrast black ink")
}

data class ColorGradingState(
    val isEnabled: Boolean = false,
    val preset: ColorGradingPreset = ColorGradingPreset.NONE,
    val liftGammaGain: LiftGammaGain = LiftGammaGain(),
    val cdl: AscCdlConfig = AscCdlConfig(),
    val whiteBalance: ColorTemperatureTint = ColorTemperatureTint(),
    val contrast: Float = 1.0f,       // 0.5 .. 2.0
    val highlights: Float = 0f,       // -1.0 .. +1.0 (Highlight recovery / compression)
    val shadows: Float = 0f,          // -1.0 .. +1.0 (Shadow boost / crush)
    val vibrance: Float = 0f,         // Smart skin-tone preserving saturation
    val curves: ColorCurves = ColorCurves()
)
