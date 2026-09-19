package com.ranjan.core.util

interface MimeTypeResolver {
    fun resolveContentType(extension: String): String
}
