package com.ranjan.domain.account.repository

import com.ranjan.domain.account.model.ProfileResponse
import java.util.UUID

interface AccountRepository {
    suspend fun getProfile(userId: UUID, viewerId: UUID?): ProfileResponse?
    suspend fun toggleFollow(followerId: UUID, followingId: UUID): Boolean // Returns true if now following, false if unfollowed
    suspend fun getPostsCount(userId: UUID): Long
    suspend fun getReelsCount(userId: UUID): Long
    suspend fun getFollowersCount(userId: UUID): Long
    suspend fun getFollowingCount(userId: UUID): Long
    suspend fun isFollowing(followerId: UUID, followingId: UUID): Boolean
}

