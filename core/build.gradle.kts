plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)
    api(libs.logback)
    api(libs.ktor.server.config.yaml)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.datetime)

    api(libs.h2)
    api(libs.postgresql)

    api(libs.hikaricp)
    api(libs.insert.koin.koin.ktor)

    api(libs.ktor.server.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    api(libs.ktor.server.cors)
    api(libs.ktor.server.status.pages)
    api(libs.ktor.server.io)
    api(libs.auth0.jwt)
}
