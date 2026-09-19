package com.ranjan.core.storage.gcs

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.StorageUrlResolver
import com.ranjan.core.util.FileFormatDetector
import com.ranjan.core.util.MimeTypeResolver
import com.ranjan.core.util.impl.DefaultMimeTypeResolver
import com.ranjan.core.util.impl.MagicByteFileFormatDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

class GcsMediaStorageService(
    private val bucketName: String,
    private val storage: Storage,
    private val urlResolver: StorageUrlResolver,
    private val mimeTypeResolver: MimeTypeResolver = DefaultMimeTypeResolver(),
    private val fileFormatDetector: FileFormatDetector = MagicByteFileFormatDetector()
) : MediaStorageService {

    override suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String = withContext(Dispatchers.IO) {
        val cleanExt = ext.trim().removePrefix(".")
        val fileName = "${UUID.randomUUID()}.$cleanExt"
        val blobInfo = buildBlobInfo(subDir, fileName, cleanExt)

        storage.createFrom(blobInfo, inputStream)
        fileName
    }

    override suspend fun saveBytes(bytes: ByteArray, subDir: String): String = withContext(Dispatchers.IO) {
        val ext = fileFormatDetector.detectExtension(bytes)
        val fileName = "${UUID.randomUUID()}.$ext"
        val blobInfo = buildBlobInfo(subDir, fileName, ext)

        storage.create(blobInfo, bytes)
        fileName
    }

    override suspend fun getFileBytes(subDir: String, fileName: String): ByteArray = withContext(Dispatchers.IO) {
        val key = buildObjectKey(subDir, fileName)
        storage.readAllBytes(BlobId.of(bucketName, key))
    }

    override fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String {
        return urlResolver.buildFileUrl(baseUrl, subDir, fileName)
    }

    override fun getUrlForRelativePath(baseUrl: String, relativePath: String): String {
        return urlResolver.resolveRelativePath(baseUrl, relativePath)
    }

    private fun buildObjectKey(subDir: String, fileName: String): String {
        val cleanSubDir = subDir.trim('/')
        val cleanFileName = fileName.trimStart('/')
        return if (cleanSubDir.isEmpty()) cleanFileName else "$cleanSubDir/$cleanFileName"
    }

    private fun buildBlobInfo(subDir: String, fileName: String, ext: String): BlobInfo {
        val key = buildObjectKey(subDir, fileName)
        val blobId = BlobId.of(bucketName, key)
        val contentType = mimeTypeResolver.resolveContentType(ext)

        return BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .build()
    }
}
