package com.appstractive.jwt.utils

import com.appstractive.jwt.Algorithm
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

internal val json = Json {
  isLenient = true
  ignoreUnknownKeys = true
  explicitNulls = false
  encodeDefaults = true
}

internal class AlgorithmSerializer : KSerializer<Algorithm> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("EnumSerializer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Algorithm) {
    encoder.encodeString(value.name)
  }

  override fun deserialize(decoder: Decoder): Algorithm {
    val decodeString = decoder.decodeString()
    return Algorithm.entries.find { it.name == decodeString } ?: Algorithm.UNSUPPORTED
  }
}
