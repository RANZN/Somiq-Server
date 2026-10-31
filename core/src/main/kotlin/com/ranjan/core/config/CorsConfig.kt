package com.ranjan.core.config

import io.ktor.server.config.ApplicationConfig

data class CorsConfig(
    val env: AppEnv,
    val allowedHosts: List<String> = emptyList()
) {
    companion object {
        fun from(config: ApplicationConfig, env: AppEnv): CorsConfig {
            val allowedHosts = config.requireString("app.corsHosts")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            return CorsConfig(
                env = env,
                allowedHosts = allowedHosts
            )
        }
    }
}