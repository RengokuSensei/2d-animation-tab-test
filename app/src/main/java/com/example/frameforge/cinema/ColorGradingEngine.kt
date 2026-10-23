package com.example.frameforge.cinema

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorGradingEngine {

    /**
     * Converts Color Temperature in Kelvin (2000K .. 10000K) to normalized RGB multipliers.
     * Uses Tanner-Helland's Planckian locus approximation.
     */
    fun kelvinToRgbMultipliers(kelvin: Float, tint: Float): Triple<Float, Float, Float> {
        val temp = (kelvin / 100f).coerceIn(20f, 100f)
        var red: Float
        var green: Float
        var blue: Float

        // Calculate Red
        if (temp <= 66f) {
            red = 255f
        } else {
            red = temp - 60f
            red = 329.698727446f * (red.toDouble().pow(-0.1332047592)).toFloat()
            red = red.coerceIn(0f, 255f)
        }

        // Calculate Green
        if (temp <= 66f) {
            green = temp
            green = 99.4708025861f * ln(green.toDouble()).toFloat() - 161.1195681661f
            green = green.coerceIn(0f, 255f)
        } else {
            green = temp - 60f
            green = 288.1221695283f * (green.toDouble().pow(-0.0755148492)).toFloat()
            green = green.coerceIn(0f, 255f)
        }

        // Calculate Blue
        if (temp >= 66f) {
            blue = 255f
        } else if (temp <= 19f) {
            blue = 0f
        } else {
            blue = temp - 10f
            blue = 138.5177312231f * ln(blue.toDouble()).toFloat() - 305.0447927307f
            blue = blue.coerceIn(0f, 255f)
        }

        // Apply Green-Magenta Tint adjustment
        val tintFactor = tint / 100f
        green = (green * (1f + tintFactor * 0.2f)).coerceIn(0f, 255f)
        red = (red * (1f - tintFactor * 0.1f)).coerceIn(0f, 255f)
        blue = (blue * (1f - tintFactor * 0.1f)).coerceIn(0f, 255f)

        // Normalize relative to standard D65 (6500K)
        return Triple(red / 255f, green / 255f, blue / 255f)
    }

    /**
     * Synthesizes an Android ColorMatrix from professional grading parameters
     * for zero-overhead GPU-accelerated hardware canvas drawing.
     */
    fun buildColorMatrix(state: ColorGradingState): ColorMatrix {
        val base = ColorMatrix()
        if (!state.isEnabled) return base

        // 1. White Balance / Color Temperature
        val (tempR, tempG, tempB) = kelvinToRgbMultipliers(
            state.whiteBalance.kelvin,
            state.whiteBalance.tint
        )
        val wbMatrix = ColorMatrix(floatArrayOf(
            tempR, 0f, 0f, 0f, 0f,
            0f, tempG, 0f, 0f, 0f,
            0f, 0f, tempB, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        base.postConcat(wbMatrix)

        // 2. ASC CDL & Lift/Gamma/Gain slope/offset synthesis
        val slopeR = (state.cdl.slopeR + state.liftGammaGain.gain.red + state.liftGammaGain.gain.master)
            .coerceAtLeast(0.05f)
        val slopeG = (state.cdl.slopeG + state.liftGammaGain.gain.green + state.liftGammaGain.gain.master)
            .coerceAtLeast(0.05f)
        val slopeB = (state.cdl.slopeB + state.liftGammaGain.gain.blue + state.liftGammaGain.gain.master)
            .coerceAtLeast(0.05f)

        val offR = (state.cdl.offsetR + state.liftGammaGain.lift.red + state.liftGammaGain.offset.red + state.liftGammaGain.lift.master) * 255f
        val offG = (state.cdl.offsetG + state.liftGammaGain.lift.green + state.liftGammaGain.offset.green + state.liftGammaGain.lift.master) * 255f
        val offB = (state.cdl.offsetB + state.liftGammaGain.lift.blue + state.liftGammaGain.offset.blue + state.liftGammaGain.lift.master) * 255f

        val cdlMatrix = ColorMatrix(floatArrayOf(
            slopeR, 0f, 0f, 0f, offR,
            0f, slopeG, 0f, 0f, offG,
            0f, 0f, slopeB, 0f, offB,
            0f, 0f, 0f, 1f, 0f
        ))
        base.postConcat(cdlMatrix)

        // 3. Contrast & Saturation Matrix
        val contrast = state.contrast.coerceIn(0.1f, 3.0f)
        val translate = (-0.5f * contrast + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, translate,
            0f, contrast, 0f, 0f, translate,
            0f, 0f, contrast, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        base.postConcat(contrastMatrix)

        val satMatrix = ColorMatrix()
        satMatrix.setSaturation(state.cdl.saturation.coerceIn(0f, 3f))
        base.postConcat(satMatrix)

        return base
    }

    /**
     * Applies preset adjustments into a concrete state
     */
    fun applyPreset(preset: ColorGradingPreset): ColorGradingState {
        return when (preset) {
            ColorGradingPreset.TEAL_AND_ORANGE -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                liftGammaGain = LiftGammaGain(
                    lift = ColorWheelValues(red = -0.15f, green = 0.05f, blue = 0.20f, master = -0.05f),
                    gain = ColorWheelValues(red = 0.25f, green = 0.10f, blue = -0.15f, master = 0.05f)
                ),
                contrast = 1.25f,
                cdl = AscCdlConfig(saturation = 1.20f)
            )
            ColorGradingPreset.KODAK_VISION3 -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                whiteBalance = ColorTemperatureTint(kelvin = 5200f, tint = 4f),
                liftGammaGain = LiftGammaGain(
                    lift = ColorWheelValues(red = 0.05f, green = 0.02f, blue = -0.04f, master = 0.02f),
                    gain = ColorWheelValues(red = 0.12f, green = 0.08f, blue = -0.05f, master = 0.0f)
                ),
                contrast = 1.12f,
                cdl = AscCdlConfig(saturation = 1.10f)
            )
            ColorGradingPreset.FUJI_ETERNA -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                whiteBalance = ColorTemperatureTint(kelvin = 5800f, tint = -2f),
                contrast = 0.88f,
                cdl = AscCdlConfig(saturation = 0.85f),
                liftGammaGain = LiftGammaGain(
                    lift = ColorWheelValues(master = 0.04f)
                )
            )
            ColorGradingPreset.BLEACH_BYPASS -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                contrast = 1.55f,
                cdl = AscCdlConfig(saturation = 0.40f)
            )
            ColorGradingPreset.MONOCHROME_NOIR -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                contrast = 1.45f,
                cdl = AscCdlConfig(saturation = 0.0f)
            )
            ColorGradingPreset.ANIME_VIBRANT -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                whiteBalance = ColorTemperatureTint(kelvin = 7200f, tint = -6f),
                contrast = 1.20f,
                cdl = AscCdlConfig(saturation = 1.45f),
                liftGammaGain = LiftGammaGain(
                    gain = ColorWheelValues(red = 0.10f, green = 0.15f, blue = 0.25f, master = 0.08f)
                )
            )
            ColorGradingPreset.UPSC_STUDY_WARM -> ColorGradingState(
                isEnabled = true,
                preset = preset,
                whiteBalance = ColorTemperatureTint(kelvin = 3600f, tint = 2f),
                contrast = 1.18f,
                cdl = AscCdlConfig(saturation = 1.05f),
                liftGammaGain = LiftGammaGain(
                    lift = ColorWheelValues(red = 0.08f, green = 0.04f, blue = -0.08f, master = -0.02f)
                )
            )
            ColorGradingPreset.NONE -> ColorGradingState(isEnabled = false, preset = preset)
        }
    }
}
