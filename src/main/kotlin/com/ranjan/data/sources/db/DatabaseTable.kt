package com.ranjan.data.sources.db

import com.ranjan.data.auth.model.RefreshTokenTable
import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.post.model.PostBookmarkTable
import com.ranjan.data.post.model.PostLikeTable
import com.ranjan.data.post.model.PostTable
import com.ranjan.data.reel.model.ReelBookmarkTable
import com.ranjan.data.reel.model.ReelLikeTable
import com.ranjan.data.reel.model.ReelTable
import com.ranjan.data.reel.model.ReelViewTable
import com.ranjan.data.account.model.FollowTable
import com.ranjan.data.comment.model.CommentTable
import com.ranjan.data.comment.model.CommentLikeTable
import com.ranjan.data.notification.model.NotificationTable
import com.ranjan.data.story.model.StoryTable
import com.ranjan.data.story.model.StoryViewTable
import com.ranjan.data.collection.model.CollectionTable
import com.ranjan.data.collection.model.CollectionItemTable

val AuthTables = listOf(UserTable, RefreshTokenTable)
val PostTables = listOf(PostTable, PostBookmarkTable, PostLikeTable)
val ReelTables = listOf(ReelTable, ReelLikeTable, ReelBookmarkTable, ReelViewTable)
val CommentTables = listOf(CommentTable, CommentLikeTable)
val NotificationTables = listOf(NotificationTable)
val StoryTables = listOf(StoryTable, StoryViewTable)
val CollectionTables = listOf(CollectionTable, CollectionItemTable)
val AccountTables = listOf(FollowTable)
val AllTables = AuthTables + PostTables + ReelTables + CommentTables + NotificationTables + StoryTables + CollectionTables + AccountTables
