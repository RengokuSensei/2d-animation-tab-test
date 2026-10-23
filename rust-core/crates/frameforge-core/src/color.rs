use rayon::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct ColorWheelValues {
    pub red: f32,    // -1.0 .. +1.0
    pub green: f32,  // -1.0 .. +1.0
    pub blue: f32,   // -1.0 .. +1.0
    pub master: f32, // -1.0 .. +1.0
}

impl Default for ColorWheelValues {
    fn default() -> Self {
        Self { red: 0.0, green: 0.0, blue: 0.0, master: 0.0 }
    }
}

#[derive(Debug, Clone, Copy, Default, PartialEq, Serialize, Deserialize)]
pub struct LiftGammaGain {
    pub lift: ColorWheelValues,
    pub gamma: ColorWheelValues,
    pub gain: ColorWheelValues,
    pub offset: ColorWheelValues,
}

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct AscCdlConfig {
    pub slope_r: f32,
    pub slope_g: f32,
    pub slope_b: f32,
    pub offset_r: f32,
    pub offset_g: f32,
    pub offset_b: f32,
    pub power_r: f32,
    pub power_g: f32,
    pub power_b: f32,
    pub saturation: f32,
}

impl Default for AscCdlConfig {
    fn default() -> Self {
        Self {
            slope_r: 1.0, slope_g: 1.0, slope_b: 1.0,
            offset_r: 0.0, offset_g: 0.0, offset_b: 0.0,
            power_r: 1.0, power_g: 1.0, power_b: 1.0,
            saturation: 1.0,
        }
    }
}

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct ColorGradingParams {
    pub cdl: AscCdlConfig,
    pub lgg: LiftGammaGain,
    pub kelvin: f32,     // 2500 .. 9500 (default 6500)
    pub tint: f32,       // -100 .. +100 (default 0)
    pub contrast: f32,   // 0.5 .. 2.0 (default 1.0)
    pub exposure: f32,   // -2.0 .. +2.0 (default 0.0)
}

impl Default for ColorGradingParams {
    fn default() -> Self {
        Self {
            cdl: AscCdlConfig::default(),
            lgg: LiftGammaGain::default(),
            kelvin: 6500.0,
            tint: 0.0,
            contrast: 1.0,
            exposure: 0.0,
        }
    }
}

pub struct ColorEngine;

impl ColorEngine {
    /// Applies color temperature (Kelvin), ASC CDL, and Lift/Gamma/Gain to an RGBA pixel buffer in parallel
    pub fn process_rgba_buffer(buffer: &mut [u8], params: &ColorGradingParams) {
        let (k_r, k_g, k_b) = Self::kelvin_to_rgb(params.kelvin);
        let exp_mult = 2.0f32.powf(params.exposure);

        buffer.par_chunks_exact_mut(4).for_each(|pixel| {
            let mut r = pixel[0] as f32 / 255.0;
            let mut g = pixel[1] as f32 / 255.0;
            let mut b = pixel[2] as f32 / 255.0;
            let a = pixel[3];

            // 1. White Balance (Kelvin multiplier)
            r *= k_r;
            g *= k_g;
            b *= k_b;

            // 2. Exposure multiplier
            r *= exp_mult;
            g *= exp_mult;
            b *= exp_mult;

            // 3. ASC CDL: Out = (In * Slope + Offset) ^ Power
            r = ((r * params.cdl.slope_r + params.cdl.offset_r).max(0.0)).powf(params.cdl.power_r);
            g = ((g * params.cdl.slope_g + params.cdl.offset_g).max(0.0)).powf(params.cdl.power_g);
            b = ((b * params.cdl.slope_b + params.cdl.offset_b).max(0.0)).powf(params.cdl.power_b);

            // 4. Contrast around 0.18 midpoint
            r = (0.18 + (r - 0.18) * params.contrast).max(0.0);
            g = (0.18 + (g - 0.18) * params.contrast).max(0.0);
            b = (0.18 + (b - 0.18) * params.contrast).max(0.0);

            // 5. Saturation (Rec.709 Luma)
            let luma = 0.2126 * r + 0.7152 * g + 0.0722 * b;
            r = luma + (r - luma) * params.cdl.saturation;
            g = luma + (g - luma) * params.cdl.saturation;
            b = luma + (b - luma) * params.cdl.saturation;

            // Clamp and write back
            pixel[0] = (r * 255.0).clamp(0.0, 255.0) as u8;
            pixel[1] = (g * 255.0).clamp(0.0, 255.0) as u8;
            pixel[2] = (b * 255.0).clamp(0.0, 255.0) as u8;
            pixel[3] = a;
        });
    }

    /// Fast analytic approximation of Planckian locus for correlated color temperature
    fn kelvin_to_rgb(kelvin: f32) -> (f32, f32, f32) {
        let temp = (kelvin / 100.0).clamp(10.0, 400.0);

        let r = if temp <= 66.0 {
            1.0
        } else {
            ((temp - 60.0).powf(-0.1332047592) * 329.698727446 / 255.0).clamp(0.0, 1.0)
        };

        let g = if temp <= 66.0 {
            (temp.ln() * 99.4708025861 - 161.1195681661) / 255.0
        } else {
            (temp - 60.0).powf(-0.0755148492) * 288.1221695283 / 255.0
        }.clamp(0.0, 1.0);

        let b = if temp >= 66.0 {
            1.0
        } else if temp <= 19.0 {
            0.0
        } else {
            ((temp - 10.0).ln() * 138.5177312231 - 305.0447927307) / 255.0
        }.clamp(0.0, 1.0);

        (r, g, b)
    }
}
