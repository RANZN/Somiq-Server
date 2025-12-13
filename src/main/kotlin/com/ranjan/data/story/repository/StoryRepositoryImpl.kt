package com.ranjan.data.story.repository

import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.data.story.model.StoryTable
import com.ranjan.data.story.model.StoryViewTable
import com.ranjan.data.util.TimeProvider
import org.jetbrains.exposed.sql.Database
import com.ranjan.domain.common.model.PaginationRequest
import com.ranjan.domain.common.model.PaginationResult
import com.ranjan.domain.story.model.MediaType
import com.ranjan.domain.story.model.StoryResponse
import com.ranjan.domain.story.repository.StoryRepository
import io.ktor.server.plugins.NotFoundException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class StoryRepositoryImpl(
    private val db: Database,
    private val timeProvider: TimeProvider,
) : StoryRepository {

    override suspend fun createStory(userId: UUID, mediaUrl: String, mediaType: String): StoryResponse = db.dbQuery {
        val storyId = UUID.randomUUID().toString()
        val now = timeProvider.nowMillis()
        val expiresAt = now + (24 * 60 * 60 * 1000) // 24 hours

        StoryTable.insert { row ->
            row[StoryTable.storyId] = storyId
            row[StoryTable.mediaUrl] = mediaUrl
            row[StoryTable.mediaType] = mediaType
            row[StoryTable.authorId] = userId
            row[StoryTable.createdAt] = now
            row[StoryTable.expiresAt] = expiresAt
        }

        buildStoryResponse(storyId, userId)
    }

    override suspend fun getStoriesFeed(userId: UUID?, pagination: PaginationRequest): PaginationResult<StoryResponse> = db.dbQuery {
        val now = timeProvider.nowMillis()
        val query = StoryTable.selectAll()
            .where { StoryTable.expiresAt greater now }
            .orderBy(StoryTable.createdAt, SortOrder.DESC)

        pagination.cursor?.toLongOrNull()?.let { after ->
            query.andWhere { StoryTable.createdAt less after }
        }

        val storyIds = query.limit(pagination.limit).map { it[StoryTable.storyId] }
        if (storyIds.isEmpty()) return@dbQuery PaginationResult(emptyList(), null)

        val storyData = StoryTable.selectAll().where { StoryTable.storyId inList storyIds }
            .associateBy { it[StoryTable.storyId] }

        val viewsCounts = StoryViewTable.select(StoryViewTable.storyId, StoryViewTable.storyId.count())
            .where { StoryViewTable.storyId inList storyIds }
            .groupBy(StoryViewTable.storyId)
            .associate { row -> row[StoryViewTable.storyId] to row[StoryViewTable.storyId.count()] }

        val userViews = userId?.let {
            StoryViewTable.selectAll().where { (StoryViewTable.userId eq it) and (StoryViewTable.storyId inList storyIds) }
                .map { it[StoryViewTable.storyId] }.toSet()
        } ?: emptySet()

        val authorIds = storyData.values.map { it[StoryTable.authorId] }.distinct()
        val authors = UserTable.selectAll().where { UserTable.userId inList authorIds }
            .associateBy { it[UserTable.userId] }

        val items = storyIds.map { storyId ->
            val row = storyData[storyId]!!
            val author = authors[row[StoryTable.authorId]]!!
            StoryResponse(
                storyId = storyId,
                mediaUrl = row[StoryTable.mediaUrl],
                mediaType = MediaType.valueOf(row[StoryTable.mediaType]),
                authorId = row[StoryTable.authorId],
                authorName = author[UserTable.name],
                authorUsername = author[UserTable.username],
                authorProfilePictureUrl = author[UserTable.profilePictureUrl],
                createdAt = row[StoryTable.createdAt],
                expiresAt = row[StoryTable.expiresAt],
                viewsCount = viewsCounts[storyId] ?: 0,
                isViewed = storyId in userViews
            )
        }

        val nextCursor = items.lastOrNull()?.createdAt?.toString()
        PaginationResult(items, nextCursor)
    }

    override suspend fun getUserStories(userId: UUID, viewerId: UUID?): List<StoryResponse> = db.dbQuery {
        val now = timeProvider.nowMillis()
        val storyIds = StoryTable.selectAll()
            .where { (StoryTable.authorId eq userId) and (StoryTable.expiresAt greater now) }
            .orderBy(StoryTable.createdAt, SortOrder.DESC)
            .map { it[StoryTable.storyId] }

        if (storyIds.isEmpty()) return@dbQuery emptyList()

        val viewsCounts = StoryViewTable.select(StoryViewTable.storyId, StoryViewTable.storyId.count())
            .where { StoryViewTable.storyId inList storyIds }
            .groupBy(StoryViewTable.storyId)
            .associate { row -> row[StoryViewTable.storyId] to row[StoryViewTable.storyId.count()] }

        val userViews = viewerId?.let {
            StoryViewTable.selectAll().where { (StoryViewTable.userId eq it) and (StoryViewTable.storyId inList storyIds) }
                .map { it[StoryViewTable.storyId] }.toSet()
        } ?: emptySet()

        val author = UserTable.selectAll().where { UserTable.userId eq userId }.single()

        storyIds.map { storyId ->
            val row = StoryTable.selectAll().where { StoryTable.storyId eq storyId }.single()
            StoryResponse(
                storyId = storyId,
                mediaUrl = row[StoryTable.mediaUrl],
                mediaType = MediaType.valueOf(row[StoryTable.mediaType]),
                authorId = row[StoryTable.authorId],
                authorName = author[UserTable.name],
                authorUsername = author[UserTable.username],
                authorProfilePictureUrl = author[UserTable.profilePictureUrl],
                createdAt = row[StoryTable.createdAt],
                expiresAt = row[StoryTable.expiresAt],
                viewsCount = viewsCounts[storyId] ?: 0,
                isViewed = storyId in userViews
            )
        }
    }

    override suspend fun getStoryById(storyId: String, viewerId: UUID?): StoryResponse? = db.dbQuery {
        StoryTable.selectAll().where { StoryTable.storyId eq storyId }.singleOrNull()?.let {
            buildStoryResponse(storyId, viewerId)
        }
    }

    override suspend fun deleteStory(storyId: String, userId: UUID) {
        db.dbQuery {
            val story = StoryTable.selectAll().where { StoryTable.storyId eq storyId }.singleOrNull()
                ?: throw NotFoundException("Story not found")

            if (story[StoryTable.authorId] != userId) {
                throw IllegalStateException("You can only delete your own stories")
            }

            StoryViewTable.deleteWhere { StoryViewTable.storyId eq storyId }
            StoryTable.deleteWhere { StoryTable.storyId eq storyId }
        }
    }

    override suspend fun recordView(storyId: String, userId: UUID) {
        db.dbQuery {
            val alreadyViewed = StoryViewTable.selectAll()
                .where { (StoryViewTable.userId eq userId) and (StoryViewTable.storyId eq storyId) }
                .limit(1).any()

            if (!alreadyViewed) {
                StoryViewTable.insert {
                    it[StoryViewTable.userId] = userId
                    it[StoryViewTable.storyId] = storyId
                    it[StoryViewTable.viewedAt] = timeProvider.nowMillis()
                }
            }
        }
    }

    override suspend fun deleteExpiredStories() {
        db.dbQuery {
            val now = timeProvider.nowMillis()
            val expiredStoryIds = StoryTable.selectAll()
                .where { StoryTable.expiresAt lessEq now }
                .map { it[StoryTable.storyId] }

            if (expiredStoryIds.isNotEmpty()) {
                expiredStoryIds.forEach { storyId ->
                    StoryViewTable.deleteWhere { StoryViewTable.storyId eq storyId }
                    StoryTable.deleteWhere { StoryTable.storyId eq storyId }
                }
            }
        }
    }

    private fun buildStoryResponse(storyId: String, viewerId: UUID? = null): StoryResponse {
        val story = (StoryTable innerJoin UserTable)
            .selectAll()
            .where { StoryTable.storyId eq storyId }
            .single()

        val viewsCount = StoryViewTable.selectAll().where { StoryViewTable.storyId eq storyId }.count()
        val isViewed = viewerId?.let {
            StoryViewTable.selectAll().where { (StoryViewTable.storyId eq storyId) and (StoryViewTable.userId eq it) }.any()
        } ?: false

        return StoryResponse(
            storyId = storyId,
            mediaUrl = story[StoryTable.mediaUrl],
            mediaType = MediaType.valueOf(story[StoryTable.mediaType]),
            authorId = story[StoryTable.authorId],
            authorName = story[UserTable.name],
            authorUsername = story[UserTable.username],
            authorProfilePictureUrl = story[UserTable.profilePictureUrl],
            createdAt = story[StoryTable.createdAt],
            expiresAt = story[StoryTable.expiresAt],
            viewsCount = viewsCount,
            isViewed = isViewed
        )
    }
}

