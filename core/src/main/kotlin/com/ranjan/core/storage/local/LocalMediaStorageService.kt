package com.ranjan.core.storage.local

import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.StorageUrlResolver
import com.ranjan.core.util.FileFormatDetector
import com.ranjan.core.util.impl.MagicByteFileFormatDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

class LocalMediaStorageService(
    private val uploadsDir: String = DEFAULT_UPLOADS_DIR,
    private val urlResolver: StorageUrlResolver = LocalStorageUrlResolver(uploadsDir),
    private val fileFormatDetector: FileFormatDetector = MagicByteFileFormatDetector()
) : MediaStorageService {
    companion object {
        const val DEFAULT_UPLOADS_DIR = "uploads"
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10 MB
    }

    override suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String = withContext(Dispatchers.IO) {
        val dir = File(uploadsDir, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val cleanExt = ext.trim().removePrefix(".")
        val name = "${UUID.randomUUID()}.$cleanExt"
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

    override suspend fun saveBytes(bytes: ByteArray, subDir: String): String = withContext(Dispatchers.IO) {
        val dir = File(uploadsDir, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val ext = fileFormatDetector.detectExtension(bytes)
        val name = "${UUID.randomUUID()}.$ext"
        val file = File(dir, name)

        if (bytes.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("File too large")
        }
        file.writeBytes(bytes)
        file.name
    }

    override suspend fun getFileBytes(subDir: String, fileName: String): ByteArray = withContext(Dispatchers.IO) {
        File(File(uploadsDir, subDir), fileName).readBytes()
    }

    override fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String {
        return urlResolver.buildFileUrl(baseUrl, subDir, fileName)
    }

    override fun getUrlForRelativePath(baseUrl: String, relativePath: String): String {
        return urlResolver.resolveRelativePath(baseUrl, relativePath)
    }
}
