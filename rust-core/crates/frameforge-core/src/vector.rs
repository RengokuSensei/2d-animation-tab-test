use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Copy, PartialEq, Serialize, Deserialize)]
pub struct StrokePoint {
    pub x: f32,
    pub y: f32,
    pub pressure: f32,
    pub tilt_deg: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct VectorStroke {
    pub id: String,
    pub points: Vec<StrokePoint>,
    pub color_argb: u32,
    pub size: f32,
    pub opacity: f32,
    pub smoothing: f32,
    pub is_eraser: Boolean,
}

type Boolean = bool;

impl VectorStroke {
    pub fn new(id: impl Into<String>, color_argb: u32, size: f32, opacity: f32) -> Self {
        Self {
            id: id.into(),
            points: Vec::new(),
            color_argb,
            size,
            opacity,
            smoothing: 0.5,
            is_eraser: false,
        }
    }

    pub fn add_point(&mut self, x: f32, y: f32, pressure: f32, tilt_deg: f32) {
        self.points.push(StrokePoint {
            x,
            y,
            pressure: pressure.clamp(0.01, 1.0),
            tilt_deg,
        });
    }

    /// Catmull-Rom spline interpolation for ultra-smooth 144Hz stylus curve generation
    pub fn generate_smooth_path(&self, subdivisions: usize) -> Vec<StrokePoint> {
        let n = self.points.len();
        if n < 4 {
            return self.points.clone();
        }

        let mut smoothed = Vec::with_capacity(n * subdivisions);
        smoothed.push(self.points[0]);

        for i in 0..n - 3 {
            let p0 = self.points[i];
            let p1 = self.points[i + 1];
            let p2 = self.points[i + 2];
            let p3 = self.points[i + 3];

            for step in 1..=subdivisions {
                let t = step as f32 / subdivisions as f32;
                let t2 = t * t;
                let t3 = t2 * t;

                // Catmull-Rom matrix blending
                let x = 0.5 * ((2.0 * p1.x) +
                    (-p0.x + p2.x) * t +
                    (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * t2 +
                    (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * t3);

                let y = 0.5 * ((2.0 * p1.y) +
                    (-p0.y + p2.y) * t +
                    (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * t2 +
                    (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * t3);

                let pressure = p1.pressure + (p2.pressure - p1.pressure) * t;
                let tilt = p1.tilt_deg + (p2.tilt_deg - p1.tilt_deg) * t;

                smoothed.push(StrokePoint { x, y, pressure, tilt_deg: tilt });
            }
        }

        smoothed.push(self.points[n - 1]);
        smoothed
    }
}
