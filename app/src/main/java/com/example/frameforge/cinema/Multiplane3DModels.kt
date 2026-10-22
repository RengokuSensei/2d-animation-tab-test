package com.example.frameforge.cinema

import kotlin.math.abs
import kotlin.math.max

data class MultiplaneLayer3D(
    val layerId: String,
    val name: String,
    val posZ: Float = 0f, // -2000f (extreme foreground) to +5000f (deep background)
    val rotX: Float = 0f, // Pitch degrees
    val rotY: Float = 0f, // Yaw degrees
    val rotZ: Float = 0f, // Roll degrees
    val autoScaleCompensation: Boolean = true, // Keeps framing constant at home camera position
    val isPinnedToCamera: Boolean = false // UI/HUD overlay layers that ignore camera motion
)

data class VirtualCamera3D(
    val truckX: Float = 0f,     // Horizontal camera shift
    val pedestalY: Float = 0f,  // Vertical camera shift
    val dollyZ: Float = 0f,     // Zoom/Dolly forward into 3D multiplane space
    val tiltX: Float = 0f,      // Pitch
    val panY: Float = 0f,       // Yaw
    val rollZ: Float = 0f,      // Roll
    val focalLengthMm: Float = 35f, // 18mm (wide dramatic) to 135mm (telephoto flat)
    val focusDistanceZ: Float = 0f, // Focal plane depth
    val aperture: Float = 2.8f,     // f/1.4 to f/22 (controls Depth-of-Field bokeh blur radius)
    val dofEnabled: Boolean = true
) {
    // Reference focal distance in pixels
    val focalDistancePx: Float get() = focalLengthMm * 30f

    /**
     * Calculates the perspective scale factor s = f / (f + (z - dollyZ))
     */
    fun computePerspectiveScale(layerZ: Float, autoScaleComp: Boolean = false): Float {
        val relativeZ = layerZ - dollyZ
        val f = focalDistancePx
        val denom = (f + relativeZ).coerceAtLeast(10f)
        val rawScale = f / denom

        return if (autoScaleComp) {
            // Initial compensation factor at home camera position (dollyZ = 0)
            val homeComp = (f + layerZ).coerceAtLeast(10f) / f
            rawScale * homeComp
        } else {
            rawScale
        }
    }

    /**
     * Calculates the optical Depth-of-Field (DoF) Bokeh blur radius for a layer.
     */
    fun computeDoFBlurRadius(layerZ: Float): Float {
        if (!dofEnabled) return 0f
        val deltaZ = abs(layerZ - focusDistanceZ)
        // Blur radius increases with distance from focal plane and wider apertures (lower f-stop)
        val apertureFactor = (5.6f / aperture.coerceAtLeast(0.7f))
        val blurRadius = (deltaZ / 100f) * apertureFactor * 0.8f
        return blurRadius.coerceIn(0f, 40f)
    }

    /**
     * Computes the 2D projected screen coordinate from a 3D point.
     */
    fun projectPoint(x: Float, y: Float, z: Float, canvasWidth: Float, canvasHeight: Float): Pair<Float, Float> {
        val scale = computePerspectiveScale(z, false)
        val cx = canvasWidth / 2f
        val cy = canvasHeight / 2f

        val projX = cx + (x - cx - truckX) * scale
        val projY = cy + (y - cy - pedestalY) * scale
        return Pair(projX, projY)
    }
}

data class MultiplaneStageState(
    val isOrbitActive: Boolean = false,
    val orbitAzimuthDeg: Float = 25f,
    val orbitElevationDeg: Float = 15f,
    val camera: VirtualCamera3D = VirtualCamera3D(),
    val layerPlanes: List<MultiplaneLayer3D> = emptyList(),
    val selectedLayerId: String? = null
)
