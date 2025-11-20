package com.ranjan.data.post.repository

import com.ranjan.data.common.extension.toDbString
import com.ranjan.data.common.extension.toMediaUrls
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.data.post.model.PostBookmarkTable
import com.ranjan.data.post.model.PostLikeTable
import com.ranjan.data.post.model.PostTable
import com.ranjan.data.util.TimeProvider
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.post.model.CreatePostRequest
import com.ranjan.domain.post.model.PostResponse
import com.ranjan.domain.post.model.UpdatePostRequest
import com.ranjan.domain.post.repository.PostRepository
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class PostRepositoryImpl(
    private val db: Database,
    private val timeProvider: TimeProvider,
) : PostRepository {

    // ------------------------------
    // CREATE POST
    // ------------------------------
    override suspend fun createPost(
        userId: UUID,
        request: CreatePostRequest
    ): PostResponse = db.dbQuery {
        val postId = UUID.randomUUID().toString()

        PostTable.insert { row ->
            row[PostTable.postId] = postId
            row[title] = request.title
            row[content] = request.content
            row[mediaUrls] = request.mediaUrls.toDbString()
            row[authorId] = userId
            row[createdAt] = timeProvider.now()
            row[updatedAt] = timeProvider.now()
        }

        buildPostResponse(postId, userId)
    }

    // ------------------------------
    // GET POSTS WITH PAGINATION
    // ------------------------------
    override suspend fun getPosts(
        userId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<PostResponse> = db.dbQuery {
        val query = PostTable.selectAll().orderBy(PostTable.createdAt, SortOrder.DESC)

        pagination.cursor?.toLongOrNull()?.let { after ->
            query.andWhere { PostTable.createdAt less after }
        }

        val postIds = query.limit(pagination.limit).map { it[PostTable.postId] }
        if (postIds.isEmpty()) return@dbQuery PaginationResult(emptyList(), null)

        // Batch fetch post data
        val postData = PostTable.selectAll().where { PostTable.postId inList postIds }
            .associateBy { it[PostTable.postId] }

        // Batch fetch likes count
        val likesCounts = PostLikeTable.select(PostLikeTable.postId, PostLikeTable.postId.count())
            .where { PostLikeTable.postId inList postIds }
            .groupBy(PostLikeTable.postId)
            .associate { row ->
                row[PostLikeTable.postId] to row[PostLikeTable.postId.count()]
            }

        // Batch fetch bookmarks count
        val bookmarksCounts = PostBookmarkTable.select(PostBookmarkTable.postId, PostBookmarkTable.postId.count())
            .where { PostBookmarkTable.postId inList postIds }
            .groupBy(PostBookmarkTable.postId)
            .associate { row ->
                row[PostBookmarkTable.postId] to row[PostBookmarkTable.postId.count()]
            }

        // User-specific flags
        val userLikes = userId?.let {
            PostLikeTable.selectAll().where { (PostLikeTable.userId eq it) and (PostLikeTable.postId inList postIds) }
                .map { it[PostLikeTable.postId] }.toSet()
        } ?: emptySet()

        val userBookmarks = userId?.let {
            PostBookmarkTable.selectAll()
                .where { (PostBookmarkTable.userId eq it) and (PostBookmarkTable.postId inList postIds) }
                .map { it[PostBookmarkTable.postId] }.toSet()
        } ?: emptySet()

        val items = postIds.map { postId ->
            val row = postData[postId]!!
            PostResponse(
                postId = postId,
                title = row[PostTable.title],
                content = row[PostTable.content],
                mediaUrls = row[PostTable.mediaUrls].toMediaUrls(),
                authorId = row[PostTable.authorId],
                createdAt = row[PostTable.createdAt],
                updatedAt = row[PostTable.updatedAt],
                likesCount = likesCounts[postId] ?: 0,
                bookmarksCount = bookmarksCounts[postId] ?: 0,
                isLiked = postId in userLikes,
                isBookmarked = postId in userBookmarks
            )
        }

        val nextCursor = items.lastOrNull()?.createdAt?.toString()
        PaginationResult(items, nextCursor)
    }

    // ------------------------------
    // GET POST BY ID
    // ------------------------------
    override suspend fun getPostById(id: String): PostResponse? = db.dbQuery {
        PostTable.selectAll().where { PostTable.postId eq id }.singleOrNull()?.let {
            buildPostResponse(id)
        }
    }

    // ------------------------------
    // UPDATE POST
    // ------------------------------
    override suspend fun updatePost(postId: String, request: UpdatePostRequest): PostResponse = db.dbQuery {
        val existing = PostTable.selectAll().where { PostTable.postId eq postId }.singleOrNull()
            ?: throw NotFoundException("Post not found")

        PostTable.update({ PostTable.postId eq postId }) { row ->
            row[title] = request.title ?: existing[PostTable.title]
            row[content] = request.content ?: existing[PostTable.content]
            row[mediaUrls] = request.mediaUrls?.toDbString() ?: existing[PostTable.mediaUrls]
            row[updatedAt] = timeProvider.now()
        }

        buildPostResponse(postId)
    }

    // ------------------------------
    // DELETE POST
    // ------------------------------
    override suspend fun deletePost(postId: String) {
        db.dbQuery {
            PostLikeTable.deleteWhere { PostLikeTable.postId eq postId }
            PostBookmarkTable.deleteWhere { PostBookmarkTable.postId eq postId }
            PostTable.deleteWhere { PostTable.postId eq postId }
        }
    }

    // ------------------------------
    // CHECK EXISTS
    // ------------------------------
    override suspend fun exists(postId: String): Boolean = db.dbQuery {
        PostTable.selectAll().where { PostTable.postId eq postId }.any()
    }

    // ------------------------------
    // TOGGLE LIKE
    // ------------------------------
    override suspend fun toggleLike(userId: UUID, postId: String): PostResponse = db.dbQuery {
        val alreadyLiked =
            PostLikeTable.selectAll().where { (PostLikeTable.userId eq userId) and (PostLikeTable.postId eq postId) }
                .limit(1).any()

        if (alreadyLiked) {
            PostLikeTable.deleteWhere { (PostLikeTable.userId eq userId) and (PostLikeTable.postId eq postId) }
        } else {
            PostLikeTable.insert {
                it[PostLikeTable.userId] = userId
                it[PostLikeTable.postId] = postId
            }
        }

        buildPostResponse(postId, userId)
    }

    // ------------------------------
    // TOGGLE BOOKMARK
    // ------------------------------
    override suspend fun toggleBookmark(userId: UUID, postId: String): PostResponse = db.dbQuery {
        val alreadyBookmarked = PostBookmarkTable.selectAll()
            .where { (PostBookmarkTable.userId eq userId) and (PostBookmarkTable.postId eq postId) }
            .limit(1).any()

        if (alreadyBookmarked) {
            PostBookmarkTable.deleteWhere { (PostBookmarkTable.userId eq userId) and (PostBookmarkTable.postId eq postId) }
        } else {
            PostBookmarkTable.insert {
                it[PostBookmarkTable.userId] = userId
                it[PostBookmarkTable.postId] = postId
            }
        }

        buildPostResponse(postId, userId)
    }

    // ------------------------------
    // BUILD POST RESPONSE
    // ------------------------------
    private fun buildPostResponse(postId: String, viewerId: UUID? = null): PostResponse {
        val post = PostTable.selectAll().where { PostTable.postId eq postId }.single()

        val likesCount = PostLikeTable.selectAll().where { PostLikeTable.postId eq postId }.count()
        val bookmarksCount = PostBookmarkTable.selectAll().where { PostBookmarkTable.postId eq postId }.count()
        val isLiked = viewerId?.let {
            PostLikeTable.selectAll().where { (PostLikeTable.postId eq postId) and (PostLikeTable.userId eq it) }.any()
        } ?: false
        val isBookmarked = viewerId?.let {
            PostBookmarkTable.selectAll()
                .where { (PostBookmarkTable.postId eq postId) and (PostBookmarkTable.userId eq it) }.any()
        } ?: false

        return PostResponse(
            postId = postId,
            title = post[PostTable.title],
            content = post[PostTable.content],
            mediaUrls = post[PostTable.mediaUrls].toMediaUrls(),
            authorId = post[PostTable.authorId],
            createdAt = post[PostTable.createdAt],
            updatedAt = post[PostTable.updatedAt],
            likesCount = likesCount,
            bookmarksCount = bookmarksCount,
            isLiked = isLiked,
            isBookmarked = isBookmarked
        )
    }
}