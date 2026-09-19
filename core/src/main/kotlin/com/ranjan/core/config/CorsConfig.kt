package com.ranjan.core.config

data class CorsConfig(
    val env: AppEnv,
    val allowedHosts: List<String> = emptyList()
) {
    companion object {
        fun fromEnv(env: AppEnv = AppEnv.LOCAL): CorsConfig {
            val hosts = System.getenv("ALLOWED_CORS_HOSTS")
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()

            return CorsConfig(env = env, allowedHosts = hosts)
        }
    }
}
