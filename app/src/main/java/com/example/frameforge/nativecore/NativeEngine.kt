package com.example.frameforge.nativecore

import android.util.Log
import java.nio.ByteBuffer

/**
 * High-performance Native Rust Engine interface for FrameForge.
 * Executes SIMD color science, Catmull-Rom spline calculations, and DAG graph layout.
 */
object NativeEngine {
    private const val TAG = "FrameForgeNative"
    private var isLoaded = false

    init {
        try {
            System.loadLibrary("frameforge")
            isLoaded = true
            Log.i(TAG, "Successfully loaded native Rust engine: libframeforge.so")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native library libframeforge.so not found, falling back to Kotlin implementations", e)
            isLoaded = false
        }
    }

    fun isNativeLoaded(): Boolean = isLoaded

    external fun nativeInit(): Boolean

    external fun nativeSmoothStroke(
        xs: FloatArray,
        ys: FloatArray,
        pressures: FloatArray,
        tilts: FloatArray,
        subdivisions: Int = 4
    ): String?

    external fun nativeApplyColorGrade(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        kelvin: Float,
        exposure: Float,
        contrast: Float,
        saturation: Float
    ): Boolean

    external fun nativeAutoArrangeGraph(
        graphJson: String,
        colWidth: Float,
        rowHeight: Float,
        startX: Float,
        startY: Float
    ): String?
}
