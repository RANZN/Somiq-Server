package com.ranjan.core.config.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Reads a comma-separated string such as "a.com, b.com" into a list, so it can come from one environment variable. */
object CommaSeparatedListSerializer : KSerializer<List<String>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CommaSeparatedList", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): List<String> =
        decoder.decodeString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    override fun serialize(encoder: Encoder, value: List<String>) {
        encoder.encodeString(value.joinToString(","))
    }
}
