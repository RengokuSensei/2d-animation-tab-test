package com.example.frameforge.engine

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import com.example.frameforge.log.AppLogger

object VideoFrameCache {

    private val lruCache = object : LruCache<String, Bitmap>(24) {
        override fun entryRemoved(evicted: Boolean, key: String?, oldValue: Bitmap?, newValue: Bitmap?) {
            // Older bitmaps will be garbage collected naturally
        }
    }

    private var currentUriStr: String? = null
    private var retriever: MediaMetadataRetriever? = null

    @Synchronized
    fun getFrame(context: Context, videoUriStr: String, frameIndex: Int, fps: Int): Bitmap? {
        val cacheKey = "$videoUriStr#$frameIndex@$fps"
        lruCache.get(cacheKey)?.let { return it }

        return try {
            if (currentUriStr != videoUriStr || retriever == null) {
                retriever?.release()
                retriever = MediaMetadataRetriever().apply {
                    setDataSource(context, Uri.parse(videoUriStr))
                }
                currentUriStr = videoUriStr
            }

            val safeFps = fps.coerceIn(1, 60)
            val timeMicros = (frameIndex.toLong() * 1_000_000L) / safeFps
            val bmp = retriever?.getFrameAtTime(timeMicros, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            if (bmp != null) {
                lruCache.put(cacheKey, bmp)
            }
            bmp
        } catch (e: Exception) {
            AppLogger.e("VIDEO_DECODER", "Error extracting video frame at frame $frameIndex", e)
            null
        }
    }

    fun clear() {
        try {
            retriever?.release()
            retriever = null
            currentUriStr = null
            lruCache.evictAll()
        } catch (_: Exception) {}
    }
}
