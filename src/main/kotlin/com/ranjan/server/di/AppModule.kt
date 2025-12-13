package com.ranjan.server.di

import com.ranjan.server.account.AccountController
import com.ranjan.server.auth.AuthController
import com.ranjan.server.collection.CollectionController
import com.ranjan.server.comment.CommentController
import com.ranjan.server.notification.NotificationController
import com.ranjan.server.post.PostController
import com.ranjan.server.reel.ReelController
import com.ranjan.server.search.SearchController
import com.ranjan.server.story.StoryController
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::AuthController)
    singleOf(::PostController)
    singleOf(::ReelController)
    singleOf(::CommentController)
    singleOf(::NotificationController)
    singleOf(::StoryController)
    singleOf(::CollectionController)
    singleOf(::SearchController)
    singleOf(::AccountController)
}