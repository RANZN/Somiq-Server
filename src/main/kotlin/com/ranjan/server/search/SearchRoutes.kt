package com.ranjan.server.search

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.searchRoutes() {
    val searchController by inject<SearchController>()
    routing {
        route("/v1/search") {
            get { searchController.search(call) }
        }
    }
}

