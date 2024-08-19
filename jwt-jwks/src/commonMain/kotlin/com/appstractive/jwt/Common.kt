package com.appstractive.jwt

import kotlinx.serialization.json.Json

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
internal val json: Json = Json {
  isLenient = true
  explicitNulls = false
  encodeDefaults = true
  ignoreUnknownKeys = true
}
