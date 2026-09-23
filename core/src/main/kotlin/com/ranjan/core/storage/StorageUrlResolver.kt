package com.ranjan.core.storage

interface StorageUrlResolver {
    fun buildFileUrl(baseUrl: String, subDir: String, fileName: String): String
    fun resolveRelativePath(baseUrl: String, relativePath: String): String
}
