package com.ranjan.core.storage

import com.ranjan.core.storage.gcs.GcsStorageUrlResolver
import com.ranjan.core.storage.local.LocalStorageUrlResolver
import kotlin.test.Test
import kotlin.test.assertEquals

class StorageUrlResolverTest {

    @Test
    fun `test LocalStorageUrlResolver buildFileUrl and resolveRelativePath`() {
        val resolver = LocalStorageUrlResolver(uploadsDir = "uploads")

        assertEquals(
            "https://api.example.com/uploads/user/profile.jpg",
            resolver.buildFileUrl("https://api.example.com", "user", "profile.jpg")
        )
        assertEquals(
            "https://api.example.com/uploads/user/profile.jpg",
            resolver.buildFileUrl("https://api.example.com/", "/user/", "/profile.jpg")
        )

        // Relative path resolution
        assertEquals(
            "https://api.example.com/uploads/user/post.jpg",
            resolver.resolveRelativePath("https://api.example.com", "uploads/user/post.jpg")
        )
        assertEquals(
            "https://api.example.com/uploads/user/post.jpg",
            resolver.resolveRelativePath("https://api.example.com/", "/uploads/user/post.jpg")
        )

        // Absolute URLs remain unchanged
        assertEquals(
            "https://cdn.example.com/photo.jpg",
            resolver.resolveRelativePath("https://api.example.com", "https://cdn.example.com/photo.jpg")
        )
    }

    @Test
    fun `test GcsStorageUrlResolver buildFileUrl and resolveRelativePath`() {
        val resolver = GcsStorageUrlResolver("https://storage.googleapis.com/somiq-bucket")

        assertEquals(
            "https://storage.googleapis.com/somiq-bucket/user/profile.jpg",
            resolver.buildFileUrl("https://api.example.com", "user", "profile.jpg")
        )

        // Stripping legacy uploads prefix (with or without leading slash)
        assertEquals(
            "https://storage.googleapis.com/somiq-bucket/user/post.jpg",
            resolver.resolveRelativePath("https://api.example.com", "uploads/user/post.jpg")
        )
        assertEquals(
            "https://storage.googleapis.com/somiq-bucket/user/post.jpg",
            resolver.resolveRelativePath("https://api.example.com", "/uploads/user/post.jpg")
        )
        assertEquals(
            "https://storage.googleapis.com/somiq-bucket/user/post.jpg",
            resolver.resolveRelativePath("https://api.example.com", "user/post.jpg")
        )

        // Absolute URLs remain unchanged
        assertEquals(
            "https://custom-domain.com/photo.jpg",
            resolver.resolveRelativePath("https://api.example.com", "https://custom-domain.com/photo.jpg")
        )
    }
}
