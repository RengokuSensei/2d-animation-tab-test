package com.example.frameforge.cinema

enum class ChromaKeyType(val label: String, val defaultColorArgb: Int) {
    GREEN_SCREEN("Green Screen (Studio Chroma)", 0xFF00FF00.toInt()),
    BLUE_SCREEN("Blue Screen (VFX Stage)", 0xFF0000FF.toInt()),
    CUSTOM_COLOR("Custom Eyedropper Key", 0xFF000000.toInt())
}

enum class DespillAlgorithm(val label: String) {
    AVERAGE("Average (Clean balanced roll-off)"),
    MAX_COLOR("Max Adjacent (Aggressive green suppression)"),
    WEIGHTED("Weighted Film Despill (Preserves warm skin tones)")
}

data class ChromaKeyConfig(
    val isEnabled: Boolean = false,
    val keyType: ChromaKeyType = ChromaKeyType.GREEN_SCREEN,
    val keyColorArgb: Int = ChromaKeyType.GREEN_SCREEN.defaultColorArgb,
    val similarity: Float = 0.38f,       // 0.0 .. 1.0 (Color distance threshold)
    val smoothness: Float = 0.15f,       // 0.0 .. 1.0 (Soft transition band)
    val despillIntensity: Float = 0.85f, // 0.0 .. 1.0 (Suppresses green reflection on skin/clothes)
    val despillAlgorithm: DespillAlgorithm = DespillAlgorithm.WEIGHTED,
    val matteChoke: Float = 1.0f,        // 0.0 .. 10.0 (Shrinks boundary to eliminate green fringe)
    val matteBlurRadius: Float = 1.5f,   // 0.0 .. 10.0 (Smooth pixelated jagged edges)
    val shadowPreservation: Float = 0.35f,// 0.0 .. 1.0 (Restores natural floor contact shadows)
    val showMatteOnly: Boolean = false   // Black/White matte diagnostic mode
)
