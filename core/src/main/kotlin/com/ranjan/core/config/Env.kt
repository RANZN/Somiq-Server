package com.ranjan.core.config

import java.io.File
import kotlin.time.Duration

class Env(private val raw: Map<String, String>) {
    private val errors = mutableListOf<String>()

    fun require(key: String): String =
        raw[key]?.trim()?.takeIf { it.isNotEmpty() } ?: run {
            errors += key
            ""
        }

    fun optional(key: String): String? =
        raw[key]?.trim()?.takeIf { it.isNotEmpty() }

    fun duration(key: String): Duration {
        val v = require(key)
        if (v.isEmpty()) return Duration.ZERO
        return Duration.parseOrNull(v)
            ?: Duration.parseOrNull(v.replace(" ", ""))
            ?: run {
                errors += "$key (invalid duration '$v', expected format like '1h', '7d', '30m')"
                Duration.ZERO
            }
    }

    fun int(key: String, default: Int): Int {
        val v = raw[key]?.trim() ?: return default
        return v.toIntOrNull() ?: run {
            errors += "$key (must be a valid integer)"
            default
        }
    }

    fun <T : Enum<T>> enum(key: String, values: Array<T>, default: T): T {
        val str = raw[key]?.trim() ?: return default
        return values.firstOrNull { it.name.equals(str, ignoreCase = true) }
            ?: run {
                errors += "$key (invalid value '$str', must be one of: ${values.joinToString { it.name }})"
                default
            }
    }

    inline fun <reified T : Enum<T>> enum(key: String, default: T): T = enum(key, enumValues<T>(), default)

    fun list(key: String): List<String> {
        val str = require(key)
        if (str.isEmpty()) return emptyList()
        val items = str.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (items.isEmpty()) {
            errors += "$key (must contain at least one element)"
        }
        return items
    }

    val hasErrors: Boolean get() = errors.isNotEmpty()

    fun validate() {
        if (hasErrors) {
            throw IllegalStateException(
                "Missing or invalid required environment variables:\n" +
                    errors.distinct().joinToString("\n") { "  - $it" } +
                    "\nPlease define them in .env.local or the system environment."
            )
        }
    }

    companion object {
        private val defaultInstance: Env by lazy {
            from(loadEnvironment())
        }

        fun load(): Env = defaultInstance

        fun from(map: Map<String, String>): Env = Env(map)

        private fun loadEnvironment(): Map<String, String> {
            val systemProps = System.getProperties().entries.associate { it.key.toString() to it.value.toString() }
            return loadDotEnv() + systemProps + System.getenv()
        }

        private fun loadDotEnv(): Map<String, String> {
            val customEnv = System.getenv("ENV_FILE")?.let { File(it) }
            val candidates = listOfNotNull(
                customEnv,
                File("/secrets/.env"),
                File("/secrets/somiq-env"),
                File("/secrets/env"),
                File(".env.local"), File(".env"),
                File("../.env.local"), File("../.env"),
                File("../../.env.local"), File("../../.env")
            )
            val file = candidates.firstOrNull { it.isFile } ?: return emptyMap()

            return file.useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                    .associate { line ->
                        val (k, v) = line.split("=", limit = 2)
                        k.trim() to v.trim().removeSurrounding("\"").removeSurrounding("'")
                    }
            }
        }
    }
}
