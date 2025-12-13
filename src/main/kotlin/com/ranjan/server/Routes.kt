package com.ranjan.server

import com.ranjan.server.account.accountRoutes
import com.ranjan.server.auth.authRoutes
import com.ranjan.server.collection.collectionRoutes
import com.ranjan.server.comment.commentRoutes
import com.ranjan.server.notification.notificationRoutes
import com.ranjan.server.post.postRoutes
import com.ranjan.server.reel.reelRoutes
import com.ranjan.server.search.searchRoutes
import com.ranjan.server.story.storyRoutes
import com.ranjan.server.update.checkUpdateRoute
import io.ktor.server.application.Application

fun Application.configureRoutes() {
    checkUpdateRoute()
    authRoutes()
    postRoutes()
    reelRoutes()
    commentRoutes()
    notificationRoutes()
    storyRoutes()
    collectionRoutes()
    searchRoutes()
    accountRoutes()
}
