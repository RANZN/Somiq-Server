package com.ranjan.data.search.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.common.extension.toMediaUrls
import com.ranjan.data.post.model.PostTable
import com.ranjan.data.reel.model.ReelTable
import com.ranjan.data.sources.db.dbQuery
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.common.model.UserResponse
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.search.repository.SearchRepository
import org.jetbrains.exposed.sql.*

class SearchRepositoryImpl(
    private val db: Database
) : SearchRepository {

    override suspend fun searchUsers(query: String, limit: Int): List<UserResponse> = db.dbQuery {
        val searchPattern = "%$query%"
        UserTable.selectAll()
            .where {
                (UserTable.name like searchPattern) or
                (UserTable.username like searchPattern) or
                (UserTable.email like searchPattern)
            }
            .limit(limit)
            .map { row ->
                UserResponse(
                    userId = row[UserTable.userId].toString(),
                    name = row[UserTable.name],
                    email = row[UserTable.email],
                    username = row[UserTable.username],
                    profilePictureUrl = row[UserTable.profilePictureUrl],
                    bio = row[UserTable.bio]
                )
            }
    }

    override suspend fun searchPosts(query: String, limit: Int): List<PostResponse> = db.dbQuery {
        val searchPattern = "%$query%"
        val posts = (PostTable innerJoin UserTable)
            .selectAll()
            .where {
                (PostTable.title like searchPattern) or
                (PostTable.content like searchPattern)
            }
            .orderBy(PostTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                PostResponse(
                    postId = row[PostTable.postId],
                    title = row[PostTable.title],
                    content = row[PostTable.content],
                    mediaUrls = row[PostTable.mediaUrls].toMediaUrls(),
                    authorId = row[PostTable.authorId],
                    authorName = row[UserTable.name],
                    authorUsername = row[UserTable.username],
                    authorProfilePictureUrl = row[UserTable.profilePictureUrl],
                    createdAt = row[PostTable.createdAt],
                    updatedAt = row[PostTable.updatedAt],
                    likesCount = 0, // Simplified for search
                    bookmarksCount = 0,
                    isLiked = false,
                    isBookmarked = false
                )
            }
        posts
    }

    override suspend fun searchReels(query: String, limit: Int): List<ReelResponse> = db.dbQuery {
        val searchPattern = "%$query%"
        val reels = (ReelTable innerJoin UserTable)
            .selectAll()
            .where {
                (ReelTable.title like searchPattern) or
                (ReelTable.description like searchPattern)
            }
            .orderBy(ReelTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { row ->
                ReelResponse(
                    reelId = row[ReelTable.reelId],
                    title = row[ReelTable.title],
                    description = row[ReelTable.description],
                    videoUrl = row[ReelTable.videoUrl],
                    thumbnailUrl = row[ReelTable.thumbnailUrl],
                    authorId = row[ReelTable.authorId],
                    authorName = row[UserTable.name],
                    authorUsername = row[UserTable.username],
                    authorProfilePictureUrl = row[UserTable.profilePictureUrl],
                    createdAt = row[ReelTable.createdAt],
                    updatedAt = row[ReelTable.updatedAt],
                    likesCount = 0, // Simplified for search
                    commentsCount = 0,
                    viewsCount = 0,
                    isLiked = false,
                    isBookmarked = false
                )
            }
        reels
    }
}

