package com.example.frameforge.export

import android.graphics.Bitmap
import android.graphics.Color
import java.io.ByteArrayOutputStream
import java.io.OutputStream

/**
 * Pure Kotlin GIF89a encoder for exporting 2D animation frames to GIF.
 */
class GifEncoder {
    private var width: Int = 0
    private var height: Int = 0
    private var delayMs: Int = 100 // default 10 fps -> 100ms
    private var repeat: Int = 0 // 0 = loop forever
    private val out = ByteArrayOutputStream()
    private var isStarted = false

    fun start(w: Int, h: Int, fps: Int, loop: Boolean = true): Boolean {
        width = w
        height = h
        delayMs = if (fps > 0) (1000 / fps).coerceAtLeast(20) else 100
        repeat = if (loop) 0 else -1
        isStarted = true
        out.reset()
        writeHeader()
        return true
    }

    fun addFrame(bitmap: Bitmap): Boolean {
        if (!isStarted) return false
        val resized = if (bitmap.width != width || bitmap.height != height) {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }

        val pixels = IntArray(width * height)
        resized.getPixels(pixels, 0, width, 0, 0, width, height)

        // Simple NeuQuant / uniform color quantizer (256 colors)
        val (indexedPixels, palette, transparentIndex) = quantizeFrame(pixels, width, height)

        writeGraphicControlExtension(delayMs, transparentIndex)
        writeImageDescriptor()
        writeColorTable(palette)
        writeLzwData(indexedPixels)

        return true
    }

    fun finish(): ByteArray {
        if (isStarted) {
            out.write(0x3B) // GIF trailer
            isStarted = false
        }
        return out.toByteArray()
    }

    private fun writeHeader() {
        writeString("GIF89a")
        // Logical Screen Descriptor
        writeShort(width)
        writeShort(height)
        out.write(0x70) // GCT flag (0), Color Resolution (7), Sort (0), GCT Size (0)
        out.write(0)    // Background Color Index
        out.write(0)    // Pixel Aspect Ratio

        // Netscape application block for looping
        if (repeat >= 0) {
            out.write(0x21) // Extension Introducer
            out.write(0xFF) // Application Extension
            out.write(11)   // Block size
            writeString("NETSCAPE2.0")
            out.write(3)    // Sub-block size
            out.write(1)    // Sub-block ID
            writeShort(repeat)
            out.write(0)    // Block terminator
        }
    }

    private fun writeGraphicControlExtension(delay: Int, transparentIndex: Int) {
        out.write(0x21) // Extension Introducer
        out.write(0xF9) // Graphic Control Label
        out.write(4)    // Block size
        val transpFlag = if (transparentIndex >= 0) 1 else 0
        val disposalMethod = 2 // Restore to background
        out.write((disposalMethod shl 2) or transpFlag)
        writeShort(delay / 10) // 1/100ths of second
        out.write(if (transparentIndex >= 0) transparentIndex else 0)
        out.write(0)    // Block terminator
    }

    private fun writeImageDescriptor() {
        out.write(0x2C) // Image Separator
        writeShort(0)   // Image Left
        writeShort(0)   // Image Top
        writeShort(width)
        writeShort(height)
        out.write(0x87) // Local Color Table present, 256 colors (2^(7+1))
    }

    private fun writeColorTable(palette: IntArray) {
        for (i in 0 until 256) {
            val color = if (i < palette.size) palette[i] else 0
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            out.write(r)
            out.write(g)
            out.write(b)
        }
    }

