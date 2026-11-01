package com.ranjan.server.post

import com.ranjan.core.storage.MediaStorageService
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.post.model.PostResponse

fun PostResponse.withAbsoluteUrls(baseUrl: String, mediaStorageService: MediaStorageService): PostResponse {
    return this.copy(
        mediaUrls = this.mediaUrls.map { relativeUrl ->
            mediaStorageService.getUrlForRelativePath(baseUrl, relativeUrl)
        }
    )
}

fun PaginationResult<PostResponse>.withAbsoluteUrls(baseUrl: String, mediaStorageService: MediaStorageService): PaginationResult<PostResponse> {
    return this.copy(
        data = this.data.map { it.withAbsoluteUrls(baseUrl, mediaStorageService) }
    )
}
