package com.ranjan.core.util

interface FileFormatDetector {
    fun detectExtension(bytes: ByteArray): String
}
