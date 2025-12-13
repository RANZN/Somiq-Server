package com.ranjan.server.collection

import com.ranjan.data.auth.service.JwtConfig
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject

fun Application.collectionRoutes() {
    val collectionController by inject<CollectionController>()
    routing {
        authenticate(JwtConfig.NAME) {
            route("/v1/collections") {
                get { collectionController.getCollections(call) }
                post { collectionController.createCollection(call) }
                get("/{collectionId}/items") { collectionController.getCollectionItems(call) }
                put("/{collectionId}") { collectionController.updateCollection(call) }
                delete("/{collectionId}") { collectionController.deleteCollection(call) }
                post("/{collectionId}/items") { collectionController.addItem(call) }
                delete("/{collectionId}/items/{itemId}") { collectionController.removeItem(call) }
            }
        }
    }
}

