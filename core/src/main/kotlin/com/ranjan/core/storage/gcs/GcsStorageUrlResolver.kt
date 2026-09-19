package com.ranjan.core.storage.gcs

import com.ranjan.core.storage.StorageUrlResolver

class GcsStorageUrlResolver(
    private val publicUrlPrefix: String
) : StorageUrlResolver {

    override fun buildFileUrl(baseUrl: String, subDir: String, fileName: String): String {
        val cleanPrefix = publicUrlPrefix.trimEnd('/')
        val cleanSubDir = subDir.trim('/')
        val cleanFileName = fileName.trimStart('/')
        return if (cleanSubDir.isEmpty()) {
            "$cleanPrefix/$cleanFileName"
        } else {
            "$cleanPrefix/$cleanSubDir/$cleanFileName"
        }
    }

    override fun resolveRelativePath(baseUrl: String, relativePath: String): String {
        if (relativePath.startsWith("http://") || relativePath.startsWith("https://")) {
            return relativePath
        }
        val cleanPrefix = publicUrlPrefix.trimEnd('/')
        val cleanPath = relativePath.trimStart('/').removePrefix("uploads/").removePrefix("uploads").trimStart('/')
        return "$cleanPrefix/$cleanPath"
    }
}
