package com.ranjan.data.reel.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.reel.model.ReelBookmarkTable
import com.ranjan.data.reel.model.ReelLikeTable
import com.ranjan.data.reel.model.ReelTable
import com.ranjan.data.reel.model.ReelViewTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.data.util.TimeProvider
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.reel.model.CreateReelRequest
import com.ranjan.domain.reel.model.ReelResponse
import com.ranjan.domain.reel.model.UpdateReelRequest
import com.ranjan.domain.reel.repository.ReelRepository
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class ReelRepositoryImpl(
    private val db: Database,
    private val timeProvider: TimeProvider,
) : ReelRepository {

    override suspend fun createReel(
        userId: UUID,
        request: CreateReelRequest
    ): ReelResponse = db.dbQuery {
        val reelId = UUID.randomUUID().toString()

        ReelTable.insert { row ->
            row[ReelTable.reelId] = reelId
            row[ReelTable.title] = request.title
            row[ReelTable.description] = request.description
            row[ReelTable.videoUrl] = request.videoUrl
            row[ReelTable.thumbnailUrl] = request.thumbnailUrl
            row[ReelTable.authorId] = userId
            row[ReelTable.createdAt] = timeProvider.nowMillis()
            row[ReelTable.updatedAt] = timeProvider.nowMillis()
        }

        buildReelResponse(reelId, userId)
    }

    override suspend fun getReels(
        userId: UUID?,
        pagination: PaginationRequest
    ): PaginationResult<ReelResponse> = db.dbQuery {
        val query = ReelTable.selectAll().orderBy(ReelTable.createdAt, SortOrder.DESC)

        pagination.cursor?.toLongOrNull()?.let { after ->
            query.andWhere { ReelTable.createdAt less after }
        }

        val reelIds = query.limit(pagination.limit).map { it[ReelTable.reelId] }
        if (reelIds.isEmpty()) return@dbQuery PaginationResult(emptyList(), null)

        val reelData = ReelTable.selectAll().where { ReelTable.reelId inList reelIds }
            .associateBy { it[ReelTable.reelId] }

        val likesCounts = ReelLikeTable.select(ReelLikeTable.reelId, ReelLikeTable.reelId.count())
            .where { ReelLikeTable.reelId inList reelIds }
            .groupBy(ReelLikeTable.reelId)
            .associate { row -> row[ReelLikeTable.reelId] to row[ReelLikeTable.reelId.count()] }

        val bookmarksCounts = ReelBookmarkTable.select(ReelBookmarkTable.reelId, ReelBookmarkTable.reelId.count())
            .where { ReelBookmarkTable.reelId inList reelIds }
            .groupBy(ReelBookmarkTable.reelId)
            .associate { row -> row[ReelBookmarkTable.reelId] to row[ReelBookmarkTable.reelId.count()] }

        val viewsCounts = ReelViewTable.select(ReelViewTable.reelId, ReelViewTable.reelId.count())
            .where { ReelViewTable.reelId inList reelIds }
            .groupBy(ReelViewTable.reelId)
            .associate { row -> row[ReelViewTable.reelId] to row[ReelViewTable.reelId.count()] }

        val userLikes = userId?.let {
            ReelLikeTable.selectAll().where { (ReelLikeTable.userId eq it) and (ReelLikeTable.reelId inList reelIds) }
                .map { it[ReelLikeTable.reelId] }.toSet()
        } ?: emptySet()

        val userBookmarks = userId?.let {
            ReelBookmarkTable.selectAll()
                .where { (ReelBookmarkTable.userId eq it) and (ReelBookmarkTable.reelId inList reelIds) }
                .map { it[ReelBookmarkTable.reelId] }.toSet()
        } ?: emptySet()

        val authorIds = reelData.values.map { it[ReelTable.authorId] }.distinct()
        val authors = UserTable.selectAll().where { UserTable.userId inList authorIds }
            .associateBy { it[UserTable.userId] }

        val items = reelIds.map { reelId ->
            val row = reelData[reelId]!!
            val author = authors[row[ReelTable.authorId]]!!
            ReelResponse(
                reelId = reelId,
                title = row[ReelTable.title],
                description = row[ReelTable.description],
                videoUrl = row[ReelTable.videoUrl],
                thumbnailUrl = row[ReelTable.thumbnailUrl],
                authorId = row[ReelTable.authorId],
                authorName = author[UserTable.name],
                authorUsername = author[UserTable.username],
                authorProfilePictureUrl = author[UserTable.profilePictureUrl],
                createdAt = row[ReelTable.createdAt],
                updatedAt = row[ReelTable.updatedAt],
                likesCount = likesCounts[reelId] ?: 0,
                commentsCount = 0, // TODO: Add comments functionality
                viewsCount = viewsCounts[reelId] ?: 0,
                isLiked = reelId in userLikes,
                isBookmarked = reelId in userBookmarks
            )
        }

        val nextCursor = items.lastOrNull()?.createdAt?.toString()
        PaginationResult(items, nextCursor)
    }

    override suspend fun getReelById(id: String): ReelResponse? = db.dbQuery {
        ReelTable.selectAll().where { ReelTable.reelId eq id }.singleOrNull()?.let {
            buildReelResponse(id)
        }
    }

    override suspend fun updateReel(reelId: String, request: UpdateReelRequest): ReelResponse = db.dbQuery {
        val existing = ReelTable.selectAll().where { ReelTable.reelId eq reelId }.singleOrNull()
            ?: throw NotFoundException("Reel not found")

        ReelTable.update({ ReelTable.reelId eq reelId }) { row ->
            row[ReelTable.title] = request.title ?: existing[ReelTable.title]
            row[ReelTable.description] = request.description ?: existing[ReelTable.description]
            row[ReelTable.thumbnailUrl] = request.thumbnailUrl ?: existing[ReelTable.thumbnailUrl]
            row[ReelTable.updatedAt] = timeProvider.nowMillis()
        }

        buildReelResponse(reelId)
    }

    override suspend fun deleteReel(reelId: String) {
        db.dbQuery {
            ReelLikeTable.deleteWhere { ReelLikeTable.reelId eq reelId }
            ReelBookmarkTable.deleteWhere { ReelBookmarkTable.reelId eq reelId }
            ReelViewTable.deleteWhere { ReelViewTable.reelId eq reelId }
            ReelTable.deleteWhere { ReelTable.reelId eq reelId }
        }
    }

    override suspend fun exists(reelId: String): Boolean = db.dbQuery {
        ReelTable.selectAll().where { ReelTable.reelId eq reelId }.any()
    }

    override suspend fun toggleLike(userId: UUID, reelId: String): ReelResponse = db.dbQuery {
        val alreadyLiked = ReelLikeTable.selectAll()
            .where { (ReelLikeTable.userId eq userId) and (ReelLikeTable.reelId eq reelId) }
            .limit(1).any()

        if (alreadyLiked) {
            ReelLikeTable.deleteWhere { (ReelLikeTable.userId eq userId) and (ReelLikeTable.reelId eq reelId) }
        } else {
            ReelLikeTable.insert {
                it[ReelLikeTable.userId] = userId
                it[ReelLikeTable.reelId] = reelId
            }
        }

        buildReelResponse(reelId, userId)
    }

    override suspend fun toggleBookmark(userId: UUID, reelId: String): ReelResponse = db.dbQuery {
        val alreadyBookmarked = ReelBookmarkTable.selectAll()
            .where { (ReelBookmarkTable.userId eq userId) and (ReelBookmarkTable.reelId eq reelId) }
            .limit(1).any()

        if (alreadyBookmarked) {
            ReelBookmarkTable.deleteWhere { (ReelBookmarkTable.userId eq userId) and (ReelBookmarkTable.reelId eq reelId) }
        } else {
            ReelBookmarkTable.insert {
                it[ReelBookmarkTable.userId] = userId
                it[ReelBookmarkTable.reelId] = reelId
            }
        }

        buildReelResponse(reelId, userId)
    }

    override suspend fun recordView(userId: UUID?, reelId: String) {
        db.dbQuery {
            // Only record unique views per user
            if (userId != null) {
                val alreadyViewed = ReelViewTable.selectAll()
                    .where { (ReelViewTable.userId eq userId) and (ReelViewTable.reelId eq reelId) }
                    .limit(1).any()

                if (!alreadyViewed) {
                    ReelViewTable.insert {
                        it[ReelViewTable.userId] = userId
                        it[ReelViewTable.reelId] = reelId
                        it[ReelViewTable.viewedAt] = timeProvider.nowMillis()
                    }
                }
            } else {
                // Anonymous view - could be tracked differently
                ReelViewTable.insert {
                    it[ReelViewTable.userId] = null
                    it[ReelViewTable.reelId] = reelId
                    it[ReelViewTable.viewedAt] = timeProvider.nowMillis()
                }
            }
        }
    }

    private fun buildReelResponse(reelId: String, viewerId: UUID? = null): ReelResponse {
        val reel = (ReelTable innerJoin UserTable)
            .selectAll()
            .where { ReelTable.reelId eq reelId }
            .single()

        val likesCount = ReelLikeTable.selectAll().where { ReelLikeTable.reelId eq reelId }.count()
        val bookmarksCount = ReelBookmarkTable.selectAll().where { ReelBookmarkTable.reelId eq reelId }.count()
        val viewsCount = ReelViewTable.selectAll().where { ReelViewTable.reelId eq reelId }.count()
        val isLiked = viewerId?.let {
            ReelLikeTable.selectAll().where { (ReelLikeTable.reelId eq reelId) and (ReelLikeTable.userId eq it) }.any()
        } ?: false
        val isBookmarked = viewerId?.let {
            ReelBookmarkTable.selectAll()
                .where { (ReelBookmarkTable.reelId eq reelId) and (ReelBookmarkTable.userId eq it) }.any()
        } ?: false

        return ReelResponse(
            reelId = reelId,
            title = reel[ReelTable.title],
            description = reel[ReelTable.description],
            videoUrl = reel[ReelTable.videoUrl],
            thumbnailUrl = reel[ReelTable.thumbnailUrl],
            authorId = reel[ReelTable.authorId],
            authorName = reel[UserTable.name],
            authorUsername = reel[UserTable.username],
            authorProfilePictureUrl = reel[UserTable.profilePictureUrl],
            createdAt = reel[ReelTable.createdAt],
            updatedAt = reel[ReelTable.updatedAt],
            likesCount = likesCount,
            commentsCount = 0, // TODO: Add comments
            viewsCount = viewsCount,
            isLiked = isLiked,
            isBookmarked = isBookmarked
        )
    }
}

