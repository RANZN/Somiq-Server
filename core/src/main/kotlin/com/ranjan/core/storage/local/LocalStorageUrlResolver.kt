package com.ranjan.core.storage.local

import com.ranjan.core.storage.StorageUrlResolver

class LocalStorageUrlResolver(
    private val uploadsDir: String = "uploads"
) : StorageUrlResolver {

    override fun buildFileUrl(baseUrl: String, subDir: String, fileName: String): String {
        val cleanBase = baseUrl.trimEnd('/')
        val cleanUploads = uploadsDir.trim('/')
        val cleanSubDir = subDir.trim('/')
        val cleanFileName = fileName.trimStart('/')

        val relative = listOf(cleanUploads, cleanSubDir, cleanFileName)
            .filter { it.isNotEmpty() }
            .joinToString("/")

        return "$cleanBase/$relative"
    }

    override fun resolveRelativePath(baseUrl: String, relativePath: String): String {
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        val cleanBase = baseUrl.trimEnd('/')
        val cleanPath = relativePath.trimStart('/')
        return "$cleanBase/$cleanPath"
    }
}
