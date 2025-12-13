package com.ranjan.data.account.repository

import com.ranjan.data.account.model.FollowTable
import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.post.model.PostTable
import com.ranjan.data.reel.model.ReelTable
import com.ranjan.data.sources.db.dbQuery
import com.ranjan.domain.account.model.ProfileResponse
import com.ranjan.domain.account.repository.AccountRepository
import com.ranjan.domain.common.model.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Database
import java.util.UUID

class AccountRepositoryImpl(
    private val db: Database
) : AccountRepository {

    override suspend fun getProfile(userId: UUID, viewerId: UUID?): ProfileResponse? = db.dbQuery {
        val user = UserTable.selectAll().where { UserTable.userId eq userId }.singleOrNull() ?: return@dbQuery null

        val userResponse = UserResponse(
            userId = user[UserTable.userId].toString(),
            name = user[UserTable.name],
            email = user[UserTable.email],
            username = user[UserTable.username],
            profilePictureUrl = user[UserTable.profilePictureUrl],
            bio = user[UserTable.bio]
        )

        ProfileResponse(
            user = userResponse,
            postsCount = getPostsCount(userId),
            reelsCount = getReelsCount(userId),
            followersCount = getFollowersCount(userId),
            followingCount = getFollowingCount(userId),
            isFollowing = viewerId?.let { isFollowing(it, userId) } ?: false
        )
    }

    override suspend fun toggleFollow(followerId: UUID, followingId: UUID): Boolean = db.dbQuery {
        if (followerId == followingId) {
            throw IllegalArgumentException("Cannot follow yourself")
        }

        val alreadyFollowing = FollowTable.selectAll()
            .where { (FollowTable.followerId eq followerId) and (FollowTable.followingId eq followingId) }
            .limit(1).any()

        if (alreadyFollowing) {
            FollowTable.deleteWhere {
                (FollowTable.followerId eq followerId) and (FollowTable.followingId eq followingId)
            }
            false
        } else {
            FollowTable.insert {
                it[FollowTable.followerId] = followerId
                it[FollowTable.followingId] = followingId
            }
            true
        }
    }

    override suspend fun getPostsCount(userId: UUID): Long = db.dbQuery {
        PostTable.selectAll().where { PostTable.authorId eq userId }.count()
    }

    override suspend fun getReelsCount(userId: UUID): Long = db.dbQuery {
        ReelTable.selectAll().where { ReelTable.authorId eq userId }.count()
    }

    override suspend fun getFollowersCount(userId: UUID): Long = db.dbQuery {
        FollowTable.selectAll().where { FollowTable.followingId eq userId }.count()
    }

    override suspend fun getFollowingCount(userId: UUID): Long = db.dbQuery {
        FollowTable.selectAll().where { FollowTable.followerId eq userId }.count()
    }

    override suspend fun isFollowing(followerId: UUID, followingId: UUID): Boolean = db.dbQuery {
        FollowTable.selectAll()
            .where { (FollowTable.followerId eq followerId) and (FollowTable.followingId eq followingId) }
            .any()
    }
}

