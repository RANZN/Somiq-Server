package com.ranjan.server.collection

import com.ranjan.domain.auth.model.ErrorResponse
import com.ranjan.domain.collection.model.AddItemRequest
import com.ranjan.domain.collection.model.CreateCollectionRequest
import com.ranjan.domain.collection.model.UpdateCollectionRequest
import com.ranjan.domain.collection.usecase.*
import com.ranjan.server.common.extension.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond

class CollectionController(
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val addItemToCollectionUseCase: AddItemToCollectionUseCase,
    private val removeItemFromCollectionUseCase: RemoveItemFromCollectionUseCase,
    private val getCollectionItemsUseCase: GetCollectionItemsUseCase,
) {

    suspend fun getCollections(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val result = getCollectionsUseCase.execute(userId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure {
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("Failed to load collections")
            )
        }
    }

    suspend fun createCollection(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val request = try {
            call.receive<CreateCollectionRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = createCollectionUseCase.execute(userId, request)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to create collection")
            )
        }
    }

    suspend fun updateCollection(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val collectionId = call.parameters["collectionId"]
        if (collectionId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Collection id required"))
            return
        }

        val request = try {
            call.receive<UpdateCollectionRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = updateCollectionUseCase.execute(userId, collectionId, request)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to update collection")
            )
        }
    }

    suspend fun deleteCollection(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val collectionId = call.parameters["collectionId"]
        if (collectionId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Collection id required"))
            return
        }

        val result = deleteCollectionUseCase.execute(userId, collectionId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Collection deleted"))
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to delete collection")
            )
        }
    }

    suspend fun getCollectionItems(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val collectionId = call.parameters["collectionId"]
        if (collectionId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Collection id required"))
            return
        }

        val result = getCollectionItemsUseCase.execute(userId, collectionId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to load collection items")
            )
        }
    }

    suspend fun addItem(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val collectionId = call.parameters["collectionId"]
        if (collectionId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Collection id required"))
            return
        }

        val request = try {
            call.receive<AddItemRequest>()
        } catch (_: Exception) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Invalid request format")
            )
            return
        }

        val result = addItemToCollectionUseCase.execute(userId, collectionId, request)

        result.onSuccess {
            call.respond(HttpStatusCode.Created, it)
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to add item to collection")
            )
        }
    }

    suspend fun removeItem(call: ApplicationCall) {
        val userId = try {
            call.userId()
        } catch (_: Exception) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Login required"))
            return
        }

        val collectionId = call.parameters["collectionId"]
        val itemId = call.parameters["itemId"]
        if (collectionId.isNullOrEmpty() || itemId.isNullOrEmpty()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Collection id and item id required"))
            return
        }

        val result = removeItemFromCollectionUseCase.execute(userId, collectionId, itemId)

        result.onSuccess {
            call.respond(HttpStatusCode.OK, mapOf("message" to "Item removed from collection"))
        }.onFailure { ex ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(ex.message ?: "Failed to remove item from collection")
            )
        }
    }
}

