package com.ranjan.server.search

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.search.usecase.SearchUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class SearchController(
    private val searchUseCase: SearchUseCase
) {
    suspend fun search(call: ApplicationCall) {
        val query = call.request.queryParameters["q"]
        val type = call.request.queryParameters["type"] ?: "all"
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

        if (query.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Search query is required"))
            return
        }

        val includeUsers = type == "all" || type == "users"
        val includePosts = type == "all" || type == "posts"
        val includeReels = type == "all" || type == "reels"

        val result = searchUseCase.execute(query, includeUsers, includePosts, includeReels, limit)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(ex.message ?: "Failed to perform search")
            )
        }
    }
}

