package com.ranjan.core.storage.stratus

import com.ranjan.core.storage.MediaStorageService
import com.ranjan.core.storage.StorageUrlResolver
import com.ranjan.core.util.FileFormatDetector
import com.ranjan.core.util.MimeTypeResolver
import com.ranjan.core.util.impl.DefaultMimeTypeResolver
import com.ranjan.core.util.impl.MagicByteFileFormatDetector
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID

class StratusMediaStorageService(
    private val bucketUrl: String,
    private val httpClient: HttpClient,
    private val tokenProvider: ZohoTokenProvider,
    private val urlResolver: StorageUrlResolver,
    private val mimeTypeResolver: MimeTypeResolver = DefaultMimeTypeResolver(),
    private val fileFormatDetector: FileFormatDetector = MagicByteFileFormatDetector()
) : MediaStorageService {

    override suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String {
        val cleanExt = ext.trim().removePrefix(".")
        val fileName = "${UUID.randomUUID()}.$cleanExt"
        val bytes = withContext(Dispatchers.IO) { inputStream.readBytes() }

        upload(buildObjectKey(subDir, fileName), bytes, mimeTypeResolver.resolveContentType(cleanExt))
        return fileName
    }

    override suspend fun saveBytes(bytes: ByteArray, subDir: String): String {
        val ext = fileFormatDetector.detectExtension(bytes)
        val fileName = "${UUID.randomUUID()}.$ext"

        upload(buildObjectKey(subDir, fileName), bytes, mimeTypeResolver.resolveContentType(ext))
        return fileName
    }

    override suspend fun getFileBytes(subDir: String, fileName: String): ByteArray {
        val key = buildObjectKey(subDir, fileName)
        val response = httpClient.get(objectUrl(key)) {
            header(HttpHeaders.Authorization, "Zoho-oauthtoken ${tokenProvider.token()}")
        }
        response.ensureSuccess("download", key)
        return response.bodyAsBytes()
    }

    override fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String {
        return urlResolver.buildFileUrl(baseUrl, subDir, fileName)
    }

    override fun getUrlForRelativePath(baseUrl: String, relativePath: String): String {
        return urlResolver.resolveRelativePath(baseUrl, relativePath)
    }

    private suspend fun upload(key: String, bytes: ByteArray, contentType: String) {
        val response = httpClient.put(objectUrl(key)) {
            header(HttpHeaders.Authorization, "Zoho-oauthtoken ${tokenProvider.token()}")
            header("overwrite", "true")
            contentType(ContentType.parse(contentType))
            setBody(bytes)
        }
        response.ensureSuccess("upload", key)
    }

    private suspend fun HttpResponse.ensureSuccess(action: String, key: String) {
        if (!status.isSuccess()) {
            error("Stratus $action failed for '$key': $status ${bodyAsText()}")
        }
    }

    private fun objectUrl(key: String): String = "${bucketUrl.trimEnd('/')}/$key"

    private fun buildObjectKey(subDir: String, fileName: String): String {
        val cleanSubDir = subDir.trim('/')
        val cleanFileName = fileName.trimStart('/')
        return if (cleanSubDir.isEmpty()) cleanFileName else "$cleanSubDir/$cleanFileName"
    }
}