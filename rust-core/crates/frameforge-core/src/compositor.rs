use rayon::prelude::*;
use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub enum BlendMode {
    Normal,
    Multiply,
    Screen,
    Add,
    Overlay,
}

#[derive(Debug, Clone)]
pub struct ImageBuffer {
    pub width: usize,
    pub height: usize,
    pub data: Vec<u8>, // RGBA 8-bit
}

impl ImageBuffer {
    pub fn new(width: usize, height: usize) -> Self {
        Self {
            width,
            height,
            data: vec![0u8; width * height * 4],
        }
    }

    pub fn from_raw(width: usize, height: usize, data: Vec<u8>) -> Self {
        assert_eq!(data.len(), width * height * 4);
        Self { width, height, data }
    }

    pub fn clear(&mut self, r: u8, g: u8, b: u8, a: u8) {
        self.data.par_chunks_exact_mut(4).for_each(|pixel| {
            pixel[0] = r;
            pixel[1] = g;
            pixel[2] = b;
            pixel[3] = a;
        });
    }

    /// High-performance multi-threaded layer compositing
    pub fn blend_over(&mut self, overlay: &ImageBuffer, blend_mode: BlendMode, opacity: f32) {
        assert_eq!(self.width, overlay.width);
        assert_eq!(self.height, overlay.height);

        let clamped_opacity = opacity.clamp(0.0, 1.0);

        self.data
            .par_chunks_exact_mut(4)
            .zip(overlay.data.par_chunks_exact(4))
            .for_each(|(dest, src)| {
                let src_a = (src[3] as f32 / 255.0) * clamped_opacity;
                if src_a <= 0.001 {
                    return;
                }

                let dest_a = dest[3] as f32 / 255.0;
                let out_a = src_a + dest_a * (1.0 - src_a);

                if out_a <= 0.001 {
                    return;
                }

                let src_r = src[0] as f32 / 255.0;
                let src_g = src[1] as f32 / 255.0;
                let src_b = src[2] as f32 / 255.0;

                let dest_r = dest[0] as f32 / 255.0;
                let dest_g = dest[1] as f32 / 255.0;
                let dest_b = dest[2] as f32 / 255.0;

                let (blended_r, blended_g, blended_b) = match blend_mode {
                    BlendMode::Normal => (src_r, src_g, src_b),
                    BlendMode::Multiply => (dest_r * src_r, dest_g * src_g, dest_b * src_b),
                    BlendMode::Screen => (
                        1.0 - (1.0 - dest_r) * (1.0 - src_r),
                        1.0 - (1.0 - dest_g) * (1.0 - src_g),
                        1.0 - (1.0 - dest_b) * (1.0 - src_b),
                    ),
                    BlendMode::Add => (
                        (dest_r + src_r).min(1.0),
                        (dest_g + src_g).min(1.0),
                        (dest_b + src_b).min(1.0),
                    ),
                    BlendMode::Overlay => (
                        if dest_r < 0.5 { 2.0 * dest_r * src_r } else { 1.0 - 2.0 * (1.0 - dest_r) * (1.0 - src_r) },
                        if dest_g < 0.5 { 2.0 * dest_g * src_g } else { 1.0 - 2.0 * (1.0 - dest_g) * (1.0 - src_g) },
                        if dest_b < 0.5 { 2.0 * dest_b * src_b } else { 1.0 - 2.0 * (1.0 - dest_b) * (1.0 - src_b) },
                    ),
                };

                // Porter-Duff Over composite
                let final_r = (blended_r * src_a + dest_r * dest_a * (1.0 - src_a)) / out_a;
                let final_g = (blended_g * src_a + dest_g * dest_a * (1.0 - src_a)) / out_a;
                let final_b = (blended_b * src_a + dest_b * dest_a * (1.0 - src_a)) / out_a;

                dest[0] = (final_r * 255.0).clamp(0.0, 255.0) as u8;
                dest[1] = (final_g * 255.0).clamp(0.0, 255.0) as u8;
                dest[2] = (final_b * 255.0).clamp(0.0, 255.0) as u8;
                dest[3] = (out_a * 255.0).clamp(0.0, 255.0) as u8;
            });
    }
}
