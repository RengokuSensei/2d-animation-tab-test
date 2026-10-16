package com.example.frameforge.export

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.view.Surface
import java.io.File

/**
 * Hardware-accelerated MP4 (H.264/AVC) video encoder for Android.
 * Encodes animation frames into a standard playable .mp4 video container.
 */
class Mp4Encoder {

    fun encodeFramesToMp4(
        frames: List<Bitmap>,
        outputFile: File,
        fps: Int = 24,
        bitRate: Int = 4_000_000 // 4 Mbps
    ): Boolean {
        if (frames.isEmpty()) return false

        // Dimensions must be even for H.264
        val rawWidth = frames[0].width
        val rawHeight = frames[0].height
        val width = (rawWidth / 2) * 2
        val height = (rawHeight / 2) * 2

        val mimeType = MediaFormat.MIMETYPE_VIDEO_AVC
        val frameDurationUs = (1_000_000L / fps.coerceAtLeast(1))

        var mediaCodec: MediaCodec? = null
        var mediaMuxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        try {
            val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // 1 second keyframe interval
            }

            mediaCodec = MediaCodec.createEncoderByType(mimeType)
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = mediaCodec.createInputSurface()
            mediaCodec.start()

            mediaMuxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            for (i in frames.indices) {
                val frame = frames[i]

                // Render bitmap onto encoder's input surface
                val surfaceCanvas = inputSurface.lockHardwareCanvas()
                surfaceCanvas.drawColor(Color.WHITE) // default white background for video
                if (frame.width != width || frame.height != height) {
                    val scaled = Bitmap.createScaledBitmap(frame, width, height, true)
                    surfaceCanvas.drawBitmap(scaled, 0f, 0f, null)
                } else {
                    surfaceCanvas.drawBitmap(frame, 0f, 0f, null)
                }
                inputSurface.unlockCanvasAndPost(surfaceCanvas)

                // Drain encoder
                while (true) {
                    val encoderStatus = mediaCodec.dequeueOutputBuffer(bufferInfo, 10_000L)
                    if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break
                    } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (muxerStarted) {
                            throw RuntimeException("Format changed after muxer started")
                        }
                        val newFormat = mediaCodec.outputFormat
                        trackIndex = mediaMuxer.addTrack(newFormat)
                        mediaMuxer.start()
                        muxerStarted = true
                    } else if (encoderStatus >= 0) {
                        val encodedData = mediaCodec.getOutputBuffer(encoderStatus)
                            ?: throw RuntimeException("Encoder buffer was null")

                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            bufferInfo.size = 0
                        }

                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            // Correct presentation timestamp (PTS)
                            bufferInfo.presentationTimeUs = i * frameDurationUs
                            mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }

                        mediaCodec.releaseOutputBuffer(encoderStatus, false)
                    }
                }
            }

            // Signal End of Stream
            mediaCodec.signalEndOfInputStream()

            // Final drain
            var eos = false
            while (!eos) {
                val status = mediaCodec.dequeueOutputBuffer(bufferInfo, 20_000L)
                if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                } else if (status >= 0) {
                    val encodedData = mediaCodec.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size != 0 && muxerStarted) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        mediaMuxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    mediaCodec.releaseOutputBuffer(status, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eos = true
                    }
                }
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        } finally {
            try {
                inputSurface?.release()
                mediaCodec?.stop()
                mediaCodec?.release()
                if (mediaMuxer != null) {
                    try { mediaMuxer.stop() } catch (_: Exception) {}
                    mediaMuxer.release()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
