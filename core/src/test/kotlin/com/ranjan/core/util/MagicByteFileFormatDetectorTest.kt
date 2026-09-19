package com.ranjan.core.util

import com.ranjan.core.util.impl.MagicByteFileFormatDetector
import kotlin.test.Test
import kotlin.test.assertEquals

class MagicByteFileFormatDetectorTest {

    private val detector = MagicByteFileFormatDetector()

    @Test
    fun `test small and edge case byte arrays do not throw exceptions`() {
        assertEquals("jpg", detector.detectExtension(byteArrayOf()))
        assertEquals("jpg", detector.detectExtension(byteArrayOf(1, 2, 3)))
        assertEquals("jpg", detector.detectExtension(ByteArray(8)))
        assertEquals("jpg", detector.detectExtension(ByteArray(9)))
        assertEquals("jpg", detector.detectExtension(ByteArray(11)))
    }

    @Test
    fun `test PNG detection`() {
        val pngHeader = byteArrayOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
            0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(), 0x00.toByte()
        )
        assertEquals("png", detector.detectExtension(pngHeader))
    }

    @Test
    fun `test JPEG detection`() {
        val jpegHeader = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(),
            0x00.toByte(), 0x10.toByte(), 0x4A.toByte(), 0x46.toByte(), 0x49.toByte()
        )
        assertEquals("jpg", detector.detectExtension(jpegHeader))
    }

    @Test
    fun `test GIF detection`() {
        val gifHeader = "GIF89a".toByteArray(Charsets.US_ASCII) + ByteArray(5)
        assertEquals("gif", detector.detectExtension(gifHeader))
    }

    @Test
    fun `test WebP detection`() {
        val webpBytes = ByteArray(14)
        webpBytes[0] = 'R'.code.toByte()
        webpBytes[1] = 'I'.code.toByte()
        webpBytes[2] = 'F'.code.toByte()
        webpBytes[3] = 'F'.code.toByte()
        webpBytes[8] = 'W'.code.toByte()
        webpBytes[9] = 'E'.code.toByte()
        webpBytes[10] = 'B'.code.toByte()
        webpBytes[11] = 'P'.code.toByte()
        assertEquals("webp", detector.detectExtension(webpBytes))
    }

    @Test
    fun `test MP4 detection`() {
        val mp4Bytes = ByteArray(16)
        val ftyp = "ftyp".toByteArray(Charsets.US_ASCII)
        System.arraycopy(ftyp, 0, mp4Bytes, 4, 4)
        assertEquals("mp4", detector.detectExtension(mp4Bytes))
    }
}
