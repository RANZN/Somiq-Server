package com.ranjan.core.config

data class CorsConfig(
    val env: AppEnv,
    val allowedHosts: List<String> = emptyList()
) {
    companion object {
        fun fromEnv(env: AppEnv = Env.appEnv): CorsConfig = CorsConfig(
            env = env,
            allowedHosts = Env.allowedCorsHosts
        )
    }
}
