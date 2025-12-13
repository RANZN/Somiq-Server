package com.ranjan.data.comment.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.comment.model.CommentLikeTable
import com.ranjan.data.comment.model.CommentTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.data.util.TimeProvider
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.comment.model.CommentResponse
import com.ranjan.domain.comment.model.CreateCommentRequest
import com.ranjan.domain.comment.model.UpdateCommentRequest
import com.ranjan.domain.comment.repository.CommentRepository
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class CommentRepositoryImpl(
    private val db: Database,
    private val timeProvider: TimeProvider,
) : CommentRepository {

    override suspend fun createComment(
        userId: UUID,
        postId: String?,
        reelId: String?,
        request: CreateCommentRequest
    ): CommentResponse = db.dbQuery {
        val commentId = UUID.randomUUID().toString()

        CommentTable.insert { row ->
            row[CommentTable.commentId] = commentId
            row[CommentTable.content] = request.content
            row[CommentTable.authorId] = userId
            row[CommentTable.postId] = postId
            row[CommentTable.reelId] = reelId
            row[CommentTable.parentCommentId] = request.parentCommentId
            row[CommentTable.createdAt] = timeProvider.nowMillis()
            row[CommentTable.updatedAt] = timeProvider.nowMillis()
        }

        buildCommentResponse(commentId, viewerId = userId)
    }

    override suspend fun getComments(
        postId: String?,
        reelId: String?,
        parentCommentId: String?,
        viewerId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<CommentResponse> = db.dbQuery {
        val query = CommentTable.selectAll()

        when {
            postId != null -> query.andWhere { CommentTable.postId eq postId }
            reelId != null -> query.andWhere { CommentTable.reelId eq reelId }
            else -> throw IllegalArgumentException("Either postId or reelId must be provided")
        }

        if (parentCommentId != null) {
            query.andWhere { CommentTable.parentCommentId eq parentCommentId }
        } else {
            query.andWhere { CommentTable.parentCommentId.isNull() }
        }

        query.orderBy(CommentTable.createdAt, SortOrder.DESC)

        pagination.cursor?.toLongOrNull()?.let { after ->
            query.andWhere { CommentTable.createdAt less after }
        }

        val commentIds = query.limit(pagination.limit).map { it[CommentTable.commentId] }
        if (commentIds.isEmpty()) return@dbQuery PaginationResult(emptyList(), null)

        val commentData = CommentTable.selectAll().where { CommentTable.commentId inList commentIds }
            .associateBy { it[CommentTable.commentId] }

        val likesCounts = CommentLikeTable.select(CommentLikeTable.commentId, CommentLikeTable.commentId.count())
            .where { CommentLikeTable.commentId inList commentIds }
            .groupBy(CommentLikeTable.commentId)
            .associate { row -> row[CommentLikeTable.commentId] to row[CommentLikeTable.commentId.count()] }

        val repliesCounts = commentIds.associateWith { getRepliesCount(it) }

        val userLikes = viewerId?.let {
            CommentLikeTable.selectAll().where { (CommentLikeTable.userId eq it) and (CommentLikeTable.commentId inList commentIds) }
                .map { it[CommentLikeTable.commentId] }.toSet()
        } ?: emptySet()

        val authorIds = commentData.values.map { it[CommentTable.authorId] }.distinct()
        val authors = UserTable.selectAll().where { UserTable.userId inList authorIds }
            .associateBy { it[UserTable.userId] }

        val items = commentIds.map { commentId ->
            val row = commentData[commentId]!!
            val author = authors[row[CommentTable.authorId]]!!
            CommentResponse(
                commentId = commentId,
                content = row[CommentTable.content],
                authorId = row[CommentTable.authorId],
                authorName = author[UserTable.name],
                authorUsername = author[UserTable.username],
                authorProfilePictureUrl = author[UserTable.profilePictureUrl],
                parentCommentId = row[CommentTable.parentCommentId],
                createdAt = row[CommentTable.createdAt],
                updatedAt = row[CommentTable.updatedAt],
                likesCount = likesCounts[commentId] ?: 0,
                repliesCount = repliesCounts[commentId] ?: 0,
                isLiked = commentId in userLikes
            )
        }

        val nextCursor = items.lastOrNull()?.createdAt?.toString()
        PaginationResult(items, nextCursor)
    }

    override suspend fun getCommentById(commentId: String, viewerId: UUID?): CommentResponse? = db.dbQuery {
        CommentTable.selectAll().where { CommentTable.commentId eq commentId }.singleOrNull()?.let {
            buildCommentResponse(commentId, viewerId)
        }
    }

    override suspend fun updateComment(commentId: String, request: UpdateCommentRequest): CommentResponse = db.dbQuery {
        val existing = CommentTable.selectAll().where { CommentTable.commentId eq commentId }.singleOrNull()
            ?: throw NotFoundException("Comment not found")

        CommentTable.update({ CommentTable.commentId eq commentId }) { row ->
            row[CommentTable.content] = request.content
            row[CommentTable.updatedAt] = timeProvider.nowMillis()
        }

        buildCommentResponse(commentId)
    }

    override suspend fun deleteComment(commentId: String) {
        db.dbQuery {
            // Delete all replies first (cascade will handle this, but being explicit)
            CommentTable.deleteWhere { CommentTable.parentCommentId eq commentId }
            CommentLikeTable.deleteWhere { CommentLikeTable.commentId eq commentId }
            CommentTable.deleteWhere { CommentTable.commentId eq commentId }
        }
    }

    override suspend fun exists(commentId: String): Boolean = db.dbQuery {
        CommentTable.selectAll().where { CommentTable.commentId eq commentId }.any()
    }

    override suspend fun toggleLike(userId: UUID, commentId: String): CommentResponse = db.dbQuery {
        val alreadyLiked = CommentLikeTable.selectAll()
            .where { (CommentLikeTable.userId eq userId) and (CommentLikeTable.commentId eq commentId) }
            .limit(1).any()

        if (alreadyLiked) {
            CommentLikeTable.deleteWhere { (CommentLikeTable.userId eq userId) and (CommentLikeTable.commentId eq commentId) }
        } else {
            CommentLikeTable.insert {
                it[CommentLikeTable.userId] = userId
                it[CommentLikeTable.commentId] = commentId
            }
        }

        buildCommentResponse(commentId, userId)
    }

    override suspend fun getRepliesCount(commentId: String): Long = db.dbQuery {
        CommentTable.selectAll().where { CommentTable.parentCommentId eq commentId }.count()
    }

    private suspend fun buildCommentResponse(commentId: String, viewerId: UUID? = null): CommentResponse = db.dbQuery {
        val comment = (CommentTable innerJoin UserTable)
            .selectAll()
            .where { CommentTable.commentId eq commentId }
            .single()

        val likesCount = CommentLikeTable.selectAll().where { CommentLikeTable.commentId eq commentId }.count()
        val repliesCount = CommentTable.selectAll().where { CommentTable.parentCommentId eq commentId }.count()
        val isLiked = viewerId?.let {
            CommentLikeTable.selectAll().where { (CommentLikeTable.commentId eq commentId) and (CommentLikeTable.userId eq it) }.any()
        } ?: false

        CommentResponse(
            commentId = commentId,
            content = comment[CommentTable.content],
            authorId = comment[CommentTable.authorId],
            authorName = comment[UserTable.name],
            authorUsername = comment[UserTable.username],
            authorProfilePictureUrl = comment[UserTable.profilePictureUrl],
            parentCommentId = comment[CommentTable.parentCommentId],
            createdAt = comment[CommentTable.createdAt],
            updatedAt = comment[CommentTable.updatedAt],
            likesCount = likesCount,
            repliesCount = repliesCount,
            isLiked = isLiked
        )
    }
}

