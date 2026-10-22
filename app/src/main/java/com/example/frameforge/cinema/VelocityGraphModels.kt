package com.example.frameforge.cinema

import kotlin.math.abs

data class BezierPoint(val x: Float, val y: Float)

data class CubicBezierCurve(
    val p1x: Float = 0.25f,
    val p1y: Float = 0.1f,
    val p2x: Float = 0.25f,
    val p2y: Float = 1.0f,
    val name: String = "Custom Bezier"
) {
    /**
     * Evaluates parametric Bezier coordinate at parameter t in [0, 1]
     */
    fun sample(t: Float): BezierPoint {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT
        val oneMinusT2 = oneMinusT * oneMinusT
        val oneMinusT3 = oneMinusT2 * oneMinusT
        val t2 = clampedT * clampedT
        val t3 = t2 * clampedT

        val x = 3f * oneMinusT2 * clampedT * p1x + 3f * oneMinusT * t2 * p2x + t3
        val y = 3f * oneMinusT2 * clampedT * p1y + 3f * oneMinusT * t2 * p2y + t3

        return BezierPoint(x, y)
    }

    /**
     * Solves for parameter t given x using Newton-Raphson with binary search fallback.
     */
    fun solveTForX(targetX: Float): Float {
        val clampedX = targetX.coerceIn(0f, 1f)
        var t = clampedX
        // Newton-Raphson iterations
        for (i in 0 until 8) {
            val currentX = sample(t).x
            val diff = currentX - clampedX
            if (abs(diff) < 0.001f) return t
            val dxdt = evaluateDxDt(t)
            if (abs(dxdt) < 0.0001f) break
            t -= diff / dxdt
            t = t.coerceIn(0f, 1f)
        }

        // Binary search fallback
        var low = 0f
        var high = 1f
        t = clampedX
        while (high - low > 0.001f) {
            val mid = (low + high) / 2f
            val midX = sample(mid).x
            if (midX < clampedX) {
                low = mid
            } else {
                high = mid
            }
            t = mid
        }
        return t
    }

    /**
     * Evaluates y given x in [0, 1]
     */
    fun evaluateY(x: Float): Float {
        val t = solveTForX(x)
        return sample(t).y
    }

    /**
     * Evaluates velocity (derivative dy/dt) representing instantaneous motion speed.
     */
    fun evaluateVelocity(t: Float): Float {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT

        // dy/dt = 3*(1-t)^2*(p1y - 0) + 6*(1-t)*t*(p2y - p1y) + 3*t^2*(1 - p2y)
        val dydt = 3f * oneMinusT * oneMinusT * p1y +
                6f * oneMinusT * clampedT * (p2y - p1y) +
                3f * clampedT * clampedT * (1f - p2y)

        return abs(dydt)
    }

    private fun evaluateDxDt(t: Float): Float {
        val clampedT = t.coerceIn(0f, 1f)
        val oneMinusT = 1f - clampedT
        return 3f * oneMinusT * oneMinusT * p1x +
                6f * oneMinusT * clampedT * (p2x - p1x) +
                3f * clampedT * clampedT * (1f - p2x)
    }

    /**
     * Evaluates y given x in [0, 1]
     */
    fun evaluate(x: Float): Float = evaluateY(x)

    companion object {
        val LINEAR = CubicBezierCurve(0.0f, 0.0f, 1.0f, 1.0f, "Linear 1:1")
        val CINEMA_SNAP = CubicBezierCurve(0.2f, 0.0f, 0.0f, 1.0f, "Cinema Snap")
        val ELASTIC_OVERSHOOT = CubicBezierCurve(0.34f, 1.56f, 0.64f, 1.0f, "Elastic Overshoot")
        val WHIP_ACCEL = CubicBezierCurve(0.7f, 0.0f, 0.9f, 0.2f, "Whip Acceleration")
        val EXPONENTIAL_BRAKE = CubicBezierCurve(0.1f, 0.9f, 0.2f, 1.0f, "Exponential Brake")
        val HEARTBEAT_PULSE = CubicBezierCurve(0.25f, 0.1f, 0.25f, 1.0f, "Heartbeat Pulse")
        val STEPPED_STOP_MOTION = CubicBezierCurve(0.0f, 0.0f, 0.0f, 1.0f, "Stepped Stop-Motion")

        fun defaultPresets(): List<CubicBezierCurve> = listOf(
            CINEMA_SNAP,
            ELASTIC_OVERSHOOT,
            WHIP_ACCEL,
            EXPONENTIAL_BRAKE,
            HEARTBEAT_PULSE,
            STEPPED_STOP_MOTION,
            LINEAR
        )
    }
}

object BezierPresets {
    val LINEAR = CubicBezierCurve.LINEAR
    val CINEMA_SNAP = CubicBezierCurve.CINEMA_SNAP
    val ELASTIC_OVERSHOOT = CubicBezierCurve.ELASTIC_OVERSHOOT
    val WHIP_ACCEL = CubicBezierCurve.WHIP_ACCEL
    val EXPONENTIAL_BRAKE = CubicBezierCurve.EXPONENTIAL_BRAKE
    val HEARTBEAT_PULSE = CubicBezierCurve.HEARTBEAT_PULSE
    val STEPPED_STOP_MOTION = CubicBezierCurve.STEPPED_STOP_MOTION
}

enum class VelocityGraphMode(val label: String) {
    VALUE("Value Curve (Position / Rotation)"),
    VELOCITY("Velocity Speed Curve (dv/dt)")
}

data class VelocityCurveState(
    val isOpen: Boolean = false,
    val selectedProperty: String = "Position X",
    val curve: CubicBezierCurve = CubicBezierCurve.CINEMA_SNAP,
    val mode: VelocityGraphMode = VelocityGraphMode.VALUE,
    val isStepMotionActive: Boolean = false,
    val stepHoldFrames: Int = 2 // On-twos (2) or on-threes (3)
)
