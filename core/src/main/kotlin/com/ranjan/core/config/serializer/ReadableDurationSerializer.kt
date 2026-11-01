package com.ranjan.core.config.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration

/** Reads durations written like "1h", "7d", "30m" or "1h 30m". */
object ReadableDurationSerializer : KSerializer<Duration> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ReadableDuration", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val value = decoder.decodeString().trim()
        return Duration.parseOrNull(value)
            ?: Duration.parseOrNull(value.replace(" ", ""))
            ?: throw SerializationException("Invalid duration '$value', expected format like '1h', '7d', '30m'")
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString(value.toString())
    }
}