    private fun quantizeFrame(pixels: IntArray, w: Int, h: Int): Triple<ByteArray, IntArray, Int> {
        val total = w * h
        val indexed = ByteArray(total)
        val paletteList = mutableListOf<Int>()
        val colorToIndex = mutableMapOf<Int, Byte>()
        var transparentIndex = -1

        // Reserve index 0 for transparent if needed
        paletteList.add(0x00000000)
        colorToIndex[0] = 0.toByte()

        for (i in 0 until total) {
            val argb = pixels[i]
            val a = Color.alpha(argb)
            if (a < 64) {
                // Transparent
                indexed[i] = 0
                transparentIndex = 0
            } else {
                // Quantize to 5-5-5 RGB to bound palette
                val r = (Color.red(argb) shr 3) shl 3
                val g = (Color.green(argb) shr 3) shl 3
                val b = (Color.blue(argb) shr 3) shl 3
                val qColor = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

                val idx = colorToIndex[qColor]
                if (idx != null) {
                    indexed[i] = idx
                } else {
                    if (paletteList.size < 256) {
                        val newIdx = paletteList.size.toByte()
                        paletteList.add(qColor)
                        colorToIndex[qColor] = newIdx
                        indexed[i] = newIdx
                    } else {
                        // Find closest match in palette
                        var bestDist = Int.MAX_VALUE
                        var bestIdx: Byte = 1
                        for (p in 1 until paletteList.size) {
                            val pc = paletteList[p]
                            val dr = Color.red(pc) - r
                            val dg = Color.green(pc) - g
                            val db = Color.blue(pc) - b
                            val dist = dr * dr + dg * dg + db * db
                            if (dist < bestDist) {
                                bestDist = dist
                                bestIdx = p.toByte()
                            }
                        }
                        indexed[i] = bestIdx
                    }
                }
            }
        }

        return Triple(indexed, paletteList.toIntArray(), transparentIndex)
    }

    private fun writeLzwData(pixels: ByteArray) {
        val initCodeSize = 8
        out.write(initCodeSize)

        val clearCode = 1 shl initCodeSize
        val eoiCode = clearCode + 1
        var codeSize = initCodeSize + 1
        var maxCode = (1 shl codeSize) - 1

        val prefixTable = ShortArray(5003) { -1 }
        val suffixTable = ByteArray(5003)
        val codeTable = ShortArray(5003) { -1 }

        var nextCode = eoiCode + 1

        var bitBuffer = 0
        var bitCount = 0
        val packet = ByteArray(256)
        var packetLen = 0

        fun flushPacket() {
            if (packetLen > 0) {
                out.write(packetLen)
                out.write(packet, 0, packetLen)
                packetLen = 0
            }
        }

        fun emitCode(code: Int) {
            bitBuffer = bitBuffer or (code shl bitCount)
            bitCount += codeSize
            while (bitCount >= 8) {
                packet[packetLen++] = (bitBuffer and 0xFF).toByte()
                if (packetLen == 254) flushPacket()
                bitBuffer = bitBuffer ushr 8
                bitCount -= 8
            }
        }

        fun resetDict() {
            prefixTable.fill(-1)
            codeTable.fill(-1)
            codeSize = initCodeSize + 1
            maxCode = (1 shl codeSize) - 1
            nextCode = eoiCode + 1
        }

        emitCode(clearCode)
        if (pixels.isEmpty()) {
            emitCode(eoiCode)
            if (bitCount > 0) packet[packetLen++] = (bitBuffer and 0xFF).toByte()
            flushPacket()
            out.write(0)
            return
        }

        var prefix = (pixels[0].toInt() and 0xFF)

        for (i in 1 until pixels.size) {
            val suffix = pixels[i].toInt() and 0xFF
            val hash = ((prefix shl 8) or suffix) % 5003
            var h = if (hash < 0) hash + 5003 else hash
            var found = false

            while (prefixTable[h] != (-1).toShort()) {
                if (prefixTable[h].toInt() == prefix && suffixTable[h].toInt() == suffix) {
                    prefix = codeTable[h].toInt()
                    found = true
                    break
                }
                h = (h + 31) % 5003
            }

            if (!found) {
                emitCode(prefix)
                if (nextCode < 4096) {
                    prefixTable[h] = prefix.toShort()
                    suffixTable[h] = suffix.toByte()
                    codeTable[h] = nextCode.toShort()
                    nextCode++
                    if (nextCode > maxCode && codeSize < 12) {
                        codeSize++
                        maxCode = (1 shl codeSize) - 1
                    }
                } else {
                    emitCode(clearCode)
                    resetDict()
                }
                prefix = suffix
            }
        }

        emitCode(prefix)
        emitCode(eoiCode)

        if (bitCount > 0) {
            packet[packetLen++] = (bitBuffer and 0xFF).toByte()
        }
        flushPacket()
        out.write(0) // block terminator
    }

    private fun writeString(s: String) {
        for (c in s) out.write(c.code)
    }

    private fun writeShort(v: Int) {
        out.write(v and 0xFF)
        out.write((v shr 8) and 0xFF)
    }
}
