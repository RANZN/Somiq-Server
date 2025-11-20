package com.ranjan.server

import com.ranjan.server.auth.authRoutes
import com.ranjan.server.post.postRoutes
import com.ranjan.server.update.checkUpdateRoute
import io.ktor.server.application.Application

fun Application.configureRoutes() {
    checkUpdateRoute()
    authRoutes()
    postRoutes()
}
