package com.ranjan.core.util.impl

import com.ranjan.core.util.FileFormatDetector

class MagicByteFileFormatDetector : FileFormatDetector {

    override fun detectExtension(bytes: ByteArray): String {
        if (bytes.size > 8) {
            // PNG signature
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte()) {
                return "png"
            }
            // JPEG signature
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return "jpg"
            }
            // GIF signature
            if (bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
                return "gif"
            }
            // WebP signature: RIFF....WEBP
            if (bytes.size >= 12 &&
                bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte() &&
                bytes[8] == 'W'.code.toByte() && bytes[9] == 'E'.code.toByte() &&
                bytes[10] == 'B'.code.toByte() && bytes[11] == 'P'.code.toByte()
            ) {
                return "webp"
            }
            // MP4 container signature: ftyp
            if (bytes.size >= 12) {
                val typeStr = String(bytes.sliceArray(4 until 12), Charsets.US_ASCII)
                if (typeStr.contains("ftyp")) {
                    return "mp4"
                }
            }
        }
        return "jpg"
    }
}
