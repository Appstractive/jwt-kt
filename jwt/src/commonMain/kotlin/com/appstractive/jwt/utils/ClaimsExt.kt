﻿package com.appstractive.jwt.utils

import com.appstractive.jwt.ClaimsBuilder
import kotlin.time.Instant
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

fun JsonObject.instantOrNull(claim: String): Instant? =
    this[claim]?.jsonPrimitive?.longOrNull?.let { Instant.fromEpochSeconds(it) }

inline fun <reified T> ClaimsBuilder.claim(
  key: String,
  value: T,
  serializer: StringFormat = Json,
) {
  claim(
      key = key, value = serializer.decodeFromString<JsonElement>(serializer.encodeToString(value)),
  )
}
