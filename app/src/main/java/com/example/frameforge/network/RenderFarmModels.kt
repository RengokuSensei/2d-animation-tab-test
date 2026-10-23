package com.example.frameforge.network

import java.util.UUID

enum class RenderJobState(val label: String) {
    IDLE("Idle / Disconnected"),
    DISCOVERING("Scanning Local Network..."),
    CONNECTED("Connected to GPU Node"),
    UPLOADING("Uploading Scene & Vector Assets..."),
    RENDERING("Rendering on NVIDIA GTX 1650..."),
    COMPLETED("Render Complete! Download Ready"),
    FAILED("Render Failed / Node Offline")
}

data class RenderFarmNode(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Studio Workstation (i7-11800H + GTX 1650)",
    val hostIp: String = "192.168.1.100",
    val port: Int = 8765,
    val isOnline: Boolean = true,
    val gpuDescription: String = "NVIDIA GeForce GTX 1650 Mobile (4GB VRAM)",
    val cpuCores: Int = 16,
    val activeRenderFps: Float = 28.5f
)

data class RenderJobRequest(
    val projectId: String,
    val projectName: String,
    val targetWidth: Int = 1920,
    val targetHeight: Int = 1080,
    val targetFps: Int = 24,
    val startFrame: Int = 0,
    val endFrame: Int = 240,
    val codec: String = "h264_nvenc",
    val bitrateMbps: Int = 45 // Broadcast documentary master quality
)

data class RenderProgressUpdate(
    val state: RenderJobState = RenderJobState.IDLE,
    val currentFrame: Int = 0,
    val totalFrames: Int = 240,
    val progressPercentage: Float = 0f,
    val currentFps: Float = 0f,
    val estimatedTimeRemainingSeconds: Int = 0,
    val completedVideoUrl: String? = null,
    val errorMessage: String? = null
)
