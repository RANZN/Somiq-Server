package com.ranjan.core.util.impl

import com.ranjan.core.util.MimeTypeResolver

class DefaultMimeTypeResolver : MimeTypeResolver {

    override fun resolveContentType(extension: String): String {
        val cleanExt = extension.trim().removePrefix(".").lowercase()
        return when (cleanExt) {
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "mp4" -> "video/mp4"
            "mov", "quicktime" -> "video/quicktime"
            "webm" -> "video/webm"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}
