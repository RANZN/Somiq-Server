package com.ranjan.server.media

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

interface MediaStorageService {
    suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String
    suspend fun saveBytes(bytes: ByteArray, subDir: String): String
    fun getFileBytes(subDir: String, fileName: String): ByteArray
    fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String
}

class LocalMediaStorageService : MediaStorageService {
    companion object {
        private const val UPLOADS_DIR = "uploads"
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10 MB
    }

    override suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String = withContext(Dispatchers.IO) {
        val dir = File(UPLOADS_DIR, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val name = "${UUID.randomUUID()}.$ext"
        val file = File(dir, name)

        var totalBytesWritten = 0L
        try {
            file.outputStream().buffered().use { outputStream ->
                val buffer = ByteArray(8192)
                while (true) {
                    val read = inputStream.read(buffer)
                    if (read == -1) break
                    outputStream.write(buffer, 0, read)
                    totalBytesWritten += read
                    if (totalBytesWritten > MAX_FILE_SIZE) {
                        throw IllegalArgumentException("File too large")
                    }
                }
            }
        } catch (e: Exception) {
            file.delete()
            throw e
        }
        file.name
    }

    private fun getExtensionFromBytes(bytes: ByteArray): String {
        if (bytes.size > 8) {
            if (bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte()) {
                return "png"
            }
            if (bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()) {
                return "jpg"
            }
            if (bytes[0] == 'G'.code.toByte() && bytes[1] == 'I'.code.toByte() && bytes[2] == 'F'.code.toByte()) {
                return "gif"
            }
            val typeStr = String(bytes.sliceArray(4 until 12), Charsets.US_ASCII)
            if (typeStr.contains("ftyp")) {
                return "mp4"
            }
        }
        return "jpg"
    }

    override suspend fun saveBytes(bytes: ByteArray, subDir: String): String = withContext(Dispatchers.IO) {
        val dir = File(UPLOADS_DIR, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val ext = getExtensionFromBytes(bytes)
        val name = "${UUID.randomUUID()}.$ext"
        val file = File(dir, name)

        if (bytes.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("File too large")
        }
        file.writeBytes(bytes)
        file.name
    }

    override fun getFileBytes(subDir: String, fileName: String): ByteArray {
        return File(File(UPLOADS_DIR, subDir), fileName).readBytes()
    }

    override fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String {
        return "$baseUrl/$UPLOADS_DIR/$subDir/$fileName"
    }
}
