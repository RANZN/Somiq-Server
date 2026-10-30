package com.ranjan.core.config

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.time.Duration

class Env(private val raw: Map<String, String>) {
    private val errors = linkedSetOf<String>()

    private fun value(key: String): String? = raw[key]?.trim()?.takeIf { it.isNotEmpty() }

    fun require(key: String): String = value(key) ?: "".also { errors += key }

    fun optional(key: String): String? = value(key)

    fun int(key: String, default: Int): Int {
        val v = value(key) ?: return default
        return v.toIntOrNull() ?: default.also { errors += "$key (must be a valid integer)" }
    }

    fun duration(key: String): Duration {
        val v = value(key) ?: return Duration.ZERO.also { errors += key }
        return Duration.parseOrNull(v)
            ?: Duration.parseOrNull(v.replace(" ", ""))
            ?: Duration.ZERO.also { errors += "$key (invalid duration '$v', expected format like '1h', '7d', '30m')" }
    }

    fun <T : Enum<T>> enum(key: String, values: Array<T>, default: T): T {
        val v = value(key) ?: return default
        return values.firstOrNull { it.name.equals(v, ignoreCase = true) }
            ?: default.also { errors += "$key (invalid value '$v', must be one of: ${values.joinToString { it.name }})" }
    }

    inline fun <reified T : Enum<T>> enum(key: String, default: T): T = enum(key, enumValues<T>(), default)

    fun list(key: String): List<String> {
        val v = value(key) ?: return emptyList<String>().also { errors += key }
        val items = v.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (items.isEmpty()) errors += "$key (must contain at least one element)"
        return items
    }

    val hasErrors: Boolean get() = errors.isNotEmpty()

    fun validate() {
        if (hasErrors) {
            throw IllegalStateException(
                "Missing or invalid required configuration:\n" +
                        errors.joinToString("\n") { "  - $it" } +
                        "\nDefine them in config.json, .env.local or the system environment."
            )
        }
    }

    companion object {
        private val defaultInstance: Env by lazy { from(loadEnvironment()) }

        fun load(): Env = defaultInstance

        fun from(map: Map<String, String>): Env = Env(map)

        private fun loadEnvironment(): Map<String, String> {
            val systemProps = System.getProperties().entries.associate { it.key.toString() to it.value.toString() }
            val dotEnv = loadDotEnv()
            val appEnv = System.getenv("APP_ENV") ?: System.getProperty("APP_ENV") ?: dotEnv["APP_ENV"]
            return loadJsonConfig(appEnv) + dotEnv + systemProps + System.getenv()
        }

        private fun loadJsonConfig(appEnv: String?): Map<String, String> {
            val base = readJsonResource("config.json")
            val envSpecific = appEnv?.let { readJsonResource("config.${it.trim().lowercase()}.json") }.orEmpty()
            return base + envSpecific
        }

        private fun readJsonResource(name: String): Map<String, String> =
            Env::class.java.classLoader.getResourceAsStream(name)?.bufferedReader()?.use { reader ->
                Json.parseToJsonElement(reader.readText()).jsonObject
                    .mapValues { (_, value) -> value.jsonPrimitive.content }
            }.orEmpty()

        private fun loadDotEnv(): Map<String, String> {
            val candidates = listOfNotNull(
                System.getenv("ENV_FILE")?.let(::File),
                File(".env.local"), File(".env"),
                File("../.env.local"), File("../.env")
            )
            val file = candidates.firstOrNull { it.isFile } ?: return emptyMap()

            return file.useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                    .associate { line ->
                        val (k, v) = line.removePrefix("export ").split("=", limit = 2)
                        k.trim() to v.trim().removeSurrounding("\"").removeSurrounding("'")
                    }
            }
        }
    }
}