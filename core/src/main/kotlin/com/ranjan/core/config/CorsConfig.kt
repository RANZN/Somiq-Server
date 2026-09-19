package com.ranjan.core.config

data class CorsConfig(
    val env: AppEnv,
    val allowedHosts: List<String> = emptyList()
) {
    companion object {
        fun from(env: Env): CorsConfig {
            val appEnv = env.enum("APP_ENV", AppEnv.LOCAL)
            val allowedHosts = env.list("ALLOWED_CORS_HOSTS")
            env.validate()
            return CorsConfig(
                env = appEnv,
                allowedHosts = allowedHosts
            )
        }
    }
}
