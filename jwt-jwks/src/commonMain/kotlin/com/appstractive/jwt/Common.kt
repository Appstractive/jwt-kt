package com.appstractive.jwt

import kotlinx.serialization.json.Json

internal val json: Json = Json {
  isLenient = true
  explicitNulls = false
  encodeDefaults = true
  ignoreUnknownKeys = true
}
