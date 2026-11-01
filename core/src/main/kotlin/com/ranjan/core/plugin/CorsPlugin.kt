package com.ranjan.core.plugin

import com.ranjan.core.config.AppEnv
import com.ranjan.core.config.CorsConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS(env: AppEnv, config: CorsConfig) {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        if (env.isProduction) {
            config.allowedHosts.forEach { host ->
                allowHost(
                    host = host,
                    schemes = listOf("http", "https")
                )
            }
        } else {
            anyHost()
        }
    }
}
