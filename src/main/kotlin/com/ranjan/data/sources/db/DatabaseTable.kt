package com.ranjan.data.sources.db

import com.ranjan.data.auth.model.RefreshTokenTable
import com.ranjan.data.auth.model.UserTable
import com.ranjan.data.post.model.PostBookmarkTable
import com.ranjan.data.post.model.PostLikeTable
import com.ranjan.data.post.model.PostTable

val AuthTables = listOf(UserTable, RefreshTokenTable)
val PostTables = listOf(PostTable, PostBookmarkTable, PostLikeTable)
val AllTables = AuthTables + PostTables
