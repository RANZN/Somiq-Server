package com.ranjan.core.storage

import java.io.InputStream

interface MediaStorageService {
    suspend fun saveStream(inputStream: InputStream, ext: String, subDir: String): String
    suspend fun saveBytes(bytes: ByteArray, subDir: String): String
    suspend fun getFileBytes(subDir: String, fileName: String): ByteArray
    fun getUrlForFile(baseUrl: String, subDir: String, fileName: String): String
    fun getUrlForRelativePath(baseUrl: String, relativePath: String): String
}
