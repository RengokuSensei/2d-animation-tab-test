package com.example.frameforge.pdf

import android.graphics.Bitmap

/**
 * Modes for importing PDF pages into the animation studio.
 */
enum class PdfImportMode(val title: String, val description: String) {
    SEQUENTIAL_CELS(
        title = "Sequential Timeline Slides",
        description = "Places each page sequentially along the timeline with hold duration. Scrub & draw over each page."
    ),
    SEPARATE_LAYERS(
        title = "Independent Keyframable Layers",
        description = "Imports pages onto separate tracks at current playhead. Animate with Transform Cage & Keyframes."
    ),
    STITCHED_SCROLL(
        title = "Continuous Document Strip",
        description = "Stitches pages vertically into a seamless scrolling track with automatic camera scroll keyframes."
    )
}

/**
 * Render resolution presets for PDF vector-to-bitmap rasterization.
 */
enum class PdfResolutionPreset(val label: String, val maxDimensionPx: Int) {
    STANDARD_1080P("Standard 1080p", 1920),
    HIGH_RES_2K("High-Res 2K", 2560),
    ULTRA_CRISP_4K("Ultra Crisp 4K", 3840)
}

/**
 * Thumbnail and metadata for a single PDF page.
 */
data class PdfPageInfo(
    val pageIndex: Int, // 0-based
    val displayPageNumber: Int = pageIndex + 1, // 1-based
    val widthPt: Int,
    val heightPt: Int,
    val thumbnailBitmap: Bitmap? = null
)

/**
 * Full configuration chosen in the PDF Import Studio dialog.
 */
data class PdfImportConfig(
    val selectedPages: Set<Int> = emptySet(), // 0-based
    val importMode: PdfImportMode = PdfImportMode.SEQUENTIAL_CELS,
    val resolutionPreset: PdfResolutionPreset = PdfResolutionPreset.STANDARD_1080P,
    val holdDurationFrames: Int = 24, // e.g., 24 frames = 1 second at 24fps
    val removeWhiteBackground: Boolean = false,
    val invertColors: Boolean = false,
    val createLinkedDrawingLayerAbove: Boolean = true,
    val paintDirectlyOnPdf: Boolean = false
)

/**
 * Active PDF track data attached to the animation project.
 */
data class PdfDocumentTrack(
    val isEnabled: Boolean = false,
    val documentTitle: String = "",
    val totalDocumentPages: Int = 0,
    val importMode: PdfImportMode = PdfImportMode.SEQUENTIAL_CELS,
    val pageIndices: List<Int> = emptyList(), // 0-based
    val holdFramesPerPage: Int = 24,
    val removeWhiteBackground: Boolean = false,
    val invertColors: Boolean = false,
    val opacity: Float = 1.0f,
    val isUnderlay: Boolean = true,
    val isPaintDirectlyOnPdf: Boolean = false,
    val stitchedScrollDurationFrames: Int = 96,
    val resolutionPreset: PdfResolutionPreset = PdfResolutionPreset.STANDARD_1080P,
    val cacheSessionId: String = java.util.UUID.randomUUID().toString()
)
